package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepCourtyardSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepPlannerSettings;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.Optional;

public class MKWalledKeepSizingCalculator {
    private static final String PERIMETER_ROOT_SLOT = "keep.perimeter";
    private static final int DEFAULT_COURTYARD_CLEARANCE = 5;
    private static final int TERRAIN_ADAPTATION_PADDING = 12;
    private static final int JIGSAW_MAX_DISTANCE_FROM_CENTER = 128;
    private static final int DEFAULT_GATE_WIDTH = 15;
    private static final int DEFAULT_GATE_LENGTH = 5;
    private static final int MIN_RECOMMENDED_WALL_UNIT_SPAN = 3;
    private static final int MAX_RECOMMENDED_WALL_UNIT_SPAN = 45;
    private static final int WALL_RECOMMENDATION_PIECE_WEIGHT = 6;
    private static final int WALL_RECOMMENDATION_EXCESS_WEIGHT = 5;
    private static final int WALL_RECOMMENDATION_IMBALANCE_WEIGHT = 2;

    public MKWalledKeepSizingReport calculate(MKStructureWorkspace workspace) {
        MKWalledKeepCourtyardSettings courtyardSettings = keepSettings(workspace).courtyardSettings();
        MKWorkspaceLinearRunFamilyDefinition wallFamily = perimeterFamilyForSide(workspace, "keep.perimeter.south")
                .or(() -> perimeterFamilyForSide(workspace, "keep.perimeter.north"))
                .or(() -> perimeterFamilyForSide(workspace, "keep.perimeter.west"))
                .or(() -> perimeterFamilyForSide(workspace, "keep.perimeter.east"))
                .orElseGet(() -> fallbackWallFamily(workspace));
        MKWorkspaceLinearRunFamilyDefinition pathFamily = courtyardPathFamily(workspace);
        MKWorkspaceLinearRunFamilyDefinition entryFamily = entryApproachFamily(workspace).orElse(pathFamily);
        MKWorkspaceRoomFamilyDefinition gateFamily = gateFamily(workspace).orElse(null);

        int horizontalRequiredSpan = horizontalPerimeterSpan(workspace, pathFamily);
        int verticalRequiredSpan = verticalPerimeterSpan(workspace, pathFamily, entryFamily);
        int horizontalSegments = segmentCountForSpan(wallFamily, horizontalRequiredSpan);
        int verticalSegments = verticalSegmentCountForSpan(workspace, wallFamily, verticalRequiredSpan);
        int frontBranchSegments = frontBranchSegmentsForSpan(wallFamily, horizontalRequiredSpan);
        int backWallSegments = frontBranchSegments * 2 + 1;
        int northWestSegments = Math.max(1, (int) Math.ceil(backWallSegments / 2.0));
        int northEastSegments = Math.max(0, backWallSegments - northWestSegments);

        int wallLength = Math.max(1, wallFamily.length());
        int frontSpan = (frontBranchSegments * 2 + 1) * wallLength;
        int backSpan = (northWestSegments + northEastSegments) * wallLength;
        int sideSpan = verticalSegments * wallLength;
        int realizedHorizontalSpan = Math.max(horizontalRequiredSpan, Math.max(frontSpan, backSpan));
        int realizedVerticalSpan = Math.max(verticalRequiredSpan, sideSpan);
        int freeHorizontal = Math.max(0, (realizedHorizontalSpan - centerWidth(workspace)) / 2);
        int freeVertical = Math.max(0, (realizedVerticalSpan - centerLength(workspace)) / 2);
        int socketMax = largestOddAtMost(Math.min(freeHorizontal, freeVertical) -
                courtyardSettings.courtyardSocketClearance());

        ResolvedOpening pathOpening = resolveOpeningProfile(workspace, pathFamily.openingProfileId());
        ResolvedOpening entryOpening = resolveOpeningProfile(workspace, entryFamily.openingProfileId());
        int laneInset = courtyardPathLaneCenterInset(workspace, pathOpening);
        int pathSize = courtyardPathSize(workspace, pathFamily, laneInset);
        int entryLength = effectiveEntryApproachLength(workspace, entryFamily.length(), entryOpening, pathFamily,
                pathOpening, pathSize);
        int entryWidth = effectiveEntryApproachWidth(workspace, entryFamily, pathFamily, pathOpening, pathSize);

        int padding = 2 * (workspace.shellMargin() + workspace.exteriorAirMargin());
        int centerExportWidth = exportedSpan(centerWidth(workspace), padding);
        int centerExportLength = exportedSpan(centerLength(workspace), padding);
        int entryExportWidth = exportedSpan(entryWidth, padding);
        int gateExportWidth = exportedSpan(gateFamily == null ? DEFAULT_GATE_WIDTH : gateFamily.roomWidth(), padding);
        int gateExportLength = exportedSpan(gateFamily == null ? DEFAULT_GATE_LENGTH : gateFamily.roomLength(), padding);
        int wallAdvance = Math.max(1, exportedSpan(wallLength, padding) - 1);
        int cornerExportWidth = exportedSpan(cornerWidth(workspace), padding);
        int entryAdvance = Math.max(1, exportedSpan(entryLength, padding) - 1);
        int courtyardHorizontalRadius = courtyardSettings.courtyardContentEnabled() &&
                courtyardSettings.courtyardSocketGenerationEnabled() ?
                courtyardHorizontalCollisionHalfSpan(workspace, courtyardSettings, exportedSpan(pathSize, padding)) : 0;

        int eastWestDistance = Math.max(centerExportWidth / 2,
                Math.max(entryExportWidth / 2, Math.max(courtyardHorizontalRadius,
                        gateExportWidth / 2 + (frontBranchSegments * wallAdvance) + (cornerExportWidth / 2))));
        int southDistance = Math.max(centerExportLength / 2,
                (centerExportLength / 2) + entryAdvance + (gateExportLength / 2));
        int northDistance = Math.max(centerExportLength / 2,
                Math.max(0, (verticalSegments * wallAdvance) + (cornerLength(workspace) + padding) / 2 -
                        southDistance));
        int requiredRadius = Math.max(Math.max(northDistance, southDistance), eastWestDistance);
        TerrainAdjustment terrainAdjustment = workspace.topologyProfile().terrainAdjustment();
        int maxDistance = maxDistanceFromCenter(terrainAdjustment);
        WallUnitRecommendation recommendedWallUnit = recommendWallUnitSpan(workspace, wallFamily,
                gateFamily, horizontalRequiredSpan, verticalRequiredSpan, centerExportWidth, centerExportLength,
                cornerExportWidth, entryAdvance, padding, maxDistance);

        return new MKWalledKeepSizingReport(
                northDistance,
                southDistance,
                eastWestDistance,
                eastWestDistance,
                eastWestDistance * 2 + 1,
                northDistance + southDistance + 1,
                requiredRadius,
                maxDistance,
                maxDistance - requiredRadius,
                terrainAdjustment,
                socketMax,
                courtyardSettings.courtyardContentTemplateSize(),
                freeHorizontal,
                freeVertical,
                horizontalRequiredSpan,
                verticalRequiredSpan,
                realizedHorizontalSpan,
                realizedVerticalSpan,
                horizontalSegments,
                verticalSegments,
                frontBranchSegments,
                backWallSegments,
                pathSize,
                entryLength,
                recommendedWallUnit.wallUnitSpan(),
                recommendedWallUnit.excessHorizontal(),
                recommendedWallUnit.excessVertical(),
                recommendedWallUnit.wallPieceCount()
        );
    }

