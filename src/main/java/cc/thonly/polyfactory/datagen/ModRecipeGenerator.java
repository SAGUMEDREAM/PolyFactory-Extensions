package cc.thonly.polyfactory.datagen;

import cc.thonly.polyfactory.item.ModItems;
import eu.pb4.polyfactory.item.FactoryItems;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

public class ModRecipeGenerator extends RecipeGenerator {
    protected ModRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
    }

    @Override
    public void generate() {
        createShaped(RecipeCategory.TOOLS, ModItems.SLIME_WRENCH)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', Items.SLIME_BALL)
                .input('#', FactoryItems.WRENCH)
                .criterion("has_slime", conditionsFromItem(Items.SLIME_BALL))
                .offerTo(exporter, getRecipeName(ModItems.SLIME_WRENCH));
    }
}
