package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class WorldAreaEffectEntry {

    protected final TargetingContext targetContext;
    protected int tickStart;

    protected WorldAreaEffectEntry(TargetingContext context) {
        this.targetContext = context;
        this.tickStart = 0;
    }

    public void setTickStart(int tickStart) {
        this.tickStart = tickStart;
    }

    public int getTickStart() {
        return tickStart;
    }

    public abstract void apply(IMKEntityData casterData, IMKEntityData targetData);

    public static WorldAreaEffectEntry forEffect(Entity directSource, MobEffectInstance effect,
                                                 TargetingContext targetContext) {
        return new VanillaEffectEntry(directSource, effect, targetContext);
    }

    public static WorldAreaEffectEntry forEffect(Entity directSource, MKEffectBuilder<?> builder,
                                                 TargetingContext targetingContext) {
        return new MKEffectEntry(directSource, builder, targetingContext);
    }

    private static class MKEffectEntry extends WorldAreaEffectEntry {
        protected final MKEffectBuilder<?> newEffect;

        public MKEffectEntry(Entity directEntity, MKEffectBuilder<?> builder, TargetingContext targetingContext) {
            super(targetingContext);
            this.newEffect = builder.directEntity(directEntity);
        }

        @Override
        public void apply(IMKEntityData casterData, IMKEntityData targetData) {
            boolean validTarget = newEffect.getEffect().isValidTarget(targetContext, casterData, targetData);
            if (!validTarget) {
                return;
            }

            targetData.getEffects().addEffect(newEffect);
        }
    }

    private static class VanillaEffectEntry extends WorldAreaEffectEntry {
        protected final MobEffectInstance effect;
        protected final Entity directSource;

        VanillaEffectEntry(Entity directSource, MobEffectInstance effect, TargetingContext targetContext) {
            super(targetContext);
            this.directSource = directSource;
            this.effect = effect;
        }

        @Override
        public void apply(IMKEntityData casterData, IMKEntityData targetData) {
            LivingEntity target = targetData.getEntity();
            boolean validTarget = Targeting.isValidTarget(targetContext, casterData.getEntity(), target);

            if (!validTarget) {
                return;
            }

            if (effect.getEffect().isInstantenous()) {
                effect.getEffect().applyInstantenousEffect(directSource, casterData.getEntity(), target, effect.getAmplifier(), 0.5D);
            } else {
                target.addEffect(new MobEffectInstance(effect));
            }
        }
    }
}
