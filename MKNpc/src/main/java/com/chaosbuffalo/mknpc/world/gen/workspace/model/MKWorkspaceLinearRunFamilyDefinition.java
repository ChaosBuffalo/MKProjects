package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MKWorkspaceLinearRunFamilyDefinition implements MKWorkspacePaletteFamily {
    public static final Codec<MKWorkspaceLinearRunFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("linearRunId").forGetter(MKWorkspaceLinearRunFamilyDefinition::linearRunId),
            MKWorkspaceCodecs.LINEAR_RUN_KIND_CODEC.optionalFieldOf("kind", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR)
                    .forGetter(MKWorkspaceLinearRunFamilyDefinition::kind),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKWorkspaceLinearRunFamilyDefinition::openingProfileId),
            Codec.INT.fieldOf("length").forGetter(MKWorkspaceLinearRunFamilyDefinition::length),
            Codec.INT.fieldOf("interiorWidth").forGetter(MKWorkspaceLinearRunFamilyDefinition::interiorWidth),
            Codec.INT.fieldOf("interiorHeight").forGetter(MKWorkspaceLinearRunFamilyDefinition::interiorHeight),
            Codec.INT.optionalFieldOf("slopeDelta", 0).forGetter(MKWorkspaceLinearRunFamilyDefinition::slopeDelta),
            Codec.BOOL.optionalFieldOf("allowOnMainPath", false).forGetter(MKWorkspaceLinearRunFamilyDefinition::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allowOnBranchPath", true).forGetter(MKWorkspaceLinearRunFamilyDefinition::allowOnBranchPath),
            MKWorkspaceCodecs.LINEAR_RUN_PROJECTION_CODEC.optionalFieldOf("projection", MKWorkspaceLinearRunProjection.RIGID)
                    .forGetter(MKWorkspaceLinearRunFamilyDefinition::projection),
            MKWorkspaceCodecs.LINEAR_RUN_SHAPE_CODEC.listOf().optionalFieldOf("supportedShapes", List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT))
                    .forGetter(MKWorkspaceLinearRunFamilyDefinition::supportedShapes),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("paletteOverride")
                    .forGetter(MKWorkspaceLinearRunFamilyDefinition::paletteOverrideOpt)
    ).apply(instance, (linearRunId, kind, openingProfileId, length, interiorWidth, interiorHeight, slopeDelta,
                       allowOnMainPath, allowOnBranchPath, projection, supportedShapes, paletteOverride) ->
            new MKWorkspaceLinearRunFamilyDefinition(linearRunId, kind, openingProfileId, length, interiorWidth,
                    interiorHeight, slopeDelta, allowOnMainPath, allowOnBranchPath, projection, supportedShapes,
                    paletteOverride.orElse(null))));

    private final String linearRunId;
    private final MKWorkspaceLinearRunKind kind;
    private final String openingProfileId;
    private final int length;
    private final int interiorWidth;
    private final int interiorHeight;
    private final int slopeDelta;
    private final boolean allowOnMainPath;
    private final boolean allowOnBranchPath;
    private final MKWorkspaceLinearRunProjection projection;
    private final List<MKWorkspaceLinearRunPieceShape> supportedShapes;
    @Nullable
    private final MKWorkspacePaletteOverride paletteOverride;

    public MKWorkspaceLinearRunFamilyDefinition(String linearRunId, MKWorkspaceLinearRunKind kind, String openingProfileId,
                                                int length, int interiorWidth, int interiorHeight, int slopeDelta,
                                                boolean allowOnMainPath, boolean allowOnBranchPath,
                                                MKWorkspaceLinearRunProjection projection,
                                                List<MKWorkspaceLinearRunPieceShape> supportedShapes,
                                                ResourceLocation floorBlock, ResourceLocation wallBlock,
                                                ResourceLocation ceilingBlock) {
        this(linearRunId, kind, openingProfileId, length, interiorWidth, interiorHeight, slopeDelta,
                allowOnMainPath, allowOnBranchPath, projection, supportedShapes,
                MKWorkspacePaletteOverride.of(floorBlock, wallBlock, ceilingBlock));
    }

    public MKWorkspaceLinearRunFamilyDefinition(String linearRunId, MKWorkspaceLinearRunKind kind, String openingProfileId,
                                                int length, int interiorWidth, int interiorHeight, int slopeDelta,
                                                boolean allowOnMainPath, boolean allowOnBranchPath,
                                                MKWorkspaceLinearRunProjection projection,
                                                List<MKWorkspaceLinearRunPieceShape> supportedShapes,
                                                @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this.linearRunId = linearRunId;
        this.kind = kind;
        this.openingProfileId = openingProfileId;
        this.length = length;
        this.interiorWidth = interiorWidth;
        this.interiorHeight = interiorHeight;
        this.slopeDelta = slopeDelta;
        this.allowOnMainPath = allowOnMainPath;
        this.allowOnBranchPath = allowOnBranchPath;
        this.projection = projection;
        this.supportedShapes = List.copyOf(supportedShapes.isEmpty() ? List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT) : supportedShapes);
        this.paletteOverride = paletteOverride != null && !paletteOverride.isEmpty() ? paletteOverride : null;
    }

    public static MKWorkspaceLinearRunFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "linear run family definition");
    }

    public static List<MKWorkspaceLinearRunFamilyDefinition> createDefaults(MKWorkspaceDimensions dimensions,
                                                                            MKWorkspaceMaterialPalette palette) {
        return List.of(
                new MKWorkspaceLinearRunFamilyDefinition(
                        "main",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "main_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        true,
                        false,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        null
                ),
                new MKWorkspaceLinearRunFamilyDefinition(
                        "branch",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "branch_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        false,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        null
                )
        );
    }

    public static List<MKWorkspaceLinearRunFamilyDefinition> fromHallwayFamilies(List<MKHallwayFamilyDefinition> hallwayFamilies) {
        return hallwayFamilies.stream()
                .map(hallway -> new MKWorkspaceLinearRunFamilyDefinition(
                        hallway.hallwayId(),
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        hallway.openingProfileId(),
                        hallway.length(),
                        hallway.interiorWidth(),
                        hallway.interiorHeight(),
                        hallway.slopeDelta(),
                        hallway.allowOnMainPath(),
                        hallway.allowOnBranchPath(),
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        hallway.paletteOverride()
                ))
                .toList();
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "linear run family definition");
    }

    public List<String> validate(Set<String> openingProfileIds) {
        List<String> errors = new ArrayList<>();
        if (linearRunId.isBlank()) {
            errors.add("linear run family id cannot be blank");
        }
        if (openingProfileId.isBlank()) {
            errors.add("linear run family opening profile id cannot be blank");
        } else if (!openingProfileIds.contains(openingProfileId)) {
            errors.add("linear run family " + linearRunId + " references missing opening profile " + openingProfileId);
        }
        if (length < 1) {
            errors.add("linear run family " + linearRunId + " length must be at least 1");
        }
        if (interiorWidth < 1) {
            errors.add("linear run family " + linearRunId + " width must be at least 1");
        }
        if (interiorWidth % 2 == 0) {
            errors.add("linear run family " + linearRunId + " width must be odd");
        }
        if (interiorHeight < 2) {
            errors.add("linear run family " + linearRunId + " height must be at least 2");
        }
        if (projection == MKWorkspaceLinearRunProjection.TERRAIN_MATCHED && kind != MKWorkspaceLinearRunKind.OPEN_WALKWAY) {
            errors.add("linear run family " + linearRunId + " can only terrain-match open walkway runs");
        }
        if (!supportedShapes.contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
            errors.add("linear run family " + linearRunId + " must support straight pieces for first-pass generation");
        }
        if (!allowOnMainPath && !allowOnBranchPath) {
            errors.add("linear run family " + linearRunId + " must be usable on the main path or branch path");
        }
        return errors;
    }

    public String linearRunId() {
        return linearRunId;
    }

    public MKWorkspaceLinearRunKind kind() {
        return kind;
    }

    public String openingProfileId() {
        return openingProfileId;
    }

    public int length() {
        return length;
    }

    public int interiorWidth() {
        return interiorWidth;
    }

    public int interiorHeight() {
        return interiorHeight;
    }

    public int slopeDelta() {
        return slopeDelta;
    }

    public boolean allowOnMainPath() {
        return allowOnMainPath;
    }

    public boolean allowOnBranchPath() {
        return allowOnBranchPath;
    }

    public MKWorkspaceLinearRunProjection projection() {
        return projection;
    }

    public List<MKWorkspaceLinearRunPieceShape> supportedShapes() {
        return supportedShapes;
    }

    @Override
    public String paletteFamilyId() {
        return linearRunId;
    }

    @Override
    public Optional<MKTowerWorkspaceCategory> paletteCategoryOpt() {
        return Optional.empty();
    }

    @Override
    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return Optional.ofNullable(paletteOverride);
    }

    @Nullable
    public MKWorkspacePaletteOverride paletteOverride() {
        return paletteOverride;
    }
}
