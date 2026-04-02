package com.chaosbuffalo.mkweapons.items.weapon.types;

import net.minecraft.resources.ResourceLocation;

public class RangedWeaponType implements IRangedWeaponType {

    private final ResourceLocation name;
    private final float baseDrawTime;
    private final float baseLaunchVelocity;
    private final float damage;

    public RangedWeaponType(ResourceLocation name,  float baseDrawTime, float baseLaunchVelocity, float damage) {
        this.name = name;
        this.baseDrawTime = baseDrawTime;
        this.baseLaunchVelocity = baseLaunchVelocity;
        this.damage = damage;
    }

    @Override
    public ResourceLocation getName() {
        return name;
    }

    @Override
    public float getBaseDrawTime() {
        return baseDrawTime;
    }

    @Override
    public float getBaseLaunchVelocity() {
        return baseLaunchVelocity;
    }

    @Override
    public float getBaseDamage() {
        return damage;
    }

    public static Builder builder(ResourceLocation name) {
        return new Builder(name);
    }

    public static class Builder {
        private final ResourceLocation name;
        private float baseDrawTime;
        private float baseLaunchVel;
        private float damage;

        public Builder(ResourceLocation name) {
            this.name = name;
        }

        public Builder drawTime(float baseDrawTime) {
            this.baseDrawTime = baseDrawTime;
            return this;
        }

        public Builder launchVel(float baseLaunchVel) {
            this.baseLaunchVel = baseLaunchVel;
            return this;
        }

        public Builder baseDamage(float baseDamage) {
            this.damage = baseDamage;
            return this;
        }

        public RangedWeaponType build() {
            return new RangedWeaponType(name,  baseDrawTime, baseLaunchVel, damage);
        }
    }
}
