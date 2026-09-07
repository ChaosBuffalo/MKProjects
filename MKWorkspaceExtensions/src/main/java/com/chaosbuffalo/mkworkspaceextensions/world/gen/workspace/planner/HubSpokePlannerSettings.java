package com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePlannerSettingsEntry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record HubSpokePlannerSettings(
        List<SpokeTemplate> spokeTemplates,
        CornerTemplateMode cornerTemplateMode,
        boolean uniqueNorthWestCorner,
        boolean uniqueNorthEastCorner,
        boolean uniqueSouthEastCorner,
        boolean uniqueSouthWestCorner
) {
    public static final String SCOPE_ID = "hub_spoke";
    public static final int MIN_SPOKE_LENGTH = 3;
    public static final int MAX_SPOKE_LENGTH = 31;
    public static final int MAX_SPOKE_TEMPLATES = 4;

    private static final List<Direction> CARDINAL_DIRECTIONS = List.of(
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public static final Codec<HubSpokePlannerSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpokeTemplate.CODEC.listOf().optionalFieldOf("spoke_templates", List.of(defaultSpokeTemplate()))
                    .forGetter(HubSpokePlannerSettings::spokeTemplates),
            CornerTemplateMode.CODEC.optionalFieldOf("corner_template_mode", CornerTemplateMode.CUSTOM)
                    .forGetter(HubSpokePlannerSettings::cornerTemplateMode),
            Codec.BOOL.optionalFieldOf("unique_north_west_corner", false)
                    .forGetter(HubSpokePlannerSettings::uniqueNorthWestCorner),
            Codec.BOOL.optionalFieldOf("unique_north_east_corner", false)
                    .forGetter(HubSpokePlannerSettings::uniqueNorthEastCorner),
            Codec.BOOL.optionalFieldOf("unique_south_east_corner", false)
                    .forGetter(HubSpokePlannerSettings::uniqueSouthEastCorner),
            Codec.BOOL.optionalFieldOf("unique_south_west_corner", false)
                    .forGetter(HubSpokePlannerSettings::uniqueSouthWestCorner)
    ).apply(instance, HubSpokePlannerSettings::new));

    public HubSpokePlannerSettings(List<SpokeTemplate> spokeTemplates) {
        this(spokeTemplates, CornerTemplateMode.SHARED, false, false, false, false);
    }

    public HubSpokePlannerSettings(List<SpokeTemplate> spokeTemplates, CornerTemplateMode cornerTemplateMode) {
        this(spokeTemplates, cornerTemplateMode,
                cornerTemplateMode == CornerTemplateMode.UNIQUE,
                cornerTemplateMode == CornerTemplateMode.UNIQUE,
                cornerTemplateMode == CornerTemplateMode.UNIQUE,
                cornerTemplateMode == CornerTemplateMode.UNIQUE);
    }

    public HubSpokePlannerSettings(List<SpokeTemplate> spokeTemplates, boolean uniqueNorthWestCorner,
                                   boolean uniqueNorthEastCorner, boolean uniqueSouthEastCorner,
                                   boolean uniqueSouthWestCorner) {
        this(spokeTemplates, modeFromBooleans(uniqueNorthWestCorner, uniqueNorthEastCorner,
                        uniqueSouthEastCorner, uniqueSouthWestCorner),
                uniqueNorthWestCorner, uniqueNorthEastCorner, uniqueSouthEastCorner, uniqueSouthWestCorner);
    }

    public HubSpokePlannerSettings {
        spokeTemplates = normalizeTemplates(spokeTemplates);
        if (cornerTemplateMode == null || cornerTemplateMode == CornerTemplateMode.CUSTOM) {
            cornerTemplateMode = modeFromBooleans(uniqueNorthWestCorner, uniqueNorthEastCorner,
                    uniqueSouthEastCorner, uniqueSouthWestCorner);
        }
        if (cornerTemplateMode == CornerTemplateMode.SHARED || cornerTemplateMode == CornerTemplateMode.PAIRED) {
            uniqueNorthWestCorner = false;
            uniqueNorthEastCorner = false;
            uniqueSouthEastCorner = false;
            uniqueSouthWestCorner = false;
        } else if (cornerTemplateMode == CornerTemplateMode.UNIQUE) {
            uniqueNorthWestCorner = true;
            uniqueNorthEastCorner = true;
            uniqueSouthEastCorner = true;
            uniqueSouthWestCorner = true;
        }
    }

    public static HubSpokePlannerSettings defaults() {
        return new HubSpokePlannerSettings(List.of(defaultSpokeTemplate()));
    }

    public static SpokeTemplate defaultSpokeTemplate() {
        return new SpokeTemplate(
                HubSpokePlanner.SPOKE_BASE_NAME,
                "Spoke 1",
                HubSpokePlanner.defaultSpokeLength(),
                HubSpokePlanner.defaultPlatformHeight(),
                CARDINAL_DIRECTIONS);
    }

    public static HubSpokePlannerSettings from(MKWorkspaceTopologyProfile profile) {
        return profile.plannerSettingsEntry(HubSpokePlanner.PLANNER_ID, SCOPE_ID)
                .map(entry -> MKWorkspaceCodecs.parseNbt(CODEC, entry.settings(), "hub spoke planner settings"))
                .orElseGet(HubSpokePlannerSettings::defaults);
    }

    public MKWorkspaceTopologyProfile applyTo(MKWorkspaceTopologyProfile profile) {
        return profile.withPlannerSettingsEntry(plannerSettingsEntry());
    }

    public MKWorkspacePlannerSettingsEntry plannerSettingsEntry() {
        return new MKWorkspacePlannerSettingsEntry(
                HubSpokePlanner.PLANNER_ID,
                SCOPE_ID,
                MKWorkspaceCodecs.encodeNbt(CODEC, this, "hub spoke planner settings"));
    }

    public Optional<SpokeTemplate> templateFor(Direction direction) {
        return Optional.ofNullable(templateAssignments().get(direction));
    }

    public Map<Direction, SpokeTemplate> templateAssignments() {
        EnumMap<Direction, SpokeTemplate> assignments = new EnumMap<>(Direction.class);
        ArrayList<SpokeTemplate> scarceFirst = new ArrayList<>(spokeTemplates);
        scarceFirst.sort(Comparator.comparingInt(template ->
                template.validDirections().isEmpty() ? Integer.MAX_VALUE : template.validDirections().size()));
        for (SpokeTemplate template : scarceFirst) {
            for (Direction direction : CARDINAL_DIRECTIONS) {
                if (template.validDirections().contains(direction) && !assignments.containsKey(direction)) {
                    assignments.put(direction, template);
                    break;
                }
            }
        }
        for (Direction direction : CARDINAL_DIRECTIONS) {
            if (assignments.containsKey(direction)) {
                continue;
            }
            spokeTemplates.stream()
                    .filter(template -> template.validDirections().contains(direction))
                    .findFirst()
                    .ifPresent(template -> assignments.put(direction, template));
        }
        return Map.copyOf(assignments);
    }

    public Optional<SpokeTemplate> templateByBaseName(String baseName) {
        return spokeTemplates.stream()
                .filter(template -> template.baseName().equals(baseName))
                .findFirst();
    }

    public HubSpokePlannerSettings withSpokeTemplate(int index, SpokeTemplate updatedTemplate) {
        if (index < 0 || index >= spokeTemplates.size() || updatedTemplate == null) {
            return this;
        }
        ArrayList<SpokeTemplate> updated = new ArrayList<>(spokeTemplates);
        updated.set(index, updatedTemplate);
        return new HubSpokePlannerSettings(updated, cornerTemplateMode, uniqueNorthWestCorner, uniqueNorthEastCorner,
                uniqueSouthEastCorner, uniqueSouthWestCorner);
    }

    public HubSpokePlannerSettings withAddedSpokeTemplate() {
        return withAddedSpokeTemplate(spokeTemplates.getLast());
    }

    public HubSpokePlannerSettings withAddedSpokeTemplate(SpokeTemplate sourceTemplate) {
        if (spokeTemplates.size() >= MAX_SPOKE_TEMPLATES) {
            return this;
        }
        SpokeTemplate source = sourceTemplate == null ? spokeTemplates.getLast() : sourceTemplate;
        SpokeTemplate added = new SpokeTemplate(
                source.baseName(),
                source.label(),
                source.length(),
                source.height(),
                source.validDirections());
        ArrayList<SpokeTemplate> updated = new ArrayList<>(spokeTemplates);
        updated.add(added);
        return new HubSpokePlannerSettings(updated, cornerTemplateMode, uniqueNorthWestCorner, uniqueNorthEastCorner,
                uniqueSouthEastCorner, uniqueSouthWestCorner);
    }

    public HubSpokePlannerSettings withRemovedSpokeTemplate(int index) {
        if (spokeTemplates.size() <= 1 || index < 0 || index >= spokeTemplates.size()) {
            return this;
        }
        ArrayList<SpokeTemplate> updated = new ArrayList<>(spokeTemplates);
        updated.remove(index);
        return new HubSpokePlannerSettings(updated, cornerTemplateMode, uniqueNorthWestCorner, uniqueNorthEastCorner,
                uniqueSouthEastCorner, uniqueSouthWestCorner);
    }

    public boolean uniqueCorner(String topologySlotId) {
        return switch (cornerTemplateMode) {
            case SHARED -> false;
            case PAIRED -> switch (topologySlotId) {
                case "hub_spoke.corner.north_west", "hub_spoke.corner.north_east" -> true;
                default -> false;
            };
            case UNIQUE -> HubSpokePlanner.concreteCornerSlots().contains(topologySlotId);
            case CUSTOM -> switch (topologySlotId) {
                case "hub_spoke.corner.north_west" -> uniqueNorthWestCorner;
                case "hub_spoke.corner.north_east" -> uniqueNorthEastCorner;
                case "hub_spoke.corner.south_east" -> uniqueSouthEastCorner;
                case "hub_spoke.corner.south_west" -> uniqueSouthWestCorner;
                default -> false;
            };
        };
    }

    public boolean anySharedCorner() {
        return switch (cornerTemplateMode) {
            case SHARED -> true;
            case PAIRED, UNIQUE -> false;
            case CUSTOM -> !uniqueNorthWestCorner || !uniqueNorthEastCorner ||
                    !uniqueSouthEastCorner || !uniqueSouthWestCorner;
        };
    }

    public String stableCornerTemplateSlot(String topologySlotId) {
        return switch (cornerTemplateMode) {
            case SHARED -> HubSpokePlanner.CORNER_SLOT;
            case PAIRED -> switch (topologySlotId) {
                case "hub_spoke.corner.south_east" -> "hub_spoke.corner.north_west";
                case "hub_spoke.corner.south_west" -> "hub_spoke.corner.north_east";
                case "hub_spoke.corner.north_west", "hub_spoke.corner.north_east" -> topologySlotId;
                default -> HubSpokePlanner.CORNER_SLOT;
            };
            case UNIQUE -> HubSpokePlanner.concreteCornerSlots().contains(topologySlotId) ?
                    topologySlotId : HubSpokePlanner.CORNER_SLOT;
            case CUSTOM -> uniqueCorner(topologySlotId) ? topologySlotId : HubSpokePlanner.CORNER_SLOT;
        };
    }

    public HubSpokePlannerSettings withCornerTemplateMode(CornerTemplateMode cornerTemplateMode) {
        return new HubSpokePlannerSettings(spokeTemplates, cornerTemplateMode);
    }

    public HubSpokePlannerSettings withCornerMode(String topologySlotId, boolean unique) {
        return switch (topologySlotId) {
            case "hub_spoke.corner.north_west" -> new HubSpokePlannerSettings(spokeTemplates, CornerTemplateMode.CUSTOM,
                    unique,
                    uniqueNorthEastCorner, uniqueSouthEastCorner, uniqueSouthWestCorner);
            case "hub_spoke.corner.north_east" -> new HubSpokePlannerSettings(spokeTemplates, CornerTemplateMode.CUSTOM,
                    uniqueNorthWestCorner, unique, uniqueSouthEastCorner, uniqueSouthWestCorner);
            case "hub_spoke.corner.south_east" -> new HubSpokePlannerSettings(spokeTemplates, CornerTemplateMode.CUSTOM,
                    uniqueNorthWestCorner, uniqueNorthEastCorner, unique, uniqueSouthWestCorner);
            case "hub_spoke.corner.south_west" -> new HubSpokePlannerSettings(spokeTemplates, CornerTemplateMode.CUSTOM,
                    uniqueNorthWestCorner, uniqueNorthEastCorner, uniqueSouthEastCorner, unique);
            default -> this;
        };
    }

    public HubSpokePlannerSettings withCornerModes(boolean northWest, boolean northEast,
                                                   boolean southEast, boolean southWest) {
        return new HubSpokePlannerSettings(spokeTemplates, CornerTemplateMode.CUSTOM,
                northWest, northEast, southEast, southWest);
    }

    public List<String> validationErrors() {
        return List.of();
    }

    private static List<SpokeTemplate> normalizeTemplates(List<SpokeTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return List.of(defaultSpokeTemplate());
        }
        ArrayList<SpokeTemplate> normalized = new ArrayList<>();
        LinkedHashSet<String> usedBaseNames = new LinkedHashSet<>();
        for (int index = 0; index < templates.size(); index++) {
            SpokeTemplate template = templates.get(index);
            if (template == null) {
                continue;
            }
            String baseName = sanitizeBaseName(template.baseName(), index, usedBaseNames);
            usedBaseNames.add(baseName);
            normalized.add(template.withIdentity(baseName, sanitizeLabel(template.label(), index)));
        }
        return normalized.isEmpty() ? List.of(defaultSpokeTemplate()) : List.copyOf(normalized);
    }

    private static String sanitizeBaseName(String baseName, int index, LinkedHashSet<String> usedBaseNames) {
        String normalized = baseName == null || baseName.isBlank() ?
                HubSpokePlanner.SPOKE_BASE_NAME + "_" + (index + 1) : baseName.trim();
        if (usedBaseNames.contains(normalized)) {
            String prefix = normalized;
            int suffix = 2;
            while (usedBaseNames.contains(normalized)) {
                normalized = prefix + "_" + suffix;
                suffix++;
            }
        }
        return normalized;
    }

    private static String sanitizeLabel(String label, int index) {
        return label == null || label.isBlank() ? "Spoke " + (index + 1) : label.trim();
    }

    private static int clampOdd(int value, int min, int max) {
        int clamped = Math.max(min, Math.min(value, max));
        if (clamped % 2 == 0) {
            clamped = clamped == max ? clamped - 1 : clamped + 1;
        }
        return Math.max(min, Math.min(clamped, max));
    }

    private static CornerTemplateMode modeFromBooleans(boolean northWest, boolean northEast,
                                                       boolean southEast, boolean southWest) {
        if (!northWest && !northEast && !southEast && !southWest) {
            return CornerTemplateMode.SHARED;
        }
        if (northWest && northEast && southEast && southWest) {
            return CornerTemplateMode.UNIQUE;
        }
        return CornerTemplateMode.CUSTOM;
    }

    public enum CornerTemplateMode {
        SHARED("shared"),
        PAIRED("paired"),
        UNIQUE("unique"),
        CUSTOM("custom");

        public static final Codec<CornerTemplateMode> CODEC = Codec.STRING.xmap(
                value -> {
                    for (CornerTemplateMode mode : values()) {
                        if (mode.serializedName.equals(value)) {
                            return mode;
                        }
                    }
                    return CUSTOM;
                },
                CornerTemplateMode::serializedName
        );

        private final String serializedName;

        CornerTemplateMode(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }
    }

    public record SpokeTemplate(
            String baseName,
            String label,
            int length,
            int height,
            List<Direction> validDirections
    ) {
        public static final Codec<SpokeTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("base_name", HubSpokePlanner.SPOKE_BASE_NAME)
                        .forGetter(SpokeTemplate::baseName),
                Codec.STRING.optionalFieldOf("label", "Spoke 1")
                        .forGetter(SpokeTemplate::label),
                Codec.INT.optionalFieldOf("length", HubSpokePlanner.defaultSpokeLength())
                        .forGetter(SpokeTemplate::length),
                Codec.INT.optionalFieldOf("height", HubSpokePlanner.defaultPlatformHeight())
                        .forGetter(SpokeTemplate::height),
                MKWorkspaceCodecs.DIRECTION_CODEC.listOf().optionalFieldOf("valid_directions", CARDINAL_DIRECTIONS)
                        .forGetter(SpokeTemplate::validDirections)
        ).apply(instance, SpokeTemplate::new));

        public SpokeTemplate {
            baseName = baseName == null || baseName.isBlank() ? HubSpokePlanner.SPOKE_BASE_NAME : baseName.trim();
            label = label == null || label.isBlank() ? "Spoke 1" : label.trim();
            length = clampOdd(length, MIN_SPOKE_LENGTH, MAX_SPOKE_LENGTH);
            height = Math.max(HubSpokePlanner.MIN_PLATFORM_HEIGHT,
                    Math.min(HubSpokePlanner.MAX_PLATFORM_HEIGHT, height));
            LinkedHashSet<Direction> directions = new LinkedHashSet<>();
            if (validDirections != null) {
                for (Direction direction : validDirections) {
                    if (direction != null && direction.getAxis().isHorizontal()) {
                        directions.add(direction);
                    }
                }
            }
            validDirections = List.copyOf(directions);
        }

        public SpokeTemplate withIdentity(String baseName, String label) {
            return new SpokeTemplate(baseName, label, length, height, validDirections);
        }

        public SpokeTemplate withLength(int length) {
            return new SpokeTemplate(baseName, label, length, height, validDirections);
        }

        public SpokeTemplate withHeight(int height) {
            return new SpokeTemplate(baseName, label, length, height, validDirections);
        }

        public SpokeTemplate withDirection(Direction direction, boolean enabled) {
            ArrayList<Direction> updated = new ArrayList<>(validDirections);
            if (enabled && !updated.contains(direction)) {
                updated.add(direction);
            } else if (!enabled) {
                updated.remove(direction);
            }
            return new SpokeTemplate(baseName, label, length, height, updated);
        }

        public String pieceName(Direction direction) {
            return baseName + "_" + direction.getSerializedName();
        }
    }
}
