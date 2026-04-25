package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKJigsawPieceRole implements StringRepresentable {
    MAIN("main"),
    BRANCH("branch"),
    ROOM("room"),
    BOSS_APPROACH("boss_approach"),
    BOSS("boss"),
    TERMINAL("terminal");

    public static final Codec<MKJigsawPieceRole> CODEC = Codec.STRING.xmap(
            MKJigsawPieceRole::fromSerializedName,
            MKJigsawPieceRole::getSerializedName
    );

    private final String serializedName;

    MKJigsawPieceRole(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static MKJigsawPieceRole fromSerializedName(String serializedName) {
        for (MKJigsawPieceRole role : values()) {
            if (role.serializedName.equals(serializedName)) {
                return role;
            }
        }
        if ("stairs_up".equals(serializedName) || "stairs_down".equals(serializedName)) {
            return ROOM;
        }
        throw new IllegalArgumentException("Unknown jigsaw piece role: " + serializedName);
    }
}
