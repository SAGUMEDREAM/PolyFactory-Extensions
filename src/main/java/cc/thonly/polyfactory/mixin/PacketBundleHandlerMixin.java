package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.util.PacketBundleHandlerBuilder;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(PacketBundleHandler.class)
public interface PacketBundleHandlerMixin {
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static <T extends PacketListener, P extends BundlePacket<? super T>> void onCreate(PacketType<P> id, Function<Iterable<Packet<? super T>>, P> bundleFunction, BundleSplitterPacket<? super T> splitter, CallbackInfoReturnable<PacketBundleHandler> cir)  {
        cir.setReturnValue(PacketBundleHandlerBuilder.getListener(id, bundleFunction, splitter));
    }

}
