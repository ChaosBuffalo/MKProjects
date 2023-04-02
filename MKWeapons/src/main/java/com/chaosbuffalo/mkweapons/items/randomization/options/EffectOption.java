package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemEffects;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class EffectOption<T extends IItemEffect> extends BaseRandomizationOption {
    private final List<T> itemEffects = new ArrayList<>();

    public EffectOption(ResourceLocation name, IRandomizationSlot slot) {
        super(name, slot);
    }

    public void addEffect(T effect){
        itemEffects.add(effect);
    }

    public List<T> getItemEffects() {
        return itemEffects;
    }

    protected abstract Optional<T> deserializeEffectToType(@Nullable IItemEffect effect);

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        List<Optional<T>> effects = dynamic.get("effects").asList(x -> {
            IItemEffect effect = ItemEffects.deserializeEffect(x);
            return deserializeEffectToType(effect);
        });
        itemEffects.clear();
        effects.forEach(x -> x.ifPresent(itemEffects::add));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("effects"), ops.createList(itemEffects.stream().map(x -> x.serialize(ops))));
    }
}
