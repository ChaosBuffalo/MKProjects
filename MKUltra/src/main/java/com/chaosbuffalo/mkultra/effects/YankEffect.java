package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingDamageEffectState;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class YankEffect extends MKEffect {

    public YankEffect() {
        super(MobEffectCategory.NEUTRAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, float base, float scale, Vec3 sourcePos) {
        return MKUEffects.YANK.get().builder(source).state(s -> {
            s.setScalingParameters(base, scale, 0.0f);
            s.setPos(sourcePos);
        });
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    @Override
    public State makeState() {
        return new State();
    }

    public static class State extends ScalingDamageEffectState {
        protected Vec3 pos;

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            LivingEntity target = targetData.getEntity();
            Vec3 targetPos = new Vec3(target.getX(), target.getY(0.6f), target.getZ());
            float force = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            float heightRatio = 1.0f;
            if (activeEffect.getSourceEntity() != null) {
                heightRatio = activeEffect.getSourceEntity().getBbHeight() / target.getBbHeight();
            }
            force *= heightRatio;
            targetData.getAbilityExecutor().interruptCast(CastInterruptReason.Jump);
            Vec3 awayFrom = pos.subtract(targetPos).normalize().scale(force);
            target.push(awayFrom.x(), awayFrom.y(), awayFrom.z());
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
            }
            return true;
        }

        public void setPos(Vec3 pos) {
            this.pos = pos;
        }

        public Vec3 getPos() {
            return pos;
        }

        @Override
        public void deserializeStorage(CompoundTag stateTag) {
            super.deserializeStorage(stateTag);
            pos = new Vec3(stateTag.getDouble("posX"), stateTag.getDouble("posY"), stateTag.getDouble("posZ"));
        }

        @Override
        public void serializeStorage(CompoundTag stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putDouble("posX", pos.x());
            stateTag.putDouble("posY", pos.y());
            stateTag.putDouble("posZ", pos.z());
        }
    }
}
