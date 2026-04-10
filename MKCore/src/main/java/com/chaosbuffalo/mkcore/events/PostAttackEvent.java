package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class PostAttackEvent extends EntityDataEvent {
    @Nullable
    private final Entity target;
    private final boolean secondaryAttack;

    public PostAttackEvent(IMKEntityData entity) {
        this(entity, null, false);
    }

    public PostAttackEvent(IMKEntityData entity, @Nullable Entity target, boolean secondaryAttack) {
        super(entity);
        this.target = target;
        this.secondaryAttack = secondaryAttack;
    }

    @Nullable
    public Entity getTarget() {
        return target;
    }

    public boolean isSecondaryAttack() {
        return secondaryAttack;
    }
}
