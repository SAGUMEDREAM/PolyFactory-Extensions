package cc.thonly.polyfactory.datagen;

import lombok.Getter;

@Getter
public enum ModLanguageKey {
    ITEM_SELECT_FIRST("item.wrench.select.first"),
    ITEM_SELECT_SECOND("item.wrench.select.second"),
    ITEM_SELECT_FINISH("item.wrench.select.finish"),
    ITEM_SELECT_FAIL("item.wrench.select.fail"),
    ITEM_SELECT_SUCCESS("item.wrench.select.success"),
    ;
    private final String code;

    ModLanguageKey(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
