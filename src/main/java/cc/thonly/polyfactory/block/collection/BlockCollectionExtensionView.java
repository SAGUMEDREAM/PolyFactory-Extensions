package cc.thonly.polyfactory.block.collection;

import eu.pb4.polyfactory.block.collection.BlockCollection;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.function.Consumer;

public interface BlockCollectionExtensionView {
    void provideCollision(Box var1, Consumer<VoxelShape> var2);

    void addCollision(BlockCollectionExtension var1);

    void removeCollision(BlockCollectionExtension var1);
}
