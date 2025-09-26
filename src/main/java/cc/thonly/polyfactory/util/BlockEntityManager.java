package cc.thonly.polyfactory.util;

import cc.thonly.polyfactory.block.collection.BlockCollectionExtension;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polyfactory.block.collection.BlockCollectionData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Getter
public class BlockEntityManager {
    public static final BlockEntityManager INSTANCE = new BlockEntityManager();
    private static final Gson GSON = new Gson();
    public boolean isLoading = true;
    private Codec<Map<Long, BlockCollectionData>> inner_codec =
            Codec.unboundedMap(Codec.STRING, this.getCollectionCodec())
                    .xmap(map -> {
                        Object2ObjectOpenHashMap<Long, BlockCollectionData> result = new Object2ObjectOpenHashMap<>();
                        map.forEach((k, v) -> result.put(Long.parseLong(k), v));
                        return result;
                    }, map -> {
                        Object2ObjectOpenHashMap<String, BlockCollectionData> result = new Object2ObjectOpenHashMap<>();
                        map.forEach((k, v) -> result.put(Long.toString(k), v));
                        return result;
                    });
    private Codec<Map<RegistryKey<World>, Map<Long, BlockCollectionData>>> codec =
            Codec.unboundedMap(World.CODEC, inner_codec)
                    .xmap(Object2ObjectOpenHashMap::new, Function.identity());
    private Map<RegistryKey<World>, Map<Long, BlockCollectionData>> world2Pos2BlockEntityMap = new Object2ObjectOpenHashMap<>();
    private MinecraftServer server;
    private Path savePath;

    private BlockEntityManager() {
    }

    public RegistryKey<World> getWorldKey(ServerWorld world) {
        return world.getRegistryKey();
    }

    public BlockCollectionData getData(ServerWorld world, BlockPos blockPos) {
        RegistryKey<World> worldKey = this.getWorldKey(world);
        Map<Long, BlockCollectionData> longBlockCollectionDataMap = this.world2Pos2BlockEntityMap.get(worldKey);
        if (longBlockCollectionDataMap == null) {
            return null;
        }
        return longBlockCollectionDataMap.get(blockPos.asLong());
    }

    public void put(ServerWorld world, BlockPos blockPos, BlockCollectionData blockCollectionData) {
        RegistryKey<World> worldKey = this.getWorldKey(world);
        Map<Long, BlockCollectionData> dataMap = this.world2Pos2BlockEntityMap.computeIfAbsent(worldKey, key -> new Object2ObjectOpenHashMap<>());
        dataMap.put(blockPos.asLong(), blockCollectionData);
        this.onSave();
    }

    public void onBreak(ServerWorld world, BlockPos blockPos) {
        RegistryKey<World> worldKey = this.getWorldKey(world);
        Map<Long, BlockCollectionData> dataMap = this.world2Pos2BlockEntityMap.computeIfAbsent(worldKey, key -> new Object2ObjectOpenHashMap<>());
        dataMap.remove(blockPos.asLong());
        this.onSave();
    }

