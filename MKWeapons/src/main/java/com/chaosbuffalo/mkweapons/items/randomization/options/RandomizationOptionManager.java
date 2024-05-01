package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RandomizationOptionManager {

    public static final Map<ResourceLocation, Codec<? extends IRandomizationOption>> OPTION_CODECS =
            new HashMap<>();
    public static final Codec<IRandomizationOption> RANDOMIZATION_OPTION_CODEC =
            CommonCodecs.createMapBackedDispatch(ResourceLocation.CODEC, OPTION_CODECS, IRandomizationOption::getName);

    public static void registerOption(ResourceLocation type,
                                      Codec<? extends IRandomizationOption> codec) {
        OPTION_CODECS.put(type, codec);
    }

    static {
        registerOption(AttributeOption.NAME, AttributeOption.CODEC);
        registerOption(AccessoryEffectOption.NAME, AccessoryEffectOption.CODEC);
        registerOption(ArmorEffectOption.NAME, ArmorEffectOption.CODEC);
        registerOption(MeleeEffectOption.NAME, MeleeEffectOption.CODEC);
        registerOption(RangedEffectOption.NAME, RangedEffectOption.CODEC);
        registerOption(AddAbilityOption.NAME, AddAbilityOption.CODEC);
        registerOption(NameOption.NAME, NameOption.CODEC);
        registerOption(PrefixNameOption.NAME, PrefixNameOption.CODEC);
    }
}
