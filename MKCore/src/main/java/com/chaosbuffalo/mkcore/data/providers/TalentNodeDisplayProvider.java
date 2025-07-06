package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TalentNodeDisplayProvider extends DatapackBuiltinEntriesProvider {
    public TalentNodeDisplayProvider(PackOutput output, CompletableFuture<RegistrySetBuilder.PatchedRegistries> registries, Set<String> modIds) {
        super(output, registries, modIds);
    }


    public static TalentNodeDisplay createDefault(BootstrapContext<TalentNodeDisplay> context, ResourceKey<TalentNodeDisplay> key) {
        var talentId = key.location();
        var filledIcon = talentId.withPath(path -> "textures/talents/" + path + "_icon_filled.png");
        var icon = talentId.withPath(path -> "textures/talents/" + path + "_icon.png");
        var display = new TalentNodeDisplay(icon, filledIcon);
        context.register(key, display);
        return display;
    }
}
