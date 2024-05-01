package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class EffectOption<T extends IItemEffect> extends BaseRandomizationOption {

    private final List<T> itemEffects;

    public EffectOption(ResourceLocation name, IRandomizationSlot slot) {
        this(name, slot, new ArrayList<>());
    }

    public EffectOption(ResourceLocation name, IRandomizationSlot slot, List<T> itemEffects) {
        super(name, slot);
        this.itemEffects = itemEffects;
    }

    public void addEffect(T effect) {
        itemEffects.add(effect);
    }

    public List<T> getItemEffects() {
        return itemEffects;
    }
}
