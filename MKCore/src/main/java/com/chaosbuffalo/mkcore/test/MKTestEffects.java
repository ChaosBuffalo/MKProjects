package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMagicDamageEffect;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongPulseEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongSustainEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import com.chaosbuffalo.mkcore.test.effects.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKTestEffects {

    public static final DeferredRegister<MKEffect> EFFECTS = DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_NAME, MKCore.MOD_ID);

    public static final RegistryObject<FeatherFallEffect> FEATHER_FALL = EFFECTS
            .register("effect.test_featherfall", FeatherFallEffect::new);

    public static final RegistryObject<NewBurningSoulEffect> BURNING_SOUL = EFFECTS
            .register("effect.v2.burning_soul", NewBurningSoulEffect::new);

    public static final RegistryObject<NewFireArmorEffect> FIRE_ARMOR = EFFECTS
            .register("effect.v2.fire_armor_effect", NewFireArmorEffect::new);

    public static final RegistryObject<NewHealEffect> NEW_HEAL = EFFECTS
            .register("effect.new_heal", NewHealEffect::new);

    public static final RegistryObject<PhoenixAspectEffect> PHOENIX_ASPECT = EFFECTS
            .register("effect.test_phoenix_aspect", PhoenixAspectEffect::new);

    public static final RegistryObject<SkinLikeWoodEffect> SKIN_LIKE_WOOD = EFFECTS
            .register("effect.v2.skin_like_wood", SkinLikeWoodEffect::new);

    public static final RegistryObject<TestFallCountingEffect> FALL_COUNTER = EFFECTS
            .register("effect.v2.fall_counter", TestFallCountingEffect::new);

    public static final RegistryObject<MKSongSustainEffect> NEW_HEAL_SONG_SUSTAIN = EFFECTS
            .register("effect.v2.new_heal_song_sustain", MKSongSustainEffect::new);

    public static final RegistryObject<MKSongPulseEffect> NEW_HEAL_SONG_PULSE = EFFECTS
            .register("effect.v2.new_heal_song_pulse", MKSongPulseEffect::new);


    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
