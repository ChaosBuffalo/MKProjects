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
        MKDamagePipeline.register(MKDamageStageOrder.BONUS_DAMAGE, MKCore.id("attacker_damage_adjustments"),
                CoreDamagePipelineStages::runAttackerDamageBonusStage);
        MKDamagePipeline.register(MKDamageStageOrder.CRIT, MKCore.id("attacker_crits"),
                CoreDamagePipelineStages::runAttackerCritStage);
        MKDamagePipeline.register(MKDamageStageOrder.RESISTANCE, MKCore.id("victim_resistance"),
                CoreDamagePipelineStages::runVictimResistanceStage);
        MKDamagePipeline.register(MKDamageStageOrder.ATTACKER_TRIGGERS, MKCore.id("attacker_triggers"),
                CoreDamagePipelineStages::runAttackerTriggersStage);
        MKDamagePipeline.register(MKDamageStageOrder.VICTIM_TRIGGERS, MKCore.id("victim_triggers"),
                CoreDamagePipelineStages::runVictimTriggersStage);
        registered = true;
    }

    private static void runAttackerDamageBonusStage(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }
        LivingEntity attacker = context.getAttacker();
        if (attacker != null && context.getAttackerData() != null) {
            context.runLegacyEventMutation("mkcore:attacker_damage_bonus", event ->
                    SpellTriggers.LIVING_HURT_ENTITY.applyDamageBonuses(event, context.getSource(),
                            context.getTarget(), context.getAttackerData()));
        }
    }

    private static void runAttackerCritStage(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }
        LivingEntity attacker = context.getAttacker();
        if (attacker != null && context.getAttackerData() != null) {
            context.runLegacyEventMutation("mkcore:attacker_crits", event ->
                    SpellTriggers.LIVING_HURT_ENTITY.applyCrits(event, context.getSource(),
                            context.getTarget(), context.getAttackerData()));
        }
    }

    private static void runVictimResistanceStage(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }
        context.runLegacyEventMutation("mkcore:victim_resistance", event ->
                SpellTriggers.ENTITY_HURT.applyResistance(event, context.getSource(), context.getTargetData()));
    }

    private static void runAttackerTriggersStage(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }
        if (context.getSource().is(DamageTypes.FALL)) {
            context.runLegacyEventMutation("mkcore:fall_trigger", event ->
                    SpellTriggers.FALL.onLivingFall(event, context.getSource(), context.getTarget()));
        }
        LivingEntity attacker = context.getAttacker();
        if (attacker == null || context.getAttackerData() == null) {
            return;
        }
        context.runLegacyEventMutation("mkcore:attacker_triggers", event ->
                SpellTriggers.LIVING_HURT_ENTITY.dispatchTriggers(event, context.getSource(),
                        context.getTarget(), context.getAttackerData()));

        if (attacker instanceof ServerPlayer serverPlayer
                && DamageUtils.isMeleeDamage(context.getSource())
                && attacker.getMainHandItem().isEmpty()) {
            var playerData = MKCore.getPlayerOrThrow(serverPlayer);
            playerData.getSkills().tryScaledIncreaseSkill(MKAttributes.HAND_TO_HAND, 0.5);
        }
    }

    private static void runVictimTriggersStage(MKDamageContext context) {
        if (context.isFullyBlocked()) {
            return;
        }
        context.runLegacyEventMutation("mkcore:victim_triggers", event ->
                SpellTriggers.ENTITY_HURT.dispatchTriggers(event, context.getSource(), context.getTargetData()));
    }
}
