package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorMaskPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExportedWorkspacePoolBootstrap {
    private ExportedWorkspacePoolBootstrap() {
    }

    public static void bootstrapExportedPoolsForNamespace(BootstrapContext<StructureTemplatePool> context,
                                                          Holder<StructureTemplatePool> empty,
                                                          Path moduleRoot,
                                                          String namespace) {
        MKWorkspaceExportManifestLoader.loadAllFromModSource(moduleRoot, namespace).stream()
                .map(MKWorkspaceExportManifestLoader.LoadedManifest::manifest)
                .filter(manifest -> manifest.namespace().equals(namespace))
                .forEach(manifest -> registerExportedPools(context, empty, manifest));
    }

    private static void registerExportedPools(BootstrapContext<StructureTemplatePool> context,
                                              Holder<StructureTemplatePool> empty,
                                              MKWorkspaceExportManifest manifest) {
        List<String> validationErrors = manifest.validateRuntimeStructureExport();
        if (!validationErrors.isEmpty()) {
            throw new IllegalStateException("Workspace export " + manifest.namespace() + ":" +
                    manifest.structureName() + " cannot be registered as a runtime structure: " +
                    String.join("; ", validationErrors));
        }

        Map<String, List<MKWorkspaceExportManifest.ExportPiece>> piecesByBaseName = manifest.pieces().stream()
                .filter(piece -> !"template".equals(piece.workspacePieceKind()))
                .collect(Collectors.groupingBy(MKWorkspaceExportManifest.ExportPiece::baseName));
        Map<String, MKWorkspaceExportManifest.ExportPiece> piecesByName = manifest.pieces().stream()
                .collect(Collectors.toMap(MKWorkspaceExportManifest.ExportPiece::pieceName, piece -> piece));

        register(context,
                ResourceLocation.fromNamespaceAndPath(manifest.namespace(), manifest.structureName() + "/start"),
                rigidPool(empty, manifest.runtimeHints().startEntries().isEmpty() ?
                        exportedVariants(manifest,
                                piecesByBaseName.getOrDefault(manifest.runtimeHints().startBaseName(), List.of())) :
                        exportedEntries(manifest, manifest.runtimeHints().startEntries(), piecesByName)));

        for (MKWorkspaceExportManifest.ExportRuntimePool pool : manifest.runtimeHints().pools()) {
            List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> children =
                    pool.entries().isEmpty() ? pool.childBaseNames().stream()
                            .flatMap(childBaseName -> exportedVariants(manifest,
                                    piecesByBaseName.getOrDefault(childBaseName, List.of())).stream())
                            .collect(Collectors.toList()) :
                            exportedEntries(manifest, pool.entries(), piecesByName);
            register(context, pool.poolId(), rigidPool(empty, children));
        }
    }

    private static void register(BootstrapContext<StructureTemplatePool> context,
                                 ResourceLocation poolId,
                                 StructureTemplatePool pool) {
        context.register(ResourceKey.create(net.minecraft.core.registries.Registries.TEMPLATE_POOL, poolId), pool);
    }

    private static StructureTemplatePool rigidPool(Holder<StructureTemplatePool> fallback,
                                                   List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> entries) {
        ImmutableList.Builder<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> builder = ImmutableList.builder();
        builder.addAll(entries);
        if (entries.isEmpty()) {
            builder.add(Pair.of(StructurePoolElement.empty(), 1));
        }
        return new StructureTemplatePool(fallback, builder.build(), StructureTemplatePool.Projection.RIGID);
    }

    private static List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> exportedVariants(
            MKWorkspaceExportManifest manifest,
            List<MKWorkspaceExportManifest.ExportPiece> pieces) {
        ImmutableList.Builder<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> builder = ImmutableList.builder();
        for (MKWorkspaceExportManifest.ExportPiece piece : pieces) {
            builder.add(Pair.of(MKSinglePoolElement.forTemplate(ResourceLocation.parse(piece.structureId()), false),
                    templateWeight(piece)));
        }
        return builder.build();
    }

    private static List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> exportedEntries(
            MKWorkspaceExportManifest manifest,
            List<MKWorkspaceExportManifest.ExportRuntimePoolEntry> entries,
            Map<String, MKWorkspaceExportManifest.ExportPiece> piecesByName) {
        ImmutableList.Builder<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> builder =
                ImmutableList.builder();
        for (MKWorkspaceExportManifest.ExportRuntimePoolEntry entry : entries) {
            MKWorkspaceExportManifest.ExportPiece piece = piecesByName.get(entry.pieceName());
            if (piece == null) {
                throw new IllegalStateException("Workspace export " + manifest.namespace() + ":" +
                        manifest.structureName() + " pool references missing piece " + entry.pieceName());
            }
            builder.add(Pair.of(MKSinglePoolElement.forTemplate(ResourceLocation.parse(piece.structureId()), false),
                    entry.weight()));
        }
        return builder.build();
    }

    private static int templateWeight(MKWorkspaceExportManifest.ExportPiece piece) {
        String weight = piece.tags().get(MKFloorMaskPools.FLOOR_MASK_WEIGHT_TAG);
        if (weight == null || weight.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(weight));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
