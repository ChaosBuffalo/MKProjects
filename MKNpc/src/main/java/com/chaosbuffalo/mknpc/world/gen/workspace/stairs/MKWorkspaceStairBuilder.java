package com.chaosbuffalo.mknpc.world.gen.workspace.stairs;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKResolvedVerticalAccessProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKWorkspaceStairBuilder {
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();

    public MKWorkspacePieceDefinition generateForPiece(ServerLevel level, MKStructureWorkspace workspace,
                                                       MKWorkspacePieceDefinition piece) {
        return generateForPiece(level, workspace, piece, workspace.stairConfigForPiece(piece));
    }

    public MKWorkspacePieceDefinition generateForPiece(ServerLevel level, MKStructureWorkspace workspace,
                                                       MKWorkspacePieceDefinition piece,
                                                       MKWorkspaceStairAuthoringConfig stairConfig) {
        MaterializedStairConfig effectiveStairConfig = withPaletteMaterials(stairConfig,
                paletteResolver.resolvePiece(workspace, piece).orElse(workspace.palette()));
        if (!isEligible(piece) || effectiveStairConfig.mode() == MKWorkspaceStairMode.NONE) {
            clearGenerated(level, workspace, piece);
            return updateGeneratedState(piece, List.of(), MKWorkspaceStairMode.NONE);
        }

        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = getGenerationGeometry(workspace, piece);
        MKWorkspaceStairMode resolvedMode = resolveMode(effectiveStairConfig, geometry);
        if (resolvedMode == MKWorkspaceStairMode.LADDER) {
            return generateLadder(level, piece, geometry, effectiveStairConfig);
        }
        if (isTerminalTop(piece)) {
            return generateTopCapContinuation(level, piece, geometry, effectiveStairConfig, resolvedMode);
        }
        MaterializedStairConfig resolvedConfig = normalizeConfigForMode(effectiveStairConfig, resolvedMode);
        int interiorHeight = getProfileInteriorHeight(piece);
        return MKResolvedVerticalAccessProfile.resolve(resolvedConfig.authoringConfig(), geometry.width(), geometry.length(), interiorHeight)
                .map(resolvedProfile -> switch (resolvedProfile.riseStrategy()) {
                    case SLAB -> generateSlabSpiral(level, workspace, piece, geometry, effectiveStairConfig,
                            resolvedProfile.asUniformProfile(), resolvedProfile);
                    case STAIR -> generateStairSpiral(level, workspace, piece, geometry, effectiveStairConfig,
                            resolvedProfile.asUniformProfile(), resolvedProfile);
                    case MIXED -> generateMixedSpiral(level, workspace, piece, geometry, effectiveStairConfig, resolvedProfile);
                })
                .orElseGet(() -> updateGeneratedState(piece, piece.generatedStairPositions(), MKWorkspaceStairMode.NONE));
    }

    public MKWorkspacePieceDefinition clearForPiece(ServerLevel level, MKWorkspacePieceDefinition piece) {
        clearGenerated(level, piece);
        return updateGeneratedState(piece, List.of(), MKWorkspaceStairMode.NONE);
    }

    private MKWorkspacePieceDefinition generateLadder(ServerLevel level,
                                                      MKWorkspacePieceDefinition piece,
                                                      MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry,
                                                      MaterializedStairConfig stairConfig) {
        List<BlockPos> generated = new ArrayList<>();
        BlockState ladderState = resolveLadderState(stairConfig.ladderBlock(),
                MKWorkspaceVerticalAccessGeometry.getPreferredLadderFacing(geometry));
        BlockPos ladderBase = getLadderBase(geometry);
        int editableMinY = getEditableMinY(piece, geometry);
        clearShaftFootprint(level, geometry, editableMinY);
        for (int y = editableMinY; y <= geometry.interiorMaxY(); y++) {
            BlockPos pos = new BlockPos(ladderBase.getX(), y, ladderBase.getZ());
            writeGeneratedBlock(level, pos, ladderState);
            generated.add(pos);
        }
        return updateGeneratedState(piece, generated, MKWorkspaceStairMode.LADDER);
    }

    private MKWorkspacePieceDefinition generateTopCapContinuation(ServerLevel level, MKWorkspacePieceDefinition piece,
                                                                  MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry,
                                                                  MaterializedStairConfig stairConfig,
                                                                  MKWorkspaceStairMode resolvedMode) {
        LinkedHashSet<BlockPos> generated = new LinkedHashSet<>();
        LinkedHashMap<BlockPos, BlockState> planned = new LinkedHashMap<>();
        clearShaftFootprint(level, geometry, geometry.interiorMinY());
        MaterializedStairConfig resolvedConfig = normalizeConfigForMode(stairConfig, resolvedMode);
        MKResolvedVerticalAccessProfile resolvedProfile = MKResolvedVerticalAccessProfile.resolve(resolvedConfig.authoringConfig(),
                geometry.width(), geometry.length(), getProfileInteriorHeight(piece)).orElse(null);
        int stairWidth = resolvedProfile != null ? resolvedProfile.stairWidth() : Math.max(1, resolvedConfig.stairWidth());
        int flatRunLength = resolvedProfile != null ? resolvedProfile.flatRunLength() : 0;
        BoundingBox centerlineBounds = getCenterlineBounds(geometry.shaftBounds(), stairWidth);
        List<BlockPos> perimeter = getPerimeterClockwise(centerlineBounds, geometry.interiorMinY());
        if (perimeter.isEmpty()) {
            return updateGeneratedState(piece, List.of(), MKWorkspaceStairMode.NONE);
        }
        int startIndex = MKWorkspaceVerticalAccessGeometry.findClosestIndex(perimeter,
                clampToBounds(MKWorkspaceVerticalAccessGeometry.getPreferredStart(geometry), centerlineBounds, geometry.interiorMinY()));
        planEntryLanding(planned, perimeter, startIndex, geometry.interiorMinY(), centerlineBounds,
                geometry.shaftBounds(), stairWidth, resolveLandingFillState(stairConfig), generated);
        Direction previousMovement = null;
        int halfHeight = 0;
        int step = 0;
        for (MKResolvedVerticalAccessProfile.RiseStepKind kind : getTopCapContinuationPattern(resolvedConfig,
                resolvedProfile)) {
            for (int segmentStep = 0; segmentStep <= flatRunLength; segmentStep++) {
                BlockPos base = perimeter.get((startIndex + step) % perimeter.size());
                BlockPos next = perimeter.get((startIndex + step + 1) % perimeter.size());
                Direction movement = getHorizontalDirection(base, next);
                boolean isRiseStep = segmentStep == 0;
                BlockPos pos = new BlockPos(base.getX(), geometry.interiorMinY(), base.getZ());
                BlockState state = resolveTopCapContinuationState(stairConfig, kind, movement, isRiseStep);
                Direction turnPreviousMovement = getTurnPreviousMovement(perimeter, startIndex + step, movement);
                if (turnPreviousMovement != null && isRiseStep &&
                        kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR) {
                    planTurnStairBand(planned, pos, turnPreviousMovement, movement, stairWidth, geometry.shaftBounds(),
                            state, generated);
                } else if (isRiseStep && kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR) {
                    planStairBand(planned, pos, state, centerlineBounds, geometry.shaftBounds(), stairWidth, generated);
                } else {
                    planGeneratedBand(planned, pos, state, centerlineBounds, geometry.shaftBounds(), stairWidth, generated);
                }
                if (previousMovement != null && movement != null && previousMovement != movement) {
                    planCornerLanding(planned, pos, previousMovement, movement, stairWidth, geometry.shaftBounds(),
                            resolveLandingFillState(stairConfig), generated);
                }
                previousMovement = movement;
                step++;
            }
            halfHeight += kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR ? 2 : 1;
            if (halfHeight >= 2) {
                break;
            }
        }
        flushPlannedBlocks(level, planned);
        if (resolvedProfile != null) {
            return updateGeneratedState(piece, List.copyOf(generated), modeForRiseStrategy(resolvedProfile.riseStrategy()),
                    resolvedProfile);
        }
        return updateGeneratedState(piece, List.copyOf(generated), resolvedMode);
    }

    private MKWorkspacePieceDefinition generateStairSpiral(ServerLevel level, MKStructureWorkspace workspace,
                                                           MKWorkspacePieceDefinition piece,
                                                           MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry,
                                                           MaterializedStairConfig stairConfig,
                                                           MKVerticalAccessProfile profile,
                                                           MKResolvedVerticalAccessProfile resolvedProfile) {
        int editableMinY = getEditableMinY(piece, geometry);
        clearShaftFootprint(level, geometry, editableMinY);
        BoundingBox centerlineBounds = getCenterlineBounds(geometry.shaftBounds(), profile.stairWidth());
        List<BlockPos> perimeter = getPerimeterClockwise(centerlineBounds, geometry.interiorMinY());
        if (perimeter.isEmpty()) {
            return updateGeneratedState(piece, List.of(), MKWorkspaceStairMode.NONE);
        }
        int startIndex = MKWorkspaceVerticalAccessGeometry.findClosestIndex(perimeter,
                clampToBounds(MKWorkspaceVerticalAccessGeometry.getPreferredStart(geometry), centerlineBounds, geometry.interiorMinY()));
        LinkedHashSet<BlockPos> generated = new LinkedHashSet<>();
        LinkedHashMap<BlockPos, BlockState> planned = new LinkedHashMap<>();
        int pathSteps = resolvedProfile.pathSteps();
        MKVerticalAccessProfile.BoundaryCompatibility compatibility = resolvedProfile.boundaryCompatibility();
        Direction previousMovement = null;
        for (int step = 0; step < pathSteps; step++) {
            BlockPos base = perimeter.get((startIndex + step) % perimeter.size());
            BlockPos next = perimeter.get((startIndex + step + 1) % perimeter.size());
            Direction movement = getHorizontalDirection(base, next);
            boolean isTurn = previousMovement != null && movement != null && previousMovement != movement;
            Direction ascentDirection = movement;
            Direction facing = ascentDirection == null ? Direction.NORTH : ascentDirection;
            boolean risingStep = step % (profile.flatRunLength() + 1) == 0;
            int y = geometry.interiorMinY() + (step / (profile.flatRunLength() + 1));
            BlockPos stairPos = new BlockPos(base.getX(), y, base.getZ());
            BlockState state = risingStep
                    ? resolveStairState(stairConfig.stairBlock(), facing)
                    : resolveRunFillState(stairConfig);
            Direction turnPreviousMovement = getTurnPreviousMovement(perimeter, startIndex + step, movement);
            if (turnPreviousMovement != null && risingStep) {
                planTurnStairBand(planned, new BlockPos(base.getX(), y, base.getZ()), turnPreviousMovement, movement,
                        profile.stairWidth(), geometry.shaftBounds(), state, generated);
            } else if (risingStep) {
                planStairBand(planned, stairPos, state, centerlineBounds, geometry.shaftBounds(), profile.stairWidth(),
                        generated);
            } else {
                planGeneratedBand(planned, stairPos, state, centerlineBounds, geometry.shaftBounds(), profile.stairWidth(), generated);
            }
            if (isTurn) {
                BlockPos landingAnchor = new BlockPos(base.getX(), y, base.getZ());
                planCornerLanding(planned, landingAnchor, previousMovement, movement, profile.stairWidth(),
                        geometry.shaftBounds(), resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM), generated);
            }
            if (step == 0) {
                BlockPos preLandingPos = perimeter.get(Math.floorMod(startIndex - 2, perimeter.size()));
                BlockPos landingPos = perimeter.get(Math.floorMod(startIndex - 1, perimeter.size()));
                BlockPos landingPosAtY = new BlockPos(landingPos.getX(), geometry.interiorMinY(), landingPos.getZ());
                BlockState landingState = resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM);
                planGeneratedBand(planned, landingPosAtY, landingState,
                        centerlineBounds, geometry.shaftBounds(), profile.stairWidth(), generated);
                Direction landingMovementIn = getHorizontalDirection(preLandingPos, landingPos);
                Direction landingMovementOut = getHorizontalDirection(landingPos, base);
                if (landingMovementIn != null && landingMovementOut != null && landingMovementIn != landingMovementOut) {
                    planCornerLanding(planned, landingPosAtY,
                            landingMovementIn, landingMovementOut, profile.stairWidth(), geometry.shaftBounds(),
                            landingState, generated);
                }
            }
            previousMovement = movement;
        }
        fillCornerGapsWithTopSlabs(planned, geometry.shaftBounds(), geometry.interiorMinY(), geometry.interiorMaxY(),
                resolveRunFillState(stairConfig), generated);
        if (compatibility.status() == MKVerticalAccessProfile.BoundaryStatus.BRIDGEABLE) {
            planBoundaryBridge(planned, perimeter, startIndex, pathSteps, geometry.interiorMaxY(), centerlineBounds,
                    geometry.shaftBounds(), profile.stairWidth(),
                    resolveSlabState(stairConfig.slabBlock(), SlabType.TOP), generated);
        }
        clipBelowMinY(planned, generated, editableMinY);
        flushPlannedBlocks(level, planned);
        return updateGeneratedState(piece, List.copyOf(generated), MKWorkspaceStairMode.STAIR_STAIRS, resolvedProfile);
    }

    private MKWorkspacePieceDefinition generateSlabSpiral(ServerLevel level, MKStructureWorkspace workspace,
                                                          MKWorkspacePieceDefinition piece,
                                                          MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry,
                                                          MaterializedStairConfig stairConfig,
                                                          MKVerticalAccessProfile profile,
                                                          MKResolvedVerticalAccessProfile resolvedProfile) {
        int editableMinY = getEditableMinY(piece, geometry);
        clearShaftFootprint(level, geometry, editableMinY);
        BoundingBox centerlineBounds = getCenterlineBounds(geometry.shaftBounds(), profile.stairWidth());
        List<BlockPos> perimeter = getPerimeterClockwise(centerlineBounds, geometry.interiorMinY());
        if (perimeter.isEmpty()) {
            return updateGeneratedState(piece, List.of(), MKWorkspaceStairMode.NONE);
        }
        int startIndex = MKWorkspaceVerticalAccessGeometry.findClosestIndex(perimeter,
                clampToBounds(MKWorkspaceVerticalAccessGeometry.getPreferredStart(geometry), centerlineBounds, geometry.interiorMinY()));
        int pathSteps = resolvedProfile.pathSteps();
        MKVerticalAccessProfile.BoundaryCompatibility compatibility = resolvedProfile.boundaryCompatibility();
        LinkedHashSet<BlockPos> generated = new LinkedHashSet<>();
        LinkedHashMap<BlockPos, BlockState> planned = new LinkedHashMap<>();
        Direction previousMovement = null;
        for (int step = 0; step < pathSteps; step++) {
            BlockPos base = perimeter.get((startIndex + step) % perimeter.size());
            BlockPos next = perimeter.get((startIndex + step + 1) % perimeter.size());
            Direction movement = getHorizontalDirection(base, next);
            int riseIndex = step / (profile.flatRunLength() + 1);
            int y = geometry.interiorMinY() + (riseIndex / 2);
            SlabType slabType = riseIndex % 2 == 0 ? SlabType.BOTTOM : SlabType.TOP;
            BlockPos slabPos = new BlockPos(base.getX(), y, base.getZ());
            planGeneratedBand(planned, slabPos, resolveSlabState(stairConfig.slabBlock(), slabType),
                    centerlineBounds, geometry.shaftBounds(), profile.stairWidth(), generated);
            if (previousMovement != null && movement != null && previousMovement != movement) {
                planCornerLanding(planned, slabPos, previousMovement, movement, profile.stairWidth(),
                        geometry.shaftBounds(), resolveSlabState(stairConfig.slabBlock(), slabType), generated);
            }
            if (step == 0) {
                BlockPos preLandingPos = perimeter.get(Math.floorMod(startIndex - 2, perimeter.size()));
                BlockPos landingPos = perimeter.get(Math.floorMod(startIndex - 1, perimeter.size()));
                BlockPos landingAtY = new BlockPos(landingPos.getX(), geometry.interiorMinY(), landingPos.getZ());
                BlockState landingState = resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM);
                planGeneratedBand(planned, landingAtY, landingState,
                        centerlineBounds, geometry.shaftBounds(), profile.stairWidth(), generated);
                Direction landingMovementIn = getHorizontalDirection(preLandingPos, landingPos);
                Direction landingMovementOut = getHorizontalDirection(landingPos, base);
                if (landingMovementIn != null && landingMovementOut != null && landingMovementIn != landingMovementOut) {
                    planCornerLanding(planned, landingAtY,
                            landingMovementIn, landingMovementOut, profile.stairWidth(), geometry.shaftBounds(),
                            landingState, generated);
                }
            }
            previousMovement = movement;
        }
        if (compatibility.status() == MKVerticalAccessProfile.BoundaryStatus.BRIDGEABLE) {
            planBoundaryBridge(planned, perimeter, startIndex, pathSteps, geometry.interiorMaxY(), centerlineBounds,
                    geometry.shaftBounds(), profile.stairWidth(),
                    resolveSlabState(stairConfig.slabBlock(), SlabType.TOP), generated);
        }
        clipBelowMinY(planned, generated, editableMinY);
        flushPlannedBlocks(level, planned);
        return updateGeneratedState(piece, List.copyOf(generated), MKWorkspaceStairMode.SLAB_STAIRS, resolvedProfile);
    }

    private MKWorkspacePieceDefinition generateMixedSpiral(ServerLevel level, MKStructureWorkspace workspace,
                                                           MKWorkspacePieceDefinition piece,
                                                           MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry,
                                                           MaterializedStairConfig stairConfig,
                                                           MKResolvedVerticalAccessProfile resolvedProfile) {
        int editableMinY = getEditableMinY(piece, geometry);
        clearShaftFootprint(level, geometry, editableMinY);
        BoundingBox centerlineBounds = getCenterlineBounds(geometry.shaftBounds(), resolvedProfile.stairWidth());
        List<BlockPos> perimeter = getPerimeterClockwise(centerlineBounds, geometry.interiorMinY());
        if (perimeter.isEmpty()) {
            return updateGeneratedState(piece, List.of(), MKWorkspaceStairMode.NONE);
        }
        int startIndex = MKWorkspaceVerticalAccessGeometry.findClosestIndex(perimeter,
                clampToBounds(MKWorkspaceVerticalAccessGeometry.getPreferredStart(geometry), centerlineBounds, geometry.interiorMinY()));
        LinkedHashSet<BlockPos> generated = new LinkedHashSet<>();
        LinkedHashMap<BlockPos, BlockState> planned = new LinkedHashMap<>();
        Direction previousMovement = null;
        int halfHeight = 0;
        int step = 0;
        for (MKResolvedVerticalAccessProfile.RiseStepKind kind : resolvedProfile.risePattern()) {
            for (int segmentStep = 0; segmentStep <= resolvedProfile.flatRunLength(); segmentStep++) {
                BlockPos base = perimeter.get((startIndex + step) % perimeter.size());
                BlockPos next = perimeter.get((startIndex + step + 1) % perimeter.size());
                Direction movement = getHorizontalDirection(base, next);
                boolean isRiseStep = segmentStep == 0;
                int y = geometry.interiorMinY() + (halfHeight / 2);
                BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
                BlockState state = resolveMixedStepState(stairConfig, kind, movement, isRiseStep);
                Direction turnPreviousMovement = getTurnPreviousMovement(perimeter, startIndex + step, movement);
                if (turnPreviousMovement != null && isRiseStep &&
                        kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR) {
                    planTurnStairBand(planned, pos, turnPreviousMovement, movement, resolvedProfile.stairWidth(),
                            geometry.shaftBounds(), state, generated);
                } else if (isRiseStep && kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR) {
                    planStairBand(planned, pos, state, centerlineBounds, geometry.shaftBounds(),
                            resolvedProfile.stairWidth(), generated);
                } else {
                    planGeneratedBand(planned, pos, state, centerlineBounds, geometry.shaftBounds(),
                            resolvedProfile.stairWidth(), generated);
                }
                if (previousMovement != null && movement != null && previousMovement != movement) {
                    planCornerLanding(planned, pos, previousMovement, movement, resolvedProfile.stairWidth(),
                            geometry.shaftBounds(), resolveLandingFillState(stairConfig, kind), generated);
                }
                if (step == 0) {
                    BlockPos preLandingPos = perimeter.get(Math.floorMod(startIndex - 2, perimeter.size()));
                    BlockPos landingPos = perimeter.get(Math.floorMod(startIndex - 1, perimeter.size()));
                    BlockPos landingAtY = new BlockPos(landingPos.getX(), geometry.interiorMinY(), landingPos.getZ());
                    BlockState landingState = resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM);
                    planGeneratedBand(planned, landingAtY, landingState, centerlineBounds, geometry.shaftBounds(),
                            resolvedProfile.stairWidth(), generated);
                    Direction landingMovementIn = getHorizontalDirection(preLandingPos, landingPos);
                    Direction landingMovementOut = getHorizontalDirection(landingPos, base);
                    if (landingMovementIn != null && landingMovementOut != null && landingMovementIn != landingMovementOut) {
                        planCornerLanding(planned, landingAtY, landingMovementIn, landingMovementOut,
                                resolvedProfile.stairWidth(), geometry.shaftBounds(), landingState, generated);
                    }
                }
                previousMovement = movement;
                step++;
            }
            halfHeight += kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR ? 2 : 1;
        }
        if (resolvedProfile.boundaryCompatibility().status() == MKVerticalAccessProfile.BoundaryStatus.BRIDGEABLE) {
            planBoundaryBridge(planned, perimeter, startIndex, resolvedProfile.pathSteps(), geometry.interiorMaxY(),
                    centerlineBounds, geometry.shaftBounds(), resolvedProfile.stairWidth(),
                    resolveSlabState(stairConfig.slabBlock(), SlabType.TOP), generated);
        }
        clipBelowMinY(planned, generated, editableMinY);
        flushPlannedBlocks(level, planned);
        return updateGeneratedState(piece, List.copyOf(generated), MKWorkspaceStairMode.STAIR_STAIRS, resolvedProfile);
    }

    private BlockState resolveMixedStepState(MaterializedStairConfig stairConfig,
                                             MKResolvedVerticalAccessProfile.RiseStepKind kind, Direction movement,
                                             boolean isRiseStep) {
        if (!isRiseStep) {
            return resolveRunFillState(stairConfig);
        }
        return switch (kind) {
            case STAIR -> resolveStairState(stairConfig.stairBlock(), movement == null ? Direction.NORTH : movement);
            case SLAB_BOTTOM -> resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM);
            case SLAB_TOP -> resolveSlabState(stairConfig.slabBlock(), SlabType.TOP);
        };
    }

    private BlockState resolveTopCapContinuationState(MaterializedStairConfig stairConfig,
                                                      MKResolvedVerticalAccessProfile.RiseStepKind kind,
                                                      Direction movement,
                                                      boolean isRiseStep) {
        if (!isRiseStep) {
            return resolveRunFillState(stairConfig);
        }
        if (kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR) {
            return resolveStairState(stairConfig.stairBlock(), movement == null ? Direction.NORTH : movement);
        }
        if (kind == MKResolvedVerticalAccessProfile.RiseStepKind.SLAB_TOP) {
            return resolveSlabState(stairConfig.slabBlock(), SlabType.TOP);
        }
        return resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM);
    }

    List<MKResolvedVerticalAccessProfile.RiseStepKind> getTopCapContinuationPattern(
            MKWorkspaceStairAuthoringConfig stairConfig, MKResolvedVerticalAccessProfile resolvedProfile) {
        return getTopCapContinuationPattern(
                new MaterializedStairConfig(stairConfig, MKWorkspaceMaterialPalette.defaultPalette()),
                resolvedProfile);
    }

    private List<MKResolvedVerticalAccessProfile.RiseStepKind> getTopCapContinuationPattern(
            MaterializedStairConfig stairConfig, MKResolvedVerticalAccessProfile resolvedProfile) {
        if (resolvedProfile != null && !resolvedProfile.risePattern().isEmpty()) {
            List<MKResolvedVerticalAccessProfile.RiseStepKind> pattern = new ArrayList<>();
            int halfHeight = 0;
            for (MKResolvedVerticalAccessProfile.RiseStepKind kind : resolvedProfile.risePattern()) {
                pattern.add(kind);
                halfHeight += kind == MKResolvedVerticalAccessProfile.RiseStepKind.STAIR ? 2 : 1;
                if (halfHeight >= 2) {
                    break;
                }
            }
            return List.copyOf(pattern);
        }
        MKWorkspaceStairRiseType riseType = stairConfig.riseType();
        if (riseType == MKWorkspaceStairRiseType.SLAB) {
            return List.of(MKResolvedVerticalAccessProfile.RiseStepKind.SLAB_BOTTOM,
                    MKResolvedVerticalAccessProfile.RiseStepKind.SLAB_TOP);
        }
        return List.of(MKResolvedVerticalAccessProfile.RiseStepKind.STAIR);
    }

    void planEntryLanding(Map<BlockPos, BlockState> planned, List<BlockPos> perimeter, int startIndex, int y,
                          BoundingBox centerlineBounds, BoundingBox outerBounds, int width, BlockState landingState,
                          LinkedHashSet<BlockPos> generated) {
        if (perimeter.isEmpty()) {
            return;
        }
        BlockPos base = perimeter.get(Math.floorMod(startIndex, perimeter.size()));
        BlockPos preLandingPos = perimeter.get(Math.floorMod(startIndex - 2, perimeter.size()));
        BlockPos landingPos = perimeter.get(Math.floorMod(startIndex - 1, perimeter.size()));
        BlockPos landingAtY = new BlockPos(landingPos.getX(), y, landingPos.getZ());
        planGeneratedBand(planned, landingAtY, landingState, centerlineBounds, outerBounds, width, generated);
        Direction landingMovementIn = getHorizontalDirection(preLandingPos, landingPos);
        Direction landingMovementOut = getHorizontalDirection(landingPos, base);
        if (landingMovementIn != null && landingMovementOut != null && landingMovementIn != landingMovementOut) {
            planCornerLanding(planned, landingAtY, landingMovementIn, landingMovementOut, width, outerBounds,
                    landingState, generated);
        }
    }

    private MKWorkspaceStairMode modeForRiseStrategy(MKWorkspaceStairRiseType riseStrategy) {
        return riseStrategy == MKWorkspaceStairRiseType.SLAB ? MKWorkspaceStairMode.SLAB_STAIRS :
                MKWorkspaceStairMode.STAIR_STAIRS;
    }

    private void clearGenerated(ServerLevel level, MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        MKWorkspaceMaterialPalette palette = paletteResolver.resolvePiece(workspace, piece).orElse(workspace.palette());
        BlockState protectedBottomState = resolveSolidState(palette.floorBlock(),
                Blocks.STONE_BRICKS.defaultBlockState());
        clearGenerated(level, piece, protectedBottomState);
    }

    private void clearGenerated(ServerLevel level, MKWorkspacePieceDefinition piece) {
        clearGenerated(level, piece, Blocks.STONE_BRICKS.defaultBlockState());
    }

    private void clearGenerated(ServerLevel level, MKWorkspacePieceDefinition piece, BlockState protectedBottomState) {
        for (BlockPos pos : piece.generatedStairPositions()) {
            if (isProtectedBottomShell(piece, pos)) {
                writeGeneratedBlock(level, pos, protectedBottomState);
            } else {
                clearGeneratedBlock(level, pos);
            }
        }
    }

    private void clearShaftFootprint(ServerLevel level, MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry,
                                    int editableMinY) {
        for (int x = geometry.shaftBounds().minX(); x <= geometry.shaftBounds().maxX(); x++) {
            for (int y = editableMinY; y <= geometry.interiorMaxY(); y++) {
                for (int z = geometry.shaftBounds().minZ(); z <= geometry.shaftBounds().maxZ(); z++) {
                    clearGeneratedBlock(level, new BlockPos(x, y, z));
                }
            }
        }
    }

    int getEditableMinY(MKWorkspacePieceDefinition piece,
                        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry) {
        if (isTerminalBottom(piece)) {
            return Math.min(geometry.interiorMaxY(), geometry.interiorMinY() + 1);
        }
        return geometry.interiorMinY();
    }

    int getProfileInteriorHeight(MKWorkspacePieceDefinition piece) {
        return Math.max(1, piece.effectiveDimensions().roomHeight());
    }

    private boolean isProtectedBottomShell(MKWorkspacePieceDefinition piece, BlockPos pos) {
        return isTerminalBottom(piece) && pos.getY() <= piece.exportBounds().minY();
    }

    private void clipBelowMinY(Map<BlockPos, BlockState> planned, LinkedHashSet<BlockPos> generated, int editableMinY) {
        planned.keySet().removeIf(pos -> pos.getY() < editableMinY);
        generated.removeIf(pos -> pos.getY() < editableMinY);
    }

    private boolean isEligible(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()) &&
                piece.connectors().stream().anyMatch(connector -> connector.facing() == Direction.UP ||
                        connector.facing() == Direction.DOWN);
    }

    MKWorkspaceVerticalAccessGeometry.ShaftGeometry getGenerationGeometry(MKStructureWorkspace workspace,
                                                                          MKWorkspacePieceDefinition piece) {
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = MKWorkspaceVerticalAccessGeometry.forPiece(workspace, piece);
        BoundingBox bounds = geometry.shaftBounds();
        if (isTerminalTop(piece)) {
            BoundingBox constrainedBounds = new BoundingBox(
                    bounds.minX(),
                    geometry.interiorMinY(),
                    bounds.minZ(),
                    bounds.maxX(),
                    geometry.interiorMinY(),
                    bounds.maxZ()
            );
            return new MKWorkspaceVerticalAccessGeometry.ShaftGeometry(constrainedBounds, geometry.interiorMinY(),
                    geometry.interiorMinY(), geometry.placement());
        }
        return geometry;
    }

    private boolean isTerminalTop(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.isTopCap(piece.tags()) || !hasVerticalConnector(piece, Direction.UP);
    }

    private boolean isTerminalBottom(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.isBottomCap(piece.tags()) || !hasVerticalConnector(piece, Direction.DOWN);
    }

    private boolean hasVerticalConnector(MKWorkspacePieceDefinition piece, Direction direction) {
        return piece.connectors().stream().anyMatch(connector -> connector.facing() == direction);
    }

    private MKWorkspaceStairMode resolveMode(MaterializedStairConfig stairConfig,
                                             MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry) {
        MKWorkspaceStairMode mode = stairConfig.mode();
        if (mode == MKWorkspaceStairMode.AUTO) {
            return geometry.width() <= 2 || geometry.length() <= 2 ? MKWorkspaceStairMode.LADDER :
                    MKWorkspaceStairMode.RUN_PROFILE;
        }
        if ((mode == MKWorkspaceStairMode.RUN_PROFILE || mode == MKWorkspaceStairMode.STAIR_STAIRS ||
                mode == MKWorkspaceStairMode.SLAB_STAIRS) &&
                (geometry.width() <= 2 || geometry.length() <= 2)) {
            return MKWorkspaceStairMode.LADDER;
        }
        return mode;
    }

    private MaterializedStairConfig normalizeConfigForMode(MaterializedStairConfig stairConfig,
                                                           MKWorkspaceStairMode resolvedMode) {
        if (resolvedMode == MKWorkspaceStairMode.STAIR_STAIRS) {
            return stairConfig.withAuthoringConfig(new MKWorkspaceStairAuthoringConfig(MKWorkspaceStairMode.RUN_PROFILE,
                    com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType.STAIR,
                    stairConfig.stairWidth()));
        }
        if (resolvedMode == MKWorkspaceStairMode.SLAB_STAIRS) {
            return stairConfig.withAuthoringConfig(new MKWorkspaceStairAuthoringConfig(MKWorkspaceStairMode.RUN_PROFILE,
                    com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType.SLAB,
                    stairConfig.stairWidth()));
        }
        return stairConfig;
    }

    private MaterializedStairConfig withPaletteMaterials(MKWorkspaceStairAuthoringConfig stairConfig,
                                                         MKWorkspaceMaterialPalette palette) {
        return new MaterializedStairConfig(stairConfig, palette);
    }

    private record MaterializedStairConfig(MKWorkspaceStairAuthoringConfig authoringConfig,
                                           MKWorkspaceMaterialPalette palette) {
        private MKWorkspaceStairMode mode() {
            return authoringConfig.mode();
        }

        private MKWorkspaceStairRiseType riseType() {
            return authoringConfig.riseType();
        }

        private int stairWidth() {
            return authoringConfig.stairWidth();
        }

        private ResourceLocation stairBlock() {
            return palette.stairBlock();
        }

        private ResourceLocation slabBlock() {
            return palette.slabBlock();
        }

        private ResourceLocation ladderBlock() {
            return palette.ladderBlock();
        }

        private MaterializedStairConfig withAuthoringConfig(MKWorkspaceStairAuthoringConfig value) {
            return new MaterializedStairConfig(value, palette);
        }
    }

    private MKWorkspacePieceDefinition updateGeneratedState(MKWorkspacePieceDefinition piece, List<BlockPos> generated,
                                                            MKWorkspaceStairMode mode) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put("generated_stair_mode", mode.getSerializedName());
        tags.put("generated_stair_revision", Long.toString(System.currentTimeMillis()));
        return piece.withGeneratedStairs(generated, tags);
    }

    private MKWorkspacePieceDefinition updateGeneratedState(MKWorkspacePieceDefinition piece, List<BlockPos> generated,
                                                            MKWorkspaceStairMode mode,
                                                            MKResolvedVerticalAccessProfile resolvedProfile) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put("generated_stair_mode", mode.getSerializedName());
        tags.put("generated_stair_revision", Long.toString(System.currentTimeMillis()));
        tags.put("resolved_rise_strategy", resolvedProfile.riseStrategy().getSerializedName());
        tags.put("resolved_flat_run_length", Integer.toString(resolvedProfile.flatRunLength()));
        tags.put("resolved_pattern", resolvedProfile.risePattern().toString());
        tags.put("resolved_boundary_status", resolvedProfile.boundaryCompatibility().status().name().toLowerCase(java.util.Locale.ROOT));
        tags.put("resolved_bridge_steps", Integer.toString(resolvedProfile.boundaryCompatibility().bridgeSteps()));
        tags.put("resolved_top_phase", Integer.toString(resolvedProfile.topPhase()));
        tags.put("resolved_cycle_length", Integer.toString(resolvedProfile.cycleLength()));
        tags.put("resolved_path_steps", Integer.toString(resolvedProfile.pathSteps()));
        tags.put("resolved_interior_height", Integer.toString(resolvedProfile.interiorHeight()));
        return piece.withGeneratedStairs(generated, tags);
    }

    private BlockPos getLadderBase(MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry) {
        BoundingBox bounds = geometry.shaftBounds();
        int centerX = (bounds.minX() + bounds.maxX()) / 2;
        int centerZ = (bounds.minZ() + bounds.maxZ()) / 2;
        return switch (MKWorkspaceVerticalAccessGeometry.getPreferredLadderFacing(geometry)) {
            case NORTH -> new BlockPos(centerX, geometry.interiorMinY(), bounds.maxZ());
            case SOUTH -> new BlockPos(centerX, geometry.interiorMinY(), bounds.minZ());
            case EAST -> new BlockPos(bounds.minX(), geometry.interiorMinY(), centerZ);
            case WEST -> new BlockPos(bounds.maxX(), geometry.interiorMinY(), centerZ);
            default -> new BlockPos(centerX, geometry.interiorMinY(), bounds.minZ());
        };
    }

    private BlockState resolveStairState(ResourceLocation id, Direction facing) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.STONE_BRICK_STAIRS);
        BlockState state = block.defaultBlockState();
        if (!(block instanceof StairBlock)) {
            state = Blocks.STONE_BRICK_STAIRS.defaultBlockState();
        }
        if (state.hasProperty(StairBlock.FACING)) {
            state = state.setValue(StairBlock.FACING, facing);
        }
        if (state.hasProperty(StairBlock.HALF)) {
            state = state.setValue(StairBlock.HALF, Half.BOTTOM);
        }
        return state;
    }

    private BlockState resolveSolidState(ResourceLocation id, BlockState fallback) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback.getBlock());
        return block.defaultBlockState();
    }

    private BlockState resolveSlabState(ResourceLocation id, SlabType slabType) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.STONE_BRICK_SLAB);
        BlockState state = block.defaultBlockState();
        if (!(block instanceof SlabBlock)) {
            state = Blocks.STONE_BRICK_SLAB.defaultBlockState();
        }
        if (state.hasProperty(SlabBlock.TYPE)) {
            state = state.setValue(SlabBlock.TYPE, slabType);
        }
        return state;
    }

    private BlockState resolveRunFillState(MaterializedStairConfig stairConfig) {
        return resolveSlabState(stairConfig.slabBlock(), SlabType.TOP);
    }

    private BlockState resolveLandingFillState(MaterializedStairConfig stairConfig) {
        return resolveSlabState(stairConfig.slabBlock(), SlabType.BOTTOM);
    }

    private BlockState resolveLandingFillState(MaterializedStairConfig stairConfig,
                                               MKResolvedVerticalAccessProfile.RiseStepKind kind) {
        if (kind == MKResolvedVerticalAccessProfile.RiseStepKind.SLAB_TOP) {
            return resolveSlabState(stairConfig.slabBlock(), SlabType.TOP);
        }
        return resolveLandingFillState(stairConfig);
    }

    private BlockState resolveLadderState(ResourceLocation id, Direction facing) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.LADDER);
        BlockState state = block.defaultBlockState();
        if (!(block instanceof LadderBlock)) {
            state = Blocks.LADDER.defaultBlockState();
        }
        if (state.hasProperty(LadderBlock.FACING)) {
            state = state.setValue(LadderBlock.FACING, facing);
        }
        return state;
    }

    private Direction getHorizontalDirection(BlockPos from, BlockPos to) {
        int deltaX = Integer.compare(to.getX(), from.getX());
        int deltaZ = Integer.compare(to.getZ(), from.getZ());
        if (deltaX > 0) {
            return Direction.EAST;
        }
        if (deltaX < 0) {
            return Direction.WEST;
        }
        if (deltaZ > 0) {
            return Direction.SOUTH;
        }
        if (deltaZ < 0) {
            return Direction.NORTH;
        }
        return null;
    }

    Direction getTurnPreviousMovement(List<BlockPos> perimeter, int index, Direction currentMovement) {
        if (perimeter.size() < 2 || currentMovement == null) {
            return null;
        }
        int currentIndex = Math.floorMod(index, perimeter.size());
        int previousIndex = Math.floorMod(index - 1, perimeter.size());
        Direction previousMovement = getHorizontalDirection(perimeter.get(previousIndex), perimeter.get(currentIndex));
        if (previousMovement == null || previousMovement == currentMovement) {
            return null;
        }
        return previousMovement;
    }

    private void planGeneratedBand(Map<BlockPos, BlockState> planned, BlockPos pos, BlockState state,
                                   BoundingBox centerlineBounds, BoundingBox outerBounds, int width,
                                   LinkedHashSet<BlockPos> generated) {
        Direction outward = getOutwardDirection(pos, centerlineBounds);
        for (int offset = 0; offset < Math.max(1, width); offset++) {
            BlockPos target = outward == null ? pos : pos.relative(outward, offset);
            if (!outerBounds.isInside(target)) {
                continue;
            }
            planned.put(target, state);
            generated.add(target);
        }
    }

    void planStairBand(Map<BlockPos, BlockState> planned, BlockPos pos, BlockState stairState,
                       BoundingBox centerlineBounds, BoundingBox outerBounds, int width,
                       LinkedHashSet<BlockPos> generated) {
        for (BlockPos target : getStairBandTargets(pos, centerlineBounds, outerBounds, width)) {
            planned.put(target, stairState);
            generated.add(target);
        }
    }

    List<BlockPos> getStairBandTargets(BlockPos pos, BoundingBox centerlineBounds, BoundingBox outerBounds, int width) {
        List<BlockPos> targets = new ArrayList<>();
        Direction outward = getOutwardDirection(pos, centerlineBounds);
        for (int offset = 0; offset < Math.max(1, width); offset++) {
            BlockPos target = outward == null ? pos : pos.relative(outward, offset);
            if (!outerBounds.isInside(target)) {
                continue;
            }
            targets.add(target);
        }
        return targets;
    }

    private void planCornerLanding(Map<BlockPos, BlockState> planned, BlockPos cornerPos, Direction previousMovement,
                                   Direction currentMovement, int width, BoundingBox outerBounds,
                                   BlockState fillerState, LinkedHashSet<BlockPos> generated) {
        Direction outwardPrev = previousMovement.getCounterClockWise();
        Direction outwardCurrent = currentMovement.getCounterClockWise();
        for (int prevOffset = 0; prevOffset < Math.max(1, width); prevOffset++) {
            for (int currentOffset = 0; currentOffset < Math.max(1, width); currentOffset++) {
                BlockPos target = cornerPos.relative(outwardPrev, prevOffset).relative(outwardCurrent, currentOffset);
                if (!outerBounds.isInside(target)) {
                    continue;
                }
                if (planned.putIfAbsent(target, fillerState) == null) {
                    generated.add(target);
                }
            }
        }
    }

    void planTurnStairBand(Map<BlockPos, BlockState> planned, BlockPos cornerPos, Direction previousMovement,
                           Direction currentMovement, int width, BoundingBox outerBounds, BlockState stairState,
                           LinkedHashSet<BlockPos> generated) {
        BlockState shapedState = withExplicitTurnShape(stairState, previousMovement, currentMovement);
        for (BlockPos target : getTurnStairBandTargets(cornerPos, currentMovement, width, outerBounds)) {
            planned.put(target, shapedState);
            generated.add(target);
        }
    }

    List<BlockPos> getTurnStairBandTargets(BlockPos cornerPos, Direction currentMovement, int width,
                                           BoundingBox outerBounds) {
        List<BlockPos> targets = new ArrayList<>();
        if (currentMovement == null || width <= 1) {
            if (outerBounds.isInside(cornerPos)) {
                targets.add(cornerPos);
            }
            return targets;
        }
        Direction outwardCurrent = currentMovement.getCounterClockWise();
        for (int offset = 0; offset < width; offset++) {
            BlockPos target = cornerPos.relative(outwardCurrent, offset);
            if (!outerBounds.isInside(target)) {
                continue;
            }
            targets.add(target);
        }
        return targets;
    }

    private void planBoundaryBridge(Map<BlockPos, BlockState> planned, List<BlockPos> perimeter, int startIndex,
                                    int pathSteps, int y, BoundingBox centerlineBounds, BoundingBox outerBounds,
                                    int width, BlockState bridgeState, LinkedHashSet<BlockPos> generated) {
        if (perimeter.isEmpty()) {
            return;
        }
        int size = perimeter.size();
        int lastTopIndex = Math.floorMod(startIndex + pathSteps - 1, size);
        int landingIndex = Math.floorMod(startIndex - 1, size);

        List<Integer> stitchIndices = new ArrayList<>();
        int currentIndex = Math.floorMod(lastTopIndex + 1, size);
        while (true) {
            stitchIndices.add(currentIndex);
            if (currentIndex == landingIndex) {
                break;
            }
            currentIndex = Math.floorMod(currentIndex + 1, size);
            if (stitchIndices.size() > size) {
                break;
            }
        }

        for (int index : stitchIndices) {
            BlockPos base = perimeter.get(index);
            BlockPos pos = new BlockPos(base.getX(), y, base.getZ());
            planGeneratedBand(planned, pos, bridgeState, centerlineBounds, outerBounds, width, generated);
        }

        List<Integer> fullPath = new ArrayList<>();
        fullPath.add(lastTopIndex);
        fullPath.addAll(stitchIndices);
        fullPath.add(Math.floorMod(startIndex, size));
        for (int i = 1; i < fullPath.size() - 1; i++) {
            BlockPos previousBase = perimeter.get(fullPath.get(i - 1));
            BlockPos currentBase = perimeter.get(fullPath.get(i));
            BlockPos nextBase = perimeter.get(fullPath.get(i + 1));
            Direction previousMovement = getHorizontalDirection(previousBase, currentBase);
            Direction nextMovement = getHorizontalDirection(currentBase, nextBase);
            if (previousMovement != null && nextMovement != null && previousMovement != nextMovement) {
                BlockPos currentPos = new BlockPos(currentBase.getX(), y, currentBase.getZ());
                planCornerLanding(planned, currentPos, previousMovement, nextMovement, width, outerBounds,
                        bridgeState, generated);
            }
        }
    }

    private Direction getOutwardDirection(BlockPos pos, BoundingBox bounds) {
        if (pos.getZ() == bounds.minZ()) {
            return Direction.NORTH;
        }
        if (pos.getZ() == bounds.maxZ()) {
            return Direction.SOUTH;
        }
        if (pos.getX() == bounds.minX()) {
            return Direction.WEST;
        }
        if (pos.getX() == bounds.maxX()) {
            return Direction.EAST;
        }
        return null;
    }

    private BoundingBox getCenterlineBounds(BoundingBox outerBounds, int width) {
        int inset = Math.max(0, width - 1);
        int minX = Math.min(outerBounds.maxX(), outerBounds.minX() + inset);
        int maxX = Math.max(minX, outerBounds.maxX() - inset);
        int minZ = Math.min(outerBounds.maxZ(), outerBounds.minZ() + inset);
        int maxZ = Math.max(minZ, outerBounds.maxZ() - inset);
        return new BoundingBox(minX, outerBounds.minY(), minZ, maxX, outerBounds.maxY(), maxZ);
    }

    private List<BlockPos> getPerimeterClockwise(BoundingBox bounds, int y) {
        List<BlockPos> perimeter = new ArrayList<>();
        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            perimeter.add(new BlockPos(x, y, bounds.minZ()));
        }
        for (int z = bounds.minZ() + 1; z <= bounds.maxZ(); z++) {
            perimeter.add(new BlockPos(bounds.maxX(), y, z));
        }
        if (bounds.maxZ() > bounds.minZ()) {
            for (int x = bounds.maxX() - 1; x >= bounds.minX(); x--) {
                perimeter.add(new BlockPos(x, y, bounds.maxZ()));
            }
        }
        if (bounds.maxX() > bounds.minX()) {
            for (int z = bounds.maxZ() - 1; z > bounds.minZ(); z--) {
                perimeter.add(new BlockPos(bounds.minX(), y, z));
            }
        }
        return perimeter;
    }

    private BlockPos clampToBounds(BlockPos pos, BoundingBox bounds, int y) {
        int x = Math.max(bounds.minX(), Math.min(bounds.maxX(), pos.getX()));
        int z = Math.max(bounds.minZ(), Math.min(bounds.maxZ(), pos.getZ()));
        return new BlockPos(x, y, z);
    }

    private void flushPlannedBlocks(ServerLevel level, Map<BlockPos, BlockState> planned) {
        applyStairCornerShapes(planned);
        for (Map.Entry<BlockPos, BlockState> entry : planned.entrySet()) {
            writeGeneratedBlock(level, entry.getKey(), entry.getValue());
        }
    }

    void applyStairCornerShapes(Map<BlockPos, BlockState> planned) {
        List<Map.Entry<BlockPos, BlockState>> stairs = planned.entrySet().stream()
                .filter(entry -> entry.getValue().hasProperty(StairBlock.FACING) &&
                        entry.getValue().hasProperty(StairBlock.SHAPE))
                .toList();
        for (Map.Entry<BlockPos, BlockState> entry : stairs) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            Direction facing = state.getValue(StairBlock.FACING);
            Half half = state.hasProperty(StairBlock.HALF) ? state.getValue(StairBlock.HALF) : Half.BOTTOM;

            BlockState front = planned.get(pos.relative(facing));
            Direction frontFacing = null;
            if (isCompatibleStair(front, half)) {
                frontFacing = front.getValue(StairBlock.FACING);
            }

            BlockState back = planned.get(pos.relative(facing.getOpposite()));
            Direction backFacing = null;
            if (isCompatibleStair(back, half)) {
                backFacing = back.getValue(StairBlock.FACING);
            }

            StairsShape inferredShape = getStairCornerShape(facing, frontFacing, backFacing);
            StairsShape currentShape = state.getValue(StairBlock.SHAPE);
            if (inferredShape != StairsShape.STRAIGHT || currentShape == StairsShape.STRAIGHT) {
                planned.put(pos, state.setValue(StairBlock.SHAPE, inferredShape));
            }
        }
    }

    StairsShape getStairCornerShape(Direction facing, Direction frontFacing, Direction backFacing) {
        if (frontFacing != null && frontFacing.getAxis() != facing.getAxis()) {
            return frontFacing == facing.getClockWise() ? StairsShape.OUTER_RIGHT : StairsShape.OUTER_LEFT;
        }
        if (backFacing != null && backFacing.getAxis() != facing.getAxis()) {
            return backFacing == facing.getClockWise() ? StairsShape.INNER_RIGHT : StairsShape.INNER_LEFT;
        }
        return StairsShape.STRAIGHT;
    }

    StairsShape getExplicitTurnStairShape(Direction previousMovement, Direction currentMovement) {
        if (previousMovement == null || currentMovement == null || previousMovement == currentMovement) {
            return StairsShape.STRAIGHT;
        }
        return getStairCornerShape(currentMovement, previousMovement, null);
    }

    private BlockState withExplicitTurnShape(BlockState stairState, Direction previousMovement,
                                             Direction currentMovement) {
        if (!stairState.hasProperty(StairBlock.SHAPE)) {
            return stairState;
        }
        return stairState.setValue(StairBlock.SHAPE,
                getExplicitTurnStairShape(previousMovement, currentMovement));
    }

    private boolean isCompatibleStair(BlockState state, Half half) {
        return state != null && state.hasProperty(StairBlock.FACING) && state.hasProperty(StairBlock.HALF) &&
                state.hasProperty(StairBlock.SHAPE) && state.getValue(StairBlock.HALF) == half;
    }

    private void fillCornerGapsWithTopSlabs(Map<BlockPos, BlockState> planned, BoundingBox bounds, int minY, int maxY,
                                            BlockState fillerState, LinkedHashSet<BlockPos> generated) {
        boolean changed;
        do {
            changed = false;
            List<BlockPos> toFill = new ArrayList<>();
            for (int y = minY; y <= maxY; y++) {
                for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
                    for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (planned.containsKey(pos)) {
                            continue;
                        }
                        boolean north = isWalkable(planned.get(pos.north()));
                        boolean south = isWalkable(planned.get(pos.south()));
                        boolean east = isWalkable(planned.get(pos.east()));
                        boolean west = isWalkable(planned.get(pos.west()));
                        if ((north && east) || (east && south) || (south && west) || (west && north)) {
                            toFill.add(pos);
                        }
                    }
                }
            }
            for (BlockPos pos : toFill) {
                if (planned.putIfAbsent(pos, fillerState) == null) {
                    generated.add(pos);
                    changed = true;
                }
            }
        } while (changed);
    }

    private boolean isWalkable(BlockState state) {
        return state != null && !state.isAir();
    }

    private void writeGeneratedBlock(ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof JigsawBlockEntity jigsaw) {
            updateJigsawFinalState(level, pos, jigsaw, serializeBlockState(state));
            return;
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private void clearGeneratedBlock(ServerLevel level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof JigsawBlockEntity jigsaw) {
            updateJigsawFinalState(level, pos, jigsaw, "minecraft:air");
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private void updateJigsawFinalState(ServerLevel level, BlockPos pos, JigsawBlockEntity jigsaw, String finalState) {
        jigsaw.setFinalState(finalState);
        jigsaw.setChanged();
        BlockState currentState = level.getBlockState(pos);
        level.sendBlockUpdated(pos, currentState, currentState, Block.UPDATE_ALL);
        level.getChunkSource().blockChanged(pos);
    }

    private String serializeBlockState(BlockState state) {
        StringBuilder builder = new StringBuilder();
        builder.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        if (!state.getProperties().isEmpty()) {
            builder.append('[');
            boolean first = true;
            for (Property<?> property : state.getProperties()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(property.getName()).append('=').append(getPropertyValueName(state, property));
            }
            builder.append(']');
        }
        return builder.toString();
    }

    private <T extends Comparable<T>> String getPropertyValueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }
}


