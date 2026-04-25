package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
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

    public static void bootstrapTowerPoolsForNamespace(BootstrapContext<StructureTemplatePool> context,
                                                       Holder<StructureTemplatePool> empty,
                                                       Path moduleRoot,
                                                       String namespace) {
        MKWorkspaceExportManifestLoader.loadAllFromModSource(moduleRoot, namespace).stream()
                .map(MKWorkspaceExportManifestLoader.LoadedManifest::manifest)
                .filter(manifest -> manifest.familyType().getSerializedName().equals("tower"))
                .filter(manifest -> manifest.namespace().equals(namespace))
                .forEach(manifest -> registerExportedTowerPools(context, empty, manifest));
    }

    private static void registerExportedTowerPools(BootstrapContext<StructureTemplatePool> context,
                                                   Holder<StructureTemplatePool> empty,
                                                   MKWorkspaceExportManifest manifest) {
        Map<String, List<MKWorkspaceExportManifest.ExportPiece>> piecesByBaseName = manifest.pieces().stream()
                .filter(piece -> !"template".equals(piece.workspacePieceKind()))
                .collect(Collectors.groupingBy(MKWorkspaceExportManifest.ExportPiece::baseName));

        register(context,
                ResourceLocation.fromNamespaceAndPath(manifest.namespace(), manifest.structureName() + "/start"),
                rigidPool(empty, exportedVariants(manifest,
                        piecesByBaseName.getOrDefault(manifest.runtimeHints().startBaseName(), List.of()))));

        for (MKWorkspaceExportManifest.ExportRuntimePool pool : manifest.runtimeHints().pools()) {
            List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> children = pool.childBaseNames().stream()
                    .flatMap(childBaseName -> exportedVariants(manifest, piecesByBaseName.getOrDefault(childBaseName, List.of())).stream())
                    .collect(Collectors.toList());
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
            builder.add(Pair.of(MKSinglePoolElement.forTemplate(ResourceLocation.parse(piece.structureId()), false), 1));
        }
        return builder.build();
    }
}
