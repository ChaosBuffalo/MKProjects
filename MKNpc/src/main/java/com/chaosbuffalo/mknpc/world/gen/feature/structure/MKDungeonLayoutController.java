package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;

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
        return getRejectionReason(parentState, connector, childMetadata, false);
    }

    public Optional<String> getRejectionReason(MKDungeonPieceState parentState, MKConnectorInfo connector,
                                               MKJigsawPieceMetadata childMetadata, boolean branchCapsAvailable) {
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
        if (childMetadata.progressionDelta() != 0 && parentState.piecesOnFloor() < settings.minPiecesPerFloor()) {
            return Optional.of("min_floor_budget");
        }
        if (connector.role() == MKConnectorRole.CONNECT_UP) {
            if (nextFloor == parentState.targetFloors() - 1) {
                if (settings.topCapApproachEnabled() &&
                        childMetadata.pieceRole() != MKJigsawPieceRole.TOP_CAP_APPROACH) {
                    return Optional.of("final_upward_step_requires_top_cap_approach");
                }
                if (!settings.topCapApproachEnabled()) {
                    if (childMetadata.pieceRole() == MKJigsawPieceRole.TOP_CAP_APPROACH) {
                        return Optional.of("top_cap_approach_disabled");
                    }
                    if (!childMetadata.terminal()) {
                        return Optional.of("final_upward_step_requires_terminal");
                    }
                }
            }
            if (nextFloor < parentState.targetFloors() - 1 && childMetadata.pieceRole() == MKJigsawPieceRole.TOP_CAP_APPROACH) {
                return Optional.of("top_cap_approach_early");
            }
        }
        if (connector.role() == MKConnectorRole.CONNECT_DOWN) {
            if (nextFloor == parentState.targetFloors() - 1) {
                if (settings.basementCapApproachEnabled() &&
                        childMetadata.pieceRole() != MKJigsawPieceRole.BASEMENT_CAP_APPROACH) {
                    return Optional.of("final_downward_step_requires_basement_cap_approach");
                }
                if (!settings.basementCapApproachEnabled() && !childMetadata.terminal()) {
                    return Optional.of("final_downward_step_requires_terminal");
                }
            }
            if (nextFloor < parentState.targetFloors() - 1 &&
                    childMetadata.pieceRole() == MKJigsawPieceRole.BASEMENT_CAP_APPROACH) {
                return Optional.of("basement_cap_approach_early");
            }
            if (nextFloor < parentState.targetFloors() - 1 && childMetadata.terminal()) {
                return Optional.of("downward_terminal_early");
            }
        }

        boolean finalFloor = parentState.progressionFloorIndex() >= parentState.targetFloors() - 1;
        if (finalFloor && childMetadata.progressionDelta() != 0) {
            return Optional.of("final_floor_progression_blocked");
        }
        if (finalFloor && !settings.allowBranchesOnFinalFloor() && connector.role() == MKConnectorRole.BRANCH) {
            return Optional.of("final_floor_branch_blocked");
        }
        if (childMetadata.topCapOnly() && nextFloor != parentState.targetFloors() - 1) {
            return Optional.of("top_cap_only_restricted");
        }
        if (isTopCapConnector(connector.role()) && nextFloor != parentState.targetFloors() - 1) {
            return Optional.of("top_cap_connector_restricted");
        }
        if (childMetadata.progressionDelta() == 0 && parentState.piecesOnFloor() >= settings.maxPiecesPerFloor()) {
            return Optional.of("per_floor_budget");
        }
        Optional<String> topologyGroupRejection = getTopologyGroupPathRejection(parentState, nextOnMainPath,
                childMetadata);
        if (topologyGroupRejection.isPresent()) {
            return topologyGroupRejection;
        }
        Optional<String> branchCapRejection = getBranchCapRejection(parentState, connector, nextOnMainPath,
                childMetadata, branchCapsAvailable);
        if (branchCapRejection.isPresent()) {
            return branchCapRejection;
        }

        MKDungeonPieceState nextState = nextState(parentState, connector, childMetadata);
        if (nextState.branchDepth() > settings.maxBranchDepth()) {
            return Optional.of("branch_depth");
        }
        return Optional.empty();
    }

    private Optional<String> getBranchCapRejection(MKDungeonPieceState parentState, MKConnectorInfo connector,
                                                   boolean nextOnMainPath, MKJigsawPieceMetadata childMetadata,
                                                   boolean branchCapsAvailable) {
        if (nextOnMainPath || connector.role() != MKConnectorRole.BRANCH || !branchCapsAvailable ||
                childMetadata.branchCap()) {
            return Optional.empty();
        }
        return settings.topologyGroupRule(parentState.topologyGroup())
                .filter(rule -> parentState.branchDepth() >= rule.maxBranchPiecesBeforeCap())
                .map(rule -> "branch_cap_required");
    }

    public MKDungeonPieceState nextState(MKDungeonPieceState parentState, MKConnectorInfo connector, MKJigsawPieceMetadata childMetadata) {
        return nextState(parentState, connector, childMetadata, null);
    }

    public MKDungeonPieceState nextState(MKDungeonPieceState parentState, MKConnectorInfo connector,
                                         MKJigsawPieceMetadata childMetadata, RandomSource random) {
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
        TopologyGroupProgress topologyGroupProgress = nextTopologyGroupProgress(parentState, childMetadata,
                nextOnMainPath, random);
        return new MKDungeonPieceState(nextFloor, nextVertical, nextPiecesOnFloor, nextBranchDepth, nextOnMainPath,
                parentState.targetFloors(), topologyGroupProgress.topologyGroup(),
                topologyGroupProgress.piecesInTopologyGroup(),
                topologyGroupProgress.targetInTopologyGroup());
    }

    public Optional<ResourceLocation> endingPoolForState(MKDungeonPieceState parentState, MKConnectorInfo connector) {
        if (!parentState.onMainPath() || connector.role() == MKConnectorRole.BRANCH) {
            return Optional.empty();
        }
        return settings.topologyGroupRule(parentState.topologyGroup())
                .filter(rule -> rule.hasMainPathEndings() &&
                        (!rule.hasMainPathContinuations() ||
                                parentState.mainPathPiecesInTopologyGroup() >=
                                        parentState.mainPathTargetInTopologyGroup()))
                .flatMap(MKDungeonTopologyGroupRule::mainPathEndingPoolOpt);
    }

    private Optional<String> getTopologyGroupPathRejection(MKDungeonPieceState parentState, boolean nextOnMainPath,
                                                           MKJigsawPieceMetadata childMetadata) {
        if (!nextOnMainPath || childMetadata.topologyGroup().isBlank()) {
            return Optional.empty();
        }
        Optional<MKDungeonTopologyGroupRule> ruleOpt = settings.topologyGroupRule(childMetadata.topologyGroup());
        if (ruleOpt.isEmpty()) {
            return childMetadata.mainPathEnding() ? Optional.of("main_path_ending_unconfigured") : Optional.empty();
        }
        MKDungeonTopologyGroupRule rule = ruleOpt.get();
        if (childMetadata.mainPathEnding()) {
            if (!rule.hasMainPathEndings()) {
                return Optional.of("main_path_ending_unavailable");
            }
            if (!rule.hasMainPathContinuations()) {
                return Optional.empty();
            }
            if (!parentState.topologyGroup().equals(childMetadata.topologyGroup())) {
                return Optional.of("main_path_ending_early");
            }
            if (parentState.mainPathPiecesInTopologyGroup() < parentState.mainPathTargetInTopologyGroup()) {
                return Optional.of("main_path_ending_early");
            }
            return Optional.empty();
        }
        if (!rule.hasMainPathContinuations() && rule.hasMainPathEndings()) {
            return Optional.of("main_path_direct_ending_required");
        }
        if (rule.hasMainPathContinuations() && rule.hasMainPathEndings() &&
                parentState.topologyGroup().equals(childMetadata.topologyGroup()) &&
                parentState.mainPathPiecesInTopologyGroup() >= parentState.mainPathTargetInTopologyGroup()) {
            return Optional.of("main_path_ending_required");
        }
        return Optional.empty();
    }

    private TopologyGroupProgress nextTopologyGroupProgress(MKDungeonPieceState parentState,
                                                            MKJigsawPieceMetadata childMetadata,
                                                            boolean nextOnMainPath, RandomSource random) {
        if (!nextOnMainPath || childMetadata.topologyGroup().isBlank()) {
            return new TopologyGroupProgress(parentState.topologyGroup(),
                    parentState.mainPathPiecesInTopologyGroup(),
                    parentState.mainPathTargetInTopologyGroup());
        }
        if (parentState.topologyGroup().equals(childMetadata.topologyGroup())) {
            return new TopologyGroupProgress(parentState.topologyGroup(),
                    parentState.mainPathPiecesInTopologyGroup() + 1,
                    parentState.mainPathTargetInTopologyGroup());
        }
        int target = chooseMainPathTarget(childMetadata.topologyGroup(), random);
        return new TopologyGroupProgress(childMetadata.topologyGroup(), 1, target);
    }

    private int chooseMainPathTarget(String topologyGroup, RandomSource random) {
        Optional<MKDungeonTopologyGroupRule> ruleOpt = settings.topologyGroupRule(topologyGroup);
        if (ruleOpt.isEmpty()) {
            return 0;
        }
        MKDungeonTopologyGroupRule rule = ruleOpt.get();
        if (rule.minMainPathPieces() == rule.maxMainPathPieces() || random == null) {
            return rule.maxMainPathPieces();
        }
        return random.nextInt(rule.maxMainPathPieces() - rule.minMainPathPieces() + 1) + rule.minMainPathPieces();
    }

    private record TopologyGroupProgress(String topologyGroup, int piecesInTopologyGroup,
                                         int targetInTopologyGroup) {
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

    private boolean isTopCapConnector(MKConnectorRole role) {
        return role == MKConnectorRole.TOP_CAP_FORWARD || role == MKConnectorRole.TOP_CAP_BACK;
    }
}

