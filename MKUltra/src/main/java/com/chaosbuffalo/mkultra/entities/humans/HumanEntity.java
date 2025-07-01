package com.chaosbuffalo.mkultra.entities.humans;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class HumanEntity extends MKEntity {

    public HumanEntity(EntityType<? extends HumanEntity> type, Level worldIn) {
        super(type, worldIn);
        if (!worldIn.isClientSide()) {
            setAttackComboStatsAndDefault(6, GameConstants.TICKS_PER_SECOND);
        }
        setLungeSpeed(0.75);
    }


    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return MKEntity.registerAttributes(attackDamage, movementSpeed)
                .add(Attributes.MAX_HEALTH, 100.0);
    }
}
