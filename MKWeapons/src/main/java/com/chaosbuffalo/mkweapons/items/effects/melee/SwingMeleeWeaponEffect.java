package com.chaosbuffalo.mkweapons.items.effects.melee;


import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

public abstract class SwingMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    private int numberOfHits;
    private double perHit;

    public SwingMeleeWeaponEffect(ResourceLocation name, ChatFormatting color, int numberOfHits, double perHit){
        this(name, color);
        this.numberOfHits = numberOfHits;
        this.perHit = perHit;
    }

    public SwingMeleeWeaponEffect(ResourceLocation name, ChatFormatting color){
        super(name, color);
    }

    public void setNumberOfHits(int numberOfHits) {
        this.numberOfHits = numberOfHits;
    }

    public void setPerHit(double perHit) {
        this.perHit = perHit;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        setNumberOfHits(dynamic.get("numberOfHits").asInt(5));
        setPerHit(dynamic.get("perHit").asDouble(.1));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("numberOfHits"), ops.createInt(getNumberOfHits()));
        builder.put(ops.createString("perHit"), ops.createDouble(getPerHit()));
    }

    public double getPerHit() {
        return perHit;
    }

    public int getNumberOfHits() {
        return numberOfHits;
    }
}
