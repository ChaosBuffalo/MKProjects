package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKTowerWorkspaceCategoryProfile {
    public static final int MIN_ROOM_HEIGHT = 2;

    public static final Codec<MKTowerWorkspaceCategoryProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.TOWER_CATEGORY_CODEC.fieldOf("category").forGetter(MKTowerWorkspaceCategoryProfile::category),
            Codec.INT.fieldOf("roomWidth").forGetter(MKTowerWorkspaceCategoryProfile::roomWidth),
            Codec.INT.fieldOf("roomLength").forGetter(MKTowerWorkspaceCategoryProfile::roomLength),
            Codec.INT.optionalFieldOf("fullHeight").forGetter(profile -> java.util.Optional.of(profile.fullHeight())),
            Codec.INT.optionalFieldOf("defaultHeight").forGetter(profile -> java.util.Optional.of(profile.fullHeight())),
            Codec.INT.optionalFieldOf("minHeight").forGetter(profile -> java.util.Optional.<Integer>empty()),
            Codec.INT.optionalFieldOf("maxHeight").forGetter(profile -> java.util.Optional.of(profile.fullHeight())),
            Codec.BOOL.optionalFieldOf("supportsVerticalAccess", true)
                    .forGetter(profile -> true)
    ).apply(instance, (category, roomWidth, roomLength, fullHeight, defaultHeight, minHeight, maxHeight, supportsVerticalAccess) ->
            new MKTowerWorkspaceCategoryProfile(
                    category,
                    roomWidth,
                    roomLength,
                    fullHeight.orElseGet(() -> defaultHeight.orElseGet(() -> maxHeight.orElse(3)))
            )));

    private final MKTowerWorkspaceCategory category;
    private final int roomWidth;
    private final int roomLength;
    private final int fullHeight;

    public MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory category, int roomWidth, int roomLength,
                                           int fullHeight) {
        this.category = category;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.fullHeight = fullHeight;
    }

    public static List<MKTowerWorkspaceCategoryProfile> createDefaults(MKWorkspaceDimensions dimensions) {
        return List.of(
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.ENTRY,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.entranceHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.MAIN,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.roomHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.basementHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.TOP_CAP,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.roomHeight()),
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT_CAP,
                        dimensions.roomWidth(), dimensions.roomLength(), dimensions.basementHeight())
        );
    }

    public static MKTowerWorkspaceCategoryProfile fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "tower workspace category profile");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "tower workspace category profile");
    }

    public List<String> validate(MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        List<String> errors = new ArrayList<>();
        validateOdd(errors, category.getSerializedName() + " room width", roomWidth, 3);
        validateOdd(errors, category.getSerializedName() + " room length", roomLength, 3);
        if (fullHeight < 3) {
            errors.add(category.getSerializedName() + " full height must be at least 3");
        }
        if (verticalAccessSpec.shaftSize() > roomWidth) {
            errors.add(category.getSerializedName() + " room width must be at least the shared shaft size");
        }
        if (verticalAccessSpec.shaftSize() > roomLength) {
            errors.add(category.getSerializedName() + " room length must be at least the shared shaft size");
        }
        return errors;
    }

    private static void validateOdd(List<String> errors, String label, int value, int min) {
        if (value < min) {
            errors.add(label + " must be at least " + min);
        }
        if (value % 2 == 0) {
            errors.add(label + " must be odd");
        }
    }

    public MKTowerWorkspaceCategory category() {
        return category;
    }

    public int roomWidth() {
        return roomWidth;
    }

    public int roomLength() {
        return roomLength;
    }

    public int fullHeight() {
        return fullHeight;
    }
}

