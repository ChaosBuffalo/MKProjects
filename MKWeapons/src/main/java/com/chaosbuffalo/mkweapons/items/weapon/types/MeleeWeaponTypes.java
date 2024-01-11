package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.melee.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MeleeWeaponTypes {

    public static final Map<ResourceLocation, IMeleeWeaponType> WEAPON_TYPES = new HashMap<>();

    public static void addWeaponType(IMeleeWeaponType weaponType) {
        WEAPON_TYPES.put(weaponType.getName(), weaponType);
    }

    @Nullable
    public static IMeleeWeaponType getWeaponType(ResourceLocation name) {
        return WEAPON_TYPES.get(name);
    }

    public static final MeleeWeaponType LONGSWORD_TYPE = MeleeWeaponType.builder(MKWeapons.id("longsword"))
            .damageMultiplier(1.5f)
            .attackSpeed(-2.4f)
            .critical(0.5f, 0.5f)
            .reach(0.0f)
            .blocking(0.75f, 25)
            .effect(new MeleeSkillScalingEffect(4.5, MKAttributes.ONE_HAND_SLASH))
            .effect(new FuryStrikeMeleeWeaponEffect(5, .25))
            .build();

    public static final MeleeWeaponType GREATSWORD_TYPE = MeleeWeaponType.builder(MKWeapons.id("greatsword"))
            .damageMultiplier(2.5f)
            .attackSpeed(-3)
            .critical(0.9f, .10f)
            .reach(1.0f)
            .isTwoHanded()
            .blocking(0.80f, 25)
            .effect(new MeleeSkillScalingEffect(7.5, MKAttributes.TWO_HAND_SLASH))
            .effect(new DoubleStrikeMeleeWeaponEffect(.2))
            .build();

    public static final MeleeWeaponType KATANA_TYPE = MeleeWeaponType.builder(MKWeapons.id("katana"))
            .damageMultiplier(1.5f)
            .attackSpeed(-2.2f)
            .critical(1.0f, 0.10f)
            .isTwoHanded()
            .blocking(0.75f, 25f)
            .effect(new MeleeSkillScalingEffect(4.5, MKAttributes.TWO_HAND_SLASH))
            .effect(new ComboStrikeMeleeWeaponEffect(5, .25))
            .build();

    public static final MeleeWeaponType DAGGER_TYPE = MeleeWeaponType.builder(MKWeapons.id("dagger"))
            .damageMultiplier(1.0f)
            .attackSpeed(-1.0f)
            .critical(1.5f, 0.10f)
            .reach(-1.0f)
            .blocking(0.50f, 20f)
            .effect(new MeleeSkillScalingEffect(3.0, MKAttributes.ONE_HAND_PIERCE))
            .effect(new BleedMeleeWeaponEffect(1.0f, 10, 4, MKAttributes.ONE_HAND_PIERCE))
            .effect(new ComboStrikeMeleeWeaponEffect(3, .50))
            .build();

    public static final MeleeWeaponType STAFF_TYPE = MeleeWeaponType.builder(MKWeapons.id("staff"))
            .damageMultiplier(1.75f)
            .attackSpeed(-2.5f)
            .critical(0.5f, 0.05f)
            .reach(1.0f)
            .isTwoHanded()
            .blocking(0.85f, 30f)
            .effect(new MeleeSkillScalingEffect(5.25, MKAttributes.TWO_HAND_BLUNT))
            .effect(new StunMeleeWeaponEffect(.20, 2))
            .effect(new ComboStrikeMeleeWeaponEffect(5, .15))
            .build();

    public static final MeleeWeaponType SPEAR_TYPE = MeleeWeaponType.builder(MKWeapons.id("spear"))
            .damageMultiplier(2.00f)
            .attackSpeed(-2.0f)
            .critical(0.75f, 0.05f)
            .reach(2.0f)
            .isTwoHanded()
            .blocking(0.75f, 30f)
            .effect(new MeleeSkillScalingEffect(6.0, MKAttributes.TWO_HAND_PIERCE))
            .effect(new BleedMeleeWeaponEffect(0.75f, 5, 5, MKAttributes.TWO_HAND_PIERCE))
            .effect(new FuryStrikeMeleeWeaponEffect(3, .4))
            .build();

    public static final MeleeWeaponType WARHAMMER_TYPE = MeleeWeaponType.builder(MKWeapons.id("warhammer"))
            .damageMultiplier(2.25f)
            .attackSpeed(-2.75f)
            .critical(0.25f, 0.05f)
            .reach(1.0f)
            .isTwoHanded()
            .blocking(0.80f, 25f)
            .effect(new MeleeSkillScalingEffect(6.75, MKAttributes.TWO_HAND_BLUNT))
            .effect(new UndeadDamageMeleeWeaponEffect(2.0f))
            .effect(new StunMeleeWeaponEffect(.1, 5))
            .build();

    public static final MeleeWeaponType BATTLEAXE_TYPE = MeleeWeaponType.builder(MKWeapons.id("battleaxe"))
            .damageMultiplier(2.25f)
            .attackSpeed(-3.2f)
            .critical(0.75f, 0.05f)
            .isTwoHanded()
            .blocking(0.80f, 25f)
            .effect(new MeleeSkillScalingEffect(6.75, MKAttributes.TWO_HAND_SLASH))
            .effect(new BleedMeleeWeaponEffect(1.0f, 2, 4, MKAttributes.TWO_HAND_SLASH))
            .build();

    public static final MeleeWeaponType MACE_TYPE = MeleeWeaponType.builder(MKWeapons.id("mace"))
            .damageMultiplier(1.75f)
            .attackSpeed(-2.1f)
            .critical(0.25f, 0.05f)
            .blocking(0.75f, 30f)
            .effect(new MeleeSkillScalingEffect(5.25, MKAttributes.ONE_HAND_BLUNT))
            .effect(new UndeadDamageMeleeWeaponEffect(1.5f))
            .effect(new DoubleStrikeMeleeWeaponEffect(.1))
            .build();


    public static void registerWeaponTypes() {
        addWeaponType(LONGSWORD_TYPE);
        addWeaponType(GREATSWORD_TYPE);
        addWeaponType(KATANA_TYPE);
        addWeaponType(DAGGER_TYPE);
        addWeaponType(STAFF_TYPE);
        addWeaponType(SPEAR_TYPE);
        addWeaponType(WARHAMMER_TYPE);
        addWeaponType(BATTLEAXE_TYPE);
        addWeaponType(MACE_TYPE);
    }
}