    private WallUnitRecommendation recommendWallUnitSpan(MKStructureWorkspace workspace,
                                                         MKWorkspaceLinearRunFamilyDefinition wallFamily,
                                                         MKWorkspaceRoomFamilyDefinition gateFamily,
                                                         int horizontalRequiredSpan,
                                                         int verticalRequiredSpan,
                                                         int centerExportWidth,
                                                         int centerExportLength,
                                                         int cornerExportWidth,
                                                         int entryAdvance,
                                                         int padding,
                                                         int maxDistance) {
        WallUnitRecommendation best = null;
        int fallbackSpan = Math.max(MIN_RECOMMENDED_WALL_UNIT_SPAN, wallFamily.length());
        for (int candidate = MIN_RECOMMENDED_WALL_UNIT_SPAN; candidate <= MAX_RECOMMENDED_WALL_UNIT_SPAN; candidate += 2) {
            WallUnitRecommendation recommendation = evaluateWallUnitSpan(workspace, wallFamily, gateFamily,
                    candidate, horizontalRequiredSpan, verticalRequiredSpan, centerExportWidth, centerExportLength,
                    cornerExportWidth, entryAdvance, padding, maxDistance);
            if (recommendation.fitsJigsawCap() && (best == null || recommendation.score() < best.score())) {
                best = recommendation;
            }
        }
        if (best != null) {
            return best;
        }
        return evaluateWallUnitSpan(workspace, wallFamily, gateFamily, fallbackSpan, horizontalRequiredSpan,
                verticalRequiredSpan, centerExportWidth, centerExportLength, cornerExportWidth, entryAdvance, padding,
                maxDistance);
    }

