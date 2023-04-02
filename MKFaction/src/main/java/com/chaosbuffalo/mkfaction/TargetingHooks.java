package com.chaosbuffalo.mkfaction;

import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.targeting_api.Targeting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TargetingHooks {

    private static Targeting.TargetRelation getPlayerMobRelation(Player source, IMobFaction mobFaction) {
        return source.getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY)
                .map(playerFaction -> playerFaction.getFactionRelation(mobFaction))
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    private static Targeting.TargetRelation playerTargetLiving(Player source, LivingEntity target) {
        return target.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .map(targetFaction -> getPlayerMobRelation(source, targetFaction))
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    private static Targeting.TargetRelation livingTargetLiving(LivingEntity source, LivingEntity target) {
        return source.getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .map(sourceFaction -> sourceFaction.getRelationToEntity(target))
                .orElse(Targeting.TargetRelation.UNHANDLED);
    }

    private static Targeting.TargetRelation targetHook(Entity source, Entity target) {
        if (source instanceof Player) {
            if (target instanceof LivingEntity && !(target instanceof Player)) {
                return playerTargetLiving((Player) source, (LivingEntity) target);
            }
        } else if (source instanceof LivingEntity) {
            if (target instanceof LivingEntity) {
                return livingTargetLiving((LivingEntity) source, (LivingEntity) target);
            }
        }

        return Targeting.TargetRelation.UNHANDLED;
    }

    public static void registerHooks() {
        Targeting.registerRelationCallback(TargetingHooks::targetHook);
    }
}