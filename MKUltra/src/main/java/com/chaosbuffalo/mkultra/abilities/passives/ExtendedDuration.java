//package com.chaosbuffalo.mkultra.abilities.passives;
//
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.core.PlayerAbility;
//import com.chaosbuffalo.mkultra.core.PlayerPassiveAbility;
//import com.chaosbuffalo.mkultra.effects.passives.PassiveAbilityPotionBase;
//import com.chaosbuffalo.mkultra.effects.spells.ExtendedDurationPotion;
//import net.minecraftforge.event.RegistryEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
//@Mod.EventBusSubscriber(modid = MKUltra.MODID)
//public class ExtendedDuration extends PlayerPassiveAbility {
//
//    public static final ExtendedDuration INSTANCE = new ExtendedDuration();
//
//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<PlayerAbility> event) {
//        event.getRegistry().register(INSTANCE.finish());
//    }
//
//    public ExtendedDuration() {
//        super(MKUltra.MODID, "ability.extended_duration");
//    }
//
//    @Override
//    public PassiveAbilityPotionBase getPassiveEffect() {
//        return ExtendedDurationPotion.INSTANCE;
//    }
//}
