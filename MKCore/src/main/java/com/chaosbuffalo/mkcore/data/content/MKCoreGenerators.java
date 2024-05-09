package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.data.providers.MKAbilityProvider;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKCoreGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        CoreLanguageProvider languageProvider = new CoreLanguageProvider(generator.getPackOutput(), "en_us");

        if (event.includeServer()) {
            MKBlockTagsProvider blockTagsProvider = new MKBlockTagsProvider(generator.getPackOutput(),
                    event.getLookupProvider(), MKCore.MOD_ID, event.getExistingFileHelper());
            generator.addProvider(true, blockTagsProvider);
            generator.addProvider(true, new MKAbilityProvider.FromMod(generator, MKCore.MOD_ID));
            generator.addProvider(true, new CoreItemTagsProvider(generator,
                    event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
            generator.addProvider(true, new CoreTalentTreeProvider(generator));
            generator.addProvider(true, new CoreParticleProvider(generator));

            new CoreAbilityLanguageProvider(languageProvider).run();
        }

        generator.addProvider(event.includeClient(), new CoreSoundProvider(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(true, languageProvider);
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

