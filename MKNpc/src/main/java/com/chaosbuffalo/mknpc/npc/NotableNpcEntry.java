package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class NotableNpcEntry implements INBTSerializable<CompoundTag> {

    private GlobalPos location;
    private TextComponent name;
    private ResourceLocation definition;
    private UUID structureId;
    private UUID spawnerId;
    private UUID notableId;

    public NotableNpcEntry(NpcDefinition definition, MKSpawnerTileEntity spawner){
        this.location = spawner.getGlobalBlockPos();
        this.name = definition.getNameForEntity(spawner.getLevel(), spawner.getSpawnUUID());
        this.definition = definition.getDefinitionName();
        this.structureId = spawner.getStructureId();
        this.spawnerId = spawner.getSpawnUUID();
        this.notableId = UUID.randomUUID();
    }

    public NotableNpcEntry(){

    }

    public GlobalPos getLocation() {
        return location;
    }

    public UUID getSpawnerId() {
        return spawnerId;
    }

    public UUID getStructureId() {
        return structureId;
    }

    public UUID getNotableId() {
        return notableId;
    }

    public TextComponent getName() {
        return name;
    }

    @Nullable
    public NpcDefinition getDefinition(){
        return NpcDefinitionManager.getDefinition(definition);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("location", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, getLocation())
                .getOrThrow(false, MKNpc.LOGGER::error));
        tag.putUUID("spawnerId", spawnerId);
        tag.putUUID("structureId", structureId);
        tag.putUUID("notableId", notableId);
        tag.putString("definition", definition.toString());
        tag.putString("name", name.getContents());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        location = GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("location"))
                .result().orElse(GlobalPos.of(Level.OVERWORLD, NbtUtils.readBlockPos(nbt.getCompound("location"))));
        spawnerId = nbt.getUUID("spawnerId");
        structureId = nbt.getUUID("structureId");
        definition = new ResourceLocation(nbt.getString("definition"));
        name = new TextComponent(nbt.getString("name"));
        notableId = nbt.getUUID("notableId");
    }
}
