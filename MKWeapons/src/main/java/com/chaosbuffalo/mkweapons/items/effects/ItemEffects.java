package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkweapons.items.effects.accesory.AccessoryModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.OnMeleeProcEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.RestoreManaOnCastEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.ArmorModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.*;
import com.chaosbuffalo.mkweapons.items.effects.ranged.*;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ItemEffects {

    public static final Map<ResourceLocation, Codec<? extends IMeleeWeaponEffect>> MELEE_EFFECT_CODECS =
            new HashMap<>();
    public static final Codec<IMeleeWeaponEffect> MELEE_EFFECT_CODEC =
            CommonCodecs.createMapBackedDispatch(ResourceLocation.CODEC, MELEE_EFFECT_CODECS, IItemEffect::getTypeName);
    public static final Map<ResourceLocation, Codec<? extends IRangedWeaponEffect>> RANGED_EFFECT_CODECS =
            new HashMap<>();
    public static final Codec<IRangedWeaponEffect> RANGED_EFFECT_CODEC =
            CommonCodecs.createMapBackedDispatch(ResourceLocation.CODEC, RANGED_EFFECT_CODECS, IItemEffect::getTypeName);
    public static final Map<ResourceLocation, Codec<? extends IAccessoryEffect>> ACCESSORY_EFFECT_CODECS =
            new HashMap<>();
    public static final Codec<IAccessoryEffect> ACCESSORY_EFFECT_CODEC =
            CommonCodecs.createMapBackedDispatch(ResourceLocation.CODEC, ACCESSORY_EFFECT_CODECS, IItemEffect::getTypeName);
    public static final Map<ResourceLocation, Codec<? extends IArmorEffect>> ARMOR_EFFECT_CODECS =
            new HashMap<>();
    public static final Codec<IArmorEffect> ARMOR_EFFECT_CODEC =
            CommonCodecs.createMapBackedDispatch(ResourceLocation.CODEC, ARMOR_EFFECT_CODECS, IItemEffect::getTypeName);

    public static void meleeEffect(ResourceLocation type,
                                   Codec<? extends IMeleeWeaponEffect> codec) {
        MELEE_EFFECT_CODECS.put(type, codec);
    }

    public static void rangedEffect(ResourceLocation type,
                                    Codec<? extends IRangedWeaponEffect> codec) {
        RANGED_EFFECT_CODECS.put(type, codec);
    }

    public static void armorEffect(ResourceLocation type,
                                   Codec<? extends IArmorEffect> codec) {
        ARMOR_EFFECT_CODECS.put(type, codec);
    }

    public static void accessoryEffect(ResourceLocation type,
                                       Codec<? extends IAccessoryEffect> codec) {
        ACCESSORY_EFFECT_CODECS.put(type, codec);
    }

    static {
        // melee
        meleeEffect(MeleeModifierEffect.NAME, MeleeModifierEffect.CODEC);
        meleeEffect(BleedMeleeWeaponEffect.NAME, BleedMeleeWeaponEffect.CODEC);
        meleeEffect(ComboStrikeMeleeWeaponEffect.NAME, ComboStrikeMeleeWeaponEffect.CODEC);
        meleeEffect(DoubleStrikeMeleeWeaponEffect.NAME, DoubleStrikeMeleeWeaponEffect.CODEC);
        meleeEffect(FuryStrikeMeleeWeaponEffect.NAME, FuryStrikeMeleeWeaponEffect.CODEC);
        meleeEffect(StunMeleeWeaponEffect.NAME, StunMeleeWeaponEffect.CODEC);
        meleeEffect(UndeadDamageMeleeWeaponEffect.NAME, UndeadDamageMeleeWeaponEffect.CODEC);
        meleeEffect(MeleeSkillScalingEffect.NAME, MeleeSkillScalingEffect.CODEC);
        meleeEffect(OnHitAbilityEffect.NAME, OnHitAbilityEffect.CODEC);
        meleeEffect(LivingDamageMeleeWeaponEffect.NAME, LivingDamageMeleeWeaponEffect.CODEC);
        meleeEffect(ManaDrainWeaponEffect.NAME, ManaDrainWeaponEffect.CODEC);

        // ranged
        rangedEffect(RangedModifierEffect.NAME, RangedModifierEffect.CODEC);
        rangedEffect(RangedSkillScalingEffect.NAME, RangedSkillScalingEffect.CODEC);
        rangedEffect(RangedManaDrainEffect.NAME, RangedManaDrainEffect.CODEC);
        rangedEffect(RapidFireRangedWeaponEffect.NAME, RapidFireRangedWeaponEffect.CODEC);

        // armor
        armorEffect(ArmorModifierEffect.NAME, ArmorModifierEffect.CODEC);

        // acc
        accessoryEffect(AccessoryModifierEffect.NAME, AccessoryModifierEffect.CODEC);
        accessoryEffect(RestoreManaOnCastEffect.NAME, RestoreManaOnCastEffect.CODEC);
        accessoryEffect(OnMeleeProcEffect.NAME, OnMeleeProcEffect.CODEC);
    }
}
