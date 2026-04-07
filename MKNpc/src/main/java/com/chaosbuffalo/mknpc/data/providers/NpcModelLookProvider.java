package com.chaosbuffalo.mknpc.data.providers;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.GolemStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.PiglinStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.SkeletonStyles;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NpcModelLookProvider extends MKDataProvider {
    public NpcModelLookProvider(DataGenerator generator) {
        super(generator, MKNpc.MODID, "NPC Model Looks");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        addSkeletonLooks(MKNpcEntityTypes.SKELETON_TYPE.get(), futures, pOutput);
        addSkeletonLooks(MKNpcEntityTypes.FLYING_SKELETON_TYPE.get(), futures, pOutput);
        addZombifiedPiglinLooks(futures, pOutput);
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

    private void addSkeletonLooks(EntityType<?> entityType, List<CompletableFuture<?>> futures, CachedOutput output) {
        Map<String, ResourceLocation> textureVariants = new HashMap<>();
        textureVariants.put("wither", SkeletonStyles.WITHER_SKELETON_TEXTURES);
        textureVariants.put("stray", SkeletonStyles.STRAY_SKELETON_TEXTURES);
        textureVariants.put("default", SkeletonStyles.SKELETON_TEXTURES);

        Map<String, ResourceLocation> clothingVariants = new HashMap<>();
        clothingVariants.put("stray", SkeletonStyles.STRAY_CLOTHES_TEXTURES);

        for (Map.Entry<String, ResourceLocation> textureVariant : textureVariants.entrySet()) {
            for (Map.Entry<String, ResourceLocation> clothingVariant : clothingVariants.entrySet()) {
                String lookName = "%s_%s".formatted(textureVariant.getKey(), clothingVariant.getKey());
                ModelLook look = new ModelLook(ModelStyles.CLOTHES_ONLY_STYLE.get(), entityType, false,
                        textureVariant.getValue(), clothingVariant.getValue());
                futures.add(writeLook(SkeletonStyles.lookKey(entityType, lookName), look, output));
            }
            String lookName = textureVariant.getKey();
            ModelLook look = new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, "default".equals(lookName),
                    textureVariant.getValue());
            futures.add(writeLook(SkeletonStyles.lookKey(entityType, lookName), look, output));
        }
    }

    private void addZombifiedPiglinLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.get();
        ModelLook look = new ModelLook(ModelStyles.BASIC_STYLE.get(), entityType, true,
                PiglinStyles.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE);
        futures.add(writeLook(PiglinStyles.DEFAULT_LOOK, look, output));
    }

    private void addGolemLooks(List<CompletableFuture<?>> futures, CachedOutput output) {
        EntityType<?> entityType = MKNpcEntityTypes.GOLEM_TYPE.get();
        ModelLook look = new ModelLook(ModelStyles.BASIC_GOLEM_STYLE.get(), entityType, true,
                GolemStyles.VANILLA_IRON_GOLEM_TEXTURE);
        futures.add(writeLook(GolemStyles.DEFAULT_LOOK, look, output));
    }
}
