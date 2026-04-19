package com.chaosbuffalo.targeting_api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


/**
 * Collection of common predefined {@link TargetingContext} instances.
 */
public class TargetingContexts {
    /**
     * Any {@link LivingEntity living entity}, including the caster.
     */
    public static TargetingContext ALL = TargetingContext.Builder.create(LivingEntity.class)
            .setTargetTest(Targeting::allowAny)
            .setLocalizationKey("targeting_api.targeting_context.all")
            .build();
    /**
     * Any {@link LivingEntity living entity} except the caster.
     */
    public static TargetingContext ALL_AROUND = TargetingContext.Builder.from(ALL)
            .canTargetCaster(false)
            .setLocalizationKey("targeting_api.targeting_context.all_around")
            .build();
    /**
     * Any {@link Player player}, including the caster if the caster is a player.
     */
    public static TargetingContext PLAYERS = TargetingContext.Builder.create(Player.class)
            .setTargetTest(Targeting::allowAny)
            .setLocalizationKey("targeting_api.targeting_context.players")
            .build();
    /**
     * Any {@link Player player} except the caster.
     */
    public static TargetingContext PLAYERS_AROUND = TargetingContext.Builder.from(PLAYERS)
            .setLocalizationKey("targeting_api.targeting_context.players_around")
            .canTargetCaster(false)
            .build();
    /**
     * Only the caster.
     */
    public static TargetingContext SELF = TargetingContext.Builder.create(LivingEntity.class)
            .setTargetTest(Targeting::areEntitiesEqual)
            .setLocalizationKey("targeting_api.targeting_context.self")
            .build();
    /**
     * Friendly {@link LivingEntity living entities}, including the caster.
     */
    public static TargetingContext FRIENDLY = TargetingContext.Builder.create(LivingEntity.class)
            .setTargetTest(Targeting::isValidFriendly)
            .setLocalizationKey("targeting_api.targeting_context.friend")
            .build();
    /**
     * Friendly {@link LivingEntity living entities} except the caster.
     */
    public static TargetingContext FRIENDLY_AROUND = TargetingContext.Builder.from(FRIENDLY)
            .setLocalizationKey("targeting_api.targeting_context.friend_around")
            .canTargetCaster(false)
            .build();
    /**
     * Hostile {@link LivingEntity living entities}.
     */
    public static TargetingContext ENEMY = TargetingContext.Builder.create(LivingEntity.class)
            .canTargetCaster(false)
            .setLocalizationKey("targeting_api.targeting_context.enemy")
            .setTargetTest(Targeting::isValidEnemy)
            .build();
    /**
     * Neutral or unhandled {@link LivingEntity living entities}.
     */
    public static TargetingContext NEUTRAL = TargetingContext.Builder.create(LivingEntity.class)
            .canTargetCaster(false)
            .setLocalizationKey("targeting_api.targeting_context.neutral")
            .setTargetTest(Targeting::isValidNeutral)
            .build();
}
