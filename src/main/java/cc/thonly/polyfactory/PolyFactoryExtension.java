package cc.thonly.polyfactory;

import cc.thonly.polyfactory.block.ModBlockEntities;
import cc.thonly.polyfactory.block.entity.TurntableBlockEntity;
import cc.thonly.polyfactory.component.ModDataComponents;
import cc.thonly.polyfactory.item.ModItems;
import cc.thonly.polyfactory.util.BlockEntityManager;
import cc.thonly.polyfactory.util.DelayedTask;
import eu.pb4.polyfactory.block.mechanical.TurntableBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.DyeTintSource;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PolyFactoryExtension implements ModInitializer {
    public static final String MOD_ID = "polyfactory-extensions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER;
    public static final Map<ServerWorld, Map<Long, TurntableBlock.Model>> WORLD_POS_TURN_MAP = new Object2ObjectLinkedOpenHashMap<>();

    @Override
    public void onInitialize() {
        ModDataComponents.registers();
        ModItems.registers();
        ModBlockEntities.registers();

        PlayerBlockBreakEvents.BEFORE.register(new PlayerBlockBreakEvents.Before() {
            @Override
            public boolean beforeBlockBreak(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity) {
                if (blockEntity instanceof TurntableBlockEntity turntableBE) {
                    turntableBE.onBreak(world, playerEntity, blockPos, blockState, blockEntity);
                }
                return true;
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(DelayedTask::tick);
        ServerLifecycleEvents.AFTER_SAVE.register(new ServerLifecycleEvents.AfterSave() {
            @Override
            public void onAfterSave(MinecraftServer minecraftServer, boolean b, boolean b1) {
                BlockEntityManager.INSTANCE.onSave();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(new ServerLifecycleEvents.ServerStopped() {
            @Override
            public void onServerStopped(MinecraftServer minecraftServer) {
                WORLD_POS_TURN_MAP.clear();
            }
        });
        PolymerResourcePackUtils.markAsRequired();
        PolymerResourcePackUtils.addModAssets("polyfactory");
        PolymerResourcePackUtils.addModAssets("polyfactory-lang");
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                id("block"),
                id("item"),
                id("lang")
        );
    }

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }
}