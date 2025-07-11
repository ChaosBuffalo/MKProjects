package com.chaosbuffalo.mkiafcompat.data;

import com.chaosbuffalo.mkfaction.data.content.MKFactionRegistrySets;
import com.chaosbuffalo.mkfaction.data.content.MKFactionsDefaultFactionGenerator;
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
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MKIAFGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();

        var registrySets = new MKFactionRegistrySets(packOutput, event.getLookupProvider());
//        gen.addProvider(event.includeServer(), registrySets);
        CompletableFuture<HolderLookup.Provider> lookupProvider = registrySets.getRegistryProvider();
        gen.addProvider(event.includeServer(), new MKIAFDefaultFactionGenerator(packOutput, lookupProvider));

        // pack.mcmeta
        gen.addProvider(true, new PackMetadataGenerator(gen.getPackOutput())
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKFaction resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE))
                ))
        );
    }
}