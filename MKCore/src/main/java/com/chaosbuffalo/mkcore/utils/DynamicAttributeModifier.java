package com.chaosbuffalo.mkcore.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class DynamicAttributeModifier extends AttributeModifier {

    private final DoubleSupplier amountSupplier;

    public DynamicAttributeModifier(String pName, DoubleSupplier pAmount, Operation pOperation) {
        super(pName, 0, pOperation);
        this.amountSupplier = pAmount;
    }

    public DynamicAttributeModifier(UUID pId, String pName, DoubleSupplier pAmount, Operation pOperation) {
        this(pId, () -> pName, pAmount, pOperation);
    }

    public DynamicAttributeModifier(UUID pId, Supplier<String> pNameGetter, DoubleSupplier pAmount, Operation pOperation) {
        super(pId, pNameGetter, 0, pOperation);
        this.amountSupplier = pAmount;
    }

    @Override
    public double getAmount() {
        return amountSupplier.getAsDouble();
    }

    @Override
    public CompoundTag save() {
        throw new IllegalCallerException("DynamicAttributeModifier can never be saved");
    }
}
