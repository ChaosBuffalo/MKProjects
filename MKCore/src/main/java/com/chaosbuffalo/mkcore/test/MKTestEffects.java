package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongPulseEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongSustainEffect;
import com.chaosbuffalo.mkcore.test.effects.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKTestEffects {

    public static final DeferredRegister<MKEffect> EFFECTS = DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<MKEffect, FeatherFallEffect> FEATHER_FALL = EFFECTS
            .register("effect.test_featherfall", FeatherFallEffect::new);

    public static final DeferredHolder<MKEffect, NewBurningSoulEffect> BURNING_SOUL = EFFECTS
            .register("effect.v2.burning_soul", NewBurningSoulEffect::new);

    public static final DeferredHolder<MKEffect, NewFireArmorEffect> FIRE_ARMOR = EFFECTS
            .register("effect.v2.fire_armor_effect", NewFireArmorEffect::new);

    public static final DeferredHolder<MKEffect, NewHealEffect> NEW_HEAL = EFFECTS
            .register("effect.new_heal", NewHealEffect::new);

    public static final DeferredHolder<MKEffect, PhoenixAspectEffect> PHOENIX_ASPECT = EFFECTS
            .register("effect.test_phoenix_aspect", PhoenixAspectEffect::new);

    public static final DeferredHolder<MKEffect, SkinLikeWoodEffect> SKIN_LIKE_WOOD = EFFECTS
            .register("effect.v2.skin_like_wood", SkinLikeWoodEffect::new);

    public static final DeferredHolder<MKEffect, TestFallCountingEffect> FALL_COUNTER = EFFECTS
            .register("effect.v2.fall_counter", TestFallCountingEffect::new);

    public static final DeferredHolder<MKEffect, MKSongSustainEffect> NEW_HEAL_SONG_SUSTAIN = EFFECTS
            .register("effect.v2.new_heal_song_sustain", MKSongSustainEffect::new);

    public static final DeferredHolder<MKEffect, MKSongPulseEffect> NEW_HEAL_SONG_PULSE = EFFECTS
            .register("effect.v2.new_heal_song_pulse", MKSongPulseEffect::new);


    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
