package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.attributes.AttributeMapExtension;
import com.chaosbuffalo.mkcore.attributes.MKRangedAttribute;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.google.common.collect.ImmutableSet;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PlayerAttributeMonitor {
    private static final boolean LOG_EN = false;
    private static final UUID EV_ID = UUID.randomUUID();
    private static final Lazy<Set<Attribute>> allInitialSync = Lazy.concurrentOf(PlayerAttributeMonitor::buildInitialSyncSet);

    private final MKPlayerData playerData;

    public interface AttributeChangeHandler {
        void onValueChanged(MKPlayerData playerData, AttributeInstance instance);
    }

    private final Map<Attribute, AttributeChangeHandler> handlerMap = new IdentityHashMap<>();
    private final Set<AttributeInstance> dirtyPrivates = new HashSet<>();
    private final Consumer<BooleanSupplier> tickRequest;

    public PlayerAttributeMonitor(MKPlayerData playerData, Consumer<BooleanSupplier> tickRequest) {
        this.playerData = playerData;
        this.tickRequest = tickRequest;
        playerData.events().subscribe(PlayerEvents.SERVER_JOIN_WORLD, EV_ID, this::onJoinWorld);
    }

    public void subscribe(Attribute attribute, AttributeChangeHandler handler) {
        handlerMap.put(attribute, handler);
    }

    private void onJoinWorld(PlayerEvents.JoinWorldEvent event) {
        if (playerData.getEntity() instanceof ServerPlayer serverPlayer) {
            // This setup is deferred until now because the entity is not fully constructed during the ctor.
            AttributeMapExtension.setModificationHandler(serverPlayer, this::onAttributeModified);
            sendInitialPrivateAttributes(serverPlayer);
        }
    }

    private void sendInitialPrivateAttributes(ServerPlayer serverPlayer) {
        List<AttributeInstance> values = allInitialSync.get().stream().map(serverPlayer::getAttribute).toList();
        if (LOG_EN) {
            MKCore.LOGGER.debug("sending {} private attr initial values to {}", values.size(), serverPlayer);
        }
        sendAttributes(serverPlayer, values);
    }

    private void sendAttributes(ServerPlayer serverPlayer, Collection<AttributeInstance> attrs) {
        serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(serverPlayer.getId(), attrs));
    }

    private void onAttributeModified(AttributeInstance instance) {
        if (LOG_EN) {
            MKCore.LOGGER.debug("attr {} for {} dirty", instance.getAttribute().getDescriptionId(), playerData.getEntity());
//        new Exception("!!attr " + instance.getAttribute().getDescriptionId() + " " + instance.getModifiers().size()).printStackTrace();
        }
        if (instance.getAttribute() instanceof MKRangedAttribute mkAttr) {
            if (mkAttr.getSyncType().syncChanges()) {
                dirtyPrivates.add(instance);
                tickRequest.accept(this::sendUpdates);
            }
        }

        if (!handlerMap.isEmpty()) {
            AttributeChangeHandler handler = handlerMap.get(instance.getAttribute());
            if (handler != null) {
                handler.onValueChanged(playerData, instance);
            }
        }
    }

    private boolean sendUpdates() {
        if (dirtyPrivates.isEmpty())
            return true;

        if (playerData.getEntity() instanceof ServerPlayer serverPlayer) {
            // If not added to the world keep trying to sync
            if (!serverPlayer.isAddedToWorld())
                return false;

            if (LOG_EN) {
                MKCore.LOGGER.debug("sending {} private attr updates to {}", dirtyPrivates.size(), serverPlayer);
            }
            sendAttributes(serverPlayer, dirtyPrivates);
            dirtyPrivates.clear();
        }
        return true;
    }

    private static Set<Attribute> buildInitialSyncSet() {
        ImmutableSet.Builder<Attribute> builder = ImmutableSet.builder();
        AttributeSupplier playerSupplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
        // Need to loop all attrs since AttributeSupplier won't let us grab the set
        ForgeRegistries.ATTRIBUTES.getValues().forEach(attr -> {
            if (playerSupplier.hasAttribute(attr) &&
                    attr instanceof MKRangedAttribute mkAttr &&
                    mkAttr.getSyncType().needsInitialSync()) {
                builder.add(mkAttr);
            }
        });
        return builder.build();
    }
}
