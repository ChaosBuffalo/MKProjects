package com.chaosbuffalo.mkcore.data;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public abstract class ParticleAnimationProvider extends MKDataProvider {
    public ParticleAnimationProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Particle Animation");
    }

    public CompletableFuture<?> writeAnimation(ResourceLocation name, ParticleAnimation animation, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        Path local = Paths.get("data", name.getNamespace(),
                ParticleAnimationManager.DEFINITION_FOLDER, name.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = animation.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(pOutput, element, path);
    }
}

