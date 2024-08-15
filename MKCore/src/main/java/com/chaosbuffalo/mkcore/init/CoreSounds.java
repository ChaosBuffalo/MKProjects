package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, MKCore.MOD_ID);

    private static Holder<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, name)));
    }

    public static final Holder<SoundEvent> casting_default = register("casting_default");
    public static final Holder<SoundEvent> spell_cast_default = register("spell_cast_default");
    public static final Holder<SoundEvent> level_up = register("level_up");
    public static final Holder<SoundEvent> block_break = register("block_break");
    public static final Holder<SoundEvent> weapon_block = register("weapon_block");
    public static final Holder<SoundEvent> arrow_block = register("arrow_block");
    public static final Holder<SoundEvent> fist_block = register("fist_block");
    public static final Holder<SoundEvent> parry = register("parry");
    public static final Holder<SoundEvent> attack_cd_reset = register("attack_cd_reset");
    public static final Holder<SoundEvent> stun_sound = register("stun");
    public static final Holder<SoundEvent> quest_complete_sound = register("quest_complete");


    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
