package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record MKDungeonLayoutSettings(
        int minFloors,
        int maxFloors,
        int minPiecesPerFloor,
        int maxPiecesPerFloor,
        int maxBranchDepth,
        boolean allowBranchesOnFinalFloor,
        MKVerticalProgressionMode verticalProgressionMode,
        MKDungeonConnectorSettings connectors
) {
    public static final Codec<MKDungeonLayoutSettings> CODEC = RecordCodecBuilder.<MKDungeonLayoutSettings>create(instance -> instance.group(
            Codec.intRange(1, 64).fieldOf("min_floors").forGetter(MKDungeonLayoutSettings::minFloors),
            Codec.intRange(1, 64).fieldOf("max_floors").forGetter(MKDungeonLayoutSettings::maxFloors),
            Codec.intRange(1, 64).fieldOf("min_pieces_per_floor").forGetter(MKDungeonLayoutSettings::minPiecesPerFloor),
            Codec.intRange(1, 64).fieldOf("max_pieces_per_floor").forGetter(MKDungeonLayoutSettings::maxPiecesPerFloor),
            Codec.intRange(0, 64).fieldOf("max_branch_depth").forGetter(MKDungeonLayoutSettings::maxBranchDepth),
            Codec.BOOL.optionalFieldOf("allow_branches_on_final_floor", false).forGetter(MKDungeonLayoutSettings::allowBranchesOnFinalFloor),
            MKVerticalProgressionMode.CODEC.optionalFieldOf("vertical_progression_mode", MKVerticalProgressionMode.MIXED).forGetter(MKDungeonLayoutSettings::verticalProgressionMode),
            MKDungeonConnectorSettings.CODEC.fieldOf("connectors").forGetter(MKDungeonLayoutSettings::connectors)
    ).apply(instance, MKDungeonLayoutSettings::new)).flatXmap(MKDungeonLayoutSettings::validate, MKDungeonLayoutSettings::validate);

    private static DataResult<MKDungeonLayoutSettings> validate(MKDungeonLayoutSettings settings) {
        if (settings.minFloors() > settings.maxFloors()) {
            return DataResult.error(() -> "min_floors must be <= max_floors");
        }
        if (settings.minPiecesPerFloor() > settings.maxPiecesPerFloor()) {
            return DataResult.error(() -> "min_pieces_per_floor must be <= max_pieces_per_floor");
        }
        return DataResult.success(settings);
    }

    public ResourceLocation mainForward() {
        return connectors.mainForward();
    }

    public ResourceLocation mainBack() {
        return connectors.mainBack();
    }

    public ResourceLocation branch() {
        return connectors.branch();
    }

    public ResourceLocation connectDown() {
        return connectors.connectDown();
    }

    public ResourceLocation connectUp() {
        return connectors.connectUp();
    }

    public ResourceLocation stairInsertDown() {
        return connectors.stairInsertDown();
    }

    public ResourceLocation stairInsertUp() {
        return connectors.stairInsertUp();
    }

    public ResourceLocation bossForward() {
        return connectors.bossForward();
    }

    public ResourceLocation bossBack() {
        return connectors.bossBack();
    }
}
