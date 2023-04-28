package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.OnHitEffect;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.effects.status.MKResistance;
import com.chaosbuffalo.mkcore.effects.triggers.LivingHurtEntityTriggers;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class MKUEffects {

    private static final DeferredRegister<MKEffect> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.EFFECT_REGISTRY_NAME, MKUltra.MODID);

    private static final UUID FIRE_RESISTANCE_UUID = UUID.fromString("e39ad714-1726-417f-a6b2-21a956fdba79");

    private static final UUID BREAK_FIRE_UUID = UUID.fromString("b610e5c3-089d-474a-9240-18074f225f6d");


    public static final RegistryObject<MKResistance> FIRE_ARMOR = REGISTRY.register("effect.fire_armor",
            () -> new MKResistance(MKAttributes.FIRE_RESISTANCE, FIRE_RESISTANCE_UUID, 0.2f));
    public static final RegistryObject<MKResistance> BREAK_FIRE = REGISTRY.register("effect.break_fire",
            () -> new MKResistance(MKAttributes.FIRE_RESISTANCE, BREAK_FIRE_UUID, -0.1f));

    public static final RegistryObject<CureEffect> CURE = REGISTRY.register("effect.cure",
            CureEffect::new);

    public static final RegistryObject<BurnEffect> BURN = REGISTRY.register("effect.burn",
            BurnEffect::new);

    public static final RegistryObject<FlameWaveEffect> FLAME_WAVE = REGISTRY.register("effect.flame_wave",
            FlameWaveEffect::new);

    public static final RegistryObject<LifeSiphonEffect> LIFE_SIPHON = REGISTRY.register("effect.life_siphon",
            LifeSiphonEffect::new);

    public static final RegistryObject<ClericHealEffect> CLERIC_HEAL = REGISTRY.register("effect.cleric_heal",
            ClericHealEffect::new);

    public static final RegistryObject<PullEffect> PULL = REGISTRY.register("effect.pull",
            PullEffect::new);

    public static final RegistryObject<YaupEffect> YAUP = REGISTRY.register("effect.yaup",
            YaupEffect::new);

    public static final RegistryObject<YankEffect> YANK = REGISTRY.register("effect.yank",
            YankEffect::new);

    public static final RegistryObject<RepulseEffect> REPULSE = REGISTRY.register("effect.repulse",
            RepulseEffect::new);

    public static final RegistryObject<WarpCurseEffect> WARP_CURSE = REGISTRY.register("effect.warp_curse",
            WarpCurseEffect::new);

    public static final RegistryObject<SeverTendonEffect> SEVER_TENDON = REGISTRY.register("effect.sever_tendon",
            SeverTendonEffect::new);

    public static final RegistryObject<DrownEffect> DROWN = REGISTRY.register("effect.drown",
            DrownEffect::new);

    public static final RegistryObject<VampiricDamageEffect> VAMPIRIC_DAMAGE = REGISTRY.register("effect.vampiric_damage",
            VampiricDamageEffect::new);

    public static final RegistryObject<WarpTargetEffect> WARP_TARGET = REGISTRY.register("effect.warp_target",
            WarpTargetEffect::new);

    public static final RegistryObject<FuriousBroodingEffect> FURIOUS_BROODING = REGISTRY.register("effect.furious_brooding",
            FuriousBroodingEffect::new);

    public static final RegistryObject<SoulDrainEffect> SOUL_DRAIN = REGISTRY.register("effect.soul_drain",
            SoulDrainEffect::new);

    public static final RegistryObject<IgniteEffect> IGNITE = REGISTRY.register("effect.ignite",
            IgniteEffect::new);

    public static final RegistryObject<SkinLikeWoodEffect> SKIN_LIKE_WOOD = REGISTRY.register("effect.skin_like_wood",
            SkinLikeWoodEffect::new);

    public static final RegistryObject<ShadowbringerEffect> SHADOWBRINGER = REGISTRY.register("effect.shadowbringer",
            ShadowbringerEffect::new);

    public static final RegistryObject<EngulfingDarknessEffect> ENGULFING_DARKNESS = REGISTRY.register("effect.engulfing_darkness",
            EngulfingDarknessEffect::new);

    public static final RegistryObject<NaturesRemedyEffect> NATURES_REMEDY = REGISTRY.register("effect.natures_remedy",
            NaturesRemedyEffect::new);

    public static final RegistryObject<GreenSoulEffect> GREEN_SOUL = REGISTRY.register("effect.green_soul",
            GreenSoulEffect::new);

    public static final RegistryObject<FrozenGraspEffect> FROZEN_GRASP = REGISTRY.register("effect.frozen_grasp",
            FrozenGraspEffect::new);

    public static final RegistryObject<OnHitEffect<FrozenGraspEffect>> FROZEN_GRASP_APPLIER = REGISTRY.register(
            "effect.frozen_grasp_applier",
            () -> new OnHitEffect<>(FROZEN_GRASP, SpellTriggers.LIVING_HURT_ENTITY::registerMeleeEffect,
                    UUID.fromString("8355cbed-9f22-4796-a382-b755ce5cbc8d"),
                    new ResourceLocation(MKUltra.MODID, "frozen_grasp_cast")));


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
