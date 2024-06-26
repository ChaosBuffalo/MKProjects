package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MKUSounds {

    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MKUltra.MODID);

    public static RegistryObject<SoundEvent> casting_general = REGISTRY.register("casting_general", () -> createSound("casting_general"));
    public static RegistryObject<SoundEvent> casting_fire = REGISTRY.register("casting_fire", () -> createSound("casting_fire"));
    public static RegistryObject<SoundEvent> casting_shadow = REGISTRY.register("casting_shadow", () -> createSound("casting_shadow"));
    public static RegistryObject<SoundEvent> casting_holy = REGISTRY.register("casting_holy", () -> createSound("casting_holy"));
    public static RegistryObject<SoundEvent> casting_water = REGISTRY.register("casting_water", () -> createSound("casting_water"));
    public static RegistryObject<SoundEvent> casting_rain = REGISTRY.register("casting_rain", () -> createSound("casting_rain"));
    public static RegistryObject<SoundEvent> hostile_casting_general = REGISTRY.register("hostile_casting_general", () -> createSound("hostile_casting_general"));
    public static RegistryObject<SoundEvent> hostile_casting_fire = REGISTRY.register("hostile_casting_fire", () -> createSound("hostile_casting_fire"));
    public static RegistryObject<SoundEvent> hostile_casting_shadow = REGISTRY.register("hostile_casting_shadow", () -> createSound("hostile_casting_shadow"));
    public static RegistryObject<SoundEvent> hostile_casting_holy = REGISTRY.register("hostile_casting_holy", () -> createSound("hostile_casting_holy"));
    public static RegistryObject<SoundEvent> hostile_casting_water = REGISTRY.register("hostile_casting_water", () -> createSound("hostile_casting_water"));
    public static RegistryObject<SoundEvent> spell_cast_2 = REGISTRY.register("spell_cast_2", () -> createSound("spell_cast_2"));
    public static RegistryObject<SoundEvent> spell_cast_3 = REGISTRY.register("spell_cast_3", () -> createSound("spell_cast_3"));
    public static RegistryObject<SoundEvent> spell_cast_5 = REGISTRY.register("spell_cast_5", () -> createSound("spell_cast_5"));
    public static RegistryObject<SoundEvent> spell_cast_6 = REGISTRY.register("spell_cast_6", () -> createSound("spell_cast_6"));
    public static RegistryObject<SoundEvent> spell_cast_7 = REGISTRY.register("spell_cast_7", () -> createSound("spell_cast_7"));
    public static RegistryObject<SoundEvent> spell_cast_10 = REGISTRY.register("spell_cast_10", () -> createSound("spell_cast_10"));
    public static RegistryObject<SoundEvent> spell_cast_11 = REGISTRY.register("spell_cast_11", () -> createSound("spell_cast_11"));
    public static RegistryObject<SoundEvent> spell_cast_12 = REGISTRY.register("spell_cast_12", () -> createSound("spell_cast_12"));
    public static RegistryObject<SoundEvent> spell_fire_3 = REGISTRY.register("spell_fire_3", () -> createSound("spell_fire_3"));
    public static RegistryObject<SoundEvent> spell_fire_7 = REGISTRY.register("spell_fire_7", () -> createSound("spell_fire_7"));
    public static RegistryObject<SoundEvent> spell_thunder_1 = REGISTRY.register("spell_thunder_1", () -> createSound("spell_thunder_1"));
    public static RegistryObject<SoundEvent> spell_thunder_3 = REGISTRY.register("spell_thunder_3", () -> createSound("spell_thunder_3"));
    public static RegistryObject<SoundEvent> spell_thunder_8 = REGISTRY.register("spell_thunder_8", () -> createSound("spell_thunder_8"));
    public static RegistryObject<SoundEvent> spell_heal_1 = REGISTRY.register("spell_heal_1", () -> createSound("spell_heal_1"));
    public static RegistryObject<SoundEvent> spell_heal_2 = REGISTRY.register("spell_heal_2", () -> createSound("spell_heal_2"));
    public static RegistryObject<SoundEvent> spell_heal_3 = REGISTRY.register("spell_heal_3", () -> createSound("spell_heal_3"));
    public static RegistryObject<SoundEvent> spell_heal_7 = REGISTRY.register("spell_heal_7", () -> createSound("spell_heal_7"));
    public static RegistryObject<SoundEvent> spell_heal_8 = REGISTRY.register("spell_heal_8", () -> createSound("spell_heal_8"));
    public static RegistryObject<SoundEvent> spell_heal_9 = REGISTRY.register("spell_heal_9", () -> createSound("spell_heal_9"));
    public static RegistryObject<SoundEvent> spell_buff_4 = REGISTRY.register("spell_buff_4", () -> createSound("spell_buff_4"));
    public static RegistryObject<SoundEvent> spell_wind_4 = REGISTRY.register("spell_wind_4", () -> createSound("spell_wind_4"));
    public static RegistryObject<SoundEvent> spell_wind_5 = REGISTRY.register("spell_wind_5", () -> createSound("spell_wind_5"));
    public static RegistryObject<SoundEvent> spell_holy_1 = REGISTRY.register("spell_holy_1", () -> createSound("spell_holy_1"));
    public static RegistryObject<SoundEvent> spell_holy_2 = REGISTRY.register("spell_holy_2", () -> createSound("spell_holy_2"));
    public static RegistryObject<SoundEvent> spell_holy_3 = REGISTRY.register("spell_holy_3", () -> createSound("spell_holy_3"));
    public static RegistryObject<SoundEvent> spell_holy_4 = REGISTRY.register("spell_holy_4", () -> createSound("spell_holy_4"));
    public static RegistryObject<SoundEvent> spell_holy_5 = REGISTRY.register("spell_holy_5", () -> createSound("spell_holy_5"));
    public static RegistryObject<SoundEvent> spell_holy_8 = REGISTRY.register("spell_holy_8", () -> createSound("spell_holy_8"));
    public static RegistryObject<SoundEvent> spell_holy_9 = REGISTRY.register("spell_holy_9", () -> createSound("spell_holy_9"));
    public static RegistryObject<SoundEvent> spell_buff_shield_3 = REGISTRY.register("spell_buff_shield_3", () -> createSound("spell_buff_shield_3"));
    public static RegistryObject<SoundEvent> spell_buff_shield_4 = REGISTRY.register("spell_buff_shield_4", () -> createSound("spell_buff_shield_4"));
    public static RegistryObject<SoundEvent> spell_buff_1 = REGISTRY.register("spell_buff_1", () -> createSound("spell_buff_1"));
    public static RegistryObject<SoundEvent> spell_buff_3 = REGISTRY.register("spell_buff_3", () -> createSound("spell_buff_3"));
    public static RegistryObject<SoundEvent> spell_buff_5 = REGISTRY.register("spell_buff_5", () -> createSound("spell_buff_5"));
    public static RegistryObject<SoundEvent> spell_buff_6 = REGISTRY.register("spell_buff_6", () -> createSound("spell_buff_6"));
    public static RegistryObject<SoundEvent> spell_buff_7 = REGISTRY.register("spell_buff_7", () -> createSound("spell_buff_7"));
    public static RegistryObject<SoundEvent> spell_buff_8 = REGISTRY.register("spell_buff_8", () -> createSound("spell_buff_8"));
    public static RegistryObject<SoundEvent> bow_arrow_1 = REGISTRY.register("bow_arrow_1", () -> createSound("bow_arrow_1"));
    public static RegistryObject<SoundEvent> bow_arrow_2 = REGISTRY.register("bow_arrow_2", () -> createSound("bow_arrow_2"));
    public static RegistryObject<SoundEvent> bow_arrow_3 = REGISTRY.register("bow_arrow_3", () -> createSound("bow_arrow_3"));
    public static RegistryObject<SoundEvent> spell_fire_1 = REGISTRY.register("spell_fire_1", () -> createSound("spell_fire_1"));
    public static RegistryObject<SoundEvent> spell_fire_2 = REGISTRY.register("spell_fire_2", () -> createSound("spell_fire_2"));
    public static RegistryObject<SoundEvent> spell_fire_4 = REGISTRY.register("spell_fire_4", () -> createSound("spell_fire_4"));
    public static RegistryObject<SoundEvent> spell_fire_5 = REGISTRY.register("spell_fire_5", () -> createSound("spell_fire_5"));
    public static RegistryObject<SoundEvent> spell_fire_6 = REGISTRY.register("spell_fire_6", () -> createSound("spell_fire_6"));
    public static RegistryObject<SoundEvent> spell_fire_8 = REGISTRY.register("spell_fire_8", () -> createSound("spell_fire_8"));
    public static RegistryObject<SoundEvent> spell_negative_effect_1 = REGISTRY.register("spell_negative_effect_1", () -> createSound("spell_negative_effect_1"));
    public static RegistryObject<SoundEvent> spell_negative_effect_2 = REGISTRY.register("spell_negative_effect_2", () -> createSound("spell_negative_effect_2"));
    public static RegistryObject<SoundEvent> spell_negative_effect_7 = REGISTRY.register("spell_negative_effect_7", () -> createSound("spell_negative_effect_7"));
    public static RegistryObject<SoundEvent> spell_punch_6 = REGISTRY.register("spell_punch_6", () -> createSound("spell_punch_6"));
    public static RegistryObject<SoundEvent> spell_shadow_2 = REGISTRY.register("spell_shadow_2", () -> createSound("spell_shadow_2"));
    public static RegistryObject<SoundEvent> spell_shadow_3 = REGISTRY.register("spell_shadow_3", () -> createSound("spell_shadow_3"));
    public static RegistryObject<SoundEvent> spell_shadow_5 = REGISTRY.register("spell_shadow_5", () -> createSound("spell_shadow_5"));
    public static RegistryObject<SoundEvent> spell_shadow_6 = REGISTRY.register("spell_shadow_6", () -> createSound("spell_shadow_6"));
    public static RegistryObject<SoundEvent> spell_shadow_8 = REGISTRY.register("spell_shadow_8", () -> createSound("spell_shadow_8"));
    public static RegistryObject<SoundEvent> spell_shadow_9 = REGISTRY.register("spell_shadow_9", () -> createSound("spell_shadow_9"));
    public static RegistryObject<SoundEvent> spell_shadow_10 = REGISTRY.register("spell_shadow_10", () -> createSound("spell_shadow_10"));
    public static RegistryObject<SoundEvent> spell_shadow_11 = REGISTRY.register("spell_shadow_11", () -> createSound("spell_shadow_11"));
    public static RegistryObject<SoundEvent> spell_magic_whoosh_1 = REGISTRY.register("spell_magic_whoosh_1", () -> createSound("spell_magic_whoosh_1"));
    public static RegistryObject<SoundEvent> spell_magic_whoosh_2 = REGISTRY.register("spell_magic_whoosh_2", () -> createSound("spell_magic_whoosh_2"));
    public static RegistryObject<SoundEvent> spell_magic_whoosh_3 = REGISTRY.register("spell_magic_whoosh_3", () -> createSound("spell_magic_whoosh_3"));
    public static RegistryObject<SoundEvent> spell_magic_whoosh_4 = REGISTRY.register("spell_magic_whoosh_4", () -> createSound("spell_magic_whoosh_4"));
    public static RegistryObject<SoundEvent> spell_shout_1 = REGISTRY.register("spell_shout_1", () -> createSound("spell_shout_1"));
    public static RegistryObject<SoundEvent> spell_whirlwind_1 = REGISTRY.register("spell_whirlwind_1", () -> createSound("spell_whirlwind_1"));
    public static RegistryObject<SoundEvent> spell_grab_1 = REGISTRY.register("spell_grab_1", () -> createSound("spell_grab_1"));
    public static RegistryObject<SoundEvent> spell_grab_2 = REGISTRY.register("spell_grab_2", () -> createSound("spell_grab_2"));
    public static RegistryObject<SoundEvent> spell_buff_attack_2 = REGISTRY.register("spell_buff_attack_2", () -> createSound("spell_buff_attack_2"));
    public static RegistryObject<SoundEvent> spell_buff_attack_3 = REGISTRY.register("spell_buff_attack_3", () -> createSound("spell_buff_attack_3"));
    public static RegistryObject<SoundEvent> spell_buff_attack_4 = REGISTRY.register("spell_buff_attack_4", () -> createSound("spell_buff_attack_4"));
    public static RegistryObject<SoundEvent> spell_earth_1 = REGISTRY.register("spell_earth_1", () -> createSound("spell_earth_1"));
    public static RegistryObject<SoundEvent> spell_earth_6 = REGISTRY.register("spell_earth_6", () -> createSound("spell_earth_6"));
    public static RegistryObject<SoundEvent> spell_earth_7 = REGISTRY.register("spell_earth_7", () -> createSound("spell_earth_7"));
    public static RegistryObject<SoundEvent> spell_earth_8 = REGISTRY.register("spell_earth_8", () -> createSound("spell_earth_8"));
    public static RegistryObject<SoundEvent> spell_magic_explosion = REGISTRY.register("spell_magic_explosion", () -> createSound("spell_magic_explosion"));
    public static RegistryObject<SoundEvent> spell_water_1 = REGISTRY.register("spell_water_1", () -> createSound("spell_water_1"));
    public static RegistryObject<SoundEvent> spell_water_2 = REGISTRY.register("spell_water_2", () -> createSound("spell_water_2"));
    public static RegistryObject<SoundEvent> spell_water_4 = REGISTRY.register("spell_water_4", () -> createSound("spell_water_4"));
    public static RegistryObject<SoundEvent> spell_water_5 = REGISTRY.register("spell_water_5", () -> createSound("spell_water_5"));
    public static RegistryObject<SoundEvent> spell_water_6 = REGISTRY.register("spell_water_6", () -> createSound("spell_water_6"));
    public static RegistryObject<SoundEvent> spell_water_7 = REGISTRY.register("spell_water_7", () -> createSound("spell_water_7"));
    public static RegistryObject<SoundEvent> spell_water_8 = REGISTRY.register("spell_water_8", () -> createSound("spell_water_8"));
    public static RegistryObject<SoundEvent> spell_water_9 = REGISTRY.register("spell_water_9", () -> createSound("spell_water_9"));
    public static RegistryObject<SoundEvent> spell_dark_1 = REGISTRY.register("spell_dark_1", () -> createSound("spell_dark_1"));
    public static RegistryObject<SoundEvent> spell_dark_3 = REGISTRY.register("spell_dark_3", () -> createSound("spell_dark_3"));
    public static RegistryObject<SoundEvent> spell_dark_4 = REGISTRY.register("spell_dark_4", () -> createSound("spell_dark_4"));
    public static RegistryObject<SoundEvent> spell_dark_5 = REGISTRY.register("spell_dark_5", () -> createSound("spell_dark_5"));
    public static RegistryObject<SoundEvent> spell_dark_7 = REGISTRY.register("spell_dark_7", () -> createSound("spell_dark_7"));
    public static RegistryObject<SoundEvent> spell_dark_8 = REGISTRY.register("spell_dark_8", () -> createSound("spell_dark_8"));
    public static RegistryObject<SoundEvent> spell_dark_9 = REGISTRY.register("spell_dark_9", () -> createSound("spell_dark_9"));
    public static RegistryObject<SoundEvent> spell_dark_11 = REGISTRY.register("spell_dark_11", () -> createSound("spell_dark_11"));
    public static RegistryObject<SoundEvent> spell_dark_13 = REGISTRY.register("spell_dark_13", () -> createSound("spell_dark_13"));
    public static RegistryObject<SoundEvent> spell_dark_14 = REGISTRY.register("spell_dark_14", () -> createSound("spell_dark_14"));
    public static RegistryObject<SoundEvent> spell_dark_15 = REGISTRY.register("spell_dark_15", () -> createSound("spell_dark_15"));
    public static RegistryObject<SoundEvent> spell_dark_16 = REGISTRY.register("spell_dark_16", () -> createSound("spell_dark_16"));
    public static RegistryObject<SoundEvent> spell_debuff_1 = REGISTRY.register("spell_debuff_1", () -> createSound("spell_debuff_1"));
    public static RegistryObject<SoundEvent> spell_debuff_2 = REGISTRY.register("spell_debuff_2", () -> createSound("spell_debuff_2"));
    public static RegistryObject<SoundEvent> spell_debuff_4 = REGISTRY.register("spell_debuff_4", () -> createSound("spell_debuff_4"));
    public static RegistryObject<SoundEvent> spell_scream_1 = REGISTRY.register("spell_scream_1", () -> createSound("spell_scream_1"));


    public static SoundEvent createSound(String name) {
        ResourceLocation r_name = new ResourceLocation(MKUltra.MODID, name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(r_name);
        return event;
    }

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
