package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceMetadata;

import com.chaosbuffalo.mknpc.MKNpc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = MKNpc.MODID)
public class MKJigsawPieceMetadataManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "mk_jigsaw_piece_meta";
    private static final MKJigsawPieceMetadataManager INSTANCE = new MKJigsawPieceMetadataManager();
    private static final Map<ResourceLocation, MKJigsawPieceMetadata> METADATA = new ConcurrentHashMap<>();

    private MKJigsawPieceMetadataManager() {
        super(GSON, DIRECTORY);
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static Optional<MKJigsawPieceMetadata> get(ResourceLocation templateId) {
        return Optional.ofNullable(METADATA.get(templateId));
    }

    public static MKJigsawPieceMetadata getRequired(ResourceLocation templateId) {
        return get(templateId).orElseThrow(() -> new IllegalStateException("Missing mk jigsaw metadata for template " + templateId));
    }

    public static MetadataOverrideSnapshot installTemporaryPreviewOverrides(
            Map<ResourceLocation, MKJigsawPieceMetadata> overrides) {
        HashMap<ResourceLocation, MKJigsawPieceMetadata> previous = new HashMap<>();
        HashSet<ResourceLocation> absent = new HashSet<>();
        overrides.forEach((templateId, metadata) -> {
            MKJigsawPieceMetadata existing = METADATA.put(templateId, metadata);
            if (existing == null) {
                absent.add(templateId);
            } else {
                previous.put(templateId, existing);
            }
        });
        return new MetadataOverrideSnapshot(Map.copyOf(previous), Set.copyOf(absent));
    }

    public static void restoreTemporaryPreviewOverrides(MetadataOverrideSnapshot snapshot) {
        snapshot.absent().forEach(METADATA::remove);
        snapshot.previous().forEach(METADATA::put);
    }

    public record MetadataOverrideSnapshot(Map<ResourceLocation, MKJigsawPieceMetadata> previous,
                                           Set<ResourceLocation> absent) {
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        METADATA.clear();
        object.forEach((id, json) -> MKJigsawPieceMetadata.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> MKNpc.LOGGER.error("Failed to parse mk jigsaw metadata {}: {}", id, error))
                .ifPresent(metadata -> METADATA.put(id, metadata)));
        if (MKNpc.DEV_LOGGING) {
            MKNpc.LOGGER.debug("Loaded {} mk jigsaw metadata entries", METADATA.size());
        }
    }
}
