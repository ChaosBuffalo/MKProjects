package com.chaosbuffalo.mknpc.capabilities.structure_tracking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.common.util.INBTSerializable;


public class StructureComponentData implements INBTSerializable<CompoundTag> {
    private ResourceLocation pieceName;
    private BoundingBox bounds;

    public StructureComponentData(ResourceLocation pieceName, BoundingBox bounds){
        this.pieceName = pieceName;
        this.bounds = bounds;
    }

    public StructureComponentData(){
        this.bounds = null;
        this.pieceName = null;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public ResourceLocation getPieceName() {
        return pieceName;
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        int[] boundsArr = {bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ()};
        nbt.putIntArray("bounds", boundsArr);
        nbt.putString("pieceName", pieceName.toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        int[] boundsArr = nbt.getIntArray("bounds");
        bounds = new BoundingBox(boundsArr[0], boundsArr[1], boundsArr[2], boundsArr[3], boundsArr[4], boundsArr[5]);
        pieceName = new ResourceLocation(nbt.getString("pieceName"));
    }
}
