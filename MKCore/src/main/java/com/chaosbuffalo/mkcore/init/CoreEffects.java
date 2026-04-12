package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMeleeDamageEffect;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMagicDamageEffect;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreEffects {
    public static final DeferredRegister<MKEffect> EFFECTS = DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<MKEffect, AbilityMagicDamageEffect> ABILITY_MAGIC_DAMAGE = EFFECTS
            .register("effect.ability_magic_damage", AbilityMagicDamageEffect::new);

    public static final DeferredHolder<MKEffect, MKAbilityDamageEffect> ABILITY_DAMAGE = EFFECTS
            .register("effect.ability_damage", MKAbilityDamageEffect::new);

    public static final DeferredHolder<MKEffect, AbilityMeleeDamageEffect> ABILITY_MELEE_DAMAGE = EFFECTS
            .register("effect.ability_melee_damage", AbilityMeleeDamageEffect::new);

    public static final DeferredHolder<MKEffect, StunEffect> STUN = EFFECTS
            .register("effect.stun", StunEffect::new);

    public static final DeferredHolder<MKEffect, MKOldParticleEffect> OLD_PARTICLE = EFFECTS
            .register("effect.old_particle", MKOldParticleEffect::new);

    public static final DeferredHolder<MKEffect, MKParticleEffect> PARTICLE = EFFECTS
            .register("effect.mk_particle", MKParticleEffect::new);

    public static final DeferredHolder<MKEffect, SoundEffect> SOUND = EFFECTS
            .register("effect.sound", SoundEffect::new);

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
