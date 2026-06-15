package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceVerticalStackBudget(
        int entryHeight,
        int mainFloorHeight,
        int basementFloorHeight,
        int basementEntryHeight,
        int topCapHeight,
        int basementCapHeight
) {
    public static final Codec<MKWorkspaceVerticalStackBudget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("entry_height", 7).forGetter(MKWorkspaceVerticalStackBudget::entryHeight),
            Codec.INT.optionalFieldOf("main_height", 7).forGetter(MKWorkspaceVerticalStackBudget::mainFloorHeight),
            Codec.INT.optionalFieldOf("basement_height", 7).forGetter(MKWorkspaceVerticalStackBudget::basementFloorHeight),
            Codec.INT.optionalFieldOf("basement_entry_height", 7).forGetter(MKWorkspaceVerticalStackBudget::basementEntryHeight),
            Codec.INT.optionalFieldOf("main_cap_height", 7).forGetter(MKWorkspaceVerticalStackBudget::topCapHeight),
            Codec.INT.optionalFieldOf("basement_cap_height", 7).forGetter(MKWorkspaceVerticalStackBudget::basementCapHeight)
    ).apply(instance, MKWorkspaceVerticalStackBudget::new));

    public MKWorkspaceVerticalStackBudget {
        entryHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, entryHeight);
        mainFloorHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, mainFloorHeight);
        basementFloorHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementFloorHeight);
        basementEntryHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementEntryHeight);
        topCapHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, topCapHeight);
        basementCapHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementCapHeight);
    }

    public static MKWorkspaceVerticalStackBudget uniform(int height) {
        return new MKWorkspaceVerticalStackBudget(height, height, height, height, height, height);
    }

    public static MKWorkspaceVerticalStackBudget fromStackSettings(MKWorkspaceVerticalStackSettings settings) {
        return settings.heights();
    }

    public static MKWorkspaceVerticalStackBudget fromDimensions(MKWorkspaceDimensions dimensions) {
        return new MKWorkspaceVerticalStackBudget(
                dimensions.entranceHeight(),
                dimensions.roomHeight(),
                dimensions.basementHeight(),
                dimensions.basementHeight(),
                dimensions.roomHeight(),
                dimensions.basementHeight()
        );
    }

    public int maxHeight() {
        return Math.max(Math.max(entryHeight, mainFloorHeight),
                Math.max(Math.max(basementFloorHeight, basementEntryHeight),
                        Math.max(topCapHeight, basementCapHeight)));
    }

    public int heightForTopologySlot(String topologySlotId) {
        return MKWorkspaceVerticalStackSlot.fromTopologySlotId(topologySlotId)
                .map(this::heightForStackSlot)
                .orElseGet(() -> heightForTopologyGroup(
                        MKWorkspaceTopologySlotMetadata.fromTopologySlotId(topologySlotId).topologyGroupId()));
    }

    public int heightForTopologyGroup(String topologyGroupId) {
        return switch (topologyGroupId) {
            case "entry" -> entryHeight;
            case "basement_entry" -> basementEntryHeight;
            case "basement" -> basementFloorHeight;
            case "basement_cap" -> basementCapHeight;
            case "top_cap", "main_cap" -> topCapHeight;
            default -> mainFloorHeight;
        };
    }

    private int heightForStackSlot(MKWorkspaceVerticalStackSlot slot) {
        return switch (slot) {
            case ENTRY -> entryHeight;
            case MAIN_FLOOR -> mainFloorHeight;
            case TOP_CAP, TOP_CAP_APPROACH -> topCapHeight;
            case BASEMENT_ENTRY -> basementEntryHeight;
            case BASEMENT_FLOOR -> basementFloorHeight;
            case BASEMENT_CAP, BASEMENT_CAP_APPROACH -> basementCapHeight;
        };
    }
}
