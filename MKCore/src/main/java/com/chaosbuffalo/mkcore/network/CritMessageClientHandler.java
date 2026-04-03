package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

final class CritMessageClientHandler {
    private CritMessageClientHandler() {
    }

    public static void handleMelee(MeleeCritMessagePacket packet) {
        ResolvedCritContext context = resolve(packet.sourceId(), packet.targetId());
        if (context == null) {
            return;
        }

        if (context.isSelf()) {
            context.player().sendSystemMessage(Component.translatable("mkcore.crit.melee.self",
                    context.target().getDisplayName(),
                    context.source().getMainHandItem().getHoverName(),
                    Math.round(packet.critDamage())
            ).withStyle(ChatFormatting.DARK_RED));
        } else {
            context.player().sendSystemMessage(Component.translatable("mkcore.crit.melee.other",
                    context.source().getDisplayName(),
                    context.target().getDisplayName(),
                    context.source().getMainHandItem().getHoverName(),
                    Math.round(packet.critDamage())
            ).withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static void handleAbility(AbilityCritMessagePacket packet) {
        ResolvedCritContext context = resolve(packet.sourceId(), packet.targetId());
        if (context == null || !(context.target() instanceof LivingEntity livingTarget)) {
            return;
        }

        MKAbility ability = MKCoreRegistry.getAbility(packet.abilityId());
        MKDamageType damageType = MKCoreRegistry.getDamageType(packet.damageTypeId());
        if (ability == null || damageType == null) {
            return;
        }

        context.player().sendSystemMessage(
                damageType.getAbilityCritMessage(context.source(), livingTarget, packet.critDamage(), ability, context.isSelf()));
    }

    public static void handleProjectile(ProjectileCritMessagePacket packet) {
        ResolvedCritContext context = resolve(packet.sourceId(), packet.targetId());
        if (context == null) {
            return;
        }

        Entity projectile = context.player().level().getEntity(packet.projectileId());
        if (projectile == null) {
            return;
        }

        if (context.isSelf()) {
            context.player().sendSystemMessage(Component.translatable("mkcore.crit.projectile.self",
                    context.target().getDisplayName(),
                    projectile.getDisplayName(),
                    Math.round(packet.critDamage())
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            context.player().sendSystemMessage(Component.translatable("mkcore.crit.projectile.other",
                    context.source().getDisplayName(),
                    context.target().getDisplayName(),
                    projectile.getDisplayName(),
                    Math.round(packet.critDamage())
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    public static void handleEffect(EffectCritMessagePacket packet) {
        ResolvedCritContext context = resolve(packet.sourceId(), packet.targetId());
        if (context == null || !(context.target() instanceof LivingEntity livingTarget)) {
            return;
        }

        MKDamageType damageType = MKCoreRegistry.getDamageType(packet.damageTypeId());
        if (damageType == null) {
            return;
        }

        context.player().sendSystemMessage(
                damageType.getEffectCritMessage(context.source(), livingTarget, packet.critDamage(), packet.effectTypeName(),
                        context.isSelf()));
    }

    @Nullable
    private static ResolvedCritContext resolve(int sourceId, int targetId) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }

        Entity source = player.level().getEntity(sourceId);
        Entity target = player.level().getEntity(targetId);
        if (!(source instanceof LivingEntity livingSource) || target == null) {
            return null;
        }

        boolean isSelf = player.is(source);
        boolean isSelfTarget = player.getId() == targetId;
        if (isSelf || isSelfTarget) {
            if (!MKConfig.CLIENT.showMyCrits.get()) {
                return null;
            }
        } else if (!MKConfig.CLIENT.showOthersCrits.get()) {
            return null;
        }

        return new ResolvedCritContext(player, livingSource, target, isSelf);
    }

    private record ResolvedCritContext(Player player, LivingEntity source, Entity target, boolean isSelf) {
    }
}
