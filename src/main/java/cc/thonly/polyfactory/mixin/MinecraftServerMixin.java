package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.PolyFactoryExtension;
import cc.thonly.polyfactory.util.BlockEntityManager;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onConstructor(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        PolyFactoryExtension.SERVER = server;
    }

    @Inject(at = @At("HEAD"), method = "loadWorld")
    private void loadWorld(CallbackInfo info) {
    }

    @Inject(at = @At("TAIL"), method = "loadWorld")
    private void loadWorldTail(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        BlockEntityManager.INSTANCE.loadServer(server);
    }
}