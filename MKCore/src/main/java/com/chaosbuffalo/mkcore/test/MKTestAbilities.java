package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.test.abilities.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MKTestAbilities {
    public static final DeferredRegister<MKAbility> ABILITIES = DeferredRegister.create(MKCoreRegistry.ABILITY_REGISTRY_NAME, MKCore.MOD_ID);
    public static final RegistryObject<EmberTestAbility> TEST_EMBER = ABILITIES.register("test_ember", EmberTestAbility::new);
    public static final RegistryObject<HealAbility> TEST_HEAL = ABILITIES.register("test_heal", HealAbility::new);
    public static final RegistryObject<HealingRainTestAbility> TEST_HEALING_RAN = ABILITIES.register("test_healing_rain", HealingRainTestAbility::new);
    public static final RegistryObject<NewBurningSoul> TEST_NEW_BURNING_SOUL = ABILITIES.register("test_burning_soul", NewBurningSoul::new);
    public static final RegistryObject<NewFireArmor> TEST_NEW_FIRE_ARMOR = ABILITIES.register("test_fire_armor", NewFireArmor::new);
    public static final RegistryObject<NewHeal> TEST_NEW_HEAL = ABILITIES.register("test_new_heal", NewHeal::new);
    public static final RegistryObject<NewHealSong> TEST_NEW_HEAL_SONG = ABILITIES.register("test_heal_song", NewHealSong::new);
    public static final RegistryObject<PhoenixAspectAbility> TEST_PHOENIX_ASPECT = ABILITIES.register("test_phoenix_aspect", PhoenixAspectAbility::new);
    public static final RegistryObject<SkinLikeWoodTestAbility> TEST_SKIN_LIKE_WOOD = ABILITIES.register("test_skin_like_wood", SkinLikeWoodTestAbility::new);
    public static final RegistryObject<WhirlwindBladesTestAbility> TEST_WHIRLWIND_BLADES = ABILITIES.register("test_whirlwind_blades", WhirlwindBladesTestAbility::new);


    public static void register(IEventBus modBus) {
        ABILITIES.register(modBus);
    }

}
