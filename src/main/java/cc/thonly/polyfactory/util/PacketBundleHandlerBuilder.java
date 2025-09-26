package cc.thonly.polyfactory.util;

import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class PacketBundleHandlerBuilder {
    public static Integer MAX_SIZE = 4096 * 4096;
    public static <T extends PacketListener, P extends BundlePacket<? super T>> PacketBundleHandler getListener(PacketType<P> id, Function<Iterable<Packet<? super T>>, P> bundleFunction, BundleSplitterPacket<? super T> splitter)  {
        return new PacketBundleHandler() {
            public void forEachPacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.getPacketType() == id) {
                    P bundlePacket = (P) packet;
                    consumer.accept(splitter);
                    bundlePacket.getPackets().forEach(consumer);
                    consumer.accept(splitter);
                } else {
                    consumer.accept(packet);
                }

            }

            @Nullable
            public Bundler createBundler(Packet<?> splitterx) {
                return splitterx == splitter ? new Bundler() {
                    private final List<Packet<? super T>> packets = new ArrayList<>();

                    @Nullable
                    public Packet<?> add(Packet<?> packet) {
                        if (packet == splitter) {
                            return bundleFunction.apply(this.packets);
                        } else {
                            Packet<T> packet2 = (Packet<T>) packet;
                            if (this.packets.size() >= MAX_SIZE) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            } else {
                                this.packets.add(packet2);
                                return null;
                            }
                        }
                    }
                } : null;
            }
        };
    }
}
