package com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceLinkSchema;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceRegionSchema;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceRoleSchema;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceTopologySchema;
import com.chaosbuffalo.mkworkspaceextensions.MKWorkspaceExtensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class HubSpokePlanner implements MKWorkspacePlanner {
    public static final ResourceLocation PLANNER_ID = MKWorkspaceExtensions.id("hub_spoke");

    public static final String LAYOUT_PRESET = "cardinal_with_corner_branches";
    public static final String CENTER_SLOT = "hub_spoke.center";
    public static final String SPOKE_SLOT = "hub_spoke.spoke.shared";
    public static final String CORNER_SLOT = "hub_spoke.corner.shared";
    public static final String CENTER_BASE_NAME = "hub_spoke_center";
    public static final String SPOKE_BASE_NAME = "hub_spoke_spoke";
    public static final String CORNER_BASE_NAME = "hub_spoke_corner";
    public static final String PRIMARY_DIMENSION_STACK_ID = "hub_spoke.center";

    public static final String FLAT_PLATFORM_KIND_TAG = "workspace_flat_platform_kind";
    public static final String FLAT_PLATFORM_CORNER_TAG = "workspace_flat_platform_corner";
    public static final String HUB_SPOKE_LAYOUT_PRESET_TAG = "workspace_hub_spoke_layout_preset";
    public static final String HUB_SPOKE_ARCHETYPE_SLOT_TAG = "workspace_hub_spoke_archetype_slot";
    public static final String HUB_SPOKE_CONCRETE_SLOT_TAG = "workspace_hub_spoke_concrete_slot";
    public static final String HUB_SPOKE_DIRECTION_TAG = "workspace_hub_spoke_direction";

    private static final int CENTER_SIZE = 15;
    private static final int SPOKE_WIDTH = 5;
    private static final int SPOKE_LENGTH = 9;
    private static final int CORNER_SIZE = 7;
    public static final int MIN_PLATFORM_HEIGHT = 3;
    public static final int MAX_PLATFORM_HEIGHT = 48;
    private static final int DEFAULT_PLATFORM_HEIGHT = 3;
    private static final int OPENING_WIDTH = 3;
    private static final int OPENING_HEIGHT = 2;
    private static final int OPENING_VERTICAL_OFFSET = 1;
    private static final int CORNER_LATERAL_OFFSET = 5;
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String SLOT_POOL_PREFIX = "hub_spoke_slots/";

    private record SpokeDefinition(String suffix, Direction outwardFacing, String rotation) {
        private String slotId() {
            return "hub_spoke.spoke." + suffix;
        }

        private String pieceName() {
            return SPOKE_BASE_NAME + "_" + suffix;
        }
    }

    private record CornerDefinition(String suffix, Direction outwardFacing, int lateralOffset, String rotation) {
        private String slotId() {
            return "hub_spoke.corner." + suffix;
        }

        private String pieceName() {
            return CORNER_BASE_NAME + "_" + suffix;
        }
    }

    public enum CornerAttachmentMode {
        CENTER,
        SPOKE
    }

    private static final List<SpokeDefinition> SPOKES = List.of(
            new SpokeDefinition("north", Direction.NORTH, MKWorkspaceTemplateReuseTags.ROTATION_NONE),
            new SpokeDefinition("east", Direction.EAST, MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90),
            new SpokeDefinition("south", Direction.SOUTH, MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180),
            new SpokeDefinition("west", Direction.WEST, MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270)
    );

    private static final List<CornerDefinition> CORNERS = List.of(
            new CornerDefinition("north_west", Direction.NORTH, -CORNER_LATERAL_OFFSET,
                    MKWorkspaceTemplateReuseTags.ROTATION_NONE),
            new CornerDefinition("north_east", Direction.NORTH, CORNER_LATERAL_OFFSET,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90),
            new CornerDefinition("south_east", Direction.SOUTH, CORNER_LATERAL_OFFSET,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180),
            new CornerDefinition("south_west", Direction.SOUTH, -CORNER_LATERAL_OFFSET,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270)
    );

    @Override
    public ResourceLocation plannerId() {
        return PLANNER_ID;
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                PLANNER_ID,
                List.of(
                        new MKWorkspaceRegionSchema("hub_spoke.center", "hub", true),
                        new MKWorkspaceRegionSchema("hub_spoke.spokes", "spoke", true),
                        new MKWorkspaceRegionSchema("hub_spoke.corners", "corner_branch", true)
                ),
                List.of(
                        new MKWorkspaceSlotSchema(CENTER_SLOT, "hub_spoke.center", "center",
                                "hub_spoke.center", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema(SPOKE_SLOT, "hub_spoke.spokes", "spoke",
                                "hub_spoke.spoke", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema(CORNER_SLOT, "hub_spoke.corners", "corner",
                                "hub_spoke.corner", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("hub_spoke.corner.north_west", "hub_spoke.corners", "corner",
                                "hub_spoke.corner", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("hub_spoke.corner.north_east", "hub_spoke.corners", "corner",
                                "hub_spoke.corner", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("hub_spoke.corner.south_east", "hub_spoke.corners", "corner",
                                "hub_spoke.corner", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("hub_spoke.corner.south_west", "hub_spoke.corners", "corner",
                                "hub_spoke.corner", MKWorkspaceSlotSchema.Repeat.DERIVED)
                ),
                List.of(
                        new MKWorkspaceLinkSchema("hub_spoke.center_to_spokes", CENTER_SLOT, SPOKE_SLOT,
                                "cardinal_spokes"),
                        new MKWorkspaceLinkSchema("hub_spoke.center_to_corners", CENTER_SLOT, CORNER_SLOT,
                                "corner_branches")
                ),
                List.of(
                        new MKWorkspaceRoleSchema("hub_spoke.center", "hub", "room",
                                false, true, Set.of("flat_platform", "start")),
                        new MKWorkspaceRoleSchema("hub_spoke.spoke", "spoke", "room",
                                false, false, Set.of("flat_platform", "cardinal")),
                        new MKWorkspaceRoleSchema("hub_spoke.corner", "corner", "room",
                                true, false, Set.of("flat_platform", "corner_branch"))
                )
        );
    }

    public static MKWorkspaceTopologyProfile defaultTopologyProfile() {
        return HubSpokePlannerSettings.defaults().applyTo(new MKWorkspaceTopologyProfile(
                PLANNER_ID,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                TerrainAdjustment.NONE
        ));
    }

    @Override
    public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
        return defaultTopologyProfile();
    }

    public static List<MKWorkspaceRoomFamilyDefinition> defaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return List.of(
                MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                        CENTER_BASE_NAME,
                        MKWorkspaceTopologySlotMetadata.explicit(CENTER_SLOT, "hub", "room", false),
                        "",
                        false,
                        CENTER_SIZE,
                        CENTER_SIZE,
                        DEFAULT_PLATFORM_HEIGHT,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        null,
                        null),
                MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                        SPOKE_BASE_NAME,
                        MKWorkspaceTopologySlotMetadata.explicit(SPOKE_SLOT, "spoke", "room", false),
                        "",
                        false,
                        SPOKE_WIDTH,
                        SPOKE_LENGTH,
                        DEFAULT_PLATFORM_HEIGHT,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        null,
                        null),
                MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                        CORNER_BASE_NAME,
                        MKWorkspaceTopologySlotMetadata.explicit(CORNER_SLOT, "corner", "room", true),
                        "",
                        false,
                        CORNER_SIZE,
                        CORNER_SIZE,
                        DEFAULT_PLATFORM_HEIGHT,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        null,
                        null)
        );
    }

    @Override
    public List<MKWorkspaceRoomFamilyDefinition> createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return defaultRoomFamilyDefinitions(dimensions);
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(workspace.topologyProfile());
        MKWorkspaceRoomFamilyDefinition centerFamily = familyOrDefault(workspace, CENTER_SLOT);
        MKWorkspaceRoomFamilyDefinition spokeFamily = familyOrDefault(workspace, SPOKE_SLOT);
        MKWorkspaceRoomFamilyDefinition sharedCornerFamily = familyOrDefault(workspace, CORNER_SLOT);
        Map<String, MKWorkspaceRoomFamilyDefinition> cornerFamilies = cornerFamilies(workspace, settings,
                sharedCornerFamily);
        CornerAttachmentMode cornerAttachmentMode = cornerAttachmentMode(centerFamily.roomWidth(),
                spokeFamily.roomWidth(), maxCornerWidth(cornerFamilies));
        Map<Direction, HubSpokePlannerSettings.SpokeTemplate> templateAssignments = settings.templateAssignments();
        Map<String, Direction> cornerAnchors = cornerAnchorDirections(templateAssignments.keySet());
        Map<String, SpokeDefinition> templateSources = sourceSpokesByTemplate(settings);

        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        pieces.add(createCenterPiece(workspace, centerFamily, spokeFamily, cornerFamilies, cornerAttachmentMode,
                templateAssignments.keySet(), cornerAnchors));
        for (SpokeDefinition spoke : SPOKES) {
            HubSpokePlannerSettings.SpokeTemplate template = templateAssignments.get(spoke.outwardFacing());
            if (template == null) {
                continue;
            }
            SpokeDefinition source = templateSources.getOrDefault(template.baseName(), SPOKES.getFirst());
            MKPlannedPiece piece = createSpokePiece(workspace, spokeFamily, cornerFamilies, template, spoke,
                    cornerAttachmentMode, cornerAnchors);
            pieces.add(withTemplateReuse(piece, template.pieceName(source.outwardFacing()),
                    rotationFrom(source.outwardFacing(), spoke.outwardFacing()), spoke == source));
        }
        Map<String, CornerDefinition> cornerSources = cornerSourcesByTemplateSlot(settings, cornerAnchors);
        for (CornerDefinition corner : CORNERS) {
            Direction anchorDirection = cornerAnchors.get(corner.suffix());
            if (anchorDirection == null) {
                continue;
            }
            String templateSlotId = settings.stableCornerTemplateSlot(corner.slotId());
            MKWorkspaceRoomFamilyDefinition cornerFamily = cornerFamilies.getOrDefault(corner.suffix(),
                    sharedCornerFamily);
            MKPlannedPiece piece = createCornerPiece(workspace, cornerFamily, corner, cornerAttachmentMode,
                    anchorDirection, templateSlotId);
            CornerDefinition source = cornerSources.getOrDefault(templateSlotId, corner);
            pieces.add(withTemplateReuse(withExportCrop(piece), source.pieceName(),
                    rotationFromCorner(source, corner), corner == source));
        }
        return List.copyOf(pieces);
    }

    @Override
    public List<String> validateTopology(MKStructureWorkspace workspace) {
        return HubSpokePlannerSettings.from(workspace.topologyProfile()).validationErrors();
    }

    public static List<String> authoringTemplateBaseNames() {
        return List.of(CENTER_BASE_NAME, SPOKE_BASE_NAME + "_north", CORNER_BASE_NAME + "_north_west");
    }

    public static int defaultSpokeLength() {
        return SPOKE_LENGTH;
    }

    public static int defaultPlatformHeight() {
        return DEFAULT_PLATFORM_HEIGHT;
    }

    public static Map<String, Direction> cornerAnchorDirections(Set<Direction> generatedSpokeDirections) {
        LinkedHashMap<String, Direction> anchors = new LinkedHashMap<>();
        boolean hasNorthSouthAnchor = generatedSpokeDirections.contains(Direction.NORTH) ||
                generatedSpokeDirections.contains(Direction.SOUTH);
        if (hasNorthSouthAnchor) {
            if (generatedSpokeDirections.contains(Direction.NORTH)) {
                anchors.put("north_west", Direction.NORTH);
                anchors.put("north_east", Direction.NORTH);
            }
            if (generatedSpokeDirections.contains(Direction.SOUTH)) {
                anchors.put("south_east", Direction.SOUTH);
                anchors.put("south_west", Direction.SOUTH);
            }
            return Map.copyOf(anchors);
        }
        if (generatedSpokeDirections.contains(Direction.EAST)) {
            anchors.put("north_east", Direction.EAST);
            anchors.put("south_east", Direction.EAST);
        }
        if (generatedSpokeDirections.contains(Direction.WEST)) {
            anchors.put("south_west", Direction.WEST);
            anchors.put("north_west", Direction.WEST);
        }
        return Map.copyOf(anchors);
    }

    public static List<String> concreteSpokeSlots() {
        return SPOKES.stream().map(SpokeDefinition::slotId).toList();
    }

    public static List<String> concreteCornerSlots() {
        return CORNERS.stream().map(CornerDefinition::slotId).toList();
    }

    public static CornerAttachmentMode cornerAttachmentMode(int centerSize, int spokeWidth) {
        return cornerAttachmentMode(centerSize, spokeWidth, OPENING_WIDTH);
    }

    public static CornerAttachmentMode cornerAttachmentMode(int centerSize, int spokeWidth, int cornerSize) {
        return centerCornerLateralOffset(centerSize, spokeWidth, cornerSize) <= maxCenterConnectorOffset(centerSize) ?
                CornerAttachmentMode.CENTER : CornerAttachmentMode.SPOKE;
    }

    private MKPlannedPiece createCenterPiece(MKStructureWorkspace workspace, MKWorkspaceRoomFamilyDefinition family,
                                             MKWorkspaceRoomFamilyDefinition spokeFamily,
                                             Map<String, MKWorkspaceRoomFamilyDefinition> cornerFamilies,
                                             CornerAttachmentMode cornerAttachmentMode,
                                             Set<Direction> generatedSpokeDirections,
                                             Map<String, Direction> cornerAnchors) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        for (SpokeDefinition spoke : SPOKES) {
            if (!generatedSpokeDirections.contains(spoke.outwardFacing())) {
                continue;
            }
            connectors.add(platformConnector(MKConnectorRole.MAIN_BACK, spoke.outwardFacing(), 0,
                    slotPool(spoke.slotId()), null));
        }
        if (cornerAttachmentMode == CornerAttachmentMode.CENTER) {
            for (CornerDefinition corner : CORNERS) {
                Direction anchorDirection = cornerAnchors.get(corner.suffix());
                if (anchorDirection == null) {
                    continue;
                }
                MKWorkspaceRoomFamilyDefinition cornerFamily = cornerFamilies.get(corner.suffix());
                int signedOffset = centerCornerLateralSign(corner.suffix(), anchorDirection) *
                        centerCornerLateralOffset(family.roomWidth(), spokeFamily.roomWidth(),
                                cornerFamily.roomWidth());
                connectors.add(platformConnector(MKConnectorRole.BRANCH, anchorDirection, signedOffset,
                        slotPool(corner.slotId()), null));
            }
        }
        return new MKPlannedPiece(
                CENTER_SLOT,
                CENTER_BASE_NAME,
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                connectors,
                baseTags(workspace, CENTER_SLOT, CENTER_SLOT, CENTER_SLOT, "center", "center", "", true, false)
        );
    }

    private MKPlannedPiece createSpokePiece(MKStructureWorkspace workspace, MKWorkspaceRoomFamilyDefinition family,
                                            Map<String, MKWorkspaceRoomFamilyDefinition> cornerFamilies,
                                            HubSpokePlannerSettings.SpokeTemplate template, SpokeDefinition spoke,
                                            CornerAttachmentMode cornerAttachmentMode,
                                            Map<String, Direction> cornerAnchors) {
        Direction incomingFacing = spoke.outwardFacing().getOpposite();
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        connectors.add(platformConnector(MKConnectorRole.MAIN_FORWARD, incomingFacing, 0, EMPTY_POOL,
                slotPool(spoke.slotId())));
        if (cornerAttachmentMode == CornerAttachmentMode.SPOKE) {
            for (SpokeCornerTarget target : cornerTargetsForSpoke(spoke, cornerAnchors)) {
                MKWorkspaceRoomFamilyDefinition cornerFamily = cornerFamilies.get(target.corner().suffix());
                connectors.add(platformConnector(MKConnectorRole.BRANCH, target.facing(),
                            spokeCornerLateralOffset(template.length(), cornerFamily, target),
                            slotPool(target.corner().slotId()), null));
            }
        }
        return new MKPlannedPiece(
                SPOKE_SLOT,
                template.pieceName(spoke.outwardFacing()),
                spoke.outwardFacing().getAxis() == Direction.Axis.X ? template.length() : family.roomWidth(),
                spoke.outwardFacing().getAxis() == Direction.Axis.X ? family.roomWidth() : template.length(),
                template.height(),
                connectors,
                baseTags(workspace, SPOKE_SLOT, spoke.slotId(), template.baseName(), "spoke", "spoke",
                        spoke.suffix(), false, false)
        );
    }

    private MKPlannedPiece createCornerPiece(MKStructureWorkspace workspace, MKWorkspaceRoomFamilyDefinition family,
                                             CornerDefinition corner, CornerAttachmentMode cornerAttachmentMode,
                                             Direction anchorDirection, String stableSlotId) {
        Direction incomingFacing = cornerAttachmentMode == CornerAttachmentMode.CENTER
                ? anchorDirection.getOpposite()
                : cornerSpokeConnectorFacing(corner.suffix(), anchorDirection).getOpposite();
        Map<String, String> tags = baseTags(workspace, CORNER_SLOT, corner.slotId(), stableSlotId,
                "corner_chamfer", "corner", corner.suffix(), false, true);
        tags.put(FLAT_PLATFORM_CORNER_TAG, corner.suffix());
        return new MKPlannedPiece(
                CORNER_SLOT,
                corner.pieceName(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                List.of(platformConnector(MKConnectorRole.BRANCH, incomingFacing, 0, EMPTY_POOL,
                        slotPool(corner.slotId()))),
                tags
        );
    }

    public static int centerCornerLateralOffset(int centerSize, int spokeWidth, int cornerSize) {
        int outerAlignedOffset = Math.max(0, (centerSize - cornerSize) / 2);
        int spokeAvoidingOffset = Math.max(0, (spokeWidth + cornerSize) / 2);
        return Math.max(outerAlignedOffset, spokeAvoidingOffset);
    }

    private static int maxCenterConnectorOffset(int centerSize) {
        return Math.max(0, (centerSize / 2) - (OPENING_WIDTH / 2));
    }

    private int spokeCornerLateralOffset(int spokeLength,
                                         MKWorkspaceRoomFamilyDefinition cornerFamily,
                                         SpokeCornerTarget target) {
        int cornerSpan = target.facing().getAxis() == Direction.Axis.X ?
                cornerFamily.roomLength() : cornerFamily.roomWidth();
        return spokeCornerLateralOffset(spokeLength, cornerSpan, target.anchorDirection());
    }

    public static int spokeCornerLateralOffset(int spokeLength, int cornerSpan, String cornerSuffix) {
        int offset = Math.max(0, (spokeLength - cornerSpan) / 2);
        return switch (cornerSuffix) {
            case "north_west", "north_east" -> offset;
            case "south_east", "south_west" -> -offset;
            default -> 0;
        };
    }

    public static int spokeCornerLateralOffset(int spokeLength, int cornerSpan, Direction anchorDirection) {
        int offset = Math.max(0, (spokeLength - cornerSpan) / 2);
        return switch (anchorDirection) {
            case NORTH, EAST -> offset;
            case SOUTH, WEST -> -offset;
            default -> 0;
        };
    }

    private List<SpokeCornerTarget> cornerTargetsForSpoke(SpokeDefinition spoke,
                                                          Map<String, Direction> cornerAnchors) {
        ArrayList<SpokeCornerTarget> targets = new ArrayList<>();
        for (String cornerSuffix : cornerSuffixesForAnchor(spoke.outwardFacing())) {
            Direction anchorDirection = cornerAnchors.get(cornerSuffix);
            if (anchorDirection == spoke.outwardFacing()) {
                CornerDefinition corner = corner(cornerSuffix).orElseThrow();
                targets.add(new SpokeCornerTarget(corner,
                        cornerSpokeConnectorFacing(corner.suffix(), anchorDirection), anchorDirection));
            }
        }
        return List.copyOf(targets);
    }

    private List<String> cornerSuffixesForAnchor(Direction anchorDirection) {
        return switch (anchorDirection) {
            case NORTH -> List.of("north_west", "north_east");
            case SOUTH -> List.of("south_west", "south_east");
            case EAST -> List.of("north_east", "south_east");
            case WEST -> List.of("north_west", "south_west");
            default -> List.of();
        };
    }

    public static Direction cornerSpokeConnectorFacing(String cornerSuffix, Direction anchorDirection) {
        return switch (anchorDirection) {
            case NORTH -> switch (cornerSuffix) {
                case "north_west" -> Direction.WEST;
                case "north_east" -> Direction.EAST;
                default -> Direction.NORTH;
            };
            case SOUTH -> switch (cornerSuffix) {
                case "south_west" -> Direction.WEST;
                case "south_east" -> Direction.EAST;
                default -> Direction.SOUTH;
            };
            case EAST -> switch (cornerSuffix) {
                case "north_east" -> Direction.NORTH;
                case "south_east" -> Direction.SOUTH;
                default -> Direction.EAST;
            };
            case WEST -> switch (cornerSuffix) {
                case "north_west" -> Direction.NORTH;
                case "south_west" -> Direction.SOUTH;
                default -> Direction.WEST;
            };
            default -> anchorDirection;
        };
    }

    private int centerCornerLateralSign(String cornerSuffix, Direction anchorDirection) {
        return switch (anchorDirection) {
            case NORTH, SOUTH -> switch (cornerSuffix) {
                case "north_west", "south_west" -> -1;
                case "north_east", "south_east" -> 1;
                default -> 0;
            };
            case EAST, WEST -> switch (cornerSuffix) {
                case "north_west", "north_east" -> -1;
                case "south_east", "south_west" -> 1;
                default -> 0;
            };
            default -> 0;
        };
    }

    private Optional<CornerDefinition> corner(String suffix) {
        return CORNERS.stream()
                .filter(corner -> suffix.equals(corner.suffix()))
                .findFirst();
    }

    private Map<String, CornerDefinition> cornerSourcesByTemplateSlot(HubSpokePlannerSettings settings,
                                                                      Map<String, Direction> cornerAnchors) {
        LinkedHashMap<String, CornerDefinition> sources = new LinkedHashMap<>();
        for (CornerDefinition corner : CORNERS) {
            if (!cornerAnchors.containsKey(corner.suffix())) {
                continue;
            }
            sources.putIfAbsent(settings.stableCornerTemplateSlot(corner.slotId()), corner);
        }
        return Map.copyOf(sources);
    }

    private Map<String, SpokeDefinition> sourceSpokesByTemplate(HubSpokePlannerSettings settings) {
        HashMap<String, SpokeDefinition> sources = new HashMap<>();
        Map<Direction, HubSpokePlannerSettings.SpokeTemplate> assignments = settings.templateAssignments();
        for (SpokeDefinition spoke : SPOKES) {
            HubSpokePlannerSettings.SpokeTemplate template = assignments.get(spoke.outwardFacing());
            if (template != null) {
                sources.putIfAbsent(template.baseName(), spoke);
            }
        }
        return sources;
    }

    private String rotationFrom(Direction source, Direction target) {
        int steps = Math.floorMod(horizontalIndex(target) - horizontalIndex(source), 4);
        return switch (steps) {
            case 1 -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90;
            case 2 -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180;
            case 3 -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270;
            default -> MKWorkspaceTemplateReuseTags.ROTATION_NONE;
        };
    }

    private int horizontalIndex(Direction direction) {
        return switch (direction) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private String rotationFromCorner(CornerDefinition source, CornerDefinition target) {
        int steps = Math.floorMod(cornerIndex(target) - cornerIndex(source), 4);
        return switch (steps) {
            case 1 -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90;
            case 2 -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180;
            case 3 -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270;
            default -> MKWorkspaceTemplateReuseTags.ROTATION_NONE;
        };
    }

    private int cornerIndex(CornerDefinition corner) {
        return switch (corner.suffix()) {
            case "north_east" -> 1;
            case "south_east" -> 2;
            case "south_west" -> 3;
            default -> 0;
        };
    }

    private record SpokeCornerTarget(CornerDefinition corner, Direction facing, Direction anchorDirection) {
    }

    private MKPlannedConnector platformConnector(MKConnectorRole role, Direction facing, int lateralOffset,
                                                 String targetPool, String incomingPool) {
        return new MKPlannedConnector(role, facing,
                OPENING_WIDTH, OPENING_HEIGHT, lateralOffset, OPENING_VERTICAL_OFFSET, targetPool, incomingPool,
                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION);
    }

    private Map<String, String> baseTags(MKStructureWorkspace workspace, String archetypeSlotId, String concreteSlotId,
                                         String stableTemplateSlotId, String platformKind, String templateKind,
                                         String direction, boolean start, boolean terminal) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", archetypeSlotId);
        tags.put("workspace_topology_slot_id", concreteSlotId);
        tags.put("workspace_topology_role_id", archetypeSlotId);
        tags.put("workspace_piece_kind", "instance");
        tags.put("tower_piece_kind", "room");
        tags.put(HUB_SPOKE_LAYOUT_PRESET_TAG, LAYOUT_PRESET);
        tags.put(HUB_SPOKE_ARCHETYPE_SLOT_TAG, archetypeSlotId);
        tags.put(HUB_SPOKE_CONCRETE_SLOT_TAG, concreteSlotId);
        tags.put(HUB_SPOKE_DIRECTION_TAG, direction);
        tags.put(FLAT_PLATFORM_KIND_TAG, platformKind);
        tags.put("workspace_horizontal_extrusion_mode", MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION.getSerializedName());
        MKWorkspaceStableSlotIdentity.apply(tags, "hub_spoke_" + templateKind, stableTemplateSlotId);
        new MKWorkspaceRuntimePieceInfo(start, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, terminal, false, "hub_spoke", false, terminal).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, workspace.palette());
        return tags;
    }

    private MKWorkspaceRoomFamilyDefinition familyOrDefault(MKStructureWorkspace workspace, String topologySlotId) {
        Optional<MKWorkspaceRoomFamilyDefinition> existing = workspace.familyDefinitions().stream()
                .filter(family -> topologySlotId.equals(family.topologySlotId()))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        return defaultRoomFamilyDefinitions(workspace.dimensions()).stream()
                .filter(family -> topologySlotId.equals(family.topologySlotId()))
                .findFirst()
                .orElseThrow();
    }

    private Map<String, MKWorkspaceRoomFamilyDefinition> cornerFamilies(MKStructureWorkspace workspace,
                                                                        HubSpokePlannerSettings settings,
                                                                        MKWorkspaceRoomFamilyDefinition sharedFamily) {
        LinkedHashMap<String, MKWorkspaceRoomFamilyDefinition> families = new LinkedHashMap<>();
        for (CornerDefinition corner : CORNERS) {
            String templateSlotId = settings.stableCornerTemplateSlot(corner.slotId());
            MKWorkspaceRoomFamilyDefinition family = CORNER_SLOT.equals(templateSlotId) ?
                    sharedFamily :
                    workspace.familyDefinitions().stream()
                            .filter(candidate -> templateSlotId.equals(candidate.topologySlotId()))
                            .findFirst()
                            .orElse(sharedFamily);
            families.put(corner.suffix(), family);
        }
        return Map.copyOf(families);
    }

    private int maxCornerWidth(Map<String, MKWorkspaceRoomFamilyDefinition> cornerFamilies) {
        return cornerFamilies.values().stream()
                .mapToInt(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .max()
                .orElse(OPENING_WIDTH);
    }

    private MKPlannedPiece withTemplateReuse(MKPlannedPiece piece, String sourceId, String rotation,
                                             boolean authoringSource) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG, sourceId);
        tags.put(MKWorkspaceTemplateReuseTags.ROTATION_TAG, rotation);
        tags.put(MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG,
                MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT);
        tags.put(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, Boolean.toString(authoringSource));
        return new MKPlannedPiece(piece.roleId(), piece.pieceName(), piece.interiorWidth(), piece.interiorLength(),
                piece.interiorHeight(), piece.connectors(), tags, piece.plannerId());
    }

    private MKPlannedPiece withExportCrop(MKPlannedPiece piece) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put(MKWorkspaceTemplateReuseTags.CROP_MODE_TAG,
                MKWorkspaceTemplateReuseTags.CROP_MODE_NON_STRUCTURE_VOID);
        return new MKPlannedPiece(piece.roleId(), piece.pieceName(), piece.interiorWidth(), piece.interiorLength(),
                piece.interiorHeight(), piece.connectors(), tags, piece.plannerId());
    }

    private String slotPool(String topologySlotId) {
        return SLOT_POOL_PREFIX + topologySlotId.replace('.', '/');
    }
}
