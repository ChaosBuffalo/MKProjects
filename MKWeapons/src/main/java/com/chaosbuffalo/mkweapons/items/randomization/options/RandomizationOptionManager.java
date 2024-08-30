package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RandomizationOptionManager {

    public static final Map<ResourceLocation, MapCodec<? extends IRandomizationOption>> OPTION_CODECS =
            new HashMap<>();
    public static final Codec<IRandomizationOption> RANDOMIZATION_OPTION_CODEC =
            CommonCodecs.createMapBackedDispatch(ResourceLocation.CODEC, OPTION_CODECS, IRandomizationOption::getName);

    public static void registerOption(ResourceLocation type,
                                      MapCodec<? extends IRandomizationOption> codec) {
        OPTION_CODECS.put(type, codec);
    }

    static {
        registerOption(AttributeOption.NAME, AttributeOption.MAP_CODEC);
        registerOption(AccessoryEffectOption.NAME, AccessoryEffectOption.MAP_CODEC);
        registerOption(ArmorEffectOption.NAME, ArmorEffectOption.MAP_CODEC);
        registerOption(MeleeEffectOption.NAME, MeleeEffectOption.MAP_CODEC);
        registerOption(RangedEffectOption.NAME, RangedEffectOption.MAP_CODEC);
        registerOption(AddAbilityOption.NAME, AddAbilityOption.MAP_CODEC);
        registerOption(NameOption.NAME, NameOption.MAP_CODEC);
        registerOption(PrefixNameOption.NAME, PrefixNameOption.MAP_CODEC);
    }
}
