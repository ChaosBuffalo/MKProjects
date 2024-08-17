package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.entities.ISyncControllerProvider;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = MKCore.MOD_ID)
public class EntityEventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Post event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(IMKEntityData::update);
    }

    private static MKPlayerData playerCapFactory(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return new MKServerPlayerData(serverPlayer);
        } else {
            return new MKPlayerData(player);
        }
    }


    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide())
            return;

        if (event.getEntity() instanceof LivingEntity) {
            MKCore.getEntityData(event.getEntity()).ifPresent(IMKEntityData::onJoinWorld);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(
                entityData -> entityData.getAbilityExecutor().interruptCast(CastInterruptReason.Logout));
    }

    @SubscribeEvent
    public static void onPlayerGainXP(PlayerXpEvent.XpChange event) {
        if (event.getAmount() == 0) {
            return;
        }
        MKCore.getPlayer(event.getEntity()).ifPresent(data -> {
            data.getTalents().addTalentXp(event.getAmount());
        });
    }

    private static int applyMending(LivingEntity entityIn, int xpValue, int xpPerDurability) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, entityIn, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack stack = entry.getValue();
            int i = Math.min((int) (xpValue * stack.getXpRepairRatio()), stack.getDamageValue());
            stack.setDamageValue(stack.getDamageValue() - i);
            xpValue -= i / Math.max(1, xpPerDurability);
        }
        return xpValue;
    }

    private static int calculateXpShare(int fullAmount, int players) {
        float split = (float) fullAmount / (float) players;
        return (int) Math.ceil(split);
    }

    @SubscribeEvent
    public static void onPlayerPickupXP(PlayerXpEvent.PickupXp event) {
        if (!MKConfig.SERVER.enablePartyXpShare.get())
            return;

        int rangeSq = MKConfig.SERVER.partyXpShareDistance.get();

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            Team team = event.getEntity().getTeam();
            MinecraftServer server = serverPlayer.getServer();
            if (team != null && server != null) {
                List<Player> playersInRange = team.getPlayers().stream()
                        .map(x -> server.getPlayerList().getPlayerByName(x))
                        .filter(other -> other != null && serverPlayer.distanceToSqr(other) <= rangeSq * rangeSq)
                        .collect(Collectors.toList());
                if (playersInRange.size() > 1) {
                    int splitAmount = calculateXpShare(event.getOrb().value, playersInRange.size());
                    splitAmount = Math.max(splitAmount, 1);

                    for (Player player : playersInRange) {
                        if (!player.is(serverPlayer)) {
//                            MKCore.LOGGER.info("onPlayerPickupXP giving {} to {}", splitAmount, player);
                            if (MKConfig.SERVER.enablePartyXpShareMending.get()) {
                                splitAmount = applyMending(player, splitAmount, 2);
//                                MKCore.LOGGER.info("onPlayerPickupXP post mending {}", splitAmount);
                            }
                            if (splitAmount > 0) {
                                player.giveExperiencePoints(splitAmount);
                            }
                        }
                    }
                    event.getOrb().value = splitAmount;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player oldPlayer = event.getOriginal();

        MKCore.getPlayer(player)
                .ifPresent(newCap -> MKCore.getPlayer(oldPlayer)
                        .ifPresent(oldCap -> newCap.clone(event.getEntity().registryAccess(), oldCap, event.isWasDeath())));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer playerEntity) {
            MKCore.getEntityData(event.getTarget())
                    .ifPresent(targetData -> targetData.onPlayerStartTracking(playerEntity));
            if (event.getTarget() instanceof ISyncControllerProvider provider) {
                provider.getSyncController().sendFullSync(playerEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJump(LivingEvent.LivingJumpEvent event) {
        MKCore.getEntityData(event.getEntity()).ifPresent(entityData -> {
            entityData.getAbilityExecutor().interruptCast(CastInterruptReason.Jump);
            if (entityData.getEffects().isEffectActive(CoreEffects.STUN.get())) {
                event.getEntity().setDeltaMovement(0, 0, 0);
            }
        });
    }
}
