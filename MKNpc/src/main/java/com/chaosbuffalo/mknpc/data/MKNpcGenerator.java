package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.data.content.MKNpcDefaultFactionProvider;
import com.chaosbuffalo.mknpc.data.content.MKNpcLanguageProvider;
import com.chaosbuffalo.mknpc.data.content.MKNpcMeleeAnimationProvider;
import com.chaosbuffalo.mknpc.data.generators.NpcRegistrySets;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcBiomeTagsProvider;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcEntityTypeTagsProvider;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcStructureTagsProvider;
import com.chaosbuffalo.mknpc.data.providers.NpcModelLookProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MKNpcGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();

        NpcRegistrySets datapackRegistrySets = new NpcRegistrySets(packOutput, lookupProvider);
        generator.addProvider(event.includeServer(), datapackRegistrySets);
        var datapackLookup = datapackRegistrySets.getRegistryProvider();
        generator.addProvider(event.includeServer(), new NpcBiomeTagsProvider(packOutput, datapackLookup, fileHelper));
        generator.addProvider(event.includeServer(), new NpcStructureTagsProvider(packOutput, datapackLookup, fileHelper));
        generator.addProvider(event.includeServer(), new NpcEntityTypeTagsProvider(packOutput, datapackLookup, fileHelper));
        generator.addProvider(event.includeServer(), new MKNpcDefaultFactionProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new MKNpcLanguageProvider(packOutput, "en_us"));
        generator.addProvider(event.includeClient(), new MKNpcMeleeAnimationProvider(packOutput));
        generator.addProvider(event.includeServer(), new NpcModelLookProvider(generator));


        // pack.mcmeta
        generator.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKNpc resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE))
                ))
        );
    }

}
