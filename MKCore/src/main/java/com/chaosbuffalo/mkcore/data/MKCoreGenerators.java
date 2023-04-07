package com.chaosbuffalo.mkcore.data;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentLineDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.BrownianMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.OrbitingInPlaneMotionTrack;
import com.chaosbuffalo.mkcore.init.CoreTags;
import com.chaosbuffalo.mkcore.init.CoreTalents;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.*;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKCoreGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            MKBlockTagsProvider blockTagsProvider = new MKBlockTagsProvider(generator.getPackOutput(),
                    event.getLookupProvider(), MKCore.MOD_ID, event.getExistingFileHelper());
            generator.addProvider(true, blockTagsProvider);
            generator.addProvider(true, new MKAbilityProvider.FromMod(generator, MKCore.MOD_ID));
            generator.addProvider(true, new ArmorClassItemTagProvider(generator,
                    event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
            generator.addProvider(true, new CoreTalentTreeProvider(generator));
            generator.addProvider(true, new CoreParticleProvider(generator));
        }
    }



    public static class CoreParticleProvider extends ParticleAnimationProvider {

        public CoreParticleProvider(DataGenerator generator) {
            super(generator, MKCore.MOD_ID);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return writeBlueMagicTest(pOutput);
        }

        public CompletableFuture<?> writeBlueMagicTest(CachedOutput pOutput) {
            ParticleAnimation anim = new ParticleAnimation()
                    .withKeyFrame(new ParticleKeyFrame()
                            .withColor(0.0f, 1.0f, 242.0f / 255.0f)
                            .withScale(0.5f, 0.25f)
                    )
                    .withKeyFrame(new ParticleKeyFrame(0, 20)
                            .withMotion(new OrbitingInPlaneMotionTrack(10.0, 0.0, .25f))
                    )
                    .withKeyFrame(new ParticleKeyFrame(20, 20)
                            .withMotion(new OrbitingInPlaneMotionTrack(15.0, 0.0, .25f))
                    )
                    .withKeyFrame(new ParticleKeyFrame(0, 40)
                            .withColor(0.0f, 0.5f, 0.5f)
                            .withScale(0.15f, .05f)
                    )
                    .withKeyFrame(new ParticleKeyFrame(40, 40)
                            .withColor(1.0f, 0.0f, 1.0f)
                            .withScale(.01f, 0.0f)
                            .withMotion(new BrownianMotionTrack(5, 0.025f))
                    );
            return writeAnimation(MKCore.makeRL("particle_anim.blue_magic"), anim, pOutput);
        }
    }

    public static class CoreTalentTreeProvider extends TalentTreeProvider {

        public CoreTalentTreeProvider(DataGenerator generator) {
            super(generator, MKCore.MOD_ID);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            TalentTreeDefinition test = new TalentTreeDefinition(MKCore.makeRL("knight"));
            test.setVersion(1);
            TalentLineDefinition line = new TalentLineDefinition(test, "knight_1");
            line.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 1, 1.0));
            test.addLine(line);
            return writeDefinition(test, pOutput);
        }
    }

    public static class MKBlockTagsProvider extends BlockTagsProvider {


        public MKBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   String modId, @Nullable ExistingFileHelper existingFileHelper) {
            super(packOutput,lookupProvider, modId, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {

        }
    }

    public static class ArmorClassItemTagProvider extends ItemTagsProvider {
        public ArmorClassItemTagProvider(DataGenerator dataGenerator,
                                         CompletableFuture<HolderLookup.Provider> lookupProvider,
                                         BlockTagsProvider blockTagProvider,
                                         ExistingFileHelper existingFileHelper) {
            super(dataGenerator.getPackOutput(), lookupProvider,  blockTagProvider.contentsGetter(), MKCore.MOD_ID, existingFileHelper);
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore armor class item tags";
        }

        @Override
        protected void addTags(HolderLookup.Provider pProvider) {
            tag(CoreTags.Items.LIGHT_ARMOR)
                    .add(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
            tag(CoreTags.Items.MEDIUM_ARMOR)
                    .add(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS)
                    .add(Items.TURTLE_HELMET);
            tag(CoreTags.Items.HEAVY_ARMOR)
                    .add(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
                    .add(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
            tag(CoreTags.Items.ARMOR)
                    .addTag(CoreTags.Items.LIGHT_ARMOR)
                    .addTag(CoreTags.Items.MEDIUM_ARMOR)
                    .addTag(CoreTags.Items.HEAVY_ARMOR);
        }
    }
}

