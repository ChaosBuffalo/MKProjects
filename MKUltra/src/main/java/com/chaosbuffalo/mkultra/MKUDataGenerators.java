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
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKUDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();
        MKCoreGenerators.MKBlockTagsProvider blockTagsProvider = new MKCoreGenerators.MKBlockTagsProvider(generator.getPackOutput(),
                event.getLookupProvider(), MKUltra.MODID, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTagsProvider);

        generator.addProvider(event.includeServer(), new MKURegistrySets(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new UltraBiomeTagsProvider(packOutput, lookupProvider, fileHelper));
        generator.addProvider(event.includeServer(), new UltraStructureTagsProvider(packOutput, lookupProvider, fileHelper));

        generator.addProvider(event.includeServer(), new MKUFactionProvider(generator));
        generator.addProvider(event.includeServer(), new MKUDialogueProvider(generator));
        generator.addProvider(event.includeServer(), new MKULootTierProvider(generator));
        generator.addProvider(event.includeServer(), new MKUTalentTreeProvider(generator));
        generator.addProvider(event.includeServer(), new MKUQuestProvider(generator));
        generator.addProvider(event.includeServer(), new MKUNpcProvider(generator));
        generator.addProvider(event.includeServer(), new MKAbilityProvider.FromMod(generator, MKUltra.MODID));
        generator.addProvider(event.includeServer(), new MKURecipeProvider(packOutput));

        generator.addProvider(event.includeClient(), new MKUItemModelProvider(packOutput, fileHelper));
        generator.addProvider(event.includeServer(), new UltraItemTagsProvider(generator,
                event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
    }
}