    private WallUnitRecommendation evaluateWallUnitSpan(MKStructureWorkspace workspace,
                                                        MKWorkspaceLinearRunFamilyDefinition wallFamily,
                                                        MKWorkspaceRoomFamilyDefinition gateFamily,
                                                        int candidateWallUnitSpan,
                                                        int horizontalRequiredSpan,
                                                        int verticalRequiredSpan,
                                                        int centerExportWidth,
                                                        int centerExportLength,
                                                        int cornerExportWidth,
                                                        int entryAdvance,
                                                        int padding,
                                                        int maxDistance) {
        int wallUnitSpan = smallestOddAtLeast(Math.max(MIN_RECOMMENDED_WALL_UNIT_SPAN, candidateWallUnitSpan));
        int horizontalSegments = Math.max(1, (int) Math.ceil(horizontalRequiredSpan / (double) wallUnitSpan));
        int verticalSegments = Math.max(1, (int) Math.ceil(verticalRequiredSpan / (double) wallUnitSpan)) +
                courtyardRearWallBufferSegments(workspace);
        int frontBranchSegments = Math.max(1, (int) Math.ceil(horizontalSegments / 2.0));
        int backWallSegments = frontBranchSegments * 2 + 1;
        int horizontalUnits = frontBranchSegments * 2 + 1;
        int realizedHorizontalSpan = Math.max(horizontalRequiredSpan, horizontalUnits * wallUnitSpan);
        int realizedVerticalSpan = Math.max(verticalRequiredSpan, verticalSegments * wallUnitSpan);
        int excessHorizontal = Math.max(0, realizedHorizontalSpan - horizontalRequiredSpan);
        int excessVertical = Math.max(0, realizedVerticalSpan - verticalRequiredSpan);
        int wallPieceCount = (frontBranchSegments * 2) + backWallSegments + (verticalSegments * 2);

        int gateExportWidth = exportedSpan(wallUnitSpan, padding);
        int gateExportLength = exportedSpan(gateFamily == null ? wallFamily.interiorWidth() : gateFamily.roomLength(),
                padding);
        int wallAdvance = Math.max(1, exportedSpan(wallUnitSpan, padding) - 1);
        int eastWestDistance = Math.max(centerExportWidth / 2,
                gateExportWidth / 2 + (frontBranchSegments * wallAdvance) + (cornerExportWidth / 2));
        int southDistance = Math.max(centerExportLength / 2,
                (centerExportLength / 2) + entryAdvance + (gateExportLength / 2));
        int northDistance = Math.max(centerExportLength / 2,
                Math.max(0, (verticalSegments * wallAdvance) + (cornerLength(workspace) + padding) / 2 -
                        southDistance));
        int requiredRadius = Math.max(Math.max(northDistance, southDistance), eastWestDistance);

        int score = (wallPieceCount * WALL_RECOMMENDATION_PIECE_WEIGHT) +
                ((excessHorizontal + excessVertical) * WALL_RECOMMENDATION_EXCESS_WEIGHT) +
                (Math.max(excessHorizontal, excessVertical) * WALL_RECOMMENDATION_IMBALANCE_WEIGHT);
        return new WallUnitRecommendation(wallUnitSpan, excessHorizontal, excessVertical, wallPieceCount,
                requiredRadius <= maxDistance, score);
    }

