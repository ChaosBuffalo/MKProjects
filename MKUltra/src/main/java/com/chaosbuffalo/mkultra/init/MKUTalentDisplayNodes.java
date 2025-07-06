package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.data.providers.TalentNodeDisplayProvider;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MKUTalentDisplayNodes extends TalentNodeDisplayProvider {
    public MKUTalentDisplayNodes(PackOutput output, CompletableFuture<RegistrySetBuilder.PatchedRegistries> registries) {
        super(output, registries, Set.of(MKUltra.MODID));
    }

    private static ResourceKey<TalentNodeDisplay> key(String name) {
        return ResourceKey.create(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY, MKUltra.id(name));
    }

    public static final ResourceKey<TalentNodeDisplay> GREEN_SOUL = key("green_soul");
    public static final ResourceKey<TalentNodeDisplay> SOUL_DRAIN = key("soul_drain");
    public static final ResourceKey<TalentNodeDisplay> LIFE_SIPHON = key("life_siphon");

    public static void bootstrap(BootstrapContext<TalentNodeDisplay> context) {
        createDefault(context, GREEN_SOUL);
        createDefault(context, SOUL_DRAIN);
        createDefault(context, LIFE_SIPHON);
    }
}
