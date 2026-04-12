package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.OnHitEffect;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.effects.status.MKResistance;
import com.chaosbuffalo.mkcore.effects.status.OnStackEffect;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.druid.FlameBlade;
import com.chaosbuffalo.mkultra.abilities.misc.FrozenGraspAbility;
import com.chaosbuffalo.mkultra.effects.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public class MKUEffects {

    private static final DeferredRegister<MKEffect> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.EFFECTS, MKUltra.MODID);

    private static final UUID FIRE_RESISTANCE_UUID = UUID.fromString("e39ad714-1726-417f-a6b2-21a956fdba79");

    private static final UUID BREAK_FIRE_UUID = UUID.fromString("b610e5c3-089d-474a-9240-18074f225f6d");

    private static final UUID ATTACK_SPEED_HASTE_UUID = UUID.fromString("57bdfc5d-207a-40d6-a3d4-6bdcf6948fa1");
    private static final UUID ATTACK_SPEED_SLOW_UUID = UUID.fromString("4f2f10f0-4dc4-48a8-844a-8c4b38e707fa");


    public static final DeferredHolder<MKEffect, MKResistance> FIRE_ARMOR = REGISTRY.register("effect.fire_armor",
            () -> new MKResistance(MKAttributes.FIRE_RESISTANCE, FIRE_RESISTANCE_UUID, 0.2f));
    public static final DeferredHolder<MKEffect, MKResistance> BREAK_FIRE = REGISTRY.register("effect.break_fire",
            () -> new MKResistance(MKAttributes.FIRE_RESISTANCE, BREAK_FIRE_UUID, -0.1f));

    public static final DeferredHolder<MKEffect, CureEffect> CURE = REGISTRY.register("effect.cure",
            CureEffect::new);

    public static final DeferredHolder<MKEffect, BurnEffect> BURN = REGISTRY.register("effect.burn",
            BurnEffect::new);

    public static final DeferredHolder<MKEffect, FlameWaveEffect> FLAME_WAVE = REGISTRY.register("effect.flame_wave",
            FlameWaveEffect::new);

    public static final DeferredHolder<MKEffect, SkullFlameBreathEffect> SKULL_FLAME_BREATH = REGISTRY.register("effect.skull_flame_breath",
            SkullFlameBreathEffect::new);

    public static final DeferredHolder<MKEffect, LifeSiphonEffect> LIFE_SIPHON = REGISTRY.register("effect.life_siphon",
            LifeSiphonEffect::new);

    public static final DeferredHolder<MKEffect, ClericHealEffect> CLERIC_HEAL = REGISTRY.register("effect.cleric_heal",
            ClericHealEffect::new);

    public static final DeferredHolder<MKEffect, PullEffect> PULL = REGISTRY.register("effect.pull",
            PullEffect::new);

    public static final DeferredHolder<MKEffect, YaupEffect> YAUP = REGISTRY.register("effect.yaup",
            YaupEffect::new);

    public static final DeferredHolder<MKEffect, YankEffect> YANK = REGISTRY.register("effect.yank",
            YankEffect::new);

    public static final DeferredHolder<MKEffect, RepulseEffect> REPULSE = REGISTRY.register("effect.repulse",
            RepulseEffect::new);

    public static final DeferredHolder<MKEffect, WarpCurseEffect> WARP_CURSE = REGISTRY.register("effect.warp_curse",
            WarpCurseEffect::new);

    public static final DeferredHolder<MKEffect, SeverTendonEffect> SEVER_TENDON = REGISTRY.register("effect.sever_tendon",
            SeverTendonEffect::new);

    public static final DeferredHolder<MKEffect, DrownEffect> DROWN = REGISTRY.register("effect.drown",
            DrownEffect::new);

    public static final DeferredHolder<MKEffect, VampiricDamageEffect> VAMPIRIC_DAMAGE = REGISTRY.register("effect.vampiric_damage",
            VampiricDamageEffect::new);

    public static final DeferredHolder<MKEffect, WarpTargetEffect> WARP_TARGET = REGISTRY.register("effect.warp_target",
            WarpTargetEffect::new);

    public static final DeferredHolder<MKEffect, FuriousBroodingEffect> FURIOUS_BROODING = REGISTRY.register("effect.furious_brooding",
            FuriousBroodingEffect::new);

    public static final DeferredHolder<MKEffect, SoulDrainEffect> SOUL_DRAIN = REGISTRY.register("effect.soul_drain",
            SoulDrainEffect::new);

    public static final DeferredHolder<MKEffect, IgniteEffect> IGNITE = REGISTRY.register("effect.ignite",
            IgniteEffect::new);

    public static final DeferredHolder<MKEffect, SkinLikeWoodEffect> SKIN_LIKE_WOOD = REGISTRY.register("effect.skin_like_wood",
            SkinLikeWoodEffect::new);

    public static final DeferredHolder<MKEffect, ShadowbringerEffect> SHADOWBRINGER = REGISTRY.register("effect.shadowbringer",
            ShadowbringerEffect::new);

    public static final DeferredHolder<MKEffect, EngulfingDarknessEffect> ENGULFING_DARKNESS = REGISTRY.register("effect.engulfing_darkness",
            EngulfingDarknessEffect::new);

    public static final DeferredHolder<MKEffect, NaturesRemedyEffect> NATURES_REMEDY = REGISTRY.register("effect.natures_remedy",
            NaturesRemedyEffect::new);

    public static final DeferredHolder<MKEffect, GreenSoulEffect> GREEN_SOUL = REGISTRY.register("effect.green_soul",
            GreenSoulEffect::new);

    public static final DeferredHolder<MKEffect, FrozenGraspEffect> FROZEN_GRASP = REGISTRY.register("effect.frozen_grasp",
            FrozenGraspEffect::new);

    public static final DeferredHolder<MKEffect, HolyWordEffect> HOLY_WORD_EFFECT = REGISTRY.register("effect.holy_word",
            HolyWordEffect::new);

    public static final DeferredHolder<MKEffect, AttackSpeedEffect> ATTACK_SPEED_HASTE = REGISTRY.register("effect.attack_speed_haste",
            () -> new AttackSpeedEffect(ATTACK_SPEED_HASTE_UUID, 0.25, 0.03, MKAttributes.ENCHANTMENT));

    public static final DeferredHolder<MKEffect, AttackSpeedEffect> ATTACK_SPEED_SLOW = REGISTRY.register("effect.attack_speed_slow",
            () -> new AttackSpeedEffect(ATTACK_SPEED_SLOW_UUID, -0.25, -0.03, MKAttributes.PHANTASM));

    public static final DeferredHolder<MKEffect, OnHitEffect> FROZEN_GRASP_APPLIER = REGISTRY.register(
            "effect.frozen_grasp_applier",
            () -> new OnHitEffect((args) ->
                    MKUAbilities.FROZEN_GRASP.get().onHitEffect(args),
                    SpellTriggers.LIVING_HURT_ENTITY::registerMeleeEffect,
                    FrozenGraspAbility.CAST_PARTICLES, true));

    public static final DeferredHolder<MKEffect, HeldItemOnHitEffect> FLAME_BLADE_APPLIER = REGISTRY.register(
            "effect.flame_blade_applier",
            () -> new HeldItemOnHitEffect((args) ->
                    MKUAbilities.FLAME_BLADE.get().onHitEffect(args),
                    SpellTriggers.LIVING_HURT_ENTITY::registerMeleeEffect,
                    FlameBlade.EDGE_PARTICLES, true));


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
