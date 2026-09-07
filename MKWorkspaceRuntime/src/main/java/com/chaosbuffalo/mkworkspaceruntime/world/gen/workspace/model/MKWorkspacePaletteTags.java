package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import java.util.Map;

public final class MKWorkspacePaletteTags {
    public static final String FLOOR_BLOCK_TAG = "workspace_palette_floor";
    public static final String WALL_BLOCK_TAG = "workspace_palette_wall";
    public static final String CEILING_BLOCK_TAG = "workspace_palette_ceiling";
    public static final String STAIR_BLOCK_TAG = "workspace_palette_stair";
    public static final String SLAB_BLOCK_TAG = "workspace_palette_slab";
    public static final String LADDER_BLOCK_TAG = "workspace_palette_ladder";

    private MKWorkspacePaletteTags() {
    }

    public static void apply(Map<String, String> tags, MKWorkspaceMaterialPalette palette) {
        tags.put(FLOOR_BLOCK_TAG, palette.floorBlock().toString());
        tags.put(WALL_BLOCK_TAG, palette.wallBlock().toString());
        tags.put(CEILING_BLOCK_TAG, palette.ceilingBlock().toString());
        tags.put(STAIR_BLOCK_TAG, palette.stairBlock().toString());
        tags.put(SLAB_BLOCK_TAG, palette.slabBlock().toString());
        tags.put(LADDER_BLOCK_TAG, palette.ladderBlock().toString());
    }
}
