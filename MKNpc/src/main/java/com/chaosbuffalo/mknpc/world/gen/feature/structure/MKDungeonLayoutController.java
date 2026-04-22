package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.util.RandomSource;

import java.util.Optional;

public class MKDungeonLayoutController {
    private final MKDungeonLayoutSettings settings;

    public MKDungeonLayoutController(MKDungeonLayoutSettings settings) {
        this.settings = settings;
    }

    public int chooseTargetFloors(RandomSource random) {
        if (settings.minFloors() == settings.maxFloors()) {
            return settings.minFloors();
        }
        return random.nextInt(settings.maxFloors() - settings.minFloors() + 1) + settings.minFloors();
    }

    public boolean canPlaceChild(MKDungeonPieceState parentState, MKConnectorInfo connector, MKJigsawPieceMetadata childMetadata) {
        return getRejectionReason(parentState, connector, childMetadata).isEmpty();
    }

    public Optional<String> getRejectionReason(MKDungeonPieceState parentState, MKConnectorInfo connector, MKJigsawPieceMetadata childMetadata) {
        if (!isVerticalDeltaAllowed(childMetadata.verticalLevelDelta())) {
            return Optional.of("illegal_vertical_transition");
        }

        boolean nextOnMainPath = isMainPathContinuation(parentState, connector, childMetadata);
        if (nextOnMainPath && !childMetadata.allowOnMainPath()) {
            return Optional.of("main_path_forbidden");
        }
        if (!nextOnMainPath && !childMetadata.allowOnBranchPath()) {
            return Optional.of("branch_path_forbidden");
        }

        int nextFloor = parentState.progressionFloorIndex() + childMetadata.progressionDelta();
        if (nextFloor < 0 || nextFloor >= parentState.targetFloors()) {
            return Optional.of("floor_limit");
        }

        boolean finalFloor = parentState.progressionFloorIndex() >= parentState.targetFloors() - 1;
        if (finalFloor && childMetadata.progressionDelta() != 0) {
            return Optional.of("final_floor_progression_blocked");
        }
        if (finalFloor && !settings.allowBranchesOnFinalFloor() && connector.role() == MKConnectorRole.BRANCH) {
            return Optional.of("final_floor_branch_blocked");
        }
        if (childMetadata.bossOnly() && nextFloor != parentState.targetFloors() - 1) {
            return Optional.of("boss_only_restricted");
        }
        if (isBossConnector(connector.role()) && nextFloor != parentState.targetFloors() - 1) {
            return Optional.of("boss_connector_restricted");
        }
        if (childMetadata.progressionDelta() == 0 && parentState.piecesOnFloor() >= settings.maxPiecesPerFloor()) {
            return Optional.of("per_floor_budget");
        }

        MKDungeonPieceState nextState = nextState(parentState, connector, childMetadata);
        if (nextState.branchDepth() > settings.maxBranchDepth()) {
            return Optional.of("branch_depth");
        }
        return Optional.empty();
    }

    public MKDungeonPieceState nextState(MKDungeonPieceState parentState, MKConnectorInfo connector, MKJigsawPieceMetadata childMetadata) {
        boolean nextOnMainPath = isMainPathContinuation(parentState, connector, childMetadata);
        int nextFloor = parentState.progressionFloorIndex() + childMetadata.progressionDelta();
        int nextVertical = parentState.verticalLevelIndex() + childMetadata.verticalLevelDelta();
        int nextPiecesOnFloor = childMetadata.progressionDelta() != 0 ? 1 : parentState.piecesOnFloor() + 1;
        int nextBranchDepth;
        if (nextOnMainPath) {
            nextBranchDepth = 0;
        } else if (connector.role() == MKConnectorRole.BRANCH) {
            nextBranchDepth = parentState.branchDepth() + 1;
        } else {
            nextBranchDepth = parentState.branchDepth();
        }
        return new MKDungeonPieceState(nextFloor, nextVertical, nextPiecesOnFloor, nextBranchDepth, nextOnMainPath, parentState.targetFloors());
    }

    private boolean isMainPathContinuation(MKDungeonPieceState parentState, MKConnectorInfo connector, MKJigsawPieceMetadata childMetadata) {
        if (!parentState.onMainPath()) {
            return false;
        }
        if (connector.role() == MKConnectorRole.BRANCH) {
            return false;
        }
        return childMetadata.pieceRole() != MKJigsawPieceRole.BRANCH;
    }

    private boolean isVerticalDeltaAllowed(int verticalDelta) {
        return switch (settings.verticalProgressionMode()) {
            case DOWNWARD -> verticalDelta <= 0;
            case UPWARD -> verticalDelta >= 0;
            case MIXED -> true;
        };
    }

    private boolean isBossConnector(MKConnectorRole role) {
        return role == MKConnectorRole.BOSS_FORWARD || role == MKConnectorRole.BOSS_BACK;
    }
}
