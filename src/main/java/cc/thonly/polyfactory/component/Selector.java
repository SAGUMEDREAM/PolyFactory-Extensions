package cc.thonly.polyfactory.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Optional;

public record Selector(Optional<BlockPos> blockPosA, Optional<BlockPos> blockPosB, RegistryKey<World> worldKey) {
    public static final Codec<Selector> CODEC = RecordCodecBuilder.create(x -> x.group(
            BlockPos.CODEC.optionalFieldOf("BlockPosA").forGetter(Selector::blockPosA),
            BlockPos.CODEC.optionalFieldOf("BlockPosB").forGetter(Selector::blockPosB),
            World.CODEC.fieldOf("WorldKey").forGetter(Selector::worldKey)
    ).apply(x, Selector::new));

    public Box toBox() {
        if (this.blockPosA.isEmpty() || this.blockPosB.isEmpty()) {
            throw new NullPointerException("Null box size");
        }
        var a = this.blockPosA().get();
        var b = this.blockPosB().get();
        return new Box(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
    }

    public boolean isComplete() {
        return this.blockPosA.isPresent() && this.blockPosB.isPresent();
    }

    public Selector copy() {
        return new Selector(blockPosA, blockPosB, worldKey);
    }
}
