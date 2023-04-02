package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkweapons.items.effects.accesory.AccessoryModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.ArmorModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.*;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedSkillScalingEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RapidFireRangedWeaponEffect;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemEffects {

    public static final Map<ResourceLocation, Supplier<IItemEffect>> ITEM_EFFECT_DESERIALIZERS =
            new HashMap<>();

    public static void addWeaponEffectDeserializer(ResourceLocation type,
                                                   Supplier<IItemEffect> deserializer){
        ITEM_EFFECT_DESERIALIZERS.put(type, deserializer);
    }

    static {
        addWeaponEffectDeserializer(BleedMeleeWeaponEffect.NAME, BleedMeleeWeaponEffect::new);
        addWeaponEffectDeserializer(ComboStrikeMeleeWeaponEffect.NAME, ComboStrikeMeleeWeaponEffect::new);
        addWeaponEffectDeserializer(DoubleStrikeMeleeWeaponEffect.NAME, DoubleStrikeMeleeWeaponEffect::new);
        addWeaponEffectDeserializer(FuryStrikeMeleeWeaponEffect.NAME, FuryStrikeMeleeWeaponEffect::new);
        addWeaponEffectDeserializer(StunMeleeWeaponEffect.NAME, StunMeleeWeaponEffect::new);
        addWeaponEffectDeserializer(UndeadDamageMeleeWeaponEffect.NAME, UndeadDamageMeleeWeaponEffect::new);
        addWeaponEffectDeserializer(ArmorModifierEffect.NAME, ArmorModifierEffect::new);
        addWeaponEffectDeserializer(MeleeModifierEffect.NAME, MeleeModifierEffect::new);
        addWeaponEffectDeserializer(RangedModifierEffect.NAME, RangedModifierEffect::new);
        addWeaponEffectDeserializer(RapidFireRangedWeaponEffect.NAME, RapidFireRangedWeaponEffect::new);
        addWeaponEffectDeserializer(AccessoryModifierEffect.NAME, AccessoryModifierEffect::new);
        addWeaponEffectDeserializer(SkillScalingEffect.NAME, SkillScalingEffect::new);
        addWeaponEffectDeserializer(RangedSkillScalingEffect.NAME, RangedSkillScalingEffect::new);
    }

    @Nullable
    public static <D> IItemEffect deserializeEffect(Dynamic<D> dynamic){
        ResourceLocation type = BaseItemEffect.getType(dynamic);
        Supplier<IItemEffect> deserializer = ITEM_EFFECT_DESERIALIZERS.get(type);
        if (deserializer == null){
            return null;
        }
        IItemEffect weaponEffect = deserializer.get();
        if (weaponEffect != null){
            weaponEffect.deserialize(dynamic);
        }
        return weaponEffect;
    }
}
