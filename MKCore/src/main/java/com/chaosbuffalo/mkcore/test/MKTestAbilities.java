package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.test.abilities.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKTestAbilities {
    public static final DeferredRegister<MKAbility> ABILITIES = DeferredRegister.create(MKCoreRegistry.ABILITY_REGISTRY_NAME, MKCore.MOD_ID);
    public static final RegistryObject<EmberAbility> TEST_EMBER = ABILITIES.register("ability.test_ember", EmberAbility::new);
    public static final RegistryObject<HealAbility> TEST_HEAL = ABILITIES.register("ability.test_heal", HealAbility::new);
    public static final RegistryObject<HealingRain> TEST_HEALING_RAN = ABILITIES.register("ability.test_healing_rain", HealingRain::new);
    public static final RegistryObject<NewBurningSoul> TEST_NEW_BURNING_SOUL = ABILITIES.register("ability.v2.burning_soul", NewBurningSoul::new);
    public static final RegistryObject<NewFireArmor> TEST_NEW_FIRE_ARMOR = ABILITIES.register("ability.v2.fire_armor", NewFireArmor::new);
    public static final RegistryObject<NewHeal> TEST_NEW_HEAL = ABILITIES.register("ability.new_heal", NewHeal::new);
    public static final RegistryObject<NewHealSong> TEST_NEW_HEAL_SONG = ABILITIES.register("ability.v2.heal_song", NewHealSong::new);
    public static final RegistryObject<PhoenixAspectAbility> TEST_PHOENIX_ASPECT = ABILITIES.register("ability.test_phoenix_aspect", PhoenixAspectAbility::new);
    public static final RegistryObject<SkinLikeWoodAbility> TEST_SKIN_LIKE_WOOD = ABILITIES.register("ability.v2.skin_like_wood", SkinLikeWoodAbility::new);
    public static final RegistryObject<WhirlwindBlades> TEST_WHIRLWIND_BLADES = ABILITIES.register("ability.test_whirlwind_blades", WhirlwindBlades::new);


    public static void register(IEventBus modBus) {
        ABILITIES.register(modBus);
    }

}
