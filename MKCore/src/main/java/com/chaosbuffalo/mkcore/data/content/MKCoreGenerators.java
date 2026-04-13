package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.data.providers.MKAbilityProvider;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
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
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MKCoreGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        var registrySets = new CoreRegistrySets(packOutput, event.getLookupProvider());
        generator.addProvider(true, registrySets);
        var datapackLookup = registrySets.getRegistryProvider();

        CoreLanguageProvider languageProvider = new CoreLanguageProvider(packOutput, "en_us");

        if (event.includeServer()) {
            MKBlockTagsProvider blockTagsProvider = new MKBlockTagsProvider(packOutput,
                    datapackLookup, MKCore.MOD_ID, event.getExistingFileHelper());
            generator.addProvider(true, blockTagsProvider);
            generator.addProvider(true, new MKAbilityProvider.FromMod(generator, MKCore.MOD_ID));
            generator.addProvider(true, new CoreItemTagsProvider(generator,
                    datapackLookup, blockTagsProvider, event.getExistingFileHelper()));
            generator.addProvider(true, new CoreParticleProvider(generator));
            generator.addProvider(true, new CoreArmorClassProvider(packOutput, datapackLookup));
            generator.addProvider(true, new CoreDimensionDifficultyProvider(packOutput, datapackLookup));
            generator.addProvider(true, new CoreItemCriticalStatsProvider(packOutput, datapackLookup));
            generator.addProvider(true, new CoreItemBlockStatsProvider(packOutput, datapackLookup));

            new CoreAbilityLanguageProvider(languageProvider).run();
        }

        generator.addProvider(event.includeClient(), new CoreSoundProvider(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new CoreMeleeAnimationProvider(packOutput));
        generator.addProvider(event.includeClient(), new CoreSpellAnimationProvider(packOutput));
        generator.addProvider(true, languageProvider);

        // pack.mcmeta
        generator.addProvider(true, new PackMetadataGenerator(generator.getPackOutput())
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKCore resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE))
                ))
        );
    }

    public static class CoreAbilityLanguageProvider extends MKAbilityProvider.AbilityLanguageProvider {

        public CoreAbilityLanguageProvider(LanguageProvider languageProvider) {
            super(languageProvider);
        }

        public void run() {
            ability(MKTestAbilities.TEST_EMBER)
                    .name("Test Ember")
                    .description("Deals %s to your target and sets them ablaze for %s seconds.")
                    .build();
        }
    }

    public static class MKBlockTagsProvider extends BlockTagsProvider {


        public MKBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   String modId, @Nullable ExistingFileHelper existingFileHelper) {
            super(packOutput, lookupProvider, modId, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {

        }
    }

}

