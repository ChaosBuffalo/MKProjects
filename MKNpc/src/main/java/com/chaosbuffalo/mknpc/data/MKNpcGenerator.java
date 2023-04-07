package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.data.generators.NpcRegistrySets;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcBiomeTagsProvider;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcStructureTagsProvider;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKNpcGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeServer(), new NpcRegistrySets(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new NpcBiomeTagsProvider(packOutput, lookupProvider, fileHelper));
        generator.addProvider(event.includeServer(), new NpcStructureTagsProvider(packOutput, lookupProvider, fileHelper));
//        gen.addProvider(event.includeServer(), new NpcDefinitionProvider(gen));
//        gen.addProvider(event.includeServer(), new NpcPoolProvider(gen));
//        gen.addProvider(event.includeServer(), new NpcConfiguredStructureProvider(gen));
//        gen.addProvider(event.includeServer(), new NpcStructureSetProvider(gen));


        // pack.mcmeta
        generator.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKNpc resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
                        Arrays.stream(PackType.values()).collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion))
                ))
        );
    }
}
