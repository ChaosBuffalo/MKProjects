package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Runtime vocabulary for user-declared insert slots.
 *
 * <p>The pool path and legacy family tag remain stable for exported datapack compatibility. New workspace data
 * also carries {@link #TAG_INSERT_SLOT_ID}, which is the authoritative identity of the topology slot.</p>
 */
public final class MKInsertSlotPools {
    public static final String TAG_INSERT_SLOT_ID = "workspace_insert_slot_id";
    public static final String LEGACY_TAG_INSERT_FAMILY_ID = "workspace_insert_family_id";
    public static final String TAG_INSERT_SLOT_KIND = "workspace_insert_family_kind";
    public static final String INSERT_SLOT_POOL_SEGMENT = "insert_families";

    private MKInsertSlotPools() {
    }

    public static String slotId(Map<String, String> tags) {
        String explicit = tags.get(TAG_INSERT_SLOT_ID);
        return explicit == null || explicit.isBlank() ?
                tags.getOrDefault(LEGACY_TAG_INSERT_FAMILY_ID, "") : explicit;
    }

    public static ResourceLocation poolId(String namespace, String structureName, String slotId) {
        return ResourceLocation.fromNamespaceAndPath(namespace,
                structureName + "/" + INSERT_SLOT_POOL_SEGMENT + "/" + slotId);
    }
}