    public void onLoad() {
//        System.out.println("loading");
        File file = new File(this.savePath.toUri());
        if (!file.exists() || file.length() == 0) {
            this.world2Pos2BlockEntityMap = new Object2ObjectOpenHashMap<>();
            this.onSave();
            return;
        }
        DynamicRegistryManager.Immutable registryManager = this.server.getRegistryManager();
        RegistryOps<JsonElement> ops = registryManager.getOps(JsonOps.INSTANCE);
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytes = inputStream.readAllBytes();
            JsonElement element = JsonParser.parseString(new String(bytes));
            DataResult<Map<RegistryKey<World>, Map<Long, BlockCollectionData>>> dataResult = this.codec.parse(ops, element);
            Optional<Map<RegistryKey<World>, Map<Long, BlockCollectionData>>> result = dataResult.result();
            result.ifPresent(data -> this.world2Pos2BlockEntityMap = data);
        } catch (Exception err) {
            log.error("Can't load BlockEntityCollections", err);
            this.world2Pos2BlockEntityMap = new Object2ObjectOpenHashMap<>();
        }
//        System.out.println(this.world2Pos2BlockEntityMap);
    }

    public void onSave() {
        File file = new File(this.savePath.toUri());
        DynamicRegistryManager.Immutable registryManager = this.server.getRegistryManager();
        RegistryOps<JsonElement> ops = registryManager.getOps(JsonOps.INSTANCE);
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            DataResult<JsonElement> dataResult = this.codec.encodeStart(ops, this.world2Pos2BlockEntityMap);
            Optional<JsonElement> result = dataResult.result();
//            System.out.println(this.world2Pos2BlockEntityMap);
//            System.out.println(result.isPresent());
            if (result.isPresent()) {
                JsonElement element = result.get();
                String json = GSON.toJson(element);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
//                System.out.println(json);
                outputStream.write(bytes);
            }
            dataResult.error().ifPresent(err -> log.error("Encode error: {}", err));
        } catch (Exception err) {
            log.error("Can't save BlockEntityCollections", err);
        }
    }

    public Codec<BlockCollectionData> getCollectionCodec() {
        if (this.server == null) {
            return null;
        }
        DynamicRegistryManager registryManager = this.server.getRegistryManager();
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("sizeX").forGetter(BlockCollectionData::sizeX),
                Codec.INT.fieldOf("sizeY").forGetter(BlockCollectionData::sizeY),
                Codec.INT.fieldOf("sizeZ").forGetter(BlockCollectionData::sizeZ),
                BlockState.CODEC.listOf().fieldOf("states").forGetter(d -> {
                    BlockState[] states = d.states();
                    return Arrays.stream(states)
                            .map(s -> s == null ? Blocks.AIR.getDefaultState() : s)
                            .toList();
                })
                ,
                Codec.list(NbtCompound.CODEC).fieldOf("blockEntities").forGetter(d -> {
                    BlockEntity[] bes = d.blockEntities();
                    return Arrays.stream(bes)
                            .map(be -> be != null ? be.createNbtWithIdentifyingData(registryManager) : new NbtCompound())
                            .toList();
                })
        ).apply(instance, (sx, sy, sz, statesList, nbtList) -> {
            BlockState[] states = statesList.toArray(new BlockState[0]);
            BlockEntity[] bes = new BlockEntity[nbtList.size()];
            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound tag = nbtList.get(i);
                if (!tag.isEmpty()) {
                    BlockState state = states[i];
                    int x = tag.getInt("x", 0);
                    int y = tag.getInt("y", 0);
                    int z = tag.getInt("z", 0);
                    bes[i] = BlockEntity.createFromNbt(new BlockPos(x, y, z), state, tag, registryManager);
                }
            }
            return new BlockCollectionData(sx, sy, sz, states, bes);
        }));
    }

    public void loadServer(MinecraftServer server) {
        this.isLoading = true;
        if (server == null) {
            return;
        }
        this.server = server;
        this.savePath = server.getSavePath(WorldSavePath.ROOT).resolve("./block_entity_info.json");
        this.world2Pos2BlockEntityMap.clear();
        this.initCodec();
        this.onLoad();
        this.isLoading = false;
    }

    private void initCodec() {
        this.inner_codec =
                Codec.unboundedMap(Codec.STRING, this.getCollectionCodec())
                        .xmap(map -> {
                            Object2ObjectOpenHashMap<Long, BlockCollectionData> result = new Object2ObjectOpenHashMap<>();
                            map.forEach((k, v) -> result.put(Long.parseLong(k), v));
                            return result;
                        }, map -> {
                            Object2ObjectOpenHashMap<String, BlockCollectionData> result = new Object2ObjectOpenHashMap<>();
                            map.forEach((k, v) -> result.put(Long.toString(k), v));
                            return result;
                        });
        this.codec = Codec.unboundedMap(World.CODEC, this.inner_codec)
                .xmap(Object2ObjectOpenHashMap::new, Function.identity());
    }
}
