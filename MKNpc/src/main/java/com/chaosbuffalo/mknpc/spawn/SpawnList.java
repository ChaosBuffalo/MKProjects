package com.chaosbuffalo.mknpc.spawn;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class SpawnList implements INBTSerializable<CompoundTag> {

    private final List<SpawnOption> options;

    public SpawnList(){
        this.options = new ArrayList<>();
    }

    public List<SpawnOption> getOptions() {
        return options;
    }

    public void addOption(SpawnOption option){
        options.add(option);
    }

    public void copyList(SpawnList other){
        this.options.clear();
        for (SpawnOption option : other.getOptions()){
            addOption(option);
        }
    }

    public void setWeightForOption(int index, double weight){
        options.get(index).setWeight(weight);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag opts = new ListTag();
        for (SpawnOption option : getOptions()){
            opts.add(option.serializeNBT());
        }
        tag.put("options", opts);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag opts = nbt.getList("options", Tag.TAG_COMPOUND);
        options.clear();
        for (int i = 0; i < opts.size(); i++){
            CompoundTag option = opts.getCompound(i);
            SpawnOption spawnOption = new SpawnOption();
            spawnOption.deserializeNBT(option);
            addOption(spawnOption);
        }
    }
}
