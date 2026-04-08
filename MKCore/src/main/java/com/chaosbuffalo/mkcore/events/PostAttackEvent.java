package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class PostAttackEvent extends EntityDataEvent {
    @Nullable
    private final Entity target;
    private final boolean secondaryAttack;
    private final InteractionHand hand;

    public PostAttackEvent(IMKEntityData entity) {
        this(entity, null, false, InteractionHand.MAIN_HAND);
    }

    public PostAttackEvent(IMKEntityData entity, @Nullable Entity target, boolean secondaryAttack) {
        this(entity, target, secondaryAttack, InteractionHand.MAIN_HAND);
    }

    public PostAttackEvent(IMKEntityData entity, @Nullable Entity target, boolean secondaryAttack, InteractionHand hand) {
        super(entity);
        this.target = target;
        this.secondaryAttack = secondaryAttack;
        this.hand = hand;
    }

    @Nullable
    public Entity getTarget() {
        return target;
    }

    public boolean isSecondaryAttack() {
        return secondaryAttack;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
