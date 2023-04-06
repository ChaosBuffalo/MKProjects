package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentLineDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.BrownianMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.OrbitingInPlaneMotionTrack;
import com.chaosbuffalo.mkcore.init.CoreTags;
import com.chaosbuffalo.mkcore.init.CoreTalents;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.*;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            MKBlockTagsProvider blockTagsProvider = new MKBlockTagsProvider(generator.getPackOutput(),
                    event.getLookupProvider(), MKCore.MOD_ID, event.getExistingFileHelper());
            generator.addProvider(true, blockTagsProvider);
            generator.addProvider(true, new AbilityDataGenerator(generator, MKCore.MOD_ID));
            generator.addProvider(true, new ArmorClassItemTagProvider(generator,
                    event.getLookupProvider(), blockTagsProvider, event.getExistingFileHelper()));
            generator.addProvider(true, new CoreTestTreeGenerator(generator));
            generator.addProvider(true, new CoreParticleAnimGenerator(generator));
        }
    }

    public abstract static class ParticleAnimationDataGenerator implements DataProvider {
        private final DataGenerator generator;

        public ParticleAnimationDataGenerator(DataGenerator generator) {
            this.generator = generator;
        }

        public CompletableFuture<?> writeDefinition(ResourceLocation name, ParticleAnimation animation, CachedOutput pOutput) {
            Path outputFolder = this.generator.getPackOutput().getOutputFolder();
            Path local = Paths.get("data", name.getNamespace(),
                    ParticleAnimationManager.DEFINITION_FOLDER, name.getPath() + ".json");
            Path path = outputFolder.resolve(local);
            JsonElement element = animation.serialize(JsonOps.INSTANCE);
            return DataProvider.saveStable(pOutput, element, path);
        }
    }

    public abstract static class TalentTreeDataGenerator implements DataProvider {
        private final DataGenerator generator;

        public TalentTreeDataGenerator(DataGenerator generator) {
            this.generator = generator;
        }

        public CompletableFuture<?> writeDefinition(TalentTreeDefinition definition, CachedOutput pOutput) {
            Path outputFolder = this.generator.getPackOutput().getOutputFolder();
            ResourceLocation key = definition.getTreeId();
            Path local = Paths.get("data", key.getNamespace(), TalentManager.DEFINITION_FOLDER, key.getPath() + ".json");
            Path path = outputFolder.resolve(local);
            JsonElement element = definition.serialize(JsonOps.INSTANCE);
            return DataProvider.saveStable(pOutput, element, path);
        }
    }

    public static class CoreParticleAnimGenerator extends ParticleAnimationDataGenerator {

        public CoreParticleAnimGenerator(DataGenerator generator) {
            super(generator);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return writeBlueMagicTest(pOutput);
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Particle Animations";
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
            return writeDefinition(MKCore.makeRL("particle_anim.blue_magic"), anim, pOutput);
        }
    }

    public static class CoreTestTreeGenerator extends TalentTreeDataGenerator {

        public CoreTestTreeGenerator(DataGenerator generator) {
            super(generator);
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

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Talent Trees";
        }
    }

    public static class AbilityDataGenerator implements DataProvider {
        private final DataGenerator generator;
        private final String modId;

        public AbilityDataGenerator(DataGenerator generator, String modId) {
            this.generator = generator;
            this.modId = modId;
        }


        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            Path outputFolder = this.generator.getPackOutput().getOutputFolder();
            return CompletableFuture.allOf(
                    MKCoreRegistry.ABILITIES.getEntries().stream()
                            .filter(entry -> entry.getKey().location().getNamespace().equals(modId))
                            .map(entry -> {
                                ResourceLocation key = entry.getKey().location();
                                MKAbility ability = entry.getValue();
                                String name = key.getPath().substring(8); // skip ability.
                                Path local = Paths.get("data", key.getNamespace(), AbilityManager.DEFINITION_FOLDER, name + ".json");
                                Path path = outputFolder.resolve(local);
                                JsonElement element = ability.serializeDynamic(JsonOps.INSTANCE);
                                return DataProvider.saveStable(pOutput, element, path);
                            }).collect(Collectors.toList()).toArray(CompletableFuture[]::new));
        }

        @Nonnull
        @Override
        public String getName() {
            return String.format("MK Abilities : %s", modId);
        }
    }

    static class MKBlockTagsProvider extends BlockTagsProvider {


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

