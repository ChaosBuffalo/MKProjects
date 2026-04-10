package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.entity.ai.controller.MovementStrategyController;
import com.chaosbuffalo.mknpc.entity.ai.goal.UseAbilityGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class MKFlyingEntity extends MKEntity{

    protected MKFlyingEntity(EntityType<? extends PathfinderMob> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        setCanFly(true);
        setNonCombatMoveType(NonCombatMoveType.RANDOM_WANDER);
        setCombatMoveType(CombatMoveType.RANGE);
    }

    @Override
    protected void enterWanderState() {
        MovementStrategyController.enterRandomFlyingWander(this);
    }

    @Override
    protected UseAbilityGoal createUseAbilityGoal() {
        return new UseAbilityGoal(this, true);
    }

    protected PathNavigation createNavigation(Level p_level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, p_level) {
            public boolean isStableDestination(BlockPos p_27947_) {
                return this.level.getBlockState(p_27947_).isAir();
            }
        };
        flyingpathnavigation.setCanOpenDoors(true);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return MKEntity.registerAttributes(attackDamage, movementSpeed)
                .add(Attributes.FLYING_SPEED, 0.6);
    }

    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }
}
