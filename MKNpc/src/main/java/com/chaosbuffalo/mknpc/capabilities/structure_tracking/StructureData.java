package com.chaosbuffalo.mknpc.capabilities.structure_tracking;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StructureData implements INBTSerializable<CompoundTag> {

    private ChunkPos chunkPos;
    private BoundingBox boundingBox;
    private final List<StructureComponentData> components;
    private ResourceKey<Level> worldKey;

    public StructureData(ResourceKey<Level> worldKey, StructureStart start,
                         Function<StructurePiece, StructureComponentData> pieceDataFactory) {
        this.worldKey = worldKey;
        this.chunkPos = start.getChunkPos();
        this.boundingBox = start.getBoundingBox();
        this.components = start.getPieces().stream().map(pieceDataFactory).toList();
    }

    public StructureData() {
        this.components = new ArrayList<>();
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("chunkX", chunkPos.x);
        tag.putInt("chunkY", chunkPos.z);
        int[] boundsArr = {boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()};
        tag.putIntArray("bounds", boundsArr);
        ListTag comps = new ListTag();
        for (StructureComponentData dat : components) {
            comps.add(dat.serializeNBT());
        }
        tag.put("components", comps);
        tag.putString("world", worldKey.location().toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        int chunkX = nbt.getInt("chunkX");
        int chunkZ = nbt.getInt("chunkY");
        chunkPos = new ChunkPos(chunkX, chunkZ);
        worldKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("world")));
        int[] boundsArr = nbt.getIntArray("bounds");
        boundingBox = new BoundingBox(boundsArr[0], boundsArr[1], boundsArr[2], boundsArr[3], boundsArr[4], boundsArr[5]);
        ListTag comps = nbt.getList("components", Tag.TAG_COMPOUND);
        List<StructureComponentData> newComps = new ArrayList<>();
        for (Tag comp : comps) {
            StructureComponentData data = new StructureComponentData();
            data.deserializeNBT((CompoundTag) comp);
            newComps.add(data);
        }
        components.clear();
        components.addAll(newComps);
    }
}
