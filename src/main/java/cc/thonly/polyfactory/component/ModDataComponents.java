package cc.thonly.polyfactory.component;

import cc.thonly.polyfactory.PolyFactoryExtension;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final ComponentType<Selector> SELECTOR = register("selector", (builder)->{
        return builder.codec(Selector.CODEC);
    });

    public static void registers() {

    }

    public static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        ComponentType<T> componentType = builderOperator.apply(ComponentType.builder()).build();
        Registry.register(Registries.DATA_COMPONENT_TYPE, PolyFactoryExtension.id(id), componentType);
        PolymerComponent.registerDataComponent(componentType);
        return componentType;
    }
}
