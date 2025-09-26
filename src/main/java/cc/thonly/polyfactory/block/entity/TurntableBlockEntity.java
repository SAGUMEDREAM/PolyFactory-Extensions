package cc.thonly.polyfactory.block.entity;

import cc.thonly.polyfactory.PolyFactoryExtension;
import cc.thonly.polyfactory.block.ModBlockEntities;
import cc.thonly.polyfactory.block.collection.BlockCollectionExtension;
import cc.thonly.polyfactory.component.Selector;
import cc.thonly.polyfactory.interfaces.BlockCollectionDSG;
import cc.thonly.polyfactory.interfaces.TurntableBlockModelDSG;
import cc.thonly.polyfactory.item.SlimeWrenchItem;
import cc.thonly.polyfactory.util.BlockEntityManager;
import cc.thonly.polyfactory.util.DelayedTask;
import eu.pb4.polyfactory.block.collection.BlockCollectionData;
import eu.pb4.polyfactory.block.mechanical.TurntableBlock;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class TurntableBlockEntity extends BlockEntity {

    private BlockCollectionData blockCollectionData;
    private BlockPos center = BlockPos.ORIGIN;
    private Vec3d offset = Vec3d.ZERO;
    private boolean nextUpdated = false;

    public TurntableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURNTABLE_BLOCK, pos, state);
    }

    // --------------------------- 数据恢复 ---------------------------
    @Override
    protected void readData(ReadView view) {
        super.readData(view);
//        System.out.println("starting read data");
        DelayedTask.when(PolyFactoryExtension.SERVER, ()->this.world!=null,1, ()->{
            Optional<BlockCollectionData> data = Optional.ofNullable(BlockEntityManager.INSTANCE.getData((ServerWorld) this.world, this.getPos()));
//            System.out.println(data.isPresent());
            if (data.isPresent()) {
                this.blockCollectionData = data.get();
                BlockEntityManager.INSTANCE.put((ServerWorld) this.world, this.getPos(), this.blockCollectionData);
//                System.out.println("loaded data");
//                System.out.println(BlockEntityManager.INSTANCE.getWorld2Pos2BlockEntityMap());
                PolyFactoryExtension.SERVER.executeSync(this::reloadModelIfNeeded);
            }
        }, ()->{

        });

        view.read("Center", BlockPos.CODEC).ifPresent(pos -> this.center = pos);
        view.read("Offset", Vec3d.CODEC).ifPresent(off -> this.offset = off);

        this.nextUpdated = false;
        this.reloadModelIfNeeded();
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (this.blockCollectionData != null && this.world instanceof ServerWorld serverWorld) {
            BlockEntityManager.INSTANCE.put(serverWorld, this.getPos(), this.blockCollectionData);
        }
        view.put("Center", BlockPos.CODEC, this.center);
        view.put("Offset", Vec3d.CODEC, this.offset);
    }

    // --------------------------- 破坏逻辑 ---------------------------
    public void onBreak(World world, PlayerEntity player, BlockPos turntablePos, BlockState turntableState, @Nullable BlockEntity blockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        Map<Long, TurntableBlock.Model> modelMap = PolyFactoryExtension.WORLD_POS_TURN_MAP.computeIfAbsent(serverWorld, w -> new Object2ObjectLinkedOpenHashMap<>());
        TurntableBlock.Model model = modelMap.get(this.getPos().asLong());
        if (model == null) return;

        TurntableBlockModelDSG dsg = (TurntableBlockModelDSG) (Object) model;
        BlockCollectionExtension collection = dsg.getBlockCollectionExtension();
        if (collection == null) return;

        BlockCollectionData data = ((BlockCollectionDSG) collection).getData();
        if (data == null) return;

        Vec3d offsetVec = collection.getOffset();
        BlockPos pivot = this.getPos();

        for (int x = 0; x < data.sizeX(); x++) {
            for (int y = 0; y < data.sizeY(); y++) {
                for (int z = 0; z < data.sizeZ(); z++) {
                    BlockState state = data.getBlockState(x, y, z);
                    if (state == null || state.isAir()) continue;

                    BlockPos relative = new BlockPos(
                            x - (int) ((BlockCollectionDSG) collection).getCenterX(),
                            y - (int) ((BlockCollectionDSG) collection).getCenterY(),
                            z - (int) ((BlockCollectionDSG) collection).getCenterZ()
                    );
                    BlockPos worldPos = pivot.add(relative).add((int) offsetVec.x, (int) offsetVec.y, (int) offsetVec.z);
                    serverWorld.setBlockState(worldPos, state);

                    BlockEntity oldBE = data.blockEntities()[data.index(x, y, z)];
                    if (oldBE != null) {
                        BlockEntity newBE = BlockEntity.createFromNbt(worldPos, state, oldBE.createNbtWithIdentifyingData(serverWorld.getRegistryManager()), serverWorld.getRegistryManager());
                        if (newBE != null) serverWorld.addBlockEntity(newBE);
                    }
                }
            }
        }

        model.removeElement(collection);

        // 重置当前 BE 数据
        this.blockCollectionData = new BlockCollectionData(1, 1, 1);
        this.center = BlockPos.ORIGIN;
        this.markDirty();
        BlockEntityManager.INSTANCE.onBreak(serverWorld, this.getPos());
    }

    // --------------------------- 更新逻辑 ---------------------------
    public boolean update(ServerWorld world, Selector selector, SlimeWrenchItem.Action action) {
        if (!world.getRegistryKey().equals(selector.worldKey())) return false;

        Map<Long, TurntableBlock.Model> modelMap = PolyFactoryExtension.WORLD_POS_TURN_MAP.computeIfAbsent(world, w -> new Object2ObjectLinkedOpenHashMap<>());
        TurntableBlock.Model model = modelMap.get(this.getPos().asLong());
        if (model == null) return false;

        long beLong = this.getPos().asLong();
        for (BlockPos pos : BlockPos.iterate(selector.toBox())) {
            if (pos.asLong() == beLong) return false;
        }

        if (action == SlimeWrenchItem.Action.SET) {
            this.writeModel(model, world, selector);
        } else if (action == SlimeWrenchItem.Action.REMOVE) {
            this.removeModel(model, world, selector);
        }

        this.nextUpdated = true;
        return true;
    }

    private void writeModel(TurntableBlock.Model model, ServerWorld world, Selector selector) {
        TurntableBlockModelDSG dsgModel = (TurntableBlockModelDSG) (Object) model;
        if (dsgModel == null) return;

        ServerWorld serverWorld = world.getServer().getWorld(selector.worldKey());
        if (serverWorld != world) return;

        Box box = selector.toBox();
        int sizeX = (int) Math.floor(box.maxX) - (int) Math.floor(box.minX) + 1;
        int sizeY = (int) Math.floor(box.maxY) - (int) Math.floor(box.minY) + 1;
        int sizeZ = (int) Math.floor(box.maxZ) - (int) Math.floor(box.minZ) + 1;

        BlockCollectionData data = new BlockCollectionData(sizeX, sizeY, sizeZ);
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockPos pos = new BlockPos((int) box.minX + x, (int) box.minY + y, (int) box.minZ + z);
                    data.setBlockState(x, y, z, serverWorld.getBlockState(pos), serverWorld.getBlockEntity(pos));
                }
            }
        }

        this.blockCollectionData = data;
        BlockCollectionExtension collection = new BlockCollectionExtension(data);

        Vec3d centerVec = calculatePivotCenter(box, this.getPos());
        this.center = new BlockPos((int) centerVec.x, (int) centerVec.y, (int) centerVec.z);
        collection.setCenter((float) centerVec.x, (float) centerVec.y, (float) centerVec.z);

        collection.setOffset(getFacingOffset(this.getCachedState().get(TurntableBlock.FACING)));
        dsgModel.setBlockCollectionExtension(collection);
        model.addElement(collection);

        for (BlockPos pos : BlockPos.iterate(selector.toBox())) {
            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        BlockEntityManager.INSTANCE.put(world, this.getPos(), this.blockCollectionData);
        this.markDirty();
    }

    private void removeModel(TurntableBlock.Model model, ServerWorld world, Selector selector) {
        TurntableBlockModelDSG dsgModel = (TurntableBlockModelDSG) (Object) model;
        if (dsgModel == null) return;

        BlockCollectionExtension collection = dsgModel.getBlockCollectionExtension();
        if (collection != null) model.removeElement(collection);

        this.blockCollectionData = new BlockCollectionData(1, 1, 1);
        this.center = BlockPos.ORIGIN;
        BlockEntityManager.INSTANCE.onBreak(world, this.getPos());
        this.markDirty();
    }

    // --------------------------- 数据同步模型 ---------------------------
    public void reloadModelIfNeeded() {
        if (!(this.world instanceof ServerWorld serverWorld)) return;
        Map<Long, TurntableBlock.Model> map = PolyFactoryExtension.WORLD_POS_TURN_MAP.get(serverWorld);
        if (map == null) return;

        TurntableBlock.Model model = map.get(this.getPos().asLong());
        if (model != null && this.blockCollectionData != null) {
            updateFromBlockEntity(model, serverWorld);
        }
    }

    public void updateFromBlockEntity(TurntableBlock.Model model, ServerWorld world) {
        TurntableBlockModelDSG dsgModel = (TurntableBlockModelDSG) (Object) model;
        if (dsgModel == null || this.blockCollectionData == null) return;

        BlockCollectionExtension collection = new BlockCollectionExtension(this.blockCollectionData);
        collection.setCenter(this.center.getX(), this.center.getY(), this.center.getZ());
        collection.setOffset(this.offset);

        dsgModel.setBlockCollectionExtension(collection);
        model.addElement(collection);

        if (world.getBlockEntity(this.getPos()) == this) {
            BlockEntityManager.INSTANCE.put(world, this.getPos(), this.blockCollectionData);
        }

        this.nextUpdated = true;
        this.markDirty();
    }

    // --------------------------- 工具方法 ---------------------------
    public static Vec3d calculatePivotCenter(Box box, BlockPos pivot) {
        double localX = pivot.getX() - Math.floor(box.minX);
        double localY = pivot.getY() - Math.floor(box.minY);
        double localZ = pivot.getZ() - Math.floor(box.minZ);
        return new Vec3d(localX, localY, localZ);
    }

    private static Vec3d getFacingOffset(Direction facing) {
        Vec3d vec = Vec3d.of(facing.getOpposite().getVector());
        switch (facing) {
            case UP -> vec = vec.add(0, 1, 0);
            case DOWN -> vec = vec.add(0, -1, 0);
            case NORTH -> vec = vec.add(0, 0, -1);
            case SOUTH -> vec = vec.add(0, 0, 1);
            case WEST -> vec = vec.add(-1, 0, 0);
            case EAST -> vec = vec.add(1, 0, 0);
        }
        return vec;
    }

    // --------------------------- Tick ---------------------------
    public static <T extends BlockEntity> void onTick(World world, BlockPos pos, BlockState state, T blockEntity) {
        if (!(blockEntity instanceof TurntableBlockEntity tbe) || !(world instanceof ServerWorld serverWorld)) return;
        if (!tbe.nextUpdated) tbe.reloadModelIfNeeded();
    }
}
