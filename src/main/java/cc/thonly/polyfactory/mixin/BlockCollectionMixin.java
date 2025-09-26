package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.interfaces.BlockCollectionDSG;
import eu.pb4.polyfactory.block.collection.BlockCollection;
import eu.pb4.polyfactory.block.collection.BlockCollectionData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockCollection.class, remap = false)
public class BlockCollectionMixin implements BlockCollectionDSG {

    @Shadow @Final private BlockCollectionData data;

    @Shadow private float centerX;

    @Shadow private float centerY;

    @Shadow private float centerZ;

    @Override
    public BlockCollectionData getData() {
        return this.data;
    }

    @Override
    public float getCenterX() {
        return this.centerX;
    }

    @Override
    public float getCenterY() {
        return this.centerY;
    }

    @Override
    public float getCenterZ() {
        return this.centerZ;
    }
}