    public static int maxDistanceFromCenter(TerrainAdjustment terrainAdjustment) {
        TerrainAdjustment adjustment = terrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : terrainAdjustment;
        return adjustment == TerrainAdjustment.NONE ?
                JIGSAW_MAX_DISTANCE_FROM_CENTER :
                JIGSAW_MAX_DISTANCE_FROM_CENTER - TERRAIN_ADAPTATION_PADDING;
    }

    private Optional<MKWorkspaceLinearRunFamilyDefinition> perimeterFamilyForSide(MKStructureWorkspace workspace,
                                                                                  String sideSlotId) {
        Optional<MKWorkspaceLinearRunFamilyDefinition> rootFamily = workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(PERIMETER_ROOT_SLOT))
                .findFirst();
        Optional<MKWorkspaceLinearRunFamilyDefinition> sideFamily = workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(sideSlotId))
                .findFirst();
        return sideFamily.or(() -> rootFamily);
    }

    private MKWorkspaceLinearRunFamilyDefinition fallbackWallFamily(MKStructureWorkspace workspace) {
        return new MKWorkspaceLinearRunFamilyDefinition("keep_wall_segment", PERIMETER_ROOT_SLOT,
                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind.DEFENSIVE_WALL,
                "branch_opening", MKWalledKeepWorkspacePlanner.DEFAULT_WALL_SEGMENT_LENGTH,
                3, 7, 0, false, true,
                com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection.RIGID,
                java.util.List.of(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape.STRAIGHT),
                0, com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy.none(), null);
    }

    private Optional<MKWorkspaceLinearRunFamilyDefinition> entryApproachFamily(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals("keep.entry_approach.main"))
                .findFirst();
    }

    private MKWorkspaceLinearRunFamilyDefinition courtyardPathFamily(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearrunMatchesAny(linearRun, "keep.walkway.west", "keep.walkway.east"))
                .findFirst()
                .orElseGet(() -> entryApproachFamily(workspace).orElseGet(() -> fallbackWallFamily(workspace)));
    }

    private boolean linearrunMatchesAny(MKWorkspaceLinearRunFamilyDefinition linearRun, String first, String second) {
        return linearRun.topologySlotId().equals(first) || linearRun.topologySlotId().equals(second);
    }

    private Optional<MKWorkspaceRoomFamilyDefinition> gateFamily(MKStructureWorkspace workspace) {
        return workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().equals("keep.gate.main"))
                .findFirst();
    }

    private ResolvedOpening resolveOpeningProfile(MKStructureWorkspace workspace, String openingProfileId) {
        return workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(openingProfileId))
                .findFirst()
                .map(this::resolvedOpening)
                .orElseGet(() -> workspace.openingProfiles().stream()
                        .findFirst()
                        .map(this::resolvedOpening)
                        .orElse(new ResolvedOpening(3, 3)));
    }

    private ResolvedOpening resolvedOpening(MKHorizontalOpeningProfile profile) {
        return new ResolvedOpening(profile.openingWidth(), profile.openingHeight());
    }

    private int horizontalPerimeterSpan(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition pathFamily) {
        int baseSpan = cornerWidth(workspace) + DEFAULT_COURTYARD_CLEARANCE + centerWidth(workspace) +
                DEFAULT_COURTYARD_CLEARANCE + cornerWidth(workspace);
        return Math.max(baseSpan, courtyardRequiredHorizontalSpan(workspace, pathFamily));
    }

    private int courtyardRequiredHorizontalSpan(MKStructureWorkspace workspace,
                                                MKWorkspaceLinearRunFamilyDefinition pathFamily) {
        MKWalledKeepCourtyardSettings settings = keepSettings(workspace).courtyardSettings();
        if (!settings.courtyardContentEnabled() || !settings.courtyardSocketGenerationEnabled()) {
            return centerWidth(workspace);
        }
        ResolvedOpening pathOpening = resolveOpeningProfile(workspace, pathFamily.openingProfileId());
        int laneInset = courtyardPathLaneCenterInset(workspace, pathOpening);
        int pathSize = courtyardPathSize(workspace, pathFamily, laneInset);
        int pathExportSpan = exportedSpan(pathSize, 2 * (workspace.shellMargin() + workspace.exteriorAirMargin()));
        int halfSpan = courtyardHorizontalCollisionHalfSpan(workspace, settings, pathExportSpan);
        return smallestOddAtLeast((2 * halfSpan) + 1);
    }

    private int verticalPerimeterSpan(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition pathFamily,
                                      MKWorkspaceLinearRunFamilyDefinition entryFamily) {
        int baseSpan = cornerLength(workspace) + DEFAULT_COURTYARD_CLEARANCE + centerLength(workspace) +
                DEFAULT_COURTYARD_CLEARANCE + cornerLength(workspace);
        ResolvedOpening pathOpening = resolveOpeningProfile(workspace, pathFamily.openingProfileId());
        ResolvedOpening entryOpening = resolveOpeningProfile(workspace, entryFamily.openingProfileId());
        int laneInset = courtyardPathLaneCenterInset(workspace, pathOpening);
        int pathSize = courtyardPathSize(workspace, pathFamily, laneInset);
        int entryLength = effectiveEntryApproachLength(workspace, entryFamily.length(), entryOpening, pathFamily,
                pathOpening, pathSize);
        int northBand = DEFAULT_COURTYARD_CLEARANCE + cornerLength(workspace);
        MKWalledKeepCourtyardSettings settings = keepSettings(workspace).courtyardSettings();
        if (settings.courtyardContentEnabled() && settings.courtyardSocketGenerationEnabled()) {
            northBand = Math.max(northBand, courtyardBandSize(workspace, settings, laneInset, pathOpening));
        }
        int entryDrivenSpan = entryLength + (centerLength(workspace) / 2) + northBand;
        return Math.max(baseSpan, smallestOddAtLeast(entryDrivenSpan));
    }

    private int segmentCountForSpan(MKWorkspaceLinearRunFamilyDefinition family, int span) {
        return Math.max(1, (int) Math.ceil(span / (double) Math.max(1, family.length())));
    }

    private int frontBranchSegmentsForSpan(MKWorkspaceLinearRunFamilyDefinition family, int span) {
        int totalWallUnits = segmentCountForSpan(family, span);
        return Math.max(1, (int) Math.ceil(Math.max(0, totalWallUnits - 1) / 2.0));
    }

    private int verticalSegmentCountForSpan(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition family,
                                            int span) {
        return segmentCountForSpan(family, span) + courtyardRearWallBufferSegments(workspace);
    }

    private int courtyardRearWallBufferSegments(MKStructureWorkspace workspace) {
        MKWalledKeepCourtyardSettings settings = keepSettings(workspace).courtyardSettings();
        return settings.courtyardContentEnabled() && settings.courtyardSocketGenerationEnabled() ? 1 : 0;
    }

    private int courtyardPathLaneCenterInset(MKStructureWorkspace workspace, ResolvedOpening opening) {
        return workspace.shellMargin() + workspace.exteriorAirMargin() +
                keepSettings(workspace).courtyardSettings().courtyardPathInnerMargin() +
                opening.openingWidth() / 2;
    }

    private int courtyardBandSize(MKStructureWorkspace workspace, MKWalledKeepCourtyardSettings settings,
                                  int laneInset, ResolvedOpening opening) {
        return laneInset + walkwayHalfWidth(opening) + settings.courtyardSocketClearance() +
                courtyardContentCollisionSpan(workspace, settings);
    }

    private int walkwayHalfWidth(ResolvedOpening opening) {
        return Math.max(1, opening.openingWidth() / 2);
    }

    private int courtyardContentCollisionSpan(MKStructureWorkspace workspace, MKWalledKeepCourtyardSettings settings) {
        return exportedSpan(settings.courtyardContentTemplateSize(),
                2 * (workspace.shellMargin() + workspace.exteriorAirMargin()));
    }

    private int courtyardHorizontalCollisionHalfSpan(MKStructureWorkspace workspace,
                                                     MKWalledKeepCourtyardSettings settings,
                                                     int pathExportSpan) {
        int pathHalf = pathExportSpan / 2;
        int contentSpan = settings.courtyardContentEnabled() && settings.courtyardSocketGenerationEnabled() ?
                courtyardContentCollisionSpan(workspace, settings) : 0;
        return pathExportSpan + pathHalf + contentSpan;
    }

    private int courtyardPathSize(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition family,
                                  int laneInset) {
        int centerSpan = Math.max(centerWidth(workspace), centerLength(workspace));
        int calculated = centerSpan + (2 * laneInset);
        return smallestOddAtLeast(Math.max(calculated, Math.max(family.length(), family.interiorWidth())));
    }

    private int effectiveEntryApproachLength(MKStructureWorkspace workspace, int requestedLength,
                                             ResolvedOpening entryOpening,
                                             MKWorkspaceLinearRunFamilyDefinition pathFamily,
                                             ResolvedOpening pathOpening, int pathSize) {
        return smallestOddAtLeast(Math.max(requestedLength,
                pathSize + workspace.shellMargin() + workspace.exteriorAirMargin() +
                        Math.max(entryOpening.openingWidth(), pathOpening.openingWidth())));
    }

    private int effectiveEntryApproachWidth(MKStructureWorkspace workspace,
                                            MKWorkspaceLinearRunFamilyDefinition entryFamily,
                                            MKWorkspaceLinearRunFamilyDefinition pathFamily,
                                            ResolvedOpening pathOpening,
                                            int pathSize) {
        MKWalledKeepCourtyardSettings settings = keepSettings(workspace).courtyardSettings();
        if (!settings.courtyardContentEnabled() || !settings.courtyardSocketGenerationEnabled()) {
            return entryFamily.interiorWidth();
        }
        return Math.max(entryFamily.interiorWidth(), pathSize);
    }

    private int centerWidth(MKStructureWorkspace workspace) {
        return verticalStackSettings(workspace, "keep.center").width();
    }

    private int centerLength(MKStructureWorkspace workspace) {
        return verticalStackSettings(workspace, "keep.center").length();
    }

    private int cornerWidth(MKStructureWorkspace workspace) {
        return verticalStackSettings(workspace, cornerStackId(workspace)).width();
    }

    private int cornerLength(MKStructureWorkspace workspace) {
        return verticalStackSettings(workspace, cornerStackId(workspace)).length();
    }

    private String cornerStackId(MKStructureWorkspace workspace) {
        return keepSettings(workspace).anySharedCornerTower() ? "keep.corner.shared" : "keep.corner.north_west";
    }

    private MKWorkspaceVerticalStackSettings verticalStackSettings(MKStructureWorkspace workspace, String stackId) {
        return workspace.topologyProfile().verticalStackSettingsOrDefault(stackId);
    }

    private MKWalledKeepPlannerSettings keepSettings(MKStructureWorkspace workspace) {
        return MKWalledKeepPlannerSettings.from(workspace.topologyProfile());
    }

    private int exportedSpan(int authoredSpan, int padding) {
        return Math.max(1, authoredSpan + padding);
    }

    private int largestOddAtMost(int value) {
        if (value < 1) {
            return 0;
        }
        return value % 2 == 0 ? value - 1 : value;
    }

    private int smallestOddAtLeast(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }

    private record ResolvedOpening(int openingWidth, int openingHeight) {
    }

    private record WallUnitRecommendation(int wallUnitSpan,
                                          int excessHorizontal,
                                          int excessVertical,
                                          int wallPieceCount,
                                          boolean fitsJigsawCap,
                                          int score) {
    }
}
