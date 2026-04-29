package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record MKDungeonLayoutSettings(
        int minFloors,
        int maxFloors,
        int minPiecesPerFloor,
        int maxPiecesPerFloor,
        int maxBranchDepth,
        boolean allowBranchesOnFinalFloor,
        MKVerticalProgressionMode verticalProgressionMode,
        boolean topCapApproachEnabled,
        boolean basementCapApproachEnabled,
        List<MKDungeonCategoryRule> categoryRules,
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
            Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true).forGetter(MKDungeonLayoutSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false).forGetter(MKDungeonLayoutSettings::basementCapApproachEnabled),
            MKDungeonCategoryRule.CODEC.listOf().optionalFieldOf("category_rules", List.of()).forGetter(MKDungeonLayoutSettings::categoryRules),
            MKDungeonConnectorSettings.CODEC.fieldOf("connectors").forGetter(MKDungeonLayoutSettings::connectors)
    ).apply(instance, MKDungeonLayoutSettings::new)).flatXmap(MKDungeonLayoutSettings::validate, MKDungeonLayoutSettings::validate);

    public MKDungeonLayoutSettings(int minFloors, int maxFloors, int minPiecesPerFloor, int maxPiecesPerFloor,
                                   int maxBranchDepth, boolean allowBranchesOnFinalFloor,
                                   MKVerticalProgressionMode verticalProgressionMode,
                                   MKDungeonConnectorSettings connectors) {
        this(minFloors, maxFloors, minPiecesPerFloor, maxPiecesPerFloor, maxBranchDepth,
                allowBranchesOnFinalFloor, verticalProgressionMode, true, false, List.of(), connectors);
    }

    private static DataResult<MKDungeonLayoutSettings> validate(MKDungeonLayoutSettings settings) {
        if (settings.minFloors() > settings.maxFloors()) {
            return DataResult.error(() -> "min_floors must be <= max_floors");
        }
        if (settings.minPiecesPerFloor() > settings.maxPiecesPerFloor()) {
            return DataResult.error(() -> "min_pieces_per_floor must be <= max_pieces_per_floor");
        }
        for (MKDungeonCategoryRule rule : settings.categoryRules()) {
            if (rule.minMainPathPieces() > rule.maxMainPathPieces()) {
                return DataResult.error(() -> "category rule " + rule.category() +
                        " min_main_path_pieces must be <= max_main_path_pieces");
            }
            if (rule.maxBranchPiecesBeforeCap() < 0) {
                return DataResult.error(() -> "category rule " + rule.category() +
                        " max_branch_pieces_before_cap must be >= 0");
            }
        }
        return DataResult.success(settings);
    }

    public Optional<MKDungeonCategoryRule> categoryRule(String category) {
        if (category == null || category.isBlank()) {
            return Optional.empty();
        }
        return categoryRules.stream()
                .filter(rule -> rule.category().equals(category))
                .findFirst();
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

    public ResourceLocation topCapForward() {
        return connectors.topCapForward();
    }

    public ResourceLocation topCapBack() {
        return connectors.topCapBack();
    }
}

