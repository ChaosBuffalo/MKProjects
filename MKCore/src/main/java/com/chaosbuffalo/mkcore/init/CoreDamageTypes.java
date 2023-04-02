package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.damage.MeleeDamageType;
import com.chaosbuffalo.mkcore.core.damage.RangedDamageType;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CoreDamageTypes {

    public static final MKDamageType FireDamage = new MKDamageType(MKCore.makeRL("damage.fire"),
            MKAttributes.FIRE_DAMAGE, MKAttributes.FIRE_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.RED);

    public static final MKDamageType FrostDamage = new MKDamageType(MKCore.makeRL("damage.frost"),
            MKAttributes.FROST_DAMAGE, MKAttributes.FROST_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.AQUA);

    public static final MKDamageType HolyDamage = new MKDamageType(MKCore.makeRL("damage.holy"),
            MKAttributes.HOLY_DAMAGE, MKAttributes.HOLY_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.GOLD).setCritMultiplier(2.0f);

    public static final MKDamageType PoisonDamage = new MKDamageType(MKCore.makeRL("damage.poison"),
            MKAttributes.POISON_DAMAGE, MKAttributes.POISON_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.GREEN);

    public static final MKDamageType ShadowDamage = new MKDamageType(MKCore.makeRL("damage.shadow"),
            MKAttributes.SHADOW_DAMAGE, MKAttributes.SHADOW_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.DARK_PURPLE);

    public static final MKDamageType ArcaneDamage = new MKDamageType(MKCore.makeRL("damage.arcane"),
            MKAttributes.ARCANE_DAMAGE, MKAttributes.ARCANE_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.LIGHT_PURPLE);

    public static final MKDamageType NatureDamage = new MKDamageType(MKCore.makeRL("damage.nature"),
            MKAttributes.NATURE_DAMAGE, MKAttributes.NATURE_RESISTANCE,
            MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
            ChatFormatting.DARK_GREEN);

    public static final MKDamageType MeleeDamage = new MeleeDamageType(MKCore.makeRL("damage.melee"));

    public static final RangedDamageType RangedDamage = new RangedDamageType(MKCore.makeRL("damage.ranged"));

    public static final MKDamageType BleedDamage = new MKDamageType(MKCore.makeRL("damage.bleed"),
            MKAttributes.BLEED_DAMAGE, MKAttributes.BLEED_RESISTANCE,
            MKAttributes.MELEE_CRIT, MKAttributes.MELEE_CRIT_MULTIPLIER,
            ChatFormatting.DARK_RED);

    @SubscribeEvent
    public static void registerDamageTypes(RegistryEvent.Register<MKDamageType> evt) {
        evt.getRegistry().register(FireDamage);
        evt.getRegistry().register(FrostDamage);
        evt.getRegistry().register(HolyDamage);
        evt.getRegistry().register(PoisonDamage);
        evt.getRegistry().register(ShadowDamage);
        evt.getRegistry().register(ArcaneDamage);
        evt.getRegistry().register(NatureDamage);
        evt.getRegistry().register(MeleeDamage);
        evt.getRegistry().register(RangedDamage);
        evt.getRegistry().register(BleedDamage);
    }
}
