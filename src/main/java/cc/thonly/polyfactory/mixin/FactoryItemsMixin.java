package cc.thonly.polyfactory.mixin;

import cc.thonly.polyfactory.item.ModItems;
import eu.pb4.polyfactory.item.FactoryItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FactoryItems.class, remap = false)
public class FactoryItemsMixin {
    @Inject(method = "register()V", at = @At("TAIL"))
    private static void register(CallbackInfo ci){
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of("polyfactory", "a_group"))).register(new ItemGroupEvents.ModifyEntries() {
            @Override
            public void modifyEntries(FabricItemGroupEntries entries) {
                entries.addAfter(FactoryItems.WRENCH, ModItems.SLIME_WRENCH);
            }
        });
    }
}
