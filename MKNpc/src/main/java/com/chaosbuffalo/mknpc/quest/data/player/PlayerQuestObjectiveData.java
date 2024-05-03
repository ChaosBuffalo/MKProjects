package com.chaosbuffalo.mknpc.quest.data.player;

import com.chaosbuffalo.mknpc.utils.NBTSerializableMappedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PlayerQuestObjectiveData extends NBTSerializableMappedData {

    private final List<Component> description = new ArrayList<>();
    private String objectiveName;

    public PlayerQuestObjectiveData(String objectiveName, List<Component> description) {
        this.objectiveName = objectiveName;
        this.description.addAll(description);
    }

    public PlayerQuestObjectiveData(CompoundTag nbt) {
        deserializeNBT(nbt);
    }

    public List<Component> getDescription() {
        return description;
    }

    public void setDescription(Component component) {
        description.clear();
        description.add(component);
    }

    public void setDescription(List<Component> components) {
        description.clear();
        description.addAll(components);
    }

    public String getObjectiveName() {
        return objectiveName;
    }

    public boolean isComplete() {
        return getBool("isComplete");
    }

    public void setComplete(boolean complete) {
        putBool("isComplete", complete);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putString("name", objectiveName);
        ListTag descriptions = new ListTag();
        for (Component comp : this.description) {
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
        for (Tag desc : descriptions) {
            description.add(Component.Serializer.fromJson(desc.getAsString()));
        }
    }
}
