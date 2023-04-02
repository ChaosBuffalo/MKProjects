package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CoreSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MKCore.MOD_ID);

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(MKCore.MOD_ID, name)));
    }

    public static final RegistryObject<SoundEvent> casting_default = register("casting_default");
    public static final RegistryObject<SoundEvent> spell_cast_default = register("spell_cast_default");
    public static final RegistryObject<SoundEvent> level_up = register("level_up");
    public static final RegistryObject<SoundEvent> block_break = register("block_break");
    public static final RegistryObject<SoundEvent> weapon_block = register("weapon_block");
    public static final RegistryObject<SoundEvent> arrow_block = register("arrow_block");
    public static final RegistryObject<SoundEvent> fist_block = register("fist_block");
    public static final RegistryObject<SoundEvent> parry = register("parry");
    public static final RegistryObject<SoundEvent> attack_cd_reset = register("attack_cd_reset");
    public static final RegistryObject<SoundEvent> stun_sound = register("stun");
    public static final RegistryObject<SoundEvent> quest_complete_sound = register("quest_complete");


    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
