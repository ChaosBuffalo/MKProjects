package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MKWorkspaceImportManifestDiscovery {
    private final Path moduleRoot;
    private final String namespace;

    public record ImportCandidate(ResourceLocation id, String familyType, int pieceCount, int categoryCount, Path path) {
    }

    public MKWorkspaceImportManifestDiscovery(Path moduleRoot, String namespace) {
        this.moduleRoot = moduleRoot;
        this.namespace = namespace;
    }

    public List<ImportCandidate> discoverCandidates() {
        return MKWorkspaceExportManifestLoader.loadAllFromModSource(moduleRoot, namespace).stream()
                .map(this::toCandidate)
                .sorted((left, right) -> left.id().toString().compareToIgnoreCase(right.id().toString()))
                .toList();
    }

    public Optional<MKWorkspaceExportManifest> loadManifest(ResourceLocation id) {
        return MKWorkspaceExportManifestLoader.loadFromModSource(moduleRoot, id)
                .map(MKWorkspaceExportManifestLoader.LoadedManifest::manifest);
    }

    private ImportCandidate toCandidate(MKWorkspaceExportManifestLoader.LoadedManifest loaded) {
        MKWorkspaceExportManifest manifest = loaded.manifest();
        return new ImportCandidate(
                ResourceLocation.fromNamespaceAndPath(manifest.namespace(), manifest.structureName()),
                manifest.familyType().getSerializedName(),
                manifest.pieces().size(),
                manifest.categories().size(),
                loaded.path()
        );
    }
}
