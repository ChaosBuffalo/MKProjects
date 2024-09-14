package com.chaosbuffalo.mkfaction;

import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.mkfaction.capabilities.IPlayerFaction;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TargetingHooks {

    private static Targeting.TargetRelation getPlayerMobRelation(Player source, IMobFaction mobFaction) {
        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(source);
        return playerFaction.getFactionRelation(mobFaction);
    }

    private static Targeting.TargetRelation playerTargetLiving(Player source, LivingEntity target) {
        IMobFaction targetFaction = IMobFaction.getMobOrThrow(target);
        return getPlayerMobRelation(source, targetFaction);
    }

    private static Targeting.TargetRelation livingTargetLiving(LivingEntity source, LivingEntity target) {
        IMobFaction sourceFaction = IMobFaction.getMobOrThrow(source);
        return sourceFaction.getRelationToEntity(target);
    }

    private static Targeting.TargetRelation targetHook(Entity source, Entity target) {
        if (source instanceof Player playerSource) {
            if (target instanceof LivingEntity mobTarget && !(target instanceof Player)) {
                return playerTargetLiving(playerSource, mobTarget);
            }
        } else if (source instanceof LivingEntity mobSource) {
            if (target instanceof LivingEntity mobTarget) {
                return livingTargetLiving(mobSource, mobTarget);
            }
        }

        return Targeting.TargetRelation.UNHANDLED;
    }

    public static void registerHooks() {
        Targeting.registerRelationCallback(TargetingHooks::targetHook);
    }
}