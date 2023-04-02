package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.nbt.CompoundTag;

public class MKEffectBehaviour {

    private int duration;
    private int period;
    private boolean infinite;
    private boolean temporary;

    public MKEffectBehaviour() {

    }

    public MKEffectBehaviour(MKEffectBehaviour template) {
        duration = template.duration;
        period = template.period;
        infinite = template.infinite;
        temporary = template.temporary;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isTimed() {
        return isInfinite() || duration > 0;
    }

    public void modifyDuration(int delta) {
        duration += delta;
    }

    public boolean isExpired() {
        return canExpire() && duration <= 0;
    }

    private boolean canExpire() {
        return !isInfinite();
    }

    public void setTemporary() {
        this.temporary = true;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public MKEffectTickAction behaviourTick(IMKEntityData targetData, MKActiveEffect activeEffect) {
        MKEffectTickAction action;
        if (isInfinite()) {
            action = infiniteTick(targetData, activeEffect);
        } else {
            action = timedTick(targetData, activeEffect);
        }

        return action;
    }

    public boolean isReady() {
        if (isExpired())
            return false;
        if (getPeriod() > 0) {
            return getDuration() % getPeriod() == 0;
        } else {
            return true;
        }
    }

    private MKEffectTickAction timedTick(IMKEntityData targetData, MKActiveEffect instance) {
        boolean keepTicking = false;
        if (getDuration() > 0) {
            keepTicking = tryPerformEffect(targetData, instance);

            duration--;
        }

        if (isExpired() || !keepTicking) {
            return MKEffectTickAction.Remove;
        }
        return MKEffectTickAction.NoUpdate;
    }

    private MKEffectTickAction infiniteTick(IMKEntityData targetData, MKActiveEffect instance) {

        boolean keepTicking = tryPerformEffect(targetData, instance);
        if (!keepTicking) {
            return MKEffectTickAction.Remove;
        }

        duration++;

        return MKEffectTickAction.NoUpdate;
    }

    private boolean tryPerformEffect(IMKEntityData targetData, MKActiveEffect instance) {
        if (targetData.isServerSide()) {
            if (instance.getState().isReady(targetData, instance)) {
                return instance.getState().performEffect(targetData, instance);
            }
        }
        return true;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        if (duration > 0) {
            tag.putInt("duration", duration);
        }
        if (period > 0) {
            tag.putInt("period", period);
        }
        if (infinite) {
            tag.putBoolean("infinite", true);
        }
        return tag;
    }

    public static MKEffectBehaviour deserialize(CompoundTag tag) {
        MKEffectBehaviour behaviour = new MKEffectBehaviour();
        behaviour.deserializeState(tag);
        return behaviour;
    }

    protected void deserializeState(CompoundTag tag) {
        duration = tag.getInt("duration");
        period = tag.getInt("period");
        infinite = tag.getBoolean("infinite");
    }

    @Override
    public String toString() {
        return "MKEffectBehaviour{" +
                "duration=" + duration +
                ", period=" + period +
                ", infinite=" + infinite +
                '}';
    }
}
