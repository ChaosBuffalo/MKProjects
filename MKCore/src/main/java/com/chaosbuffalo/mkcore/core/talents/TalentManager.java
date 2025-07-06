package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class TalentManager {
    private static Collection<ResourceKey<TalentTreeDefinition>> defaultTrees;

    @Nullable
    public static TalentTreeDefinition getTalentTree(RegistryAccess registryAccess, ResourceLocation treeId) {
        return getTalentTree(registryAccess, ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, treeId));
    }

    @Nullable
    public static TalentTreeDefinition getTalentTree(RegistryAccess registryAccess, ResourceKey<TalentTreeDefinition> treeId) {
        return registryAccess.registryOrThrow(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY).get(treeId);
    }

    public static Collection<ResourceKey<TalentTreeDefinition>> getDefaultTrees(RegistryAccess registryAccess) {
        if (defaultTrees == null) {
            defaultTrees = registryAccess.registryOrThrow(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY).entrySet().stream()
                    .filter(e -> e.getValue().isDefault())
                    .map(Map.Entry::getKey)
                    .toList();
        }
        return defaultTrees;
    }
}
