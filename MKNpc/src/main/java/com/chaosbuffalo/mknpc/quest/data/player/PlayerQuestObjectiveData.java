package com.chaosbuffalo.mknpc.quest.data.player;

import com.chaosbuffalo.mknpc.utils.NBTSerializableMappedData;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.*;

public class PlayerQuestObjectiveData extends NBTSerializableMappedData {

    private String objectiveName;
    private List<MutableComponent> description = new ArrayList<>();

    public PlayerQuestObjectiveData(String objectiveName, MutableComponent... description){
        this(objectiveName, Arrays.asList(description));
    }

    public PlayerQuestObjectiveData(String objectiveName, List<MutableComponent> description){
        this.objectiveName = objectiveName;
        this.description.addAll(description);
    }

    public void setDescription(MutableComponent... description) {
        this.description.clear();
        this.description.addAll(Arrays.asList(description));
    }

    public List<MutableComponent> getDescription() {
        return description;
    }

    public String getObjectiveName() {
        return objectiveName;
    }

    public PlayerQuestObjectiveData(CompoundTag nbt){
        deserializeNBT(nbt);
    }

    public boolean isComplete() {
        return getBool("isComplete");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putString("name", objectiveName);
        ListTag descriptions = new ListTag();
        for (MutableComponent comp : this.description){
            descriptions.add(StringTag.valueOf(Component.Serializer.toJson(comp)));
        }
        nbt.put("description", descriptions);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        objectiveName = nbt.getString("name");
        ListTag descriptions = nbt.getList("description", Tag.TAG_STRING);
        for (Tag desc : descriptions){
            description.add(Component.Serializer.fromJson(desc.getAsString()));
        }
    }
}
