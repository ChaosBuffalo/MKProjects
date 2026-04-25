package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class MKWorkspaceImportManifestDiscovery {
    public record ImportCandidate(ResourceLocation id, String familyType, int pieceCount, int categoryCount, Path path) {
    }

    public List<ImportCandidate> discoverCandidates() {
        return MKWorkspaceExportManifestLoader.loadAll().stream()
                .map(this::toCandidate)
                .sorted((left, right) -> left.id().toString().compareToIgnoreCase(right.id().toString()))
                .toList();
    }

    public Optional<MKWorkspaceExportManifest> loadManifest(ResourceLocation id) {
        return MKWorkspaceExportManifestLoader.loadAll().stream()
                .map(MKWorkspaceExportManifestLoader.LoadedManifest::manifest)
                .filter(manifest -> matches(id, manifest))
                .findFirst();
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

    private boolean matches(ResourceLocation id, MKWorkspaceExportManifest manifest) {
        return manifest.namespace().equals(id.getNamespace()) && manifest.structureName().equals(id.getPath());
    }
}
