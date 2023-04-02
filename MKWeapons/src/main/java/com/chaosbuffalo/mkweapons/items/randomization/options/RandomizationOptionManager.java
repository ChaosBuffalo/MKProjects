package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RandomizationOptionManager {

    public static final Map<ResourceLocation, Supplier<IRandomizationOption>> OPTION_DESERIALIZERS =
            new HashMap<>();

    public static void addOptionDeserializer(ResourceLocation type,
                                             Supplier<IRandomizationOption> deserializer){
        OPTION_DESERIALIZERS.put(type, deserializer);
    }

    static {
        addOptionDeserializer(AttributeOption.NAME, AttributeOption::new);
        addOptionDeserializer(AccessoryEffectOption.NAME, AccessoryEffectOption::new);
        addOptionDeserializer(ArmorEffectOption.NAME, ArmorEffectOption::new);
        addOptionDeserializer(MeleeEffectOption.NAME, MeleeEffectOption::new);
        addOptionDeserializer(RangedEffectOption.NAME, RangedEffectOption::new);
        addOptionDeserializer(AddAbilityOption.NAME, AddAbilityOption::new);
        addOptionDeserializer(NameOption.NAME, NameOption::new);
        addOptionDeserializer(PrefixNameOption.NAME, PrefixNameOption::new);
    }

    public static <D> IRandomizationOption deserializeOption(Dynamic<D> dynamic){
        ResourceLocation type = BaseRandomizationOption.getType(dynamic);
        IRandomizationOption option = OPTION_DESERIALIZERS.get(type).get();
        if (option != null){
            option.deserialize(dynamic);
        }
        return option;
    }
}
