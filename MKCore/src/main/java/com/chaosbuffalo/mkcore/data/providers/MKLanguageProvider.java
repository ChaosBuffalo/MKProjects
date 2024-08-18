package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MKLanguageProvider extends LanguageProvider {
    public MKLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {

    }

    public void damageType(Holder<MKDamageType> type, String directName, String periodicName) {
        add(MKDamageType.nameKey(type.getKey().location()), directName);
        add(MKDamageType.periodicNameKey(type.getKey().location()), periodicName);
    }
}
