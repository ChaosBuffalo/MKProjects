package com.chaosbuffalo.mknpc.data.providers;

import com.chaosbuffalo.mkcore.data.providers.MKLanguageProvider;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;

public class NpcLanguageProvider extends MKLanguageProvider {

    public NpcLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    public void faction(ResourceKey<MKFaction> faction, String name) {
        add(faction.location().toLanguageKey("faction", "name"), name);
    }
}
