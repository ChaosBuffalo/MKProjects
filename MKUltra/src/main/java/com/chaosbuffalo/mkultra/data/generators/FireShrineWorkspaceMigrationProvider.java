package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertAttachmentFace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.google.gson.JsonElement;
import com.google.common.hash.Hashing;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FireShrineWorkspaceMigrationProvider implements DataProvider {
    private static final String STRUCTURE_NAME = "fire_shrine_new";
    private static final String SOURCE_FOLDER = "/data/mkultra/structure/fire_shrine/";
    private static final BlockPos ANCHOR = BlockPos.ZERO;
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    private static final String PLATFORM_CONTENTS_INSERT_FAMILY = "fire_shrine_platform_contents";
    private static final String PILLAR_INSERT_FAMILY = "fire_shrine_pillars";
    private static final ResourceLocation OLD_PLATFORM_CONTENTS_POOL =
            MKUltra.id("fire_shrine/platform_contents");
    private static final ResourceLocation OLD_PILLARS_POOL = MKUltra.id("fire_shrine/pillars");
    private static final ResourceLocation INSERT_BASE = MKUltra.id("base");
    private static final ResourceLocation INSERT_ATTACH = MKUltra.id("attach");
    private static final String INSERT_FINAL_STATE = "minecraft:red_nether_bricks";

    private final PackOutput.PathProvider manifestPathProvider;
    private final PackOutput.PathProvider structurePathProvider;

    private record ExportBlock(BlockPos pos, BlockState state, CompoundTag nbt) {
    }

    private enum PaddingMode {
        CENTER_SLICE,
        EXTERNAL_VOID
    }

    private record TransformSpec(String sourceName, Rotation rotation, int width, int height, int length,
                                 PaddingMode paddingMode, boolean preserveAllJigsaws,
                                 Map<ResourceLocation, ResourceLocation> rewrittenJigsawPools,
                                 boolean emptyContentTemplate) {
        private TransformSpec(String sourceName, Rotation rotation, int width, int height, int length) {
            this(sourceName, rotation, width, height, length, PaddingMode.CENTER_SLICE, false, Map.of(), false);
        }

        private static TransformSpec externalVoid(String sourceName, int width, int height, int length,
                                                  boolean preserveAllJigsaws) {
            return new TransformSpec(sourceName, Rotation.NONE, width, height, length, PaddingMode.EXTERNAL_VOID,
                    preserveAllJigsaws, Map.of(), false);
        }

        private static TransformSpec emptyExternalVoid(String sourceName, int width, int height, int length,
                                                       boolean preserveAllJigsaws) {
            return new TransformSpec(sourceName, Rotation.NONE, width, height, length, PaddingMode.EXTERNAL_VOID,
                    preserveAllJigsaws, Map.of(), true);
        }

        private TransformSpec withRewrittenJigsawPool(ResourceLocation sourcePool, ResourceLocation targetPool) {
            return new TransformSpec(sourceName, rotation, width, height, length, paddingMode, preserveAllJigsaws,
                    Map.of(sourcePool, targetPool), emptyContentTemplate);
        }
    }

    private record PieceContext(BlockPos origin, int width, int height, int length) {
        BoundingBox bounds() {
            return new BoundingBox(origin.getX(), origin.getY(), origin.getZ(),
                    origin.getX() + width - 1, origin.getY() + height - 1, origin.getZ() + length - 1);
        }
    }

    public FireShrineWorkspaceMigrationProvider(PackOutput output) {
        manifestPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "mk_workspace_exports");
        structurePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        try {
            MKStructureWorkspace workspace = workspace();
            HubSpokePlanner planner = new HubSpokePlanner();
            List<MKPlannedPiece> plannedPieces = planner.createCanonicalPieces(workspace);
            List<MKWorkspacePieceDefinition> pieces = pieceDefinitions(workspace, plannedPieces);
            MKStructureWorkspace migrated = workspace.withPieces(pieces);
            MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(migrated, 4,
                    Instant.EPOCH.toString());
            CompletableFuture<?> manifestFuture = saveManifest(output, manifest);
            saveAuthoringTemplates(output, pieces);
            return manifestFuture;
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<?> saveManifest(CachedOutput output, MKWorkspaceExportManifest manifest) {
        JsonElement json = MKWorkspaceExportManifest.CODEC.encodeStart(JsonOps.INSTANCE, manifest)
                .getOrThrow();
        return DataProvider.saveStable(output, json, manifestPathProvider.json(MKUltra.id(STRUCTURE_NAME)));
    }

    private void saveAuthoringTemplates(CachedOutput output, List<MKWorkspacePieceDefinition> pieces) throws IOException {
        Map<String, MKWorkspacePieceDefinition> byName = new LinkedHashMap<>();
        for (MKWorkspacePieceDefinition piece : pieces) {
            byName.putIfAbsent(piece.pieceName(), piece);
        }
        ResourceLocation platformContentsPool = MKWorkspaceInsertFamilyDefinition.poolId(MKUltra.MODID,
                STRUCTURE_NAME, PLATFORM_CONTENTS_INSERT_FAMILY);
        ResourceLocation pillarPool = MKWorkspaceInsertFamilyDefinition.poolId(MKUltra.MODID,
                STRUCTURE_NAME, PILLAR_INSERT_FAMILY);
        Map<String, TransformSpec> specs = Map.of(
                HubSpokePlanner.CENTER_BASE_NAME,
                new TransformSpec("center_1", Rotation.COUNTERCLOCKWISE_90, 11, 30, 11),
                "fire_shrine_tower_north",
                new TransformSpec("tower_1", Rotation.COUNTERCLOCKWISE_90, 11, 30, 15),
                "fire_shrine_platform_east",
                new TransformSpec("platform_1", Rotation.COUNTERCLOCKWISE_90, 15, 30, 11)
                        .withRewrittenJigsawPool(OLD_PLATFORM_CONTENTS_POOL, platformContentsPool),
                "hub_spoke_corner_north_west",
                new TransformSpec("corner_east", Rotation.CLOCKWISE_90, 15, 30, 15)
                        .withRewrittenJigsawPool(OLD_PILLARS_POOL, pillarPool),
                "hub_spoke_corner_north_east",
                new TransformSpec("corner_west", Rotation.CLOCKWISE_90, 15, 30, 15)
                        .withRewrittenJigsawPool(OLD_PILLARS_POOL, pillarPool),
                PLATFORM_CONTENTS_INSERT_FAMILY,
                TransformSpec.emptyExternalVoid("gazebo", 9, 11, 9, true),
                "fire_shrine_gazebo",
                TransformSpec.externalVoid("gazebo", 9, 11, 9, true),
                "fire_shrine_lava_fountain",
                TransformSpec.externalVoid("lava_fountain", 9, 11, 9, true),
                PILLAR_INSERT_FAMILY,
                TransformSpec.emptyExternalVoid("pillar_1", 5, 20, 5, true),
                "fire_shrine_pillar",
                TransformSpec.externalVoid("pillar_1", 5, 20, 5, true)
        );
        for (Map.Entry<String, TransformSpec> entry : specs.entrySet()) {
            MKWorkspacePieceDefinition piece = byName.get(entry.getKey());
            if (piece == null) {
                throw new IllegalStateException("Missing migrated Fire Shrine workspace piece " + entry.getKey());
            }
            CompoundTag tag = transformedTemplate(entry.getValue(), piece);
            Path path = structurePathProvider.file(MKUltra.id(STRUCTURE_NAME + "/" + entry.getKey()), "nbt");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, bytes);
            byte[] payload = bytes.toByteArray();
            output.writeIfNeeded(path, payload, Hashing.sha1().hashBytes(payload));
        }
    }

    private MKStructureWorkspace workspace() {
        MKWorkspaceDimensions dimensions = new MKWorkspaceDimensions(11, 11, 30, 30, 30, 3, 3, 2);
        MKWorkspaceMaterialPalette palette = MKWorkspaceMaterialPalette.defaultPalette();
        MKWorkspaceStairAuthoringConfig stairConfig = MKWorkspaceStairAuthoringConfig.defaultConfig();
        MKWorkspaceTopologyProfile topologyProfile = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("fire_shrine_tower", "Tower", 15, 30,
                        List.of(Direction.NORTH, Direction.SOUTH)),
                new HubSpokePlannerSettings.SpokeTemplate("fire_shrine_platform", "Platform", 15, 30,
                        List.of(Direction.EAST, Direction.WEST))
        ), HubSpokePlannerSettings.CornerTemplateMode.PAIRED).applyTo(new MKWorkspaceTopologyProfile(
                HubSpokePlanner.PLANNER_ID,
                List.of(),
                TerrainAdjustment.NONE
        ));
        return new MKStructureWorkspace(
                UUID.nameUUIDFromBytes("mkultra:fire_shrine_new".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                ANCHOR,
                MKUltra.MODID,
                STRUCTURE_NAME,
                topologyProfile,
                dimensions,
                palette,
                stairConfig,
                MKVerticalAccessPlacement.CENTER,
                1,
                0,
                2,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                familyDefinitions(),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                insertFamilies(),
                0,
                0,
                List.of(),
                List.of()
        );
    }

    private List<MKWorkspaceRoomFamilyDefinition> familyDefinitions() {
        return List.of(
                family(HubSpokePlanner.CENTER_BASE_NAME, HubSpokePlanner.CENTER_SLOT, "hub", "room", false,
                        11, 11, 30),
                family(HubSpokePlanner.SPOKE_BASE_NAME, HubSpokePlanner.SPOKE_SLOT, "spoke", "room", false,
                        11, 15, 30),
                family("fire_shrine_tower_north", "hub_spoke.spoke.north", "spoke", "room", false,
                        11, 15, 30),
                family("fire_shrine_platform_east", "hub_spoke.spoke.east", "spoke", "room", false,
                        15, 11, 30),
                family(HubSpokePlanner.CORNER_BASE_NAME, HubSpokePlanner.CORNER_SLOT, "corner", "room", true,
                        15, 15, 30),
                family("hub_spoke_corner_north_west", "hub_spoke.corner.north_west", "corner", "room", true,
                        15, 15, 30),
                family("hub_spoke_corner_north_east", "hub_spoke.corner.north_east", "corner", "room", true,
                        15, 15, 30)
        );
    }

    private List<MKWorkspaceInsertFamilyDefinition> insertFamilies() {
        return List.of(
                new MKWorkspaceInsertFamilyDefinition(PLATFORM_CONTENTS_INSERT_FAMILY,
                        MKWorkspaceInsertFamilyKind.COURTYARD_SOCKET, 9, 11, 9,
                        Optional.of(MKWorkspaceInsertAttachmentFace.BOTTOM), 4, 4, "minecraft:air"),
                new MKWorkspaceInsertFamilyDefinition(PILLAR_INSERT_FAMILY,
                        MKWorkspaceInsertFamilyKind.COURTYARD_SOCKET, 5, 20, 5,
                        Optional.of(MKWorkspaceInsertAttachmentFace.BOTTOM), 2, 2, "minecraft:air")
        );
    }

    private MKWorkspaceRoomFamilyDefinition family(String baseName, String topologySlotId, String roleKind,
                                                   String pieceKind, boolean terminal, int width, int length,
                                                   int height) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                baseName,
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, roleKind, pieceKind, terminal),
                "",
                false,
                width,
                length,
                height,
                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                List.of(),
                0,
                0,
                MKWorkspaceFoundationPolicy.none(),
                null
        );
    }

    private List<MKWorkspacePieceDefinition> pieceDefinitions(MKStructureWorkspace workspace,
                                                              List<MKPlannedPiece> plannedPieces) {
        List<MKWorkspacePieceDefinition> pieces = new ArrayList<>();
        Map<String, MKWorkspacePieceDefinition> authoringByName = new LinkedHashMap<>();
        int nextX = 8;
        for (MKPlannedPiece plannedPiece : plannedPieces) {
            if (MKWorkspaceTemplateReuseTags.isDerived(plannedPiece.tags())) {
                continue;
            }
            PieceContext context = new PieceContext(new BlockPos(nextX, 0, 8),
                    plannedPiece.interiorWidth(), plannedPiece.interiorHeight(), plannedPiece.interiorLength());
            MKWorkspacePieceDefinition piece = pieceDefinition(workspace, plannedPiece, context);
            pieces.add(piece);
            authoringByName.put(plannedPiece.pieceName(), piece);
            nextX += plannedPiece.interiorWidth() + 6;
        }
        for (MKPlannedPiece insertPiece : insertPieces()) {
            PieceContext context = new PieceContext(new BlockPos(nextX, 0, 8),
                    insertPiece.interiorWidth(), insertPiece.interiorHeight(), insertPiece.interiorLength());
            pieces.add(pieceDefinition(workspace, insertPiece, context));
            nextX += insertPiece.interiorWidth() + 6;
        }
        for (MKPlannedPiece plannedPiece : plannedPieces) {
            if (!MKWorkspaceTemplateReuseTags.isDerived(plannedPiece.tags())) {
                continue;
            }
            MKWorkspacePieceDefinition source = authoringByName.get(MKWorkspaceTemplateReuseTags.sourceId(plannedPiece.tags()));
            if (source == null) {
                throw new IllegalStateException("Missing source piece for derived hub-spoke piece " +
                        plannedPiece.pieceName());
            }
            pieces.add(pieceDefinition(workspace, plannedPiece, new PieceContext(source.worldOrigin(),
                    plannedPiece.interiorWidth(), plannedPiece.interiorHeight(), plannedPiece.interiorLength())));
        }
        return pieces.stream()
                .sorted(Comparator.comparing(MKWorkspacePieceDefinition::pieceName))
                .toList();
    }

    private List<MKPlannedPiece> insertPieces() {
        return List.of(
                insertVariant("fire_shrine_gazebo", PLATFORM_CONTENTS_INSERT_FAMILY,
                        PLATFORM_CONTENTS_INSERT_FAMILY, 9, 9, 11, 1),
                insertVariant("fire_shrine_lava_fountain", PLATFORM_CONTENTS_INSERT_FAMILY,
                        PLATFORM_CONTENTS_INSERT_FAMILY, 9, 9, 11, 2),
                insertVariant("fire_shrine_pillar", PILLAR_INSERT_FAMILY, PILLAR_INSERT_FAMILY, 5, 5, 20, 1)
        );
    }

    private MKPlannedPiece insertVariant(String pieceName, String insertFamilyId, String baseName, int width,
                                         int length, int height, int variantIndex) {
        return insertPiece(pieceName, insertFamilyId, baseName, width, length, height, "instance", variantIndex);
    }

    private MKPlannedPiece insertPiece(String pieceName, String insertFamilyId, String baseName, int width, int length,
                                       int height, String workspacePieceKind, int variantIndex) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", "hub_spoke.insert");
        tags.put("workspace_topology_slot_id", "hub_spoke.insert." + insertFamilyId);
        tags.put("workspace_topology_role_id", "hub_spoke.insert");
        tags.put("workspace_piece_kind", workspacePieceKind);
        tags.put("workspace_base_name", baseName);
        tags.put("tower_piece_kind", "insert");
        tags.put(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID, insertFamilyId);
        tags.put(MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND,
                MKWorkspaceInsertFamilyKind.INSERT_SOCKET.getSerializedName());
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, true, false, "hub_spoke", false, true).applyToTags(tags);
        MKWorkspaceMaterialPalette palette = MKWorkspaceMaterialPalette.defaultPalette();
        tags.put("workspace_floor_block", palette.floorBlock().toString());
        tags.put("workspace_wall_block", palette.wallBlock().toString());
        tags.put("workspace_ceiling_block", palette.ceilingBlock().toString());
        tags.put("workspace_variant_index", Integer.toString(variantIndex));
        return new MKPlannedPiece("hub_spoke.insert", pieceName, width, length, height, List.of(), tags);
    }

    private MKWorkspacePieceDefinition pieceDefinition(MKStructureWorkspace workspace, MKPlannedPiece plannedPiece,
                                                       PieceContext context) {
        List<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>(plannedPiece.connectors().stream()
                .filter(MKPlannedConnector::placesJigsaw)
                .map(connector -> connectorDefinition(workspace, plannedPiece, connector, context))
                .toList());
        connectors.addAll(fireShrineInsertConnectors(workspace, plannedPiece));
        MKWorkspaceDimensions effectiveDimensions = new MKWorkspaceDimensions(
                plannedPiece.interiorWidth(),
                plannedPiece.interiorLength(),
                plannedPiece.interiorHeight(),
                plannedPiece.interiorHeight(),
                plannedPiece.interiorHeight(),
                3,
                3,
                2
        );
        BlockPos structureBlockPos = context.origin().offset(-2, 1, context.length() / 2);
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(plannedPiece.tags());
        if (tags.containsKey(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID) &&
                !tags.containsKey("workspace_piece_kind")) {
            tags.put("workspace_piece_kind", "template");
            tags.putIfAbsent("workspace_variant_index", "0");
        }
        return new MKWorkspacePieceDefinition(
                UUID.nameUUIDFromBytes((STRUCTURE_NAME + ":" + plannedPiece.pieceName())
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                workspace.id(),
                plannedPiece.pieceName(),
                plannedPiece.roleId(),
                plannedPiece.plannerId(),
                parseIntTag(tags, "workspace_variant_index", 0),
                effectiveDimensions,
                connectors,
                context.origin(),
                context.bounds(),
                context.bounds(),
                structureBlockPos,
                structureBlockPos.west(),
                List.of(),
                List.of(),
                tags
        );
    }

    private List<MKWorkspaceConnectorDefinition> fireShrineInsertConnectors(MKStructureWorkspace workspace,
                                                                            MKPlannedPiece piece) {
        ResourceLocation platformContentsPool = MKWorkspaceInsertFamilyDefinition.poolId(workspace.namespace(),
                workspace.structureName(), PLATFORM_CONTENTS_INSERT_FAMILY);
        ResourceLocation pillarPool = MKWorkspaceInsertFamilyDefinition.poolId(workspace.namespace(),
                workspace.structureName(), PILLAR_INSERT_FAMILY);
        return switch (piece.pieceName()) {
            case "fire_shrine_platform_east" -> List.of(insertParentConnector(
                    new BlockPos(7, 0, 5), "up_north", platformContentsPool));
            case "fire_shrine_platform_west" -> List.of(insertParentConnector(
                    new BlockPos(7, 0, 5), "up_south", platformContentsPool));
            case "hub_spoke_corner_north_west" -> List.of(insertParentConnector(
                    new BlockPos(3, 0, 3), "up_south", pillarPool));
            case "hub_spoke_corner_north_east" -> List.of(insertParentConnector(
                    new BlockPos(11, 0, 3), "up_west", pillarPool));
            case "hub_spoke_corner_south_east" -> List.of(insertParentConnector(
                    new BlockPos(11, 0, 11), "up_north", pillarPool));
            case "hub_spoke_corner_south_west" -> List.of(insertParentConnector(
                    new BlockPos(3, 0, 11), "up_east", pillarPool));
            case PLATFORM_CONTENTS_INSERT_FAMILY, "fire_shrine_gazebo", "fire_shrine_lava_fountain" ->
                    List.of(insertAttachConnector(new BlockPos(4, 0, 3), platformContentsPool));
            case PILLAR_INSERT_FAMILY, "fire_shrine_pillar" ->
                    List.of(insertAttachConnector(new BlockPos(2, 0, 2), pillarPool));
            default -> List.of();
        };
    }

    private MKWorkspaceConnectorDefinition insertParentConnector(BlockPos pos, String orientation,
                                                                ResourceLocation targetPool) {
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.LINK_CANDIDATE,
                Direction.UP,
                pos,
                1,
                1,
                0,
                pos.getY(),
                INSERT_BASE,
                INSERT_ATTACH,
                targetPool,
                EMPTY_POOL,
                orientation,
                INSERT_FINAL_STATE,
                MKWorkspaceConnectorDefinition.DEFAULT_JIGSAW_JOINT
        );
    }

    private MKWorkspaceConnectorDefinition insertAttachConnector(BlockPos pos, ResourceLocation incomingPool) {
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.LINK_CANDIDATE,
                Direction.DOWN,
                pos,
                1,
                1,
                0,
                pos.getY(),
                INSERT_ATTACH,
                INSERT_BASE,
                EMPTY_POOL,
                incomingPool,
                "down_east",
                INSERT_FINAL_STATE,
                MKWorkspaceConnectorDefinition.DEFAULT_JIGSAW_JOINT
        );
    }

    private MKWorkspaceConnectorDefinition connectorDefinition(MKStructureWorkspace workspace, MKPlannedPiece piece,
                                                               MKPlannedConnector connector, PieceContext context) {
        Direction facing = connector.facing();
        int centerX = context.width() / 2 + connector.lateralOffset();
        int centerZ = context.length() / 2 + connector.lateralOffset();
        int y = connector.verticalOffset();
        BlockPos pos = switch (facing) {
            case NORTH -> new BlockPos(centerX, y, 0);
            case SOUTH -> new BlockPos(centerX, y, context.length() - 1);
            case WEST -> new BlockPos(0, y, centerZ);
            case EAST -> new BlockPos(context.width() - 1, y, centerZ);
            case UP -> new BlockPos(context.width() / 2, context.height() - 1, context.length() / 2);
            case DOWN -> new BlockPos(context.width() / 2, 0, context.length() / 2);
        };
        ResourceLocation pool = connectorPool(workspace, connector.targetPoolName(), piece);
        ResourceLocation incomingPool = incomingPool(workspace, connector.incomingPoolName());
        ResourceLocation name = EMPTY_POOL.equals(incomingPool) ?
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(), connector.role().getSerializedName()) :
                incomingPool;
        ResourceLocation target = EMPTY_POOL.equals(pool) ?
                ResourceLocation.fromNamespaceAndPath(workspace.namespace(), targetName(connector.role())) :
                pool;
        return new MKWorkspaceConnectorDefinition(
                connector.role(),
                facing,
                pos,
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                name,
                target,
                pool,
                incomingPool
        );
    }

    private ResourceLocation connectorPool(MKStructureWorkspace workspace, String explicitTargetBaseName,
                                           MKPlannedPiece piece) {
        if (explicitTargetBaseName != null) {
            return parseConnectorPool(workspace, explicitTargetBaseName);
        }
        return parseConnectorPool(workspace, piece.pieceName());
    }

    private ResourceLocation incomingPool(MKStructureWorkspace workspace, String incomingPoolName) {
        if (incomingPoolName == null || incomingPoolName.isBlank()) {
            return EMPTY_POOL;
        }
        return parseConnectorPool(workspace, incomingPoolName);
    }

    private ResourceLocation parseConnectorPool(MKStructureWorkspace workspace, String poolName) {
        if (poolName.contains(":")) {
            return ResourceLocation.parse(poolName);
        }
        return ResourceLocation.fromNamespaceAndPath(workspace.namespace(), workspace.structureName() + "/" + poolName);
    }

    private String targetName(MKConnectorRole role) {
        return switch (role) {
            case MAIN_FORWARD -> MKConnectorRole.MAIN_BACK.getSerializedName();
            case MAIN_BACK -> MKConnectorRole.MAIN_FORWARD.getSerializedName();
            case BRANCH -> MKConnectorRole.BRANCH.getSerializedName();
            case CONNECT_UP -> MKConnectorRole.CONNECT_DOWN.getSerializedName();
            case CONNECT_DOWN -> MKConnectorRole.CONNECT_UP.getSerializedName();
            case TOP_CAP_FORWARD -> MKConnectorRole.TOP_CAP_BACK.getSerializedName();
            case TOP_CAP_BACK -> MKConnectorRole.TOP_CAP_FORWARD.getSerializedName();
            case LINK_CANDIDATE -> MKConnectorRole.LINK_CANDIDATE.getSerializedName();
            default -> throw new IllegalStateException("Unsupported connector role " + role);
        };
    }

    private CompoundTag transformedTemplate(TransformSpec spec, MKWorkspacePieceDefinition piece) throws IOException {
        CompoundTag sourceTag = readSourceTemplate(spec.sourceName());
        ListTag sourceSize = sourceTag.getList("size", Tag.TAG_INT);
        int sourceWidth = sourceSize.getInt(0);
        int sourceLength = sourceSize.getInt(2);
        HolderGetter<Block> blockGetter = BuiltInRegistries.BLOCK.asLookup();
        List<BlockState> sourcePalette = readPalette(blockGetter, sourceTag);
        LinkedHashMap<BlockPos, ExportBlock> rotatedBlocksByPos = new LinkedHashMap<>();
        ListTag sourceBlocks = sourceTag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < sourceBlocks.size(); i++) {
            CompoundTag sourceBlock = sourceBlocks.getCompound(i);
            BlockState state = sourcePalette.get(sourceBlock.getInt("state")).rotate(spec.rotation());
            if (state.is(Blocks.JIGSAW) && !shouldPreserveJigsaw(sourceBlock, spec)) {
                continue;
            }
            BlockPos sourcePos = readBlockPos(sourceBlock.getList("pos", Tag.TAG_INT));
            BlockPos targetPos = rotatePos(sourcePos, sourceWidth, sourceLength, spec.rotation());
            CompoundTag blockNbt = sourceBlock.contains("nbt", Tag.TAG_COMPOUND) ?
                    sourceBlock.getCompound("nbt").copy() : null;
            if (state.is(Blocks.JIGSAW) && blockNbt != null) {
                rewriteJigsawPool(blockNbt, spec);
            }
            if (blockNbt != null) {
                blockNbt.putInt("x", targetPos.getX());
                blockNbt.putInt("y", targetPos.getY());
                blockNbt.putInt("z", targetPos.getZ());
            }
            rotatedBlocksByPos.put(targetPos, new ExportBlock(targetPos, state, blockNbt));
        }
        int rotatedWidth = rotatedWidth(sourceWidth, sourceLength, spec.rotation());
        int rotatedLength = rotatedLength(sourceWidth, sourceLength, spec.rotation());
        LinkedHashMap<BlockPos, ExportBlock> blocksByPos = switch (spec.paddingMode()) {
            case CENTER_SLICE -> centerPad(rotatedBlocksByPos, rotatedWidth, rotatedLength, spec);
            case EXTERNAL_VOID -> externalVoidPad(rotatedBlocksByPos, rotatedWidth, sourceSize.getInt(1),
                    rotatedLength, spec);
        };
        if (spec.emptyContentTemplate()) {
            blocksByPos = emptyContentTemplate(blocksByPos, spec);
        }
        for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
            ExportBlock block = connectorJigsawBlock(connector);
            blocksByPos.put(block.pos(), block);
        }
        CompoundTag result = sourceTag.copy();
        result.put("size", intList(spec.width(), spec.height(), spec.length()));
        result.remove("palettes");
        writePaletteAndBlocks(result, blocksByPos.values().stream()
                .filter(block -> inBounds(block.pos(), spec))
                .toList());
        return result;
    }

    private int parseIntTag(Map<String, String> tags, String key, int fallback) {
        try {
            return Integer.parseInt(tags.getOrDefault(key, Integer.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private LinkedHashMap<BlockPos, ExportBlock> emptyContentTemplate(LinkedHashMap<BlockPos, ExportBlock> blocks,
                                                                      TransformSpec spec) {
        LinkedHashMap<BlockPos, ExportBlock> empty = new LinkedHashMap<>();
        BlockState voidState = Blocks.STRUCTURE_VOID.defaultBlockState();
        for (int x = 0; x < spec.width(); x++) {
            for (int y = 0; y < spec.height(); y++) {
                for (int z = 0; z < spec.length(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    empty.put(pos, new ExportBlock(pos, voidState, null));
                }
            }
        }
        for (ExportBlock block : blocks.values()) {
            if (block.state().is(Blocks.JIGSAW)) {
                empty.put(block.pos(), block);
            }
        }
        return empty;
    }

    private boolean shouldPreserveJigsaw(CompoundTag sourceBlock, TransformSpec spec) {
        if (!sourceBlock.contains("nbt", Tag.TAG_COMPOUND)) {
            return spec.preserveAllJigsaws();
        }
        CompoundTag nbt = sourceBlock.getCompound("nbt");
        ResourceLocation pool = ResourceLocation.parse(nbt.getString("pool"));
        return spec.preserveAllJigsaws() || spec.rewrittenJigsawPools().containsKey(pool);
    }

    private void rewriteJigsawPool(CompoundTag jigsawNbt, TransformSpec spec) {
        ResourceLocation pool = ResourceLocation.parse(jigsawNbt.getString("pool"));
        ResourceLocation rewritten = spec.rewrittenJigsawPools().get(pool);
        if (rewritten != null) {
            jigsawNbt.putString("pool", rewritten.toString());
        }
    }

    private int rotatedWidth(int sourceWidth, int sourceLength, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> sourceLength;
            default -> sourceWidth;
        };
    }

    private int rotatedLength(int sourceWidth, int sourceLength, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> sourceWidth;
            default -> sourceLength;
        };
    }

    private LinkedHashMap<BlockPos, ExportBlock> centerPad(LinkedHashMap<BlockPos, ExportBlock> blocks,
                                                           int sourceWidth, int sourceLength,
                                                           TransformSpec spec) {
        int padX = spec.width() - sourceWidth;
        int padZ = spec.length() - sourceLength;
        if (padX < 0 || padZ < 0) {
            throw new IllegalStateException("Fire Shrine migration cannot shrink " + spec.sourceName() +
                    " from " + sourceWidth + "x" + sourceLength + " to " + spec.width() + "x" + spec.length());
        }
        LinkedHashMap<BlockPos, ExportBlock> padded = centerPadX(blocks, sourceWidth, padX);
        return centerPadZ(padded, sourceLength, padZ);
    }

    private LinkedHashMap<BlockPos, ExportBlock> externalVoidPad(LinkedHashMap<BlockPos, ExportBlock> blocks,
                                                                 int sourceWidth, int sourceHeight, int sourceLength,
                                                                 TransformSpec spec) {
        int padX = spec.width() - sourceWidth;
        int padY = spec.height() - sourceHeight;
        int padZ = spec.length() - sourceLength;
        if (padX < 0 || padY < 0 || padZ < 0) {
            throw new IllegalStateException("Fire Shrine migration cannot shrink " + spec.sourceName() +
                    " from " + sourceWidth + "x" + sourceHeight + "x" + sourceLength + " to " +
                    spec.width() + "x" + spec.height() + "x" + spec.length());
        }
        int offsetX = centeredExternalOffset(padX);
        int offsetY = 0;
        int offsetZ = centeredExternalOffset(padZ);
        LinkedHashMap<BlockPos, ExportBlock> padded = new LinkedHashMap<>();
        for (ExportBlock block : blocks.values()) {
            BlockPos pos = block.pos().offset(offsetX, offsetY, offsetZ);
            padded.put(pos, moveBlock(block, pos));
        }
        BlockState voidState = Blocks.STRUCTURE_VOID.defaultBlockState();
        for (int x = 0; x < spec.width(); x++) {
            for (int y = 0; y < spec.height(); y++) {
                for (int z = 0; z < spec.length(); z++) {
                    boolean insideSource = x >= offsetX && x < offsetX + sourceWidth &&
                            y >= offsetY && y < offsetY + sourceHeight &&
                            z >= offsetZ && z < offsetZ + sourceLength;
                    if (!insideSource) {
                        BlockPos pos = new BlockPos(x, y, z);
                        padded.putIfAbsent(pos, new ExportBlock(pos, voidState, null));
                    }
                }
            }
        }
        return padded;
    }

    private int centeredExternalOffset(int padding) {
        return padding / 2;
    }

    private LinkedHashMap<BlockPos, ExportBlock> centerPadX(LinkedHashMap<BlockPos, ExportBlock> blocks,
                                                            int sourceWidth, int pad) {
        if (pad == 0) {
            return blocks;
        }
        int insertAt = sourceWidth / 2;
        LinkedHashMap<BlockPos, ExportBlock> shifted = new LinkedHashMap<>();
        for (ExportBlock block : blocks.values()) {
            int x = block.pos().getX() < insertAt ? block.pos().getX() : block.pos().getX() + pad;
            BlockPos pos = new BlockPos(x, block.pos().getY(), block.pos().getZ());
            shifted.put(pos, moveBlock(block, pos));
        }
        int leftCopyCount = (pad + 1) / 2;
        for (int i = 0; i < pad; i++) {
            int sourceX = i < leftCopyCount ? insertAt - 1 : insertAt;
            sourceX = Math.max(0, Math.min(sourceWidth - 1, sourceX));
            int targetX = insertAt + i;
            for (ExportBlock block : blocks.values()) {
                if (block.pos().getX() == sourceX) {
                    BlockPos pos = new BlockPos(targetX, block.pos().getY(), block.pos().getZ());
                    shifted.put(pos, moveBlock(block, pos));
                }
            }
        }
        return shifted;
    }

    private LinkedHashMap<BlockPos, ExportBlock> centerPadZ(LinkedHashMap<BlockPos, ExportBlock> blocks,
                                                            int sourceLength, int pad) {
        if (pad == 0) {
            return blocks;
        }
        int insertAt = sourceLength / 2;
        LinkedHashMap<BlockPos, ExportBlock> shifted = new LinkedHashMap<>();
        for (ExportBlock block : blocks.values()) {
            int z = block.pos().getZ() < insertAt ? block.pos().getZ() : block.pos().getZ() + pad;
            BlockPos pos = new BlockPos(block.pos().getX(), block.pos().getY(), z);
            shifted.put(pos, moveBlock(block, pos));
        }
        int leftCopyCount = (pad + 1) / 2;
        for (int i = 0; i < pad; i++) {
            int sourceZ = i < leftCopyCount ? insertAt - 1 : insertAt;
            sourceZ = Math.max(0, Math.min(sourceLength - 1, sourceZ));
            int targetZ = insertAt + i;
            for (ExportBlock block : blocks.values()) {
                if (block.pos().getZ() == sourceZ) {
                    BlockPos pos = new BlockPos(block.pos().getX(), block.pos().getY(), targetZ);
                    shifted.put(pos, moveBlock(block, pos));
                }
            }
        }
        return shifted;
    }

    private ExportBlock moveBlock(ExportBlock block, BlockPos pos) {
        CompoundTag nbt = block.nbt() == null ? null : block.nbt().copy();
        if (nbt != null) {
            nbt.putInt("x", pos.getX());
            nbt.putInt("y", pos.getY());
            nbt.putInt("z", pos.getZ());
        }
        return new ExportBlock(pos, block.state(), nbt);
    }

    private boolean inBounds(BlockPos pos, TransformSpec spec) {
        return pos.getX() >= 0 && pos.getX() < spec.width() &&
                pos.getY() >= 0 && pos.getY() < spec.height() &&
                pos.getZ() >= 0 && pos.getZ() < spec.length();
    }

    private CompoundTag readSourceTemplate(String name) throws IOException {
        try (InputStream input = FireShrineWorkspaceMigrationProvider.class.getResourceAsStream(
                SOURCE_FOLDER + name + ".nbt")) {
            if (input == null) {
                throw new IOException("Missing Fire Shrine source template " + name);
            }
            return NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
        }
    }

    private List<BlockState> readPalette(HolderGetter<Block> blockGetter, CompoundTag tag) {
        ListTag paletteTag = tag.contains("palette", Tag.TAG_LIST) ?
                tag.getList("palette", Tag.TAG_COMPOUND) :
                tag.getList("palettes", Tag.TAG_LIST).getList(0);
        List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(blockGetter, paletteTag.getCompound(i)));
        }
        return palette;
    }

    private void writePaletteAndBlocks(CompoundTag tag, Iterable<ExportBlock> blocks) {
        Map<BlockState, Integer> paletteIndexes = new LinkedHashMap<>();
        ListTag blockList = new ListTag();
        for (ExportBlock block : blocks) {
            int stateId = paletteIndexes.computeIfAbsent(block.state(), ignored -> paletteIndexes.size());
            CompoundTag blockTag = new CompoundTag();
            blockTag.put("pos", intList(block.pos().getX(), block.pos().getY(), block.pos().getZ()));
            blockTag.putInt("state", stateId);
            if (block.nbt() != null) {
                blockTag.put("nbt", block.nbt());
            }
            blockList.add(blockTag);
        }
        ListTag paletteTag = new ListTag();
        for (BlockState state : paletteIndexes.keySet()) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        tag.put("blocks", blockList);
        tag.put("palette", paletteTag);
    }

    private ExportBlock connectorJigsawBlock(MKWorkspaceConnectorDefinition connector) {
        BlockPos pos = connector.relativePos();
        BlockState state = Blocks.JIGSAW.defaultBlockState()
                .setValue(JigsawBlock.ORIENTATION, getJigsawOrientation(connector));
        CompoundTag jigsawNbt = new CompoundTag();
        jigsawNbt.putString("id", "minecraft:jigsaw");
        jigsawNbt.putString("name", connector.jigsawName().toString());
        jigsawNbt.putString("target", connector.jigsawTarget().toString());
        jigsawNbt.putString("pool", connector.targetPool().toString());
        jigsawNbt.putString("final_state", connector.jigsawFinalState());
        jigsawNbt.putString("joint", connector.jigsawJoint());
        jigsawNbt.putInt("x", pos.getX());
        jigsawNbt.putInt("y", pos.getY());
        jigsawNbt.putInt("z", pos.getZ());
        return new ExportBlock(pos, state, jigsawNbt);
    }

    private FrontAndTop getJigsawOrientation(MKWorkspaceConnectorDefinition connector) {
        if (!connector.jigsawOrientation().isBlank()) {
            for (FrontAndTop orientation : FrontAndTop.values()) {
                if (orientation.getSerializedName().equals(connector.jigsawOrientation())) {
                    return orientation;
                }
            }
        }
        Direction facing = connector.facing();
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return FrontAndTop.fromFrontAndTop(facing, Direction.NORTH);
        }
        return FrontAndTop.fromFrontAndTop(facing, Direction.UP);
    }

    private BlockPos rotatePos(BlockPos pos, int sourceWidth, int sourceLength, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new BlockPos(sourceLength - 1 - pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> new BlockPos(sourceWidth - 1 - pos.getX(), pos.getY(), sourceLength - 1 - pos.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), sourceWidth - 1 - pos.getX());
            default -> pos;
        };
    }

    private BlockPos readBlockPos(ListTag list) {
        return new BlockPos(list.getInt(0), list.getInt(1), list.getInt(2));
    }

    private ListTag intList(int x, int y, int z) {
        ListTag list = new ListTag();
        list.add(IntTag.valueOf(x));
        list.add(IntTag.valueOf(y));
        list.add(IntTag.valueOf(z));
        return list;
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKUltra Fire Shrine Workspace Migration";
    }
}
