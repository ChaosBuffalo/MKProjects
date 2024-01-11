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
                           float blockEfficiency, float maxPoise, List<IMeleeWeaponEffect> effects) {
        this.damageMultiplier = damageMultiplier;
        this.name = name;
        this.attackSpeed = attackSpeed;
        this.critMultiplier = critMultiplier;
        this.critChance = critChance;
        this.reach = reach;
        this.maxPoise = maxPoise;
        this.blockEfficiency = blockEfficiency;
        this.effects = new ArrayList<>(effects);
        this.isTwoHanded = isTwoHanded;
    }

    @Override
    public boolean isTwoHanded() {
        return isTwoHanded;
    }

    @Override
    public List<IMeleeWeaponEffect> getWeaponEffects() {
        return effects;
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("damageMultiplier"), ops.createFloat(getDamageMultiplier()));
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
            if (effect instanceof IMeleeWeaponEffect meleeWeaponEffect) {
                return meleeWeaponEffect;
            } else {
                MKWeapons.LOGGER.error("Failed to deserialize melee weapon effect from '{}'", d);
                return null;
            }
        });
        effects.clear();
        for (IMeleeWeaponEffect effect : deserializedEffects) {
            if (effect != null) {
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
    public boolean canBlock() {
        return true;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static class Builder {

        private final ResourceLocation name;
        private float damageMultiplier = 1.0f;
        private float attackSpeed = -2.4f;
        private float critMultiplier = 1.5f;
        private float critChance = 0.05f;
        private float reachModifier = 0.0f;
        private float blockEfficiency = 0.75f;
        private float maxPoise = 20f;
        private final List<IMeleeWeaponEffect> effects = new ArrayList<>();
        private boolean isTwoHanded = false;

        public Builder(ResourceLocation typeId) {
            this.name = typeId;
        }

        public Builder damageMultiplier(float m) {
            damageMultiplier = m;
            return this;
        }

        public Builder attackSpeed(float s) {
            attackSpeed = s;
            return this;
        }

        public Builder critical(float mult, float chance) {
            critChance = chance;
            critMultiplier = mult;
            return this;
        }

        public Builder reach(float r) {
            reachModifier = r;
            return this;
        }

        public Builder isTwoHanded() {
            isTwoHanded = true;
            return this;
        }

        public Builder blocking(float b, float m) {
            blockEfficiency = b;
            maxPoise = m;
            return this;
        }

        public Builder effect(IMeleeWeaponEffect effect) {
            effects.add(effect);
            return this;
        }

        public MeleeWeaponType build() {
            return new MeleeWeaponType(name, damageMultiplier, attackSpeed, critMultiplier, critChance, reachModifier,
                    isTwoHanded, blockEfficiency, maxPoise, effects);
        }
    }
}
