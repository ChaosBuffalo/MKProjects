package com.chaosbuffalo.mknpc.capabilities.structure_tracking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class StructureData implements INBTSerializable<CompoundTag> {

    private int chunkX;
    private int chunkZ;
    private BoundingBox boundingBox;
    private final List<StructureComponentData> components;
    private ResourceKey<Level> worldKey;

    public StructureData(ResourceKey<Level> worldKey, int chunkX, int chunkZ, BoundingBox bounds, List<StructureComponentData> data){
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.boundingBox = bounds;
        this.components = new ArrayList<>();
        this.worldKey = worldKey;
        components.addAll(data);
    }

    public StructureData(){
        this.components = new ArrayList<>();
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("chunkX", chunkX);
        tag.putInt("chunkY", chunkZ);
        int[] boundsArr = {boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()};
        tag.putIntArray("bounds", boundsArr);
        ListTag comps = new ListTag();
        for (StructureComponentData dat : components){
            comps.add(dat.serializeNBT());
        }
        tag.put("components", comps);
        tag.putString("world", worldKey.location().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        chunkX = nbt.getInt("chunkX");
        chunkZ = nbt.getInt("chunkY");
        worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("world")));
        int[] boundsArr = nbt.getIntArray("bounds");
        boundingBox = new BoundingBox(boundsArr[0], boundsArr[1], boundsArr[2], boundsArr[3], boundsArr[4], boundsArr[5]);
        ListTag comps = nbt.getList("components", Tag.TAG_COMPOUND);
        List<StructureComponentData> newComps = new ArrayList<>();
        for (Tag comp : comps){
            StructureComponentData data = new StructureComponentData();
            data.deserializeNBT((CompoundTag) comp);
            newComps.add(data);
        }
        components.clear();
        components.addAll(newComps);
    }
}
