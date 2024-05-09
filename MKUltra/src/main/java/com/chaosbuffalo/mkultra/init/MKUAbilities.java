package com.chaosbuffalo.mkultra.init;


import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.brawler.*;
import com.chaosbuffalo.mkultra.abilities.cleric.*;
import com.chaosbuffalo.mkultra.abilities.green_knight.*;
import com.chaosbuffalo.mkultra.abilities.misc.*;
import com.chaosbuffalo.mkultra.abilities.necromancer.EngulfingDarknessAbility;
import com.chaosbuffalo.mkultra.abilities.necromancer.LifeSpikeAbility;
import com.chaosbuffalo.mkultra.abilities.necromancer.ShadowBoltAbility;
import com.chaosbuffalo.mkultra.abilities.necromancer.ShadowPulseAbility;
import com.chaosbuffalo.mkultra.abilities.nether_mage.*;
import com.chaosbuffalo.mkultra.abilities.passives.GreenSoulAbility;
import com.chaosbuffalo.mkultra.abilities.passives.LifeSiphonAbility;
import com.chaosbuffalo.mkultra.abilities.passives.SoulDrainAbility;
import com.chaosbuffalo.mkultra.abilities.structure.NecrotideGolemBeam;
import com.chaosbuffalo.mkultra.abilities.wet_wizard.DrownAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKUAbilities {

    public static final DeferredRegister<MKAbility> REGISTRY = DeferredRegister.create(MKCoreRegistry.ABILITY_REGISTRY_NAME, MKUltra.MODID);
    //misc
    public static final RegistryObject<WrathBeamAbility> WRATH_BEAM = REGISTRY.register("wrath_beam", WrathBeamAbility::new);
    public static final RegistryObject<WrathBeamFlurryAbility> WRATH_BEAM_FLURRY = REGISTRY.register("wrath_beam_flurry", WrathBeamFlurryAbility::new);
    public static final RegistryObject<SeverTendonAbility> SEVER_TENDON = REGISTRY.register("sever_tendon", SeverTendonAbility::new);
    public static final RegistryObject<FireballAbility> FIREBALL = REGISTRY.register("fireball", FireballAbility::new);
    public static final RegistryObject<ShadowPulseFlurryAbility> SHADOW_PUlSE_FLURRY = REGISTRY.register("shadow_pulse_flurry", ShadowPulseFlurryAbility::new);

    public static final RegistryObject<SeafuryAbility> SEAFURY = REGISTRY.register("seafury", SeafuryAbility::new);

    public static final RegistryObject<NecrotideGolemBeam> NECROTIDE_GOLEM_BEAM = REGISTRY.register("necrotide_golem_beam", NecrotideGolemBeam::new);

    public static final RegistryObject<FrozenGraspAbility> FROZEN_GRASP = REGISTRY.register("frozen_grasp",
            FrozenGraspAbility::new);

    public static final RegistryObject<HolyWordAbility> HOLY_WORD = REGISTRY.register("holy_word",
            HolyWordAbility::new);

    //necromancer
    public static final RegistryObject<MKEntitySummonAbility> TEST_SUMMON = REGISTRY.register("test_summon",
            () -> new MKEntitySummonAbility(new ResourceLocation(MKUltra.MODID, "hyborean_sorcerer_queen"), MKAttributes.NECROMANCY));

    public static final RegistryObject<MKEntitySummonAbility> NECROTIDE_WARRIOR_SUMMON = REGISTRY.register("necrotide_warrior_summon",
            () -> new MKEntitySummonAbility(new ResourceLocation(MKUltra.MODID, "necrotide_skeletal_warrior"), MKAttributes.NECROMANCY));
    public static final RegistryObject<ShadowPulseAbility> SHADOW_PULSE = REGISTRY.register("shadow_pulse", ShadowPulseAbility::new);
    public static final RegistryObject<ShadowBoltAbility> SHADOW_BOLT = REGISTRY.register("shadow_bolt", ShadowBoltAbility::new);
    public static final RegistryObject<LifeSpikeAbility> LIFE_SPIKE = REGISTRY.register("life_spike", LifeSpikeAbility::new);
    public static final RegistryObject<EngulfingDarknessAbility> ENGULFING_DARKNESS = REGISTRY.register("engulfing_darkness", EngulfingDarknessAbility::new);

    // nethermage
    public static final RegistryObject<EmberAbility> EMBER = REGISTRY.register("ember", EmberAbility::new);
    public static final RegistryObject<FireArmorAbility> FIRE_ARMOR = REGISTRY.register("fire_armor", FireArmorAbility::new);
    public static final RegistryObject<FlameWaveAbility> FLAME_WAVE = REGISTRY.register("flame_wave", FlameWaveAbility::new);
    public static final RegistryObject<IgniteAbility> IGNITE = REGISTRY.register("ignite", IgniteAbility::new);
    public static final RegistryObject<WarpCurseAbility> WARP_CURSE = REGISTRY.register("warp_curse", WarpCurseAbility::new);

    //green knight
    public static final RegistryObject<SpiritBombAbility> SPIRIT_BOMB = REGISTRY.register("spirit_bomb", SpiritBombAbility::new);
    public static final RegistryObject<SkinLikeWoodAbility> SKIN_LIKE_WOOD = REGISTRY.register("skin_like_wood", SkinLikeWoodAbility::new);
    public static final RegistryObject<NaturesRemedyAbility> NATURES_REMEDY = REGISTRY.register("natures_remedy", NaturesRemedyAbility::new);
    public static final RegistryObject<ExplosiveGrowthAbility> EXPLOSIVE_GROWTH = REGISTRY.register("explosive_growth", ExplosiveGrowthAbility::new);
    public static final RegistryObject<CleansingSeedAbility> CLEANSING_SEED = REGISTRY.register("cleansing_seed", CleansingSeedAbility::new);

    public static final RegistryObject<GreenSoulAbility> GREEN_SOUL = REGISTRY.register("green_soul", GreenSoulAbility::new);

    //cleric
    public static final RegistryObject<SmiteAbility> SMITE = REGISTRY.register("smite", SmiteAbility::new);
    public static final RegistryObject<PowerWordSummonAbility> POWER_WORD_SUMMON = REGISTRY.register("power_word_summon", PowerWordSummonAbility::new);
    public static final RegistryObject<InspireAbility> INSPIRE = REGISTRY.register("inspire", InspireAbility::new);
    public static final RegistryObject<HealAbility> HEAL = REGISTRY.register("heal", HealAbility::new);
    public static final RegistryObject<GalvanizeAbility> GALVANIZE = REGISTRY.register("galvanize", GalvanizeAbility::new);

    //brawler
    public static final RegistryObject<YaupAbility> YAUP = REGISTRY.register("yaup", YaupAbility::new);
    public static final RegistryObject<YankAbility> YANK = REGISTRY.register("yank", YankAbility::new);
    public static final RegistryObject<WhirlwindBladesAbility> WHIRLWIND_BLADES = REGISTRY.register("whirlwind_blades", WhirlwindBladesAbility::new);
    public static final RegistryObject<StunningShoutAbility> STUNNING_SHOUT = REGISTRY.register("stunning_shout", StunningShoutAbility::new);
    public static final RegistryObject<FuriousBroodingAbility> FURIOUS_BROODING = REGISTRY.register("furious_brooding", FuriousBroodingAbility::new);

    //Wet Wizard
    public static final RegistryObject<DrownAbility> DROWN = REGISTRY.register("drown", DrownAbility::new);

    //talents
    public static final RegistryObject<LifeSiphonAbility> LIFE_SIPHON = REGISTRY.register("life_siphon", LifeSiphonAbility::new);
    public static final RegistryObject<SoulDrainAbility> SOUL_DRAIN = REGISTRY.register("soul_drain", SoulDrainAbility::new);


    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
