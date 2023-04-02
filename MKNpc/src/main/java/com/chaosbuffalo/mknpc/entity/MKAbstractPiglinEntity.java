package com.chaosbuffalo.mknpc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.Level;

public abstract class MKAbstractPiglinEntity extends MKEntity implements IPiglinActionProvider{

    protected MKAbstractPiglinEntity(EntityType<? extends MKAbstractPiglinEntity> type, Level worldIn) {
        super(type, worldIn);
        setupBreakDoors();
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    private void setupBreakDoors() {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        }
    }

    @Override
    public double getMyRidingOffset() {
        return this.isBaby() ? -0.05D : -0.45D;
    }

    @Override
    public abstract PiglinArmPose getPiglinAction();

    protected boolean isHoldingMeleeWeapon() {
        return getMainHandItem().getItem() instanceof TieredItem;
    }
}
