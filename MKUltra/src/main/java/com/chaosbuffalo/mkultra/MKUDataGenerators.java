package com.chaosbuffalo.mkultra;

import com.chaosbuffalo.mkcore.data.providers.MKAbilityProvider;
import com.chaosbuffalo.mkcore.data.content.MKCoreGenerators;
import com.chaosbuffalo.mkultra.data.generators.*;
import com.chaosbuffalo.mkultra.data.generators.tags.UltraBiomeTagsProvider;
import com.chaosbuffalo.mkultra.data.generators.tags.UltraItemTagsProvider;
import com.chaosbuffalo.mkultra.data.generators.tags.UltraStructureTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MKUDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();

        var datapackRegistrySets = new MKURegistrySets(packOutput, lookupProvider);
        generator.addProvider(event.includeServer(), datapackRegistrySets);
        var datapackLookup = datapackRegistrySets.getRegistryProvider();

        MKCoreGenerators.MKBlockTagsProvider blockTagsProvider = new MKCoreGenerators.MKBlockTagsProvider(packOutput,
                datapackLookup, MKUltra.MODID, fileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);

        generator.addProvider(event.includeServer(), new UltraBiomeTagsProvider(packOutput, datapackLookup, fileHelper));
        generator.addProvider(event.includeServer(), new UltraStructureTagsProvider(packOutput, datapackLookup, fileHelper));

//        generator.addProvider(event.includeServer(), new MKUFactionProvider(generator));
        generator.addProvider(event.includeServer(), new MKUDialogueProvider(generator));
        generator.addProvider(event.includeServer(), new MKULootTierProvider(generator));
        generator.addProvider(event.includeServer(), new MKUTalentTreeProvider(generator));
        generator.addProvider(event.includeServer(), new MKUQuestProvider(generator, datapackLookup));
        generator.addProvider(event.includeServer(), new MKUNpcProvider(generator, datapackLookup));
        generator.addProvider(event.includeServer(), new MKAbilityProvider.FromMod(generator, MKUltra.MODID));
        generator.addProvider(event.includeServer(), new MKURecipeProvider(packOutput, datapackLookup));

        generator.addProvider(event.includeClient(), new MKUItemModelProvider(packOutput, fileHelper));
        generator.addProvider(event.includeServer(), new UltraItemTagsProvider(generator, datapackLookup, blockTagsProvider, fileHelper));
    }
}
