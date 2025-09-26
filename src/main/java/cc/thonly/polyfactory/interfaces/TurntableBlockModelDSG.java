package cc.thonly.polyfactory.interfaces;

import cc.thonly.polyfactory.block.collection.BlockCollectionExtension;
import eu.pb4.polyfactory.block.collection.BlockCollection;

public interface TurntableBlockModelDSG {
    void setBlockCollection(BlockCollection collection);
    BlockCollection getBlockCollection();
    void setBlockCollectionExtension(BlockCollectionExtension collection);
    BlockCollectionExtension getBlockCollectionExtension();
}
