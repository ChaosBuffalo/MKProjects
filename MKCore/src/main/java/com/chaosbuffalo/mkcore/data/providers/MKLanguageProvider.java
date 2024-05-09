package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

public class MKLanguageProvider extends LanguageProvider {
    public MKLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {

    }

    public void damageType(RegistryObject<MKDamageType> type, String directName, String periodicName) {
        add(MKDamageType.nameKey(type.getId()), directName);
        add(MKDamageType.periodicNameKey(type.getId()), periodicName);
    }
}
