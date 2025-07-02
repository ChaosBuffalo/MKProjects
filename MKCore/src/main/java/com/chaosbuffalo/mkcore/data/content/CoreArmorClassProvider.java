package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.data.providers.ArmorClassDataMapProvider;
import com.chaosbuffalo.mkcore.init.CoreTags;
import com.chaosbuffalo.mkcore.init.CoreArmorClasses;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class CoreArmorClassProvider extends ArmorClassDataMapProvider {
    protected CoreArmorClassProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gatherClasses(HolderLookup.Provider provider) {
        itemTag(CoreTags.Items.ROBES_ARMOR, CoreArmorClasses.ROBES_ARMOR);
        itemTag(CoreTags.Items.LIGHT_ARMOR, CoreArmorClasses.LIGHT_ARMOR);
        itemTag(CoreTags.Items.MEDIUM_ARMOR, CoreArmorClasses.MEDIUM_ARMOR);
        itemTag(CoreTags.Items.HEAVY_ARMOR, CoreArmorClasses.HEAVY_ARMOR);
    }
}
