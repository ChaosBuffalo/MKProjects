package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            MKBlockTagsProvider blockTagsProvider = new MKBlockTagsProvider(generator, MKCore.MOD_ID, event.getExistingFileHelper());
            generator.addProvider(blockTagsProvider);
            generator.addProvider(new AbilityDataGenerator(generator, MKCore.MOD_ID));
            generator.addProvider(new ArmorClassItemTagProvider(generator, blockTagsProvider, event.getExistingFileHelper()));
            generator.addProvider(new CoreTestTreeGenerator(generator));
            generator.addProvider(new CoreParticleAnimGenerator(generator));
        }
    }

    public abstract static class ParticleAnimationDataGenerator implements DataProvider {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        private final DataGenerator generator;

        public ParticleAnimationDataGenerator(DataGenerator generator) {
            this.generator = generator;
        }

        public void writeDefinition(ResourceLocation name, ParticleAnimation animation, @Nonnull HashCache cache) {
            Path outputFolder = this.generator.getOutputFolder();
            Path local = Paths.get("data", name.getNamespace(), ParticleAnimationManager.DEFINITION_FOLDER, name.getPath() + ".json");
            Path path = outputFolder.resolve(local);
            try {
                JsonElement element = animation.serialize(JsonOps.INSTANCE);
                DataProvider.save(GSON, cache, element, path);
            } catch (IOException e) {
                MKCore.LOGGER.error("Couldn't write particle animation {}", path, e);
            }
        }
    }

    public abstract static class TalentTreeDataGenerator implements DataProvider {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        private final DataGenerator generator;

        public TalentTreeDataGenerator(DataGenerator generator) {
            this.generator = generator;
        }

        public void writeDefinition(TalentTreeDefinition definition, @Nonnull HashCache cache) {
            Path outputFolder = this.generator.getOutputFolder();
            ResourceLocation key = definition.getTreeId();
            Path local = Paths.get("data", key.getNamespace(), TalentManager.DEFINITION_FOLDER, key.getPath() + ".json");
            Path path = outputFolder.resolve(local);
            try {
                JsonElement element = definition.serialize(JsonOps.INSTANCE);
                DataProvider.save(GSON, cache, element, path);
            } catch (IOException e) {
                MKCore.LOGGER.error("Couldn't write talent tree definition {}", path, e);
            }
        }
    }

    public static class CoreParticleAnimGenerator extends ParticleAnimationDataGenerator {

        public CoreParticleAnimGenerator(DataGenerator generator) {
            super(generator);
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            writeBlueMagicTest(cache);
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Particle Animations";
        }

        public void writeBlueMagicTest(@Nonnull HashCache cache) {
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
            writeDefinition(MKCore.makeRL("particle_anim.blue_magic"), anim, cache);
        }
    }

    public static class CoreTestTreeGenerator extends TalentTreeDataGenerator {

        public CoreTestTreeGenerator(DataGenerator generator) {
            super(generator);
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            TalentTreeDefinition test = new TalentTreeDefinition(MKCore.makeRL("knight"));
            test.setVersion(1);
            TalentLineDefinition line = new TalentLineDefinition(test, "knight_1");
            line.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 1, 1.0));
            test.addLine(line);
            writeDefinition(test, cache);

        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Talent Trees";
        }
    }

    public static class AbilityDataGenerator implements DataProvider {
        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        private final DataGenerator generator;
        private final String modId;

        public AbilityDataGenerator(DataGenerator generator, String modId) {
            this.generator = generator;
            this.modId = modId;
        }

        @Override
        public void run(@Nonnull HashCache cache) {
            Path outputFolder = this.generator.getOutputFolder();
            MKCoreRegistry.ABILITIES.forEach(ability -> {
                ResourceLocation key = ability.getAbilityId();
                if (!key.getNamespace().equals(modId)) {
                    MKCore.LOGGER.info("Skipping ability {} not from this mod", key);
                    return;
                }
                MKCore.LOGGER.info("Dumping ability {}", key);
                if (!key.getPath().startsWith("ability.")) {
                    MKCore.LOGGER.warn("Skipping {} because it did not have the 'ability.' prefix", key);
                    return;
                }
                String name = key.getPath().substring(8); // skip ability.
                Path local = Paths.get("data", key.getNamespace(), AbilityManager.DEFINITION_FOLDER, name + ".json");
                Path path = outputFolder.resolve(local);
                try {
                    JsonElement element = ability.serializeDynamic(JsonOps.INSTANCE);
                    DataProvider.save(GSON, cache, element, path);
                } catch (IOException e) {
                    MKCore.LOGGER.error("Couldn't write ability {}", path, e);
                }
            });
        }

        @Nonnull
        @Override
        public String getName() {
            return "MKCore Abilities";
        }
    }

    static class MKBlockTagsProvider extends BlockTagsProvider {

        public MKBlockTagsProvider(DataGenerator generatorIn, String modId,
                                   @Nullable ExistingFileHelper existingFileHelper) {
            super(generatorIn, modId, existingFileHelper);
        }

        @Override
        protected void addTags() {
        }
    }

    public static class ArmorClassItemTagProvider extends ItemTagsProvider {
        public ArmorClassItemTagProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider,
                                         ExistingFileHelper existingFileHelper) {
            super(dataGenerator, blockTagProvider, MKCore.MOD_ID, existingFileHelper);
        }

        @Override
        protected void addTags() {
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

        @Nonnull
        @Override
        public String getName() {
            return "MKCore armor class item tags";
        }
    }
}
