package com.chaosbuffalo.mkultra;

import com.chaosbuffalo.mkultra.data.generators.MKUDialogueProvider;
import com.chaosbuffalo.mkultra.data.generators.MKUFactionProvider;
import com.chaosbuffalo.mkultra.data.generators.MKURegistrySets;
import com.chaosbuffalo.mkultra.data.generators.tags.UltraBiomeTagsProvider;
import com.chaosbuffalo.mkultra.data.generators.tags.UltraStructureTagsProvider;
import com.chaosbuffalo.mkultra.data_generators.MKULootTierProvider;
import com.chaosbuffalo.mkultra.data_generators.MKUTalentTreeProvider;
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

        generator.addProvider(event.includeServer(), new MKURegistrySets(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new UltraBiomeTagsProvider(packOutput, lookupProvider, fileHelper));
        generator.addProvider(event.includeServer(), new UltraStructureTagsProvider(packOutput, lookupProvider, fileHelper));

        generator.addProvider(event.includeServer(), new MKUFactionProvider(generator));
        generator.addProvider(event.includeServer(), new MKUDialogueProvider(generator));
        generator.addProvider(event.includeServer(), new MKULootTierProvider(generator));
        generator.addProvider(event.includeServer(), new MKUTalentTreeProvider(generator));


        if (event.includeServer()) {
//            generator.addProvider(new AbilityDataGenerator(generator, MKUltra.MODID));
//            generator.addProvider(new MKUNpcProvider(generator));
//            generator.addProvider(new MKUQuestProvider(generator));
//            generator.addProvider(new MKUConfiguredStructureProvider(generator));

        }
        if (event.includeClient()) {
//            generator.addProvider(new MKUItemModelProvider(generator, helper));
        }
    }
}
