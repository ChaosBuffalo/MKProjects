package com.chaosbuffalo.mknpc.npc.option_entries;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;


public class FactionNameOptionEntry implements INpcOptionEntry, INameEntry{
    private String name;

    public FactionNameOptionEntry(){
        this.name = "";
    }

    public FactionNameOptionEntry(String name){
        this.name = name;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (!name.equals("") && entity instanceof LivingEntity){
            entity.setCustomName(getName());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", name);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.name = nbt.getString("name");
    }

    @Override
    public TextComponent getName() {
        return new TextComponent(name);
    }
}
