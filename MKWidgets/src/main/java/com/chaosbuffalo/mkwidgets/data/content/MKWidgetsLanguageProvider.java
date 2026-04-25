package com.chaosbuffalo.mkwidgets.data.content;

import com.chaosbuffalo.mkwidgets.MKWidgets;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Language provider for MKWidgets-generated translation entries.
 */
public class MKWidgetsLanguageProvider extends LanguageProvider {

    /**
     * @param output pack output for generated resources
     * @param locale locale code being generated
     */
    public MKWidgetsLanguageProvider(PackOutput output, String locale) {
        super(output, MKWidgets.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("key.mkwidgets.category", "MKWidgets");
        add("key.mkwidgets.test.desc", "Open Widget Test Screen");
    }
}
