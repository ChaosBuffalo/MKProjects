package com.chaosbuffalo.targeting_api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public class TargetingContexts {
    public static TargetingContext ALL = TargetingContext.Builder.create(LivingEntity.class)
            .setTargetTest(Targeting::allowAny)
            .setLocalizationKey("targeting_api.targeting_context.all")
            .build();
    public static TargetingContext ALL_AROUND = TargetingContext.Builder.from(ALL)
            .canTargetCaster(false)
            .setLocalizationKey("targeting_api.targeting_context.all_around")
            .build();
    public static TargetingContext PLAYERS = TargetingContext.Builder.create(Player.class)
            .setTargetTest(Targeting::allowAny)
            .setLocalizationKey("targeting_api.targeting_context.players")
            .build();
    public static TargetingContext PLAYERS_AROUND = TargetingContext.Builder.from(PLAYERS)
            .setLocalizationKey("targeting_api.targeting_context.players_around")
            .canTargetCaster(false)
            .build();
    public static TargetingContext SELF = TargetingContext.Builder.create(LivingEntity.class)
            .setTargetTest(Targeting::areEntitiesEqual)
            .setLocalizationKey("targeting_api.targeting_context.self")
            .build();
    public static TargetingContext FRIENDLY = TargetingContext.Builder.create(LivingEntity.class)
            .setTargetTest(Targeting::isValidFriendly)
            .setLocalizationKey("targeting_api.targeting_context.friend")
            .build();
    public static TargetingContext FRIENDLY_AROUND = TargetingContext.Builder.from(FRIENDLY)
            .setLocalizationKey("targeting_api.targeting_context.friend_around")
            .canTargetCaster(false)
            .build();
    public static TargetingContext ENEMY = TargetingContext.Builder.create(LivingEntity.class)
            .canTargetCaster(false)
            .setLocalizationKey("targeting_api.targeting_context.enemy")
            .setTargetTest(Targeting::isValidEnemy)
            .build();
    public static TargetingContext NEUTRAL = TargetingContext.Builder.create(LivingEntity.class)
            .canTargetCaster(false)
            .setLocalizationKey("targeting_api.targeting_context.neutral")
            .setTargetTest(Targeting::isValidNeutral)
            .build();
}
