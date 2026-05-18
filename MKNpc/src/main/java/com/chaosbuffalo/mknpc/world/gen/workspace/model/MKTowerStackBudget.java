package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public record MKTowerStackBudget(
        int entryHeight,
        int mainFloorHeight,
        int basementFloorHeight,
        int topCapHeight,
        int basementCapHeight
) {
    public MKTowerStackBudget {
        entryHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, entryHeight);
        mainFloorHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, mainFloorHeight);
        basementFloorHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementFloorHeight);
        topCapHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, topCapHeight);
        basementCapHeight = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, basementCapHeight);
    }

    public static MKTowerStackBudget uniform(int height) {
        return new MKTowerStackBudget(height, height, height, height, height);
    }

    public static MKTowerStackBudget fromStackSettings(MKWorkspaceTowerStackSettings settings) {
        return uniform(settings.height());
    }

    public static MKTowerStackBudget fromDimensions(MKWorkspaceDimensions dimensions) {
        return new MKTowerStackBudget(
                dimensions.entranceHeight(),
                dimensions.roomHeight(),
                dimensions.basementHeight(),
                dimensions.roomHeight(),
                dimensions.basementHeight()
        );
    }
}
