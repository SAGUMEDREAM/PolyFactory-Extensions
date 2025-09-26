package cc.thonly.polyfactory.item;

import cc.thonly.polyfactory.PolyFactoryExtension;
import cc.thonly.polyfactory.item.overlay.PolymerItemImpl;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class ModItems {

    public static final Item SLIME_WRENCH = registerItem("slime_wrench", SlimeWrenchItem::new, new Item.Settings().maxCount(1));

    public static void registers() {

    }

    public static Item registerItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, PolyFactoryExtension.id(name));
        Item item = factory.apply(settings.registryKey(registryKey));
        Registry.register(Registries.ITEM, registryKey, item);
        registerOverlay(item);
        return item;
    }

    public static void registerOverlay(Item item) {
        PolymerItem.registerOverlay(item, new PolymerItemImpl(item));
    }
}
