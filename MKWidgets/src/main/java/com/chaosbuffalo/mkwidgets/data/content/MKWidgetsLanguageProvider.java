package com.chaosbuffalo.mkwidgets.data.content;

import com.chaosbuffalo.mkwidgets.MKWidgets;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MKWidgetsLanguageProvider extends LanguageProvider {

    public MKWidgetsLanguageProvider(PackOutput output, String locale) {
        super(output, MKWidgets.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("key.mkwidgets.category", "MKWidgets");
        add("key.mkwidgets.test.desc", "Open Widget Test Screen");
    }
}
