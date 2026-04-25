package com.chaosbuffalo.mknpc.data.registries;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NpcStructurePools {
    public static final ResourceKey<StructureTemplatePool> DIGGER_CAMP_POOL = createKey("digger/diggercamp");
    public static final ResourceKey<StructureTemplatePool> DIGGER_BASE_POOL = createKey("digger/diggerbase");
    public static final ResourceKey<StructureTemplatePool> DIGGER_ROAD_POOL = createKey("digger/diggerroad");

    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_START_POOL = createKey("test_tower/start");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_ENTRY_POOL = createKey("test_tower/entry");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_ENTRY_STAIRS_UP_POOL = createKey("test_tower/entry_stairs_up");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_FLOOR_MAIN_POOL = createKey("test_tower/floor_main");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_STAIRS_UP_POOL = createKey("test_tower/stairs_up");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_STAIRS_DOWN_POOL = createKey("test_tower/stairs_down");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_BASEMENT_ENTRY_POOL = createKey("test_tower/basement_entry");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_BASEMENT_MAIN_POOL = createKey("test_tower/basement_main");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_BASEMENT_CAP_POOL = createKey("test_tower/basement_cap");
    public static final ResourceKey<StructureTemplatePool> TEST_TOWER_BOSS_CAP_POOL = createKey("test_tower/boss_cap");

    private static final ResourceLocation DIGGER_TENT_DBL_1 = MKNpc.id("digger/diggertentdbl1");
    private static final ResourceLocation DIGGER_TENT_SGL_1 = MKNpc.id("digger/diggertentsgl1");
    private static final ResourceLocation DIGGER_BIG_1 = MKNpc.id("digger/diggerbig1");
    private static final ResourceLocation DIGGER_ROAD_1 = MKNpc.id("digger/diggerrd1");

    public static ResourceKey<StructureTemplatePool> createKey(String pName) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, MKNpc.id(pName));
    }

    public static void register(BootstrapContext<StructureTemplatePool> pContext, String pName, StructureTemplatePool pValue) {
        pContext.register(createKey(pName), pValue);
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> pContext) {
        HolderGetter<StructureTemplatePool> holderGetter = pContext.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = holderGetter.getOrThrow(Pools.EMPTY);
        pContext.register(DIGGER_CAMP_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(DIGGER_TENT_DBL_1, false), 1),
                        Pair.of(MKSinglePoolElement.forTemplate(DIGGER_TENT_SGL_1, false), 1),
                        Pair.of(StructurePoolElement.empty(), 2)
                ),
                StructureTemplatePool.Projection.RIGID));

        pContext.register(DIGGER_BASE_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(DIGGER_BIG_1, false), 1)
                ),
                StructureTemplatePool.Projection.RIGID));

        pContext.register(DIGGER_ROAD_POOL, new StructureTemplatePool(empty,
                ImmutableList.of(
                        Pair.of(MKSinglePoolElement.forTemplate(DIGGER_ROAD_1, false), 1)
                ),
                StructureTemplatePool.Projection.TERRAIN_MATCHING));

        bootstrapExportedTowerPools(pContext, empty);
    }

    private static void bootstrapExportedTowerPools(BootstrapContext<StructureTemplatePool> context, Holder<StructureTemplatePool> empty) {
        MKWorkspaceExportManifestLoader.loadAll().stream()
                .map(MKWorkspaceExportManifestLoader.LoadedManifest::manifest)
                .filter(manifest -> manifest.familyType().getSerializedName().equals("tower"))
                .filter(manifest -> manifest.namespace().equals(MKNpc.MODID))
                .forEach(manifest -> registerExportedTowerPools(context, empty, manifest));
    }

    private static void registerExportedTowerPools(BootstrapContext<StructureTemplatePool> context,
                                                   Holder<StructureTemplatePool> empty,
                                                   MKWorkspaceExportManifest manifest) {
        Map<String, List<MKWorkspaceExportManifest.ExportPiece>> piecesByBaseName = manifest.pieces().stream()
                .filter(piece -> !"template".equals(piece.workspacePieceKind()))
                .collect(Collectors.groupingBy(MKWorkspaceExportManifest.ExportPiece::baseName));

        register(context, manifest.structureName() + "/start", rigidPool(empty,
                exportedVariants(manifest, piecesByBaseName.getOrDefault(manifest.runtimeHints().startBaseName(), List.of()))));

        Map<String, MKWorkspaceExportManifest.ExportRuntimePool> runtimePools = new LinkedHashMap<>();
        for (MKWorkspaceExportManifest.ExportRuntimeCategory category : manifest.runtimeHints().categories()) {
            runtimePools.put(category.baseName(), new MKWorkspaceExportManifest.ExportRuntimePool(
                    category.baseName(),
                    category.poolId(),
                    category.childBaseNames()
            ));
        }
        for (MKWorkspaceExportManifest.ExportRuntimePool pool : manifest.runtimeHints().pools()) {
            runtimePools.put(pool.baseName(), pool);
        }

        for (MKWorkspaceExportManifest.ExportRuntimePool pool : runtimePools.values()) {
            List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> children = pool.childBaseNames().stream()
                    .flatMap(childBaseName -> exportedVariants(manifest, piecesByBaseName.getOrDefault(childBaseName, List.of())).stream())
                    .toList();
            register(context, manifest.structureName() + "/" + pool.baseName(), rigidPool(empty, children));
        }
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
