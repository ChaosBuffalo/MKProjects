package com.chaosbuffalo.mkcore.client.rendering.animations;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.combat.IVisualMeleeAttackEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class AdditionalBipedAnimation<T extends LivingEntity> extends AdditionalAnimation<T> {

    public AdditionalBipedAnimation(HumanoidModel<?> model) {
        super(model);
    }

    public HumanoidModel<?> getModel() {
        return (HumanoidModel<?>) super.getModel();
    }

    protected boolean hasActiveVisualMeleeAttack(T entity, InteractionHand hand) {
        if (entity instanceof Player player) {
            return MKCore.getPlayer(player)
                    .map(playerData -> playerData.getCombatExtension().hasActiveVisualMeleeAttack(hand, 0.0f))
                    .orElse(false);
        }
        if (entity instanceof IVisualMeleeAttackEntity visualMeleeAttackEntity) {
            return visualMeleeAttackEntity.hasActiveVisualMeleeAttack(hand, 0.0f);
        }
        return false;
    }

    protected boolean hasActiveVisualMeleeAttackArm(T entity, HumanoidArm arm) {
        InteractionHand hand = arm == entity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return hasActiveVisualMeleeAttack(entity, hand);
    }

    protected boolean hasVisualMeleeAttackSequence(T entity, InteractionHand hand) {
        if (entity instanceof Player player) {
            return MKCore.getPlayer(player)
                    .map(playerData -> playerData.getCombatExtension().hasVisualMeleeAttackSequence(hand))
                    .orElse(false);
        }
        if (entity instanceof IVisualMeleeAttackEntity visualMeleeAttackEntity) {
            return visualMeleeAttackEntity.hasVisualMeleeAttackSequence(hand);
        }
        return false;
    }

    protected boolean hasVisualMeleeAttackSequenceArm(T entity, HumanoidArm arm) {
        InteractionHand hand = arm == entity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return hasVisualMeleeAttackSequence(entity, hand);
    }
}
