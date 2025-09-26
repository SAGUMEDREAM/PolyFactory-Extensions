package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.block.collection.BlockCollectionExtension;
import cc.thonly.polyfactory.interfaces.TurntableBlockModelDSG;
import eu.pb4.polyfactory.block.collection.BlockCollection;
import eu.pb4.polyfactory.block.mechanical.TurntableBlock;
import eu.pb4.polyfactory.models.RotationAwareModel;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TurntableBlock.Model.class, remap = false)
public abstract class TurntableBlockModelMixin extends RotationAwareModel implements TurntableBlockModelDSG {

    @Shadow
    private BlockCollection blocks;
    @Shadow @Final private ItemDisplayElement mainElement;
    @Unique
    private BlockCollectionExtension blockCollectionExtensions;

    @Override
    public void setBlockCollection(BlockCollection collection) {
        this.blocks = collection;
    }

    @Override
    public BlockCollection getBlockCollection() {
        return this.blocks;
    }

    @Override
    public void setBlockCollectionExtension(BlockCollectionExtension collection) {
        this.blockCollectionExtensions = collection;
    }

    @Override
    public BlockCollectionExtension getBlockCollectionExtension() {
        return this.blockCollectionExtensions;
    }


    @Inject(method = "setAttachment", at= @At(value = "INVOKE", target = "Leu/pb4/polyfactory/models/RotationAwareModel;setAttachment(Leu/pb4/polymer/virtualentity/api/attachment/HolderAttachment;)V"))
    public void onSetAttachment(HolderAttachment attachment, CallbackInfo ci) {
        if (this.blockCollectionExtensions != null) {
            this.blockCollectionExtensions.setWorld(attachment != null ? attachment.getWorld() : null);
        }
    }

    @Inject(method = "destroy", at = @At("TAIL"))
    public void OnDestroy(CallbackInfo ci) {
        if (this.blockCollectionExtensions != null) {
            this.blockCollectionExtensions.setWorld((ServerWorld)null);
        }
    }

    @Inject(method = "onTick", at = @At(value = "INVOKE", target = "Leu/pb4/polyfactory/block/mechanical/TurntableBlock$Model;getUpdateRate()I"))
    public void OnTick(CallbackInfo ci) {
        if (this.blockCollectionExtensions != null) {
            Direction facing = this.blockState().get(TurntableBlock.FACING);
            float rotation = this.getRotation();
            this.blockCollectionExtensions.setQuaternion((new Quaternionf()).rotateAxis(facing.getDirection() == Direction.AxisDirection.NEGATIVE ? rotation : -rotation, facing.getOpposite().getUnitVector()));
            this.blockCollectionExtensions.tick();
        }
    }
}
