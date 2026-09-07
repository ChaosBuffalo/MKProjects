package com.chaosbuffalo.mknpc.mixins;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolElement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.IMKPoolPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;
import java.util.UUID;

@Mixin(PoolElementStructurePiece.class)
public abstract class PoolElementStructurePieceMixins implements IMKPoolPiece {
    @Unique
    private static final String MKNPC_INSTANCE_ID = "mknpc_instance_id";
    @Unique
    private static final String MKNPC_STRUCTURE = "mknpc_structure";
    @Unique
    private static final String MKNPC_LOCKED_FLOOR_TOPOLOGY_GROUP = "mknpc_locked_floor_topology_group";
    @Unique
    private static final String MKNPC_LOCKED_FLOOR_SEGMENT_INDEX = "mknpc_locked_floor_segment_index";

    @Shadow
    public abstract StructurePoolElement getElement();

    @Shadow
    @Final
    private StructureTemplateManager structureTemplateManager;
    @Shadow
    protected BlockPos position;
    @Shadow
    @Final
    protected Rotation rotation;
    @Shadow
    @Final
    private LiquidSettings liquidSettings;
    @Unique
    protected UUID mknpc$instanceId;
    @Unique
    protected Structure mknpc$structure;
    @Unique
    protected String mknpc$lockedFloorTopologyGroup = "";
    @Unique
    protected int mknpc$lockedFloorSegmentIndex = -1;


    @Override
    public void setContainingStructure(UUID instanceId, Structure structure) {
        this.mknpc$instanceId = instanceId;
        this.mknpc$structure = structure;
    }

    @Override
    public void setLockedFloorPlanSegment(String topologyGroup, int segmentIndex) {
        this.mknpc$lockedFloorTopologyGroup = topologyGroup == null ? "" : topologyGroup;
        this.mknpc$lockedFloorSegmentIndex = segmentIndex;
    }

    @Override
    public OptionalInt getLockedFloorPlanSegment(String topologyGroup) {
        if (mknpc$lockedFloorSegmentIndex < 0 || topologyGroup == null ||
                !topologyGroup.equals(mknpc$lockedFloorTopologyGroup)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(mknpc$lockedFloorSegmentIndex);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN"))
    private void mknpc$loadAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag,
                                              CallbackInfo ci) {
        if (tag.contains(MKNPC_INSTANCE_ID)) {
            this.mknpc$instanceId = tag.getUUID(MKNPC_INSTANCE_ID);
        }
        if (tag.contains(MKNPC_STRUCTURE)) {
            Registry<Structure> structureRegistry = context.registryAccess().registryOrThrow(Registries.STRUCTURE);
            this.mknpc$structure = structureRegistry.get(ResourceLocation.parse(tag.getString(MKNPC_STRUCTURE)));
        }
        if (tag.contains(MKNPC_LOCKED_FLOOR_TOPOLOGY_GROUP) &&
                tag.contains(MKNPC_LOCKED_FLOOR_SEGMENT_INDEX)) {
            this.mknpc$lockedFloorTopologyGroup = tag.getString(MKNPC_LOCKED_FLOOR_TOPOLOGY_GROUP);
            this.mknpc$lockedFloorSegmentIndex = tag.getInt(MKNPC_LOCKED_FLOOR_SEGMENT_INDEX);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void mknpc$saveAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag,
                                              CallbackInfo ci) {
        if (this.mknpc$instanceId != null) {
            tag.putUUID(MKNPC_INSTANCE_ID, this.mknpc$instanceId);
        }
        if (this.mknpc$structure != null) {
            ResourceLocation structureId = context.registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .getKey(this.mknpc$structure);
            if (structureId != null) {
                tag.putString(MKNPC_STRUCTURE, structureId.toString());
            }
        }
        if (this.mknpc$lockedFloorSegmentIndex >= 0 && !this.mknpc$lockedFloorTopologyGroup.isBlank()) {
            tag.putString(MKNPC_LOCKED_FLOOR_TOPOLOGY_GROUP, this.mknpc$lockedFloorTopologyGroup);
            tag.putInt(MKNPC_LOCKED_FLOOR_SEGMENT_INDEX, this.mknpc$lockedFloorSegmentIndex);
        }
    }

    /**
     * @author kovak
     * @reason call our custom mkPlace function to pass structure metadata
     */
    @Overwrite
    public void place(WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator,
                      RandomSource pRandom, BoundingBox pBox, BlockPos pPos, boolean pKeepJigsaws) {

        if (getElement() instanceof IMKPoolElement poolElement) {
            ResourceLocation name = pStructureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(mknpc$structure);
            poolElement.mkPlace(structureTemplateManager, pLevel, pStructureManager, pGenerator,
                    position, pPos, rotation, pBox, pRandom, liquidSettings, pKeepJigsaws, name, mknpc$instanceId);
        } else {
            getElement().place(structureTemplateManager, pLevel, pStructureManager, pGenerator, position, pPos, rotation,
                    pBox, pRandom, liquidSettings, pKeepJigsaws);
        }
    }
}
