package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.PolyFactoryExtension;
import cc.thonly.polyfactory.block.ModBlockEntities;
import cc.thonly.polyfactory.block.entity.TurntableBlockEntity;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.polyfactory.block.configurable.ConfigurableBlock;
import eu.pb4.polyfactory.block.mechanical.RotationalNetworkBlock;
import eu.pb4.polyfactory.block.mechanical.TurntableBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(TurntableBlock.class)
public abstract class TurntableBlockMixin extends RotationalNetworkBlock implements FactoryBlock, BarrierBasedWaterloggable, ConfigurableBlock, BlockEntityProvider {
    protected TurntableBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "createElementHolder", at = @At("RETURN"))
    public void createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState, CallbackInfoReturnable<ElementHolder> cir) {
        ElementHolder holder = cir.getReturnValue();
        if (holder instanceof TurntableBlock.Model model) {
            Map<Long, TurntableBlock.Model> longModelMap = PolyFactoryExtension.WORLD_POS_TURN_MAP.computeIfAbsent(world, w -> new Object2ObjectLinkedOpenHashMap<>());
            longModelMap.put(pos.asLong(), model);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.TURNTABLE_BLOCK ? TurntableBlockEntity::onTick : null;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TurntableBlockEntity(pos, state);
    }
}
