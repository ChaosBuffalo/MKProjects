package com.chaosbuffalo.mkweapons.items.weapon.types;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemEffects;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeleeWeaponType implements IMeleeWeaponType {
    private float damageMultiplier;
    private float attackSpeed;
    private float critMultiplier;
    private float critChance;
    private float reach;
    private float blockEfficiency;
    private float maxPoise;
    private final ResourceLocation name;
    private final List<IMeleeWeaponEffect> effects;
    private boolean isTwoHanded;

    public MeleeWeaponType(ResourceLocation name, float damageMultiplier, float attackSpeed,
                           float critMultiplier, float critChance, float reach, boolean isTwoHanded,
                           float blockEfficiency, float maxPoise, IMeleeWeaponEffect... effects){
        this.damageMultiplier = damageMultiplier;
        this.name = name;
        this.attackSpeed = attackSpeed;
        this.critMultiplier = critMultiplier;
        this.critChance = critChance;
        this.reach = reach;
        this.maxPoise = maxPoise;
        this.blockEfficiency = blockEfficiency;
        this.effects = new ArrayList<>();
        this.effects.addAll(Arrays.asList(effects));
        this.isTwoHanded = isTwoHanded;
    }

    @Override
    public boolean isTwoHanded() {
        return isTwoHanded;
    }

    public String getTranslationName(){
        return String.format("mkweapon.melee.type.%s.%s", getName().getNamespace(), getName().getPath());
    }

    @Override
    public List<IMeleeWeaponEffect> getWeaponEffects() {
        return effects;
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put( ops.createString("damageMultiplier"), ops.createFloat(getDamageMultiplier()));
        builder.put(ops.createString("attackSpeed"), ops.createFloat(getAttackSpeed()));
        builder.put(ops.createString("reach"), ops.createFloat(getReach()));
        builder.put(ops.createString("critMultiplier"), ops.createFloat(getCritMultiplier()));
        builder.put(ops.createString("critChance"), ops.createFloat(getCritChance()));
        builder.put(ops.createString("isTwoHanded"), ops.createBoolean(isTwoHanded()));
        builder.put(ops.createString("effects"), ops.createList(getWeaponEffects().stream().map(effect -> effect.serialize(ops))));
        builder.put(ops.createString("blockEfficiency"), ops.createFloat(getBlockEfficiency()));
        builder.put(ops.createString("maxPoise"), ops.createFloat(getMaxPoise()));

        return ops.createMap(builder.build());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        damageMultiplier = dynamic.get("damageMultiplier").asFloat(1.0f);
        attackSpeed = dynamic.get("attackSpeed").asFloat(-2.4f);
        reach = dynamic.get("reach").asFloat(0f);
        critChance = dynamic.get("critChance").asFloat(0.05f);
        critMultiplier = dynamic.get("critMultiplier").asFloat(1.5f);
        isTwoHanded = dynamic.get("isTwoHanded").asBoolean(false);
        blockEfficiency = dynamic.get("blockEfficiency").asFloat(0.75f);
        maxPoise = dynamic.get("maxPoise").asFloat(20.0f);
        List<IMeleeWeaponEffect> deserializedEffects = dynamic.get("effects").asList(d -> {
            IItemEffect effect = ItemEffects.deserializeEffect(d);
            if (effect instanceof IMeleeWeaponEffect){
                return (IMeleeWeaponEffect) effect;
            } else {
                MKWeapons.LOGGER.error("Failed to deserialize {} not a melee effect type for {}",
                        effect.getTypeName(), getName());
                return null;
            }
        });
        effects.clear();
        for (IMeleeWeaponEffect effect : deserializedEffects){
            if (effect != null){
                effects.add(effect);
            }
        }
    }

    @Override
    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    @Override
    public float getAttackSpeed() {
        return attackSpeed;
    }

    @Override
    public float getCritMultiplier() {
        return critMultiplier;
    }

    @Override
    public float getCritChance() {
        return critChance;
    }

    @Override
    public float getReach() {
        return reach;
    }

    public float getBlockEfficiency() {
        return blockEfficiency;
    }

    public float getMaxPoise() {
        return maxPoise;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }
}
