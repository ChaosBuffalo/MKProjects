package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKTowerStackBudget(
        int entryHeight,
        int mainFloorHeight,
        int basementFloorHeight,
        int basementEntryHeight,
        int topCapHeight,
        int basementCapHeight
) {
    public static final Codec<MKTowerStackBudget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("entry_height", 7).forGetter(MKTowerStackBudget::entryHeight),
            Codec.INT.optionalFieldOf("main_height", 7).forGetter(MKTowerStackBudget::mainFloorHeight),
            Codec.INT.optionalFieldOf("basement_height", 7).forGetter(MKTowerStackBudget::basementFloorHeight),
            Codec.INT.optionalFieldOf("basement_entry_height", 7).forGetter(MKTowerStackBudget::basementEntryHeight),
            Codec.INT.optionalFieldOf("main_cap_height", 7).forGetter(MKTowerStackBudget::topCapHeight),
            Codec.INT.optionalFieldOf("basement_cap_height", 7).forGetter(MKTowerStackBudget::basementCapHeight)
    ).apply(instance, MKTowerStackBudget::new));

    public MKTowerStackBudget {
        entryHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, entryHeight);
        mainFloorHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, mainFloorHeight);
        basementFloorHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementFloorHeight);
        basementEntryHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementEntryHeight);
        topCapHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, topCapHeight);
        basementCapHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementCapHeight);
    }

    public static MKTowerStackBudget uniform(int height) {
        return new MKTowerStackBudget(height, height, height, height, height, height);
    }

    public static MKTowerStackBudget fromStackSettings(MKWorkspaceVerticalStackSettings settings) {
        return settings.heights();
    }

    public static MKTowerStackBudget fromDimensions(MKWorkspaceDimensions dimensions) {
        return new MKTowerStackBudget(
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
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
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

    private int heightForStackSlot(MKTowerWorkspaceStackSlot slot) {
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
