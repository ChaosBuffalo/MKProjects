package com.chaosbuffalo.mkcore.abilities;

import net.minecraft.resources.ResourceLocation;

public class AbilityTranslations {

    public static String nameKey(ResourceLocation abilityId) {
        return abilityId.toLanguageKey("ability", "name");
    }

    public static String descriptionKey(ResourceLocation abilityId) {
        return abilityId.toLanguageKey("ability", "description");
    }

    public static String customKey(ResourceLocation abilityId, String type) {
        return abilityId.toLanguageKey("ability", type);
    }

}
