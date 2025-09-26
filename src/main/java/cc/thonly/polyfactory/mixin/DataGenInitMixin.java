package cc.thonly.polyfactory.mixin;

import eu.pb4.polyfactory.datagen.DataGenInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DataGenInit.class, remap = false)
public class DataGenInitMixin {
    @Inject(method = "onInitializeDataGenerator", at = @At("HEAD"), cancellable = true)
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator, CallbackInfo ci) {
        ci.cancel();
    }
}
