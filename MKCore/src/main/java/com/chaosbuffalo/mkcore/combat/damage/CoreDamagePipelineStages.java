package com.chaosbuffalo.mkcore.combat.damage;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public final class CoreDamagePipelineStages {
    private static boolean registered;

    private CoreDamagePipelineStages() {
    }

    public static synchronized void registerStages() {
        if (registered) {
            return;
        }
        MKDamagePipeline.register(MKDamageStageOrder.CORE, MKCore.id("combat_event_handler"),
                CoreDamagePipelineStages::runCoreDamageStage);
        registered = true;
    }

    private static void runCoreDamageStage(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }

        if (context.getSource().is(DamageTypes.FALL)) {
            SpellTriggers.FALL.onLivingFall(context.getEvent(), context.getSource(), context.getTarget());
        }

        LivingEntity attacker = context.getAttacker();
        if (attacker != null && context.getAttackerData() != null) {
            SpellTriggers.LIVING_HURT_ENTITY.onLivingHurtEntity(context.getEvent(), context.getSource(),
                    context.getTarget(), context.getAttackerData());

            if (attacker instanceof ServerPlayer serverPlayer
                    && DamageUtils.isMeleeDamage(context.getSource())
                    && attacker.getMainHandItem().isEmpty()) {
                var playerData = MKCore.getPlayerOrThrow(serverPlayer);
                playerData.getSkills().tryScaledIncreaseSkill(MKAttributes.HAND_TO_HAND, 0.5);
            }
        }

        SpellTriggers.ENTITY_HURT.onEntityHurtLiving(context.getEvent(), context.getSource(), context.getTargetData());
    }
}
