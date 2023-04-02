package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.entities.IUpdateEngineProvider;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.scores.Team;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MKCore.MOD_ID)
public class EntityEventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity living = event.getEntityLiving();

        if (living instanceof Player) {
            living.getCapability(CoreCapabilities.PLAYER_CAPABILITY).ifPresent(MKPlayerData::update);
        } else {
            living.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent(MKEntityData::update);
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) {
            Player playerEntity = (Player) e.getObject();
            CoreCapabilities.PlayerDataProvider.attach(e, playerEntity);
        } else if (e.getObject() instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) e.getObject();
            CoreCapabilities.EntityDataProvider.attach(e, livingEntity);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity().getCommandSenderWorld().isClientSide())
            return;

        if (event.getEntity() instanceof LivingEntity) {
            MKCore.getEntityData(event.getEntity()).ifPresent(IMKEntityData::onJoinWorld);
        }
    }

    @SubscribeEvent
    public static void onPlayerGainXP(PlayerXpEvent.XpChange event) {
        MKCore.getPlayer(event.getPlayer()).ifPresent(data -> {
            data.getTalents().addTalentXp(event.getAmount());
        });
    }

    private static int applyMending(LivingEntity entityIn, int xpValue, int xpPerDamage) {
        Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, entityIn, ItemStack::isDamaged);
        if (entry != null) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty() && stack.isDamaged()) {
                int i = Math.min((int) (xpValue * stack.getXpRepairRatio()), stack.getDamageValue());
                xpValue -= i / Math.max(1, xpPerDamage);
                stack.setDamageValue(stack.getDamageValue() - i);
            }
        }
        return xpValue;
    }

    private static int calculateXpShare(int fullAmount, int players) {
        float split = (float) fullAmount / (float) players;
        return (int) Math.ceil(split);
    }

    @SubscribeEvent
    public static void onPlayerPickupXP(PlayerXpEvent.PickupXp event) {
        if (!MKConfig.SERVER.enablePartyXpShare.get().booleanValue())
            return;

        int rangeSq = MKConfig.SERVER.partyXpShareDistance.get().intValue();

        if (event.getPlayer() instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) event.getPlayer();
            Team team = event.getPlayer().getTeam();
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
                            MKCore.LOGGER.info("onPlayerPickupXP giving {} to {}", splitAmount, player);
                            if (MKConfig.SERVER.enablePartyXpShareMending.get().booleanValue()) {
                                splitAmount = applyMending(player, splitAmount, 2);
                                MKCore.LOGGER.info("onPlayerPickupXP post mending {}", splitAmount);
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
        Player player = event.getPlayer();
        Player oldPlayer = event.getOriginal();

        oldPlayer.reviveCaps();
        MKCore.getPlayer(player)
                .ifPresent(newCap -> MKCore.getPlayer(oldPlayer)
                        .ifPresent(oldCap -> newCap.clone(oldCap, event.isWasDeath())));
        oldPlayer.invalidateCaps();
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
//        MKCore.LOGGER.info("StartTracking {} {}", event.getTarget(), event.getTarget().getEntityId());
        if (event.getPlayer() instanceof ServerPlayer) {
            ServerPlayer playerEntity = (ServerPlayer) event.getPlayer();

            MKCore.getEntityData(event.getTarget()).ifPresent(targetData -> targetData.onPlayerStartTracking(playerEntity));
            if (event.getTarget() instanceof IUpdateEngineProvider) {
                ((IUpdateEngineProvider) event.getTarget()).getUpdateEngine().sendAll(playerEntity);
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
