package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class FrozenGraspEffect extends MKEffect {
    public static final int DEFAULT_PERIOD = GameConstants.TICKS_PER_SECOND * 3;

    private static final UUID moveUUID = UUID.fromString("bde03af5-32ed-4f6b-9f2c-c23296d60fa8");
    private static final UUID castingUUID = UUID.fromString("c06a3740-5c18-4063-b807-84ef4cb3e931");
    private static final UUID attackUUID = UUID.fromString("32faca56-9979-4546-afbb-83f7075592a2");

    public static MKEffectBuilder<?> applierFrom(LivingEntity source, int duration, int onHitDuration, int maxStacks,
                                                 ResourceLocation particles) {
        return MKUEffects.FROZEN_GRASP_APPLIER.get().builder(source)
                .state(s -> {
                    s.setMaxStacks(maxStacks);
                    s.setDuration(onHitDuration);
                    s.setEffectParticles(particles);
                }).setBaseStackCount(maxStacks).timed(duration);
    }

    public FrozenGraspEffect() {
        super(MobEffectCategory.HARMFUL);
        addAttribute(Attributes.MOVEMENT_SPEED, moveUUID, -0.05, -0.05,
                AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.NECROMANCY);
        addAttribute(MKAttributes.CASTING_SPEED, castingUUID, -0.05, -0.05,
                AttributeModifier.Operation.ADDITION, MKAttributes.NECROMANCY);
        addAttribute(Attributes.ATTACK_SPEED, attackUUID, -0.05, -0.05,
                AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.NECROMANCY);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
