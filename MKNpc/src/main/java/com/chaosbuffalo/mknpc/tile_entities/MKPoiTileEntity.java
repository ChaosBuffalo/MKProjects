package com.chaosbuffalo.mknpc.tile_entities;

import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.init.MKNpcTileEntityTypes;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class MKPoiTileEntity extends BlockEntity implements IStructurePlaced {
    private ResourceLocation structureName;
    private UUID structureId;
    private UUID poiID;
    private boolean needsUploadToWorld;
    private boolean placedByStructure;
    private String tag;


    public MKPoiTileEntity(BlockPos blockPos, BlockState blockState) {
        super(MKNpcTileEntityTypes.MK_POI_TILE_ENTITY_TYPE.get(), blockPos, blockState);
        this.structureName = null;
        this.structureId = null;
        this.placedByStructure = false;
        this.needsUploadToWorld = false;
        this.poiID = UUID.randomUUID();
    }

    @Override
    public boolean isInsideStructure() {
        return structureName != null && structureId != null;
    }

    @Override
    public void setStructureName(ResourceLocation structureName) {
        this.structureName = structureName;
    }

    @Override
    @Nullable
    public ResourceLocation getStructureName() {
        return structureName;
    }

    public UUID getPoiID() {
        return poiID;
    }

    public void setPoiTag(String tag) {
        this.tag = tag;
    }

    public String getPoiTag() {
        return tag;
    }

    @Override
    @Nullable
    public UUID getStructureId() {
        return structureId;
    }

    @Override
    public void setStructureId(UUID structureId) {
        this.structureId = structureId;
    }


    @Override
    public GlobalPos getGlobalPos() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }

    @Override
    @Nullable
    public Level getStructureLevel() {
        return getLevel();
    }

    public static void poiTick(Level world, BlockPos blockPos, BlockState blockState, MKPoiTileEntity tileEntity) {
        tileEntity.tick(world);
    }

    public void tick(Level level) {
        if (level != null && !level.isClientSide) {
            if (needsUploadToWorld) {
                ContentDB.getPrimaryData().addPointOfInterest(this);

                level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
                needsUploadToWorld = false;
            }
        }
    }

    public void regenerateId() {
        if (!placedByStructure) {
            this.poiID = UUID.randomUUID();
            this.needsUploadToWorld = true;
            this.placedByStructure = true;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putUUID("poiId", poiID);
        compound.putBoolean("hasUploadedToWorld", needsUploadToWorld);
        compound.putBoolean("placedByStructure", placedByStructure);
        if (isInsideStructure()) {
            compound.putString("structureName", structureName.toString());
            compound.putUUID("structureId", structureId);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("structureName")) {
            setStructureName(new ResourceLocation(compound.getString("structureName")));
        }
        if (compound.contains("structureId")) {
            setStructureId(compound.getUUID("structureId"));
        }
        if (compound.contains("hasUploadedToWorld")) {
            needsUploadToWorld = compound.getBoolean("hasUploadedToWorld");
        }
        if (compound.contains("placedByStructure")) {
            placedByStructure = compound.getBoolean("placedByStructure");
        }

        if (compound.contains("poiId")) {
            poiID = compound.getUUID("poiId");
        } else {
            poiID = UUID.randomUUID();
        }
    }
}
