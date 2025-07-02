package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.data.content.MKCoreGenerators;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.data.content.*;
import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.*;

@EventBusSubscriber
public class MKWeaponsGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        var lookup = event.getLookupProvider();

        gen.addProvider(event.includeServer(), new MKWeaponRecipeProvider(gen.getPackOutput(), lookup));
        gen.addProvider(event.includeServer(), new MKWeaponTypesProvider(gen, lookup));
        gen.addProvider(event.includeServer(), new MKWeaponsLootTierProvider(gen, lookup));
        MKCoreGenerators.MKBlockTagsProvider blockTagsProvider = new MKCoreGenerators.MKBlockTagsProvider(
                gen.getPackOutput(), lookup, MKWeapons.MODID, helper);
        gen.addProvider(event.includeServer(), blockTagsProvider);
        gen.addProvider(event.includeServer(), new MKWeaponsItemTagProvider(gen, lookup, blockTagsProvider, helper));
        gen.addProvider(event.includeClient(), new MKWeaponModelProvider(gen.getPackOutput(), helper, MKWeapons.MODID));
        gen.addProvider(event.includeServer(), new MKWeaponCurioGenerator(MKWeapons.MODID, gen.getPackOutput(), event.getExistingFileHelper(), lookup));

        // pack.mcmeta
        gen.addProvider(true, new PackMetadataGenerator(gen.getPackOutput())
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKWeapons resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE))
                ))
        );
    }

}