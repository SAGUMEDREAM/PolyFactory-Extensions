package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.block.collection.BlockCollectionExtension;
import cc.thonly.polyfactory.block.collection.BlockCollectionExtensionView;
import eu.pb4.polyfactory.block.collection.BlockCollection;
import eu.pb4.polyfactory.block.collection.BlockCollectionView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements BlockCollectionExtensionView {
    @Unique
    private final List<BlockCollectionExtension> collections = new ArrayList<>();

    public ServerWorldMixin() {
    }

    public void provideCollision(Box box, Consumer<VoxelShape> consumer) {
        Iterator<BlockCollectionExtension> iterator = this.collections.iterator();

        while (iterator.hasNext()) {
            BlockCollectionExtension x = (BlockCollectionExtension) iterator.next();
            x.provideCollisions(box, consumer);
        }

    }

    public void addCollision(BlockCollectionExtension collection) {
        this.collections.add(collection);
    }

    public void removeCollision(BlockCollectionExtension collection) {
        this.collections.remove(collection);
    }
}
