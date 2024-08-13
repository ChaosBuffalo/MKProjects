package com.chaosbuffalo.mkultra.entities.humans;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class HumanGhostEntity extends HumanEntity{


    public HumanGhostEntity(EntityType<? extends HumanGhostEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return HumanEntity.registerAttributes(attackDamage, movementSpeed)
                .add(MKAttributes.SHADOW_RESISTANCE, 0.25)
                .add(MKAttributes.HOLY_RESISTANCE, -0.25);
    }


    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }
}
