package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.instant.AbilityMagicDamageEffect;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKOldParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.MKParticleEffect;
import com.chaosbuffalo.mkcore.effects.utility.SoundEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CoreEffects {
    public static final DeferredRegister<MKEffect> EFFECTS = DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_NAME, MKCore.MOD_ID);

    public static final RegistryObject<AbilityMagicDamageEffect> ABILITY_MAGIC_DAMAGE = EFFECTS
            .register("effect.ability_magic_damage", AbilityMagicDamageEffect::new);

    public static final RegistryObject<MKAbilityDamageEffect> ABILITY_DAMAGE = EFFECTS
            .register("effect.ability_damage", MKAbilityDamageEffect::new);

    public static final RegistryObject<StunEffect> STUN = EFFECTS
            .register("effect.stun", StunEffect::new);

    public static final RegistryObject<MKOldParticleEffect> OLD_PARTICLE = EFFECTS
            .register("effect.old_particle", MKOldParticleEffect::new);

    public static final RegistryObject<MKParticleEffect> PARTICLE = EFFECTS
            .register("effect.mk_particle", MKParticleEffect::new);

    public static final RegistryObject<SoundEffect> SOUND = EFFECTS
            .register("effect.sound", SoundEffect::new);

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
