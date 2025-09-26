package cc.thonly.polyfactory.block;

import cc.thonly.polyfactory.PolyFactoryExtension;
import cc.thonly.polyfactory.block.entity.TurntableBlockEntity;
import eu.pb4.polyfactory.block.FactoryBlocks;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<TurntableBlockEntity> TURNTABLE_BLOCK =
            registerBlockEntity("turntable", TurntableBlockEntity::new, FactoryBlocks.TURNTABLE);
    public static void registers() {

    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        Identifier id = PolyFactoryExtension.id(name);
        BlockEntityType<T> entityType = FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build();
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id, entityType);
        PolymerBlockUtils.registerBlockEntity(entityType);
        return entityType;
    }
}
