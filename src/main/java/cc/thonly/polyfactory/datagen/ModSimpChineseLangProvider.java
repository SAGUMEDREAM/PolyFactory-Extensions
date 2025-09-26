package cc.thonly.polyfactory.datagen;

import cc.thonly.polyfactory.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModSimpChineseLangProvider extends FabricLanguageProvider {

    public ModSimpChineseLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ModItems.SLIME_WRENCH, "粘液扳手");

        translationBuilder.add(ModLanguageKey.ITEM_SELECT_FIRST.getCode(), "选择了第一个点: %s");
        translationBuilder.add(ModLanguageKey.ITEM_SELECT_SECOND.getCode(), "选择了第二个点: %s");
        translationBuilder.add(ModLanguageKey.ITEM_SELECT_FINISH.getCode(), "两个点已选择完毕!");
        translationBuilder.add(ModLanguageKey.ITEM_SELECT_SUCCESS.getCode(), "完成");
        translationBuilder.add(ModLanguageKey.ITEM_SELECT_FAIL.getCode(), "你选择的区域太大了! %s > %s");
    }
}
