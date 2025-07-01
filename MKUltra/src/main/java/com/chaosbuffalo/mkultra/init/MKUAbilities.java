package com.chaosbuffalo.mkultra.init;


import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.brawler.*;
import com.chaosbuffalo.mkultra.abilities.cleric.*;
import com.chaosbuffalo.mkultra.abilities.green_knight.*;
import com.chaosbuffalo.mkultra.abilities.misc.*;
import com.chaosbuffalo.mkultra.abilities.necromancer.*;
import com.chaosbuffalo.mkultra.abilities.nether_mage.*;
import com.chaosbuffalo.mkultra.abilities.passives.GreenSoulAbility;
import com.chaosbuffalo.mkultra.abilities.passives.LifeSiphonAbility;
import com.chaosbuffalo.mkultra.abilities.passives.SoulDrainAbility;
import com.chaosbuffalo.mkultra.abilities.structure.NecrotideGolemBeam;
import com.chaosbuffalo.mkultra.abilities.wet_wizard.DrownAbility;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKUAbilities {

    public static final DeferredRegister<MKAbility> REGISTRY = DeferredRegister.create(MKCoreRegistry.ABILITIES, MKUltra.MODID);
    //misc
    public static final DeferredHolder<MKAbility, WrathBeamAbility> WRATH_BEAM = REGISTRY.register("wrath_beam", WrathBeamAbility::new);
    public static final DeferredHolder<MKAbility, WrathBeamFlurryAbility> WRATH_BEAM_FLURRY = REGISTRY.register("wrath_beam_flurry", WrathBeamFlurryAbility::new);
    public static final DeferredHolder<MKAbility, SeverTendonAbility> SEVER_TENDON = REGISTRY.register("sever_tendon", SeverTendonAbility::new);
    public static final DeferredHolder<MKAbility, FireballAbility> FIREBALL = REGISTRY.register("fireball", FireballAbility::new);
    public static final DeferredHolder<MKAbility, FireballBurstAbility> FIREBALL_BURST = REGISTRY.register("fireball_burst", FireballBurstAbility::new);
    public static final DeferredHolder<MKAbility, ShadowPulseFlurryAbility> SHADOW_PUlSE_FLURRY = REGISTRY.register("shadow_pulse_flurry", ShadowPulseFlurryAbility::new);

    public static final DeferredHolder<MKAbility, SeafuryAbility> SEAFURY = REGISTRY.register("seafury", SeafuryAbility::new);

    public static final DeferredHolder<MKAbility, NecrotideGolemBeam> NECROTIDE_GOLEM_BEAM = REGISTRY.register("necrotide_golem_beam", NecrotideGolemBeam::new);

    public static final DeferredHolder<MKAbility, FrozenGraspAbility> FROZEN_GRASP = REGISTRY.register("frozen_grasp",
            FrozenGraspAbility::new);

    public static final DeferredHolder<MKAbility, HolyWordAbility> HOLY_WORD = REGISTRY.register("holy_word",
            HolyWordAbility::new);
    public static final DeferredHolder<MKAbility, HolyWordShotgunAbility> HOLY_WORD_SHOTGUN = REGISTRY.register("holy_word_shotgun",
            HolyWordShotgunAbility::new);
    public static final DeferredHolder<MKAbility, HolyFireAbility> HOLY_FIRE = REGISTRY.register("holy_fire",
            HolyFireAbility::new);
    public static final DeferredHolder<MKAbility, HolyFireFlurryAbility> HOLY_FIRE_FLURRY = REGISTRY.register("holy_fire_flurry",
            HolyFireFlurryAbility::new);
    public static final DeferredHolder<MKAbility, HolyWordBurstAbility> HOLY_WORD_BURST = REGISTRY.register("holy_word_burst",
            HolyWordBurstAbility::new);
    public static final DeferredHolder<MKAbility, HealingCircleAbility> HOLY_CIRCLE_ABILITY = REGISTRY.register("healing_circle", HealingCircleAbility::new);

    //necromancer
    public static final DeferredHolder<MKAbility, MKEntitySummonAbility> TEST_SUMMON = REGISTRY.register("test_summon",
            () -> new MKEntitySummonAbility(MKUltra.id("hyborean_sorcerer_queen"), MKAttributes.NECROMANCY));

    public static final DeferredHolder<MKAbility, MKEntitySummonAbility> NECROTIDE_WARRIOR_SUMMON = REGISTRY.register("necrotide_warrior_summon",
            () -> new MKEntitySummonAbility(MKUltra.id("necrotide_skeletal_warrior"), MKAttributes.NECROMANCY));
    public static final DeferredHolder<MKAbility, ShadowPulseAbility> SHADOW_PULSE = REGISTRY.register("shadow_pulse", ShadowPulseAbility::new);
    public static final DeferredHolder<MKAbility, ShadowBoltAbility> SHADOW_BOLT = REGISTRY.register("shadow_bolt", ShadowBoltAbility::new);
    public static final DeferredHolder<MKAbility, LifeSpikeAbility> LIFE_SPIKE = REGISTRY.register("life_spike", LifeSpikeAbility::new);
    public static final DeferredHolder<MKAbility, EngulfingDarknessAbility> ENGULFING_DARKNESS = REGISTRY.register("engulfing_darkness", EngulfingDarknessAbility::new);
    public static final DeferredHolder<MKAbility, ShadowboltDualShotgunAbility> SHADOW_BOLT_DUAL_SHOTGUN = REGISTRY.register("shadow_bolt_dual_shotgun", ShadowboltDualShotgunAbility::new);

    // nethermage
    public static final DeferredHolder<MKAbility, EmberAbility> EMBER = REGISTRY.register("ember", EmberAbility::new);
    public static final DeferredHolder<MKAbility, FireArmorAbility> FIRE_ARMOR = REGISTRY.register("fire_armor", FireArmorAbility::new);
    public static final DeferredHolder<MKAbility, FlameWaveAbility> FLAME_WAVE = REGISTRY.register("flame_wave", FlameWaveAbility::new);
    public static final DeferredHolder<MKAbility, IgniteAbility> IGNITE = REGISTRY.register("ignite", IgniteAbility::new);
    public static final DeferredHolder<MKAbility, WarpCurseAbility> WARP_CURSE = REGISTRY.register("warp_curse", WarpCurseAbility::new);

    //green knight
    public static final DeferredHolder<MKAbility, SpiritBombAbility> SPIRIT_BOMB = REGISTRY.register("spirit_bomb", SpiritBombAbility::new);
    public static final DeferredHolder<MKAbility, SkinLikeWoodAbility> SKIN_LIKE_WOOD = REGISTRY.register("skin_like_wood", SkinLikeWoodAbility::new);
    public static final DeferredHolder<MKAbility, NaturesRemedyAbility> NATURES_REMEDY = REGISTRY.register("natures_remedy", NaturesRemedyAbility::new);
    public static final DeferredHolder<MKAbility, ExplosiveGrowthAbility> EXPLOSIVE_GROWTH = REGISTRY.register("explosive_growth", ExplosiveGrowthAbility::new);
    public static final DeferredHolder<MKAbility, CleansingSeedAbility> CLEANSING_SEED = REGISTRY.register("cleansing_seed", CleansingSeedAbility::new);

    public static final DeferredHolder<MKAbility, GreenSoulAbility> GREEN_SOUL = REGISTRY.register("green_soul", GreenSoulAbility::new);

    //cleric
    public static final DeferredHolder<MKAbility, SmiteAbility> SMITE = REGISTRY.register("smite", SmiteAbility::new);
    public static final DeferredHolder<MKAbility, PowerWordSummonAbility> POWER_WORD_SUMMON = REGISTRY.register("power_word_summon", PowerWordSummonAbility::new);
    public static final DeferredHolder<MKAbility, InspireAbility> INSPIRE = REGISTRY.register("inspire", InspireAbility::new);
    public static final DeferredHolder<MKAbility, HealAbility> HEAL = REGISTRY.register("heal", HealAbility::new);
    public static final DeferredHolder<MKAbility, GalvanizeAbility> GALVANIZE = REGISTRY.register("galvanize", GalvanizeAbility::new);

    //brawler
    public static final DeferredHolder<MKAbility, YaupAbility> YAUP = REGISTRY.register("yaup", YaupAbility::new);
    public static final DeferredHolder<MKAbility, YankAbility> YANK = REGISTRY.register("yank", YankAbility::new);
    public static final DeferredHolder<MKAbility, WhirlwindBladesAbility> WHIRLWIND_BLADES = REGISTRY.register("whirlwind_blades", WhirlwindBladesAbility::new);
    public static final DeferredHolder<MKAbility, StunningShoutAbility> STUNNING_SHOUT = REGISTRY.register("stunning_shout", StunningShoutAbility::new);
    public static final DeferredHolder<MKAbility, FuriousBroodingAbility> FURIOUS_BROODING = REGISTRY.register("furious_brooding", FuriousBroodingAbility::new);

    //Wet Wizard
    public static final DeferredHolder<MKAbility, DrownAbility> DROWN = REGISTRY.register("drown", DrownAbility::new);

    //talents
    public static final DeferredHolder<MKAbility, LifeSiphonAbility> LIFE_SIPHON = REGISTRY.register("life_siphon", LifeSiphonAbility::new);
    public static final DeferredHolder<MKAbility, SoulDrainAbility> SOUL_DRAIN = REGISTRY.register("soul_drain", SoulDrainAbility::new);


    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
