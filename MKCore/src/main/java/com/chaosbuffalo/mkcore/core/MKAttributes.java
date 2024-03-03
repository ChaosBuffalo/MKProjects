package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.attributes.AttributeSyncType;
import com.chaosbuffalo.mkcore.attributes.MKRangedAttribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

public class MKAttributes {

    // Players Only Attributes
    public static final Attribute MAX_MANA = new MKRangedAttribute("attribute.name.mk.max_mana", 0, 0, 1024)
            .setName(MKCore.makeRL("max_mana"))
            .setSyncType(AttributeSyncType.Public);

    public static final Attribute MANA_REGEN = new MKRangedAttribute("attribute.name.mk.mana_regen", 0, 0, 1024)
            .setName(MKCore.makeRL("mana_regen"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute MELEE_CRIT = new MKRangedAttribute("attribute.name.mk.melee_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setName(MKCore.makeRL("melee_crit_chance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute MELEE_CRIT_MULTIPLIER = new MKRangedAttribute("attribute.name.mk.melee_crit_multiplier", 1.0, 0.0, 10.0)
            .setName(MKCore.makeRL("melee_crit_multiplier"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute SPELL_CRIT = new MKRangedAttribute("attribute.name.mk.spell_crit_chance", 0.1, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setName(MKCore.makeRL("spell_crit_chance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute SPELL_CRIT_MULTIPLIER = new MKRangedAttribute("attribute.name.mk.spell_crit_multiplier", 1.5, 0.0, 10.0)
            .setName(MKCore.makeRL("spell_crit_multiplier"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute RANGED_CRIT = new MKRangedAttribute("attribute.name.mk.ranged_crit_chance", 0.00, 0.0, 1.0)
            .setAdditionIsPercentage(true)
            .setName(MKCore.makeRL("ranged_crit_chance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute RANGED_CRIT_MULTIPLIER = new MKRangedAttribute("attribute.name.mk.ranged_crit_multiplier", 1.0, 0.0, 10.0)
            .setName(MKCore.makeRL("ranged_crit_multiplier"))
            .setSyncType(AttributeSyncType.Private);


    // Everyone Attributes
    public static final Attribute RANGED_DAMAGE = new MKRangedAttribute("attribute.name.mk.ranged_damage", 0.0, 0.0, 2048)
            .setName(MKCore.makeRL("ranged_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute RANGED_RESISTANCE = new MKRangedAttribute("attribute.name.mk.ranged_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("ranged_resistance"))
            .setSyncType(AttributeSyncType.Private);

    // This is slightly confusing.
    // 1.9 max means the cooldown will progress at most 10x faster than the normal rate. This translates into a 90% reduction in observed cooldown.
    // -3.0 minimum means that a cooldown can be increased up to 5x of the normal value. This translates into a 500% increase in the observed cooldown
    public static final Attribute COOLDOWN = new MKRangedAttribute("attribute.name.mk.cooldown_rate", 1.0, -3.0, 1.9)
            .setName(MKCore.makeRL("cooldown_rate"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute HEAL_BONUS = new MKRangedAttribute("attribute.name.mk.heal_bonus", 0.0, 0.0, 2048)
            .setName(MKCore.makeRL("heal_bonus"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute CASTING_SPEED = new MKRangedAttribute("attribute.name.mk.casting_speed", 1.0, -3.0, 1.9)
            .setName(MKCore.makeRL("casting_speed"))
            .setAdditionIsPercentage(true)
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute BUFF_DURATION = new MKRangedAttribute("attribute.name.mk.buff_duration", 1.0, 0.0, 5.0)
            .setName(MKCore.makeRL("buff_duration"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ARCANE_RESISTANCE = new MKRangedAttribute("attribute.name.mk.arcane_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("arcane_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ARCANE_DAMAGE = new MKRangedAttribute("attribute.name.mk.arcane_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("arcane_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute FIRE_RESISTANCE = new MKRangedAttribute("attribute.name.mk.fire_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("fire_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute FIRE_DAMAGE = new MKRangedAttribute("attribute.name.mk.fire_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("fire_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute FROST_RESISTANCE = new MKRangedAttribute("attribute.name.mk.frost_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("frost_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute FROST_DAMAGE = new MKRangedAttribute("attribute.name.mk.frost_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("frost_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute SHADOW_RESISTANCE = new MKRangedAttribute("attribute.name.mk.shadow_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("shadow_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute SHADOW_DAMAGE = new MKRangedAttribute("attribute.name.mk.shadow_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("shadow_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute HOLY_RESISTANCE = new MKRangedAttribute("attribute.name.mk.holy_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("holy_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute HOLY_DAMAGE = new MKRangedAttribute("attribute.name.mk.holy_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("holy_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute NATURE_RESISTANCE = new MKRangedAttribute("attribute.name.mk.nature_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("nature_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute NATURE_DAMAGE = new MKRangedAttribute("attribute.name.mk.nature_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("nature_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute POISON_RESISTANCE = new MKRangedAttribute("attribute.name.mk.poison_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("poison_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute POISON_DAMAGE = new MKRangedAttribute("attribute.name.mk.poison_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("poison_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute BLEED_RESISTANCE = new MKRangedAttribute("attribute.name.mk.bleed_resistance", 0, -1.0, 1.0)
            .setName(MKCore.makeRL("bleed_resistance"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute BLEED_DAMAGE = new MKRangedAttribute("attribute.name.mk.bleed_damage", 0, 0, 2048)
            .setName(MKCore.makeRL("bleed_damage"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ABJURATION = new MKRangedAttribute("attribute.name.mk.abjuration", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("abjuration"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ALTERATON = new MKRangedAttribute("attribute.name.mk.alteration", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("alteration"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute CONJURATION = new MKRangedAttribute("attribute.name.mk.conjuration", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("conjuration"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute DIVINATION = new MKRangedAttribute("attribute.name.mk.divination", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("divination"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ENCHANTMENT = new MKRangedAttribute("attribute.name.mk.enchantment", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("enchantment"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute PHANTASM = new MKRangedAttribute("attribute.name.mk.phantasm", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("phantasm"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute NECROMANCY = new MKRangedAttribute("attribute.name.mk.necromancy", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("necromancy"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute RESTORATION = new MKRangedAttribute("attribute.name.mk.restoration", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("restoration"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ARETE = new MKRangedAttribute("attribute.name.mk.arete", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("arete"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute PNEUMA = new MKRangedAttribute("attribute.name.mk.pneuma", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("pneuma"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute PANKRATION = new MKRangedAttribute("attribute.name.mk.pankration", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("pankration"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute EVOCATION = new MKRangedAttribute("attribute.name.mk.evocation", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("evocation"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute MARKSMANSHIP = new MKRangedAttribute("attribute.name.mk.marksmanship", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("marksmanship"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute TWO_HAND_SLASH = new MKRangedAttribute("attribute.name.mk.two_hand_slash", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("two_hand_slash"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ONE_HAND_SLASH = new MKRangedAttribute("attribute.name.mk.one_hand_slash", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("one_hand_slash"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute TWO_HAND_BLUNT = new MKRangedAttribute("attribute.name.mk.two_hand_blunt", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("two_hand_blunt"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ONE_HAND_BLUNT = new MKRangedAttribute("attribute.name.mk.one_hand_blunt", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("one_hand_blunt"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute TWO_HAND_PIERCE = new MKRangedAttribute("attribute.name.mk.two_hand_pierce", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("two_hand_pierce"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute ONE_HAND_PIERCE = new MKRangedAttribute("attribute.name.mk.one_hand_pierce", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("one_hand_pierce"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute HAND_TO_HAND = new MKRangedAttribute("attribute.name.mk.hand_to_hand", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("hand_to_hand"))
            .setSyncable(true);

    public static final Attribute BLOCK = new MKRangedAttribute("attribute.name.mk.block", 0, 0, GameConstants.SKILL_MAX)
            .setName(MKCore.makeRL("block"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute MAX_POISE = new MKRangedAttribute("attribute.name.mk.max_poise", 0, 0, 2048)
            .setName(MKCore.makeRL("max_poise"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute POISE_REGEN = new MKRangedAttribute("attribute.name.mk.poise_regen", 2, 0, 2048)
            .setName(MKCore.makeRL("poise_regen"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute POISE_BREAK_CD = new MKRangedAttribute("attribute.name.mk.poise_break_cd", 20.0, 0, 2048)
            .setName(MKCore.makeRL("poise_break_cd"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute BLOCK_EFFICIENCY = new MKRangedAttribute("attribute.name.mk.block_efficiency", 0, 0, 1.0)
            .setName(MKCore.makeRL("block_efficiency"))
            .setSyncType(AttributeSyncType.Private);

    public static final Attribute HEAL_EFFICIENCY = new MKRangedAttribute("attribute.name.mk.heal_efficiency", 1.0, 0, 1000.0)
            .setName(MKCore.makeRL("heal_efficiency"))
            .setSyncType(AttributeSyncType.Private);

    public static double getValueSafe(Attribute attr, LivingEntity target) {
        AttributeInstance instance = target.getAttribute(attr);
        if (instance != null) {
            return instance.getValue();
        } else {
            return attr.getDefaultValue();
        }
    }

    public static void iterateEntityAttributes(Consumer<Attribute> consumer) {
        consumer.accept(COOLDOWN);
        consumer.accept(CASTING_SPEED);
        consumer.accept(HEAL_BONUS);
        consumer.accept(BUFF_DURATION);
        consumer.accept(ARCANE_DAMAGE);
        consumer.accept(ARCANE_RESISTANCE);
        consumer.accept(FIRE_DAMAGE);
        consumer.accept(FIRE_RESISTANCE);
        consumer.accept(FROST_DAMAGE);
        consumer.accept(FROST_RESISTANCE);
        consumer.accept(SHADOW_DAMAGE);
        consumer.accept(SHADOW_RESISTANCE);
        consumer.accept(HOLY_DAMAGE);
        consumer.accept(HOLY_RESISTANCE);
        consumer.accept(NATURE_DAMAGE);
        consumer.accept(NATURE_RESISTANCE);
        consumer.accept(POISON_DAMAGE);
        consumer.accept(POISON_RESISTANCE);
        consumer.accept(BLEED_DAMAGE);
        consumer.accept(BLEED_RESISTANCE);
        consumer.accept(RANGED_DAMAGE);
        consumer.accept(RANGED_RESISTANCE);
        consumer.accept(ABJURATION);
        consumer.accept(ALTERATON);
        consumer.accept(CONJURATION);
        consumer.accept(DIVINATION);
        consumer.accept(ENCHANTMENT);
        consumer.accept(PHANTASM);
        consumer.accept(NECROMANCY);
        consumer.accept(RESTORATION);
        consumer.accept(ARETE);
        consumer.accept(PNEUMA);
        consumer.accept(PANKRATION);
        consumer.accept(EVOCATION);
        consumer.accept(MARKSMANSHIP);
        consumer.accept(TWO_HAND_BLUNT);
        consumer.accept(ONE_HAND_BLUNT);
        consumer.accept(TWO_HAND_PIERCE);
        consumer.accept(ONE_HAND_PIERCE);
        consumer.accept(TWO_HAND_SLASH);
        consumer.accept(ONE_HAND_SLASH);
        consumer.accept(BLOCK);
        consumer.accept(MAX_POISE);
        consumer.accept(POISE_REGEN);
        consumer.accept(POISE_BREAK_CD);
        consumer.accept(BLOCK_EFFICIENCY);
        consumer.accept(HEAL_EFFICIENCY);
        consumer.accept(MAX_MANA);
        consumer.accept(MANA_REGEN);
        consumer.accept(HAND_TO_HAND);
    }

    public static void iteratePlayerAttributes(Consumer<Attribute> consumer) {
        consumer.accept(MELEE_CRIT);
        consumer.accept(MELEE_CRIT_MULTIPLIER);
        consumer.accept(SPELL_CRIT);
        consumer.accept(SPELL_CRIT_MULTIPLIER);
        consumer.accept(RANGED_CRIT);
        consumer.accept(RANGED_CRIT_MULTIPLIER);
    }

    @Mod.EventBusSubscriber(modid = MKCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {

        @SubscribeEvent
        public static void registerAttributes(RegisterEvent event) {
            if (event.getRegistryKey() != ForgeRegistries.ATTRIBUTES.getRegistryKey()) {
                return;
            }
            iterateEntityAttributes(attr -> {
                if (attr instanceof MKRangedAttribute mkAttr) {
                    event.register(ForgeRegistries.ATTRIBUTES.getRegistryKey(), mkAttr.getName(), () -> attr);
                }
            });
            iteratePlayerAttributes(attr -> {
                if (attr instanceof MKRangedAttribute mkAttr) {
                    event.register(ForgeRegistries.ATTRIBUTES.getRegistryKey(), mkAttr.getName(), () -> attr);
                }
            });
        }
    }
}
