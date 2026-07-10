package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorMaskPools;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MKFloorMaskVariantExporter {
    public static final String FLOOR_MASK_TAG = MKFloorMaskPools.FLOOR_MASK_TAG;
    public static final String FLOOR_MASK_WEIGHT_TAG = MKFloorMaskPools.FLOOR_MASK_WEIGHT_TAG;
    public static final String FLOOR_RANDOMIZE_MAIN_EXIT_TAG = "workspace_floor_randomize_main_exit";
    public static final String FLOOR_SELECTED_MAIN_EXIT_TAG = "workspace_floor_selected_main_exit";
    public static final String CLOSED_CONNECTOR_COUNT_TAG = "workspace_floor_closed_connector_count";
    public static final String CLOSED_CONNECTOR_PREFIX = "workspace_floor_closed_connector_";
    public static final String MASK_POOL_SEGMENT = MKFloorMaskPools.MASK_POOL_SEGMENT;

    private MKFloorMaskVariantExporter() {
    }

    public static List<MKWorkspacePieceDefinition> exportPieces(MKStructureWorkspace workspace,
                                                                boolean includeRuntimeVariants) {
        ArrayList<MKWorkspacePieceDefinition> pieces = new ArrayList<>(workspace.pieces());
        if (!includeRuntimeVariants) {
            return List.copyOf(pieces);
        }
        Map<String, List<MKWorkspacePieceDefinition>> runtimeSourcesByBaseName = workspace.pieces().stream()
                .filter(MKFloorMaskVariantExporter::isRuntimeContentVariant)
                .collect(Collectors.groupingBy(
                        piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            if (isFloorAuthoringTemplate(piece)) {
                String baseName = piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
                List<MKWorkspacePieceDefinition> runtimeSources = runtimeSourcesByBaseName.get(baseName);
                if ((runtimeSources == null || runtimeSources.isEmpty()) && hasRuntimePieceInfo(piece)) {
                    runtimeSources = List.of(piece);
                }
                if (runtimeSources == null) {
                    continue;
                }
                for (MKWorkspacePieceDefinition runtimeSource : runtimeSources) {
                    pieces.addAll(createMaskVariants(workspace, runtimeSource));
                }
            }
        }
        return List.copyOf(pieces);
    }

    public static boolean isFloorAuthoringTemplate(MKWorkspacePieceDefinition piece) {
        return "floor_plan_room".equals(piece.tags().get("tower_piece_kind")) &&
                "template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance"));
    }

    private static boolean isRuntimeContentVariant(MKWorkspacePieceDefinition piece) {
        return "floor_plan_room".equals(piece.tags().get("tower_piece_kind")) &&
                !"template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance")) &&
                !MKWorkspaceTemplateReuseTags.isDerived(piece.tags());
    }

    private static boolean hasRuntimePieceInfo(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceRuntimePieceInfo.fromTags(piece.tags()).isPresent();
    }

    private static List<MKWorkspacePieceDefinition> createMaskVariants(MKStructureWorkspace workspace,
                                                                       MKWorkspacePieceDefinition sourcePiece) {
        if (randomizesMainExit(sourcePiece)) {
            return createRandomizedMainExitVariants(workspace, sourcePiece);
        }
        List<MKWorkspaceConnectorDefinition> optionalBranches = optionalBranchConnectors(sourcePiece);
        List<MKWorkspaceConnectorDefinition> linkCandidates = linkCandidateConnectors(sourcePiece);
        if (optionalBranches.isEmpty()) {
            return List.of(createVariant(workspace, sourcePiece, linkCandidates, "none",
                    activeConnectors(sourcePiece, List.of())));
        }
        ArrayList<MKWorkspacePieceDefinition> variants = new ArrayList<>();
        int variantCount = 1 << optionalBranches.size();
        float sprawl = parseFloat(sourcePiece.tags().get("workspace_floor_sprawl"), 0.5f);
        for (int mask = 0; mask < variantCount; mask++) {
            if (sprawl <= 0.0f && mask != 0) {
                continue;
            }
            ArrayList<MKWorkspaceConnectorDefinition> activeOptional = new ArrayList<>();
            ArrayList<MKWorkspaceConnectorDefinition> closedOptional = new ArrayList<>();
            for (int bit = 0; bit < optionalBranches.size(); bit++) {
                MKWorkspaceConnectorDefinition connector = optionalBranches.get(bit);
                if ((mask & (1 << bit)) != 0) {
                    activeOptional.add(connector);
                } else {
                    closedOptional.add(connector);
                }
            }
            String maskName = maskName(activeOptional);
            closedOptional.addAll(linkCandidates);
            variants.add(createVariant(workspace, sourcePiece, closedOptional, maskName,
                    activeConnectors(sourcePiece, activeOptional)));
        }
        return List.copyOf(variants);
    }

    private static List<MKWorkspacePieceDefinition> createRandomizedMainExitVariants(
            MKStructureWorkspace workspace,
            MKWorkspacePieceDefinition sourcePiece) {
        List<MKWorkspaceConnectorDefinition> candidates = randomMainExitCandidates(sourcePiece);
        if (candidates.size() <= 1) {
            return List.of(createVariant(workspace, sourcePiece, List.of(), "none", sourcePiece.connectors()));
        }
        MKWorkspaceConnectorDefinition mainTemplate = mainExitConnector(sourcePiece);
        MKWorkspaceConnectorDefinition branchTemplate = candidates.stream()
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH)
                .findFirst()
                .orElse(mainTemplate);
        float sprawl = parseFloat(sourcePiece.tags().get("workspace_floor_sprawl"), 0.5f);
        ArrayList<MKWorkspacePieceDefinition> variants = new ArrayList<>();
        for (MKWorkspaceConnectorDefinition selectedMain : candidates) {
            List<MKWorkspaceConnectorDefinition> optionalBranches = candidates.stream()
                    .filter(connector -> connector != selectedMain)
                    .toList();
            int variantCount = 1 << optionalBranches.size();
            for (int mask = 0; mask < variantCount; mask++) {
                if (sprawl <= 0.0f && mask != 0) {
                    continue;
                }
                ArrayList<MKWorkspaceConnectorDefinition> activeOptional = new ArrayList<>();
                ArrayList<MKWorkspaceConnectorDefinition> closedOptional = new ArrayList<>();
                for (int bit = 0; bit < optionalBranches.size(); bit++) {
                    MKWorkspaceConnectorDefinition connector = optionalBranches.get(bit);
                    if ((mask & (1 << bit)) != 0) {
                        activeOptional.add(connector);
                    } else {
                        closedOptional.add(connector);
                    }
                }
                closedOptional.addAll(linkCandidateConnectors(sourcePiece));
                String maskName = maskName(activeOptional);
                String suffix = "_main_" + selectedMain.facing().getSerializedName().charAt(0) + "_mask_" + maskName;
                variants.add(createVariant(workspace, sourcePiece, closedOptional, maskName,
                        activeRandomizedMainConnectors(sourcePiece, selectedMain, activeOptional, mainTemplate,
                                branchTemplate),
                        suffix,
                        selectedMain.facing()));
            }
        }
        return List.copyOf(variants);
    }

    private static MKWorkspacePieceDefinition createVariant(MKStructureWorkspace workspace,
                                                            MKWorkspacePieceDefinition sourcePiece,
                                                            List<MKWorkspaceConnectorDefinition> closedOptional,
                                                            String maskName,
                                                            List<MKWorkspaceConnectorDefinition> activeConnectors) {
        return createVariant(workspace, sourcePiece, closedOptional, maskName, activeConnectors,
                "_mask_" + maskName, null);
    }

    private static MKWorkspacePieceDefinition createVariant(MKStructureWorkspace workspace,
                                                            MKWorkspacePieceDefinition sourcePiece,
                                                            List<MKWorkspaceConnectorDefinition> closedOptional,
                                                            String maskName,
                                                            List<MKWorkspaceConnectorDefinition> activeConnectors,
                                                            String pieceNameSuffix,
                                                            Direction selectedMainExit) {
        String pieceName = sourcePiece.pieceName() + pieceNameSuffix;
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(sourcePiece.tags());
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_base_name", pieceName);
        tags.put(MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG,
                MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT);
        tags.put(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, "false");
        tags.put(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG,
                sourcePiece.tags().getOrDefault("workspace_base_name", sourcePiece.pieceName()));
        tags.put(MKWorkspaceTemplateReuseTags.ROTATION_TAG, MKWorkspaceTemplateReuseTags.ROTATION_NONE);
        tags.put(FLOOR_MASK_TAG, maskName);
        tags.put(FLOOR_MASK_WEIGHT_TAG, Integer.toString(maskWeight(maskName, tags)));
        if (selectedMainExit != null) {
            tags.put(FLOOR_SELECTED_MAIN_EXIT_TAG, selectedMainExit.getSerializedName());
        }
        addClosedConnectorTags(tags, sourcePiece, closedOptional);
        return new MKWorkspacePieceDefinition(
                UUID.nameUUIDFromBytes((workspace.id() + ":" + pieceName).getBytes(StandardCharsets.UTF_8)),
                sourcePiece.workspaceId(),
                pieceName,
                sourcePiece.roleId(),
                sourcePiece.variantIndex(),
                sourcePiece.effectiveDimensions(),
                sourcePiece.shellMargin(),
                activeConnectors,
                sourcePiece.worldOrigin(),
                sourcePiece.exportBounds(),
                sourcePiece.previewBounds(),
                sourcePiece.structureBlockPos(),
                sourcePiece.signPos(),
                sourcePiece.markerPositions(),
                sourcePiece.generatedStairPositions(),
                tags
        );
    }

    private static List<MKWorkspaceConnectorDefinition> activeConnectors(
            MKWorkspacePieceDefinition sourcePiece,
            List<MKWorkspaceConnectorDefinition> activeOptional) {
        ArrayList<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>();
        for (MKWorkspaceConnectorDefinition connector : sourcePiece.connectors()) {
            if (isLinkCandidate(connector)) {
                continue;
            }
            if (!isOptionalBranch(sourcePiece, connector) || activeOptional.contains(connector)) {
                connectors.add(connector);
            }
        }
        return List.copyOf(connectors);
    }

    private static List<MKWorkspaceConnectorDefinition> activeRandomizedMainConnectors(
            MKWorkspacePieceDefinition sourcePiece,
            MKWorkspaceConnectorDefinition selectedMain,
            List<MKWorkspaceConnectorDefinition> activeOptional,
            MKWorkspaceConnectorDefinition mainTemplate,
            MKWorkspaceConnectorDefinition branchTemplate) {
        ArrayList<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>();
        for (MKWorkspaceConnectorDefinition connector : sourcePiece.connectors()) {
            if (!isRandomMainCandidate(sourcePiece, connector) && !isLinkCandidate(connector)) {
                connectors.add(connector);
            }
        }
        connectors.add(connectorWithTemplate(selectedMain, mainTemplate));
        for (MKWorkspaceConnectorDefinition connector : activeOptional) {
            connectors.add(connectorWithTemplate(connector, branchTemplate));
        }
        return List.copyOf(connectors);
    }

    private static MKWorkspaceConnectorDefinition connectorWithTemplate(MKWorkspaceConnectorDefinition source,
                                                                        MKWorkspaceConnectorDefinition template) {
        return new MKWorkspaceConnectorDefinition(
                template.role(),
                source.facing(),
                source.relativePos(),
                source.openingWidth(),
                source.openingHeight(),
                source.lateralOffset(),
                source.verticalOffset(),
                template.jigsawName(),
                template.jigsawTarget(),
                template.targetPool(),
                template.incomingPool()
        );
    }

    private static List<MKWorkspaceConnectorDefinition> optionalBranchConnectors(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .filter(connector -> isOptionalBranch(piece, connector))
                .sorted(Comparator.comparing(connector -> connector.facing().getSerializedName()))
                .toList();
    }

    private static List<MKWorkspaceConnectorDefinition> linkCandidateConnectors(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .filter(MKFloorMaskVariantExporter::isLinkCandidate)
                .sorted(Comparator.comparing(connector -> connector.facing().getSerializedName()))
                .toList();
    }

    private static boolean isLinkCandidate(MKWorkspaceConnectorDefinition connector) {
        return connector.role() == MKConnectorRole.LINK_CANDIDATE &&
                connector.facing().getAxis().isHorizontal();
    }

    private static boolean isOptionalBranch(MKWorkspacePieceDefinition piece,
                                            MKWorkspaceConnectorDefinition connector) {
        MKFloorRoomKind kind = floorKind(piece);
        return allowsOptionalBranchExits(kind) &&
                connector.role() == MKConnectorRole.BRANCH &&
                connector.facing().getAxis().isHorizontal() &&
                connector.facing() != Direction.SOUTH;
    }

    private static boolean randomizesMainExit(MKWorkspacePieceDefinition piece) {
        return floorKind(piece) == MKFloorRoomKind.MAIN_ROOM &&
                Boolean.parseBoolean(piece.tags().getOrDefault(FLOOR_RANDOMIZE_MAIN_EXIT_TAG, "false")) &&
                randomMainExitCandidates(piece).size() > 1;
    }

    private static List<MKWorkspaceConnectorDefinition> randomMainExitCandidates(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .filter(connector -> isRandomMainCandidate(piece, connector))
                .sorted(Comparator.comparing(connector -> connector.facing().getSerializedName()))
                .toList();
    }

    private static boolean isRandomMainCandidate(MKWorkspacePieceDefinition piece,
                                                 MKWorkspaceConnectorDefinition connector) {
        return connector.facing().getAxis().isHorizontal() &&
                connector.facing() != Direction.SOUTH &&
                (connector.role() == MKConnectorRole.MAIN_BACK || isOptionalBranch(piece, connector));
    }

    private static MKWorkspaceConnectorDefinition mainExitConnector(MKWorkspacePieceDefinition piece) {
        return piece.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.MAIN_BACK &&
                        connector.facing().getAxis().isHorizontal())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("randomized main exit room requires a main exit connector"));
    }

    private static MKFloorRoomKind floorKind(MKWorkspacePieceDefinition piece) {
        return MKFloorRoomKind.valueOf(piece.tags()
                .getOrDefault("workspace_floor_room_kind", MKFloorRoomKind.MAIN_ROOM.getSerializedName())
                .toUpperCase());
    }

    private static boolean allowsOptionalBranchExits(MKFloorRoomKind kind) {
        return kind != MKFloorRoomKind.BRANCH_CAP &&
                kind != MKFloorRoomKind.MAIN_CAP;
    }

    private static String maskName(List<MKWorkspaceConnectorDefinition> activeOptional) {
        StringBuilder mask = new StringBuilder();
        activeOptional.stream()
                .map(MKWorkspaceConnectorDefinition::facing)
                .sorted(Comparator.comparing(Direction::getSerializedName))
                .forEach(direction -> mask.append(direction.getSerializedName().charAt(0)));
        return mask.isEmpty() ? "none" : mask.toString();
    }

    private static int maskWeight(String maskName, Map<String, String> tags) {
        float sprawl = parseFloat(tags.get("workspace_floor_sprawl"), 0.5f);
        int activeCount = "none".equals(maskName) ? 0 : maskName.length();
        if (activeCount == 0) {
            return Math.max(1, Math.round((1.0f - sprawl) * 8.0f) + 1);
        }
        return Math.max(1, Math.round(1.0f + sprawl * activeCount * 4.0f));
    }

    public static ResourceLocation maskPool(ResourceLocation basePool, String maskName) {
        return MKFloorMaskPools.maskPool(basePool, maskName);
    }

    private static void addClosedConnectorTags(Map<String, String> tags,
                                               MKWorkspacePieceDefinition sourcePiece,
                                               List<MKWorkspaceConnectorDefinition> closedOptional) {
        tags.put(CLOSED_CONNECTOR_COUNT_TAG, Integer.toString(closedOptional.size()));
        for (int i = 0; i < closedOptional.size(); i++) {
            MKWorkspaceConnectorDefinition connector = closedOptional.get(i);
            String prefix = CLOSED_CONNECTOR_PREFIX + i + "_";
            tags.put(prefix + "role", connector.role().getSerializedName());
            tags.put(prefix + "facing", connector.facing().getSerializedName());
            tags.put(prefix + "x", Integer.toString(connector.relativePos().getX()));
            tags.put(prefix + "y", Integer.toString(connector.relativePos().getY()));
            tags.put(prefix + "z", Integer.toString(connector.relativePos().getZ()));
            tags.put(prefix + "opening_width", Integer.toString(connector.openingWidth()));
            tags.put(prefix + "opening_height", Integer.toString(connector.openingHeight()));
            tags.put(prefix + "closure_depth", Integer.toString(MKFloorConnectorPatch.closureDepth(sourcePiece, connector)));
            tags.put(prefix + "lateral_offset", Integer.toString(connector.lateralOffset()));
            tags.put(prefix + "vertical_offset", Integer.toString(connector.verticalOffset()));
        }
    }

    private static float parseFloat(String value, float fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(0.0f, Math.min(1.0f, Float.parseFloat(value)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
