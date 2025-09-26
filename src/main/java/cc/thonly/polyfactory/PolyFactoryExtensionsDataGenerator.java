package cc.thonly.polyfactory;

import cc.thonly.polyfactory.datagen.ModRecipeProvider;
import cc.thonly.polyfactory.datagen.ModSimpChineseLangProvider;
import cc.thonly.polyfactory.datagen.ModModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PolyFactoryExtensionsDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModSimpChineseLangProvider::new);
        pack.addProvider(ModRecipeProvider::new);
    }
}
