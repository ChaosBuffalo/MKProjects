package com.chaosbuffalo.mknpc.client.render.models.styling.looks;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModelLookManager {
    private static RegistryAccess cachedRegistryAccess;
    private static int cachedLookCount = -1;
    private static final Map<ResourceLocation, Map<ResourceLocation, ModelLook>> LOOKS = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> DEFAULT_LOOKS = new HashMap<>();

    private static void rebuildCache(RegistryAccess registryAccess) {
        LOOKS.clear();
        DEFAULT_LOOKS.clear();
        Registry<ModelLook> lookRegistry = registryAccess.registryOrThrow(NpcRegistries.MODEL_LOOKS);
        lookRegistry.stream().forEach(look -> {
            ResourceLocation lookId = lookRegistry.getKey(look);
            ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(look.getEntityType());
            if (lookId == null || entityTypeId == null) {
                return;
            }
            try {
                look.getBaseStyle();
            } catch (IllegalStateException e) {
                MKNpc.LOGGER.error("Failed to resolve model style for look {}", lookId, e);
                return;
            }
            LOOKS.computeIfAbsent(entityTypeId, ignored -> new HashMap<>()).put(lookId, look);
            if (look.isDefaultLook()) {
                ResourceLocation previous = DEFAULT_LOOKS.put(entityTypeId, lookId);
                if (previous != null && !previous.equals(lookId)) {
                    MKNpc.LOGGER.error("Multiple default looks for {}: {} and {}", entityTypeId, previous, lookId);
                }
            }
        });
    }

    @Nullable
    public static ModelLook getLook(RegistryAccess registryAccess, EntityType<?> entityType, ResourceLocation lookId) {
        Registry<ModelLook> lookRegistry = registryAccess.registryOrThrow(NpcRegistries.MODEL_LOOKS);
        int lookCount = lookRegistry.size();
        if (cachedRegistryAccess != registryAccess || cachedLookCount != lookCount) {
            cachedRegistryAccess = registryAccess;
            cachedLookCount = lookCount;
            rebuildCache(registryAccess);
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        Map<ResourceLocation, ModelLook> looksForEntity = LOOKS.get(entityId);
        if (looksForEntity == null) {
            return null;
        }
        ModelLook resolved = looksForEntity.get(lookId);
        if (resolved != null) {
            return resolved;
        }
        ResourceLocation defaultLookId = DEFAULT_LOOKS.get(entityId);
        return defaultLookId != null ? looksForEntity.get(defaultLookId) : null;
    }
}
