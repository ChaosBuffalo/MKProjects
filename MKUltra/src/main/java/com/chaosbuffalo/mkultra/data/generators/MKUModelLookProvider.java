package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.SkeletonStyles;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUClothes;
import com.chaosbuffalo.mkultra.client.render.styling.MKUGolems;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUOrcs;
import com.chaosbuffalo.mkultra.client.render.styling.MKUPiglins;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUModelStyles;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MKUModelLookProvider extends MKDataProvider {
    public MKUModelLookProvider(DataGenerator generator) {
        super(generator, MKUltra.MODID, "MKUltra Model Looks");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        addHyboreanSkeletonLooks(futures, pOutput);
        addZombifiedPiglinLooks(futures, pOutput);
        addHumanLooks(futures, pOutput);
        addHumanGhostLooks(futures, pOutput);
        addOrcLooks(futures, pOutput);
        addGolemLooks(futures, pOutput);
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> writeLook(ResourceKey<ModelLook> key, ModelLook look, CachedOutput output) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        ResourceLocation keyLoc = key.location();
        Path local = Paths.get("data", keyLoc.getNamespace(), ModelLook.DEFINITION_FOLDER, keyLoc.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = ModelLook.CODEC.encodeStart(JsonOps.INSTANCE, look).getOrThrow();
        return DataProvider.saveStable(output, element, path);
    }

    private void addHyboreanSkeletonLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKUEntities.HYBOREAN_SKELETON_TYPE.get();
        futures.add(writeLook(MKUSkeletons.DEFAULT_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, true, SkeletonStyles.SKELETON_TEXTURES), output));
        futures.add(writeLook(MKUSkeletons.ANCIENT_KING_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.WITHER_SKELETON_TEXTURES, MKUClothes.FUR_LINED_SCRAPS), output));
        futures.add(writeLook(MKUSkeletons.SORCERER_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.SKELETON_TEXTURES, MKUClothes.IRON_PONCHO), output));
        futures.add(writeLook(MKUSkeletons.SORCERER_QUEEN_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.STRAY_SKELETON_TEXTURES, MKUClothes.FUR_LINED_SCRAPS_2), output));
        futures.add(writeLook(MKUSkeletons.HONOR_GUARD_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.SKELETON_TEXTURES, MKUClothes.HYBOREAN_ARMOR), output));
        futures.add(writeLook(MKUSkeletons.HYBOREAN_ARCHER_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.SKELETON_TEXTURES, MKUClothes.LOINCLOTH_2), output));
        futures.add(writeLook(MKUSkeletons.HYBOREAN_WARRIOR_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.SKELETON_TEXTURES, MKUClothes.LOINCLOTH), output));
        futures.add(writeLook(MKUSkeletons.BURNING_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.WITHER_SKELETON_TEXTURES, MKUClothes.IRON_PONCHO), output));
        futures.add(writeLook(MKUSkeletons.BASIC_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, false, SkeletonStyles.SKELETON_TEXTURES), output));
        futures.add(writeLook(MKUSkeletons.SEAWOVEN_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        SkeletonStyles.WITHER_SKELETON_TEXTURES, MKUClothes.SEAWOVEN_PONCHO), output));
        futures.add(writeLook(MKUSkeletons.SEAWOVEN_WRETCH_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, false, SkeletonStyles.STRAY_SKELETON_TEXTURES), output));
    }

    private void addZombifiedPiglinLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKUEntities.ZOMBIFIED_PIGLIN_TYPE.get();
        futures.add(writeLook(MKUPiglins.DEFAULT_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, true, MKUPiglins.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE), output));
        futures.add(writeLook(MKUPiglins.ZOMBIE_PIG_TROOPER_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.get(), entityType, false,
                        MKUPiglins.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE, MKUPiglins.IMPERIAL_TROOPER_ARMOR_DAMAGED), output));
        futures.add(writeLook(MKUPiglins.ZOMBIE_PIG_MAGUS_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.get(), entityType, false,
                        MKUPiglins.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE, MKUPiglins.IMPERIAL_MAGUS_ARMOR_DAMAGED), output));
        futures.add(writeLook(MKUPiglins.ZOMBIE_PIG_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, false, MKUPiglins.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE), output));
        futures.add(writeLook(MKUPiglins.SKELETAL_TROOPER_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ARMOR_STYLE.get(), entityType, false,
                        MKUPiglins.SKELETAL_ZOMBIFIED_PIGLIN_TEXTURE, MKUPiglins.IMPERIAL_TROOPER_ARMOR_NO_HELMET), output));
        futures.add(writeLook(MKUPiglins.SKELETAL_MAGE_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ARMOR_STYLE.get(), entityType, false,
                        MKUPiglins.SKELETAL_ZOMBIFIED_PIGLIN_TEXTURE, MKUPiglins.IMPERIAL_MAGUS_ARMOR_NO_HELMET), output));
        futures.add(writeLook(MKUPiglins.DESTROYED_SKELETAL_MAGE_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.get(), entityType, false,
                        MKUPiglins.SKELETAL_ZOMBIFIED_PIGLIN_TEXTURE, MKUPiglins.IMPERIAL_MAGUS_ARMOR_DAMAGED), output));
        futures.add(writeLook(MKUPiglins.DESTROYED_SKELETAL_TROOPER_LOOK,
                new ModelLook(ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.get(), entityType, false,
                        MKUPiglins.SKELETAL_ZOMBIFIED_PIGLIN_TEXTURE, MKUPiglins.IMPERIAL_TROOPER_ARMOR_DAMAGED), output));
    }

    private void addHumanLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKUEntities.HUMAN_TYPE.get();
        futures.add(writeLook(MKUHumans.DEFAULT_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, true, MKUHumans.HUMAN_SKIN_1), output));
        futures.add(writeLook(MKUHumans.CLERIC_1_LOOK,
                new ModelLook(MKUModelStyles.TWO_LAYER_CLOTHES_SHORT_HAIR.get(), entityType, false,
                        MKUHumans.HUMAN_SKIN_1, MKUHumans.HUMAN_HAIR_1, MKUClothes.SOLANG_ROBES_1, MKUClothes.SOLANG_ROBES_2), output));
        futures.add(writeLook(MKUHumans.CLERIC_2_LOOK,
                new ModelLook(MKUModelStyles.TWO_LAYER_CLOTHES_SHORT_HAIR.get(), entityType, false,
                        MKUHumans.HUMAN_SKIN_2, MKUHumans.HUMAN_HAIR_2, MKUClothes.SOLANG_ROBES_1, MKUClothes.SOLANG_ROBES_2), output));
        futures.add(writeLook(MKUHumans.GHOST_1_LOOK,
                new ModelLook(MKUModelStyles.GHOST_LONG_HAIR.get(), entityType, false,
                        MKUHumans.GHOST_SKIN_1, MKUHumans.GHOST_HAIR_1, MKUClothes.GHOST_LEATHERS_1, MKUHumans.GHOST_HAIR_2), output));
        futures.add(writeLook(MKUHumans.GHOST_CLEAN_LOOK,
                new ModelLook(MKUModelStyles.GHOST_LONG_HAIR_NO_CLOTHES.get(), entityType, false,
                        MKUHumans.GHOST_SKIN_1, MKUHumans.GHOST_HAIR_1, MKUHumans.GHOST_HAIR_2), output));
        futures.add(writeLook(MKUHumans.GHOST_CLEAN_SHORT_LOOK,
                new ModelLook(MKUModelStyles.GHOST_SHORT_HAIR_NO_CLOTHES.get(), entityType, false,
                        MKUHumans.GHOST_SKIN_1, MKUHumans.GHOST_HAIR_1), output));
        futures.add(writeLook(MKUHumans.NETHER_MAGE_1_LOOK,
                new ModelLook(ModelStyles.SHORT_HAIR_STYLE.get(), entityType, false,
                        MKUHumans.HUMAN_SKIN_1, MKUHumans.HUMAN_HAIR_3, MKUClothes.NETHER_MAGE_ROBES_1), output));
        futures.add(writeLook(MKUHumans.BANDIT_RAIDER_1_LOOK,
                new ModelLook(ModelStyles.SHORT_HAIR_STYLE.get(), entityType, false,
                        MKUHumans.HUMAN_SKIN_2, MKUHumans.HUMAN_HAIR_1, MKUClothes.BANDIT_LEATHERS_1), output));
        futures.add(writeLook(MKUHumans.TEMPLE_GUARD_1_LOOK,
                new ModelLook(MKUModelStyles.TWO_LAYER_ARMOR_SHORT_HAIR.get(), entityType, false,
                        MKUHumans.HUMAN_SKIN_1, MKUHumans.HUMAN_HAIR_2, MKUClothes.SOLANGIAN_ARMOR_2, MKUClothes.SOLANGIAN_ARMOR_1), output));
        futures.add(writeLook(MKUHumans.TEMPLE_GUARD_2_LOOK,
                new ModelLook(MKUModelStyles.TWO_LAYER_ARMOR_SHORT_HAIR.get(), entityType, false,
                        MKUHumans.HUMAN_SKIN_1, MKUHumans.HUMAN_HAIR_3, MKUClothes.SOLANGIAN_ARMOR_2, MKUClothes.SOLANGIAN_ARMOR_1), output));
        futures.add(writeLook(MKUHumans.NECROTIDE_CULTIST_1_LOOK,
                new ModelLook(MKUModelStyles.TWO_LAYER_ARMOR_NO_HAIR.get(), entityType, false,
                        MKUHumans.PALE_HUMAN_SKIN_1, MKUClothes.NECROTIDE_ROBES_2, MKUClothes.NECROTIDE_ROBES_1), output));
        futures.add(writeLook(MKUHumans.NECROTIDE_CULTIST_SKULL_1_LOOK,
                new ModelLook(MKUModelStyles.TWO_LAYER_ARMOR_NO_HAIR.get(), entityType, false,
                        MKUHumans.PALE_HUMAN_SKIN_1, MKUClothes.NECROTIDE_ROBES_2, MKUClothes.NECROTIDE_ROBES_SKULL_HOOD), output));
    }

    private void addHumanGhostLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKUEntities.HUMAN_GHOST_TYPE.get();
        futures.add(writeLook(MKUHumans.GHOST_DEFAULT_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, true, MKUHumans.HUMAN_SKIN_1), output));
        futures.add(writeLook(MKUHumans.GHOST_ENTITY_GHOST_1_LOOK,
                new ModelLook(MKUModelStyles.GHOST_LONG_HAIR.get(), entityType, false,
                        MKUHumans.GHOST_SKIN_1, MKUHumans.GHOST_HAIR_1, MKUClothes.GHOST_LEATHERS_1, MKUHumans.GHOST_HAIR_2), output));
        futures.add(writeLook(MKUHumans.GHOST_ENTITY_GHOST_CLEAN_LOOK,
                new ModelLook(MKUModelStyles.GHOST_LONG_HAIR_NO_CLOTHES.get(), entityType, false,
                        MKUHumans.GHOST_SKIN_1, MKUHumans.GHOST_HAIR_1, MKUHumans.GHOST_HAIR_2), output));
        futures.add(writeLook(MKUHumans.GHOST_ENTITY_GHOST_CLEAN_SHORT_LOOK,
                new ModelLook(MKUModelStyles.GHOST_SHORT_HAIR_NO_CLOTHES.get(), entityType, false,
                        MKUHumans.GHOST_SKIN_1, MKUHumans.GHOST_HAIR_1), output));
    }

    private void addOrcLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKUEntities.ORC_TYPE.get();
        futures.add(writeLook(MKUOrcs.DEFAULT_LOOK,
                new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, true, MKUOrcs.GREEN_ORC), output));
        futures.add(writeLook(MKUOrcs.GREEN_LADY_LOOK,
                new ModelLook(ModelStyles.LONG_HAIR_STYLE.get(), entityType, false,
                        MKUOrcs.GREEN_ORC, MKUOrcs.GREEN_LADY_HAIR_1, MKUClothes.GREEN_LADY_CLOTHES, MKUOrcs.GREEN_LADY_HAIR_2), output));
        futures.add(writeLook(MKUOrcs.GREEN_LADY_GUARD_1_LOOK,
                new ModelLook(ModelStyles.LONG_HAIR_STYLE.get(), entityType, false,
                        MKUOrcs.BLUE_ORC, MKUOrcs.ORC_HAIR_2, MKUClothes.LOINCLOTH, MKUOrcs.ORC_LONG_HAIR_3_LAYER_2), output));
        futures.add(writeLook(MKUOrcs.GREEN_LADY_GUARD_2_LOOK,
                new ModelLook(ModelStyles.LONG_HAIR_STYLE.get(), entityType, false,
                        MKUOrcs.GREEN_ORC, MKUOrcs.ORC_HAIR_1, MKUClothes.LOINCLOTH_2, MKUOrcs.ORC_LONG_HAIR_1_LAYER_2), output));
        futures.add(writeLook(MKUOrcs.GREEN_SMITH_LOOK,
                new ModelLook(ModelStyles.LONG_HAIR_STYLE.get(), entityType, false,
                        MKUOrcs.GREEN_ORC, MKUOrcs.ORC_HAIR_2, MKUClothes.LOINCLOTH, MKUOrcs.ORC_LONG_HAIR_2_LAYER_2), output));
    }

    private void addGolemLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKNpcEntityTypes.GOLEM_TYPE.get();
        futures.add(writeLook(MKUGolems.NECROTIDE_GOLEM_LOOK,
                new ModelLook(ModelStyles.BASIC_GOLEM_STYLE.get(), entityType, false, MKUGolems.NECROTIDE_GOLEM), output));
    }
}
