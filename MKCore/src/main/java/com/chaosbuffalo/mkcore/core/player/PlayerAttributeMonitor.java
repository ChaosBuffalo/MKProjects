package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.attributes.AttributeMapExtension;
import com.chaosbuffalo.mkcore.attributes.IMKAttribute;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.*;

public class PlayerAttributeMonitor {
    private static final boolean LOG_EN = false;
    private static final boolean LOG_DIRTY_SOURCE_EN = false;

    private static final Lazy<Set<Holder<Attribute>>> allInitialSync = Lazy.of(PlayerAttributeMonitor::buildInitialSyncSet);

    private final MKPlayerData playerData;

    public interface AttributeChangeHandler {
        void onValueChanged(MKPlayerData playerData, AttributeInstance instance);
    }

    private final Map<Holder<Attribute>, AttributeChangeHandler> handlerMap = new HashMap<>();
    private final Set<AttributeInstance> dirtyPrivates = new HashSet<>();

    public PlayerAttributeMonitor(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public void monitor(Holder<Attribute> attribute, AttributeChangeHandler handler) {
        handlerMap.put(attribute, handler);
    }

    private void sendInitialPrivateAttributes(ServerPlayer serverPlayer) {
        List<AttributeInstance> values = allInitialSync.get().stream().map(serverPlayer::getAttribute).toList();
        if (LOG_EN) {
            MKCore.LOGGER.debug("sending {} private attr initial values to {}", values.size(), serverPlayer);
        }
        sendAttributes(serverPlayer, values);
    }

    private void sendAttributes(ServerPlayer serverPlayer, Collection<AttributeInstance> attrs) {
        if (LOG_EN) {
            MKCore.LOGGER.debug("sending {} attributes to {}", attrs.size(), serverPlayer);
            attrs.forEach(i -> MKCore.LOGGER.debug("   {} - {}", i.getAttribute().getRegisteredName(), i.getValue()));
        }
        serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(serverPlayer.getId(), attrs));
    }

    private void onAttributeModified(AttributeInstance instance) {
        if (LOG_EN) {
            MKCore.LOGGER.debug("attr {} for {} dirty", instance.getAttribute().value().getDescriptionId(), playerData.getEntity());
            if (LOG_DIRTY_SOURCE_EN) {
                new Exception("!!attr " + instance.getAttribute().getRegisteredName() + " dirty by:" + instance.getModifiers().size()).printStackTrace();
            }
        }

        if (instance.getAttribute().value() instanceof IMKAttribute mkAttribute) {
            if (mkAttribute.getSyncType().syncChanges()) {
                dirtyPrivates.add(instance);
            }
        }

        if (!handlerMap.isEmpty()) {
            AttributeChangeHandler handler = handlerMap.get(instance.getAttribute());
            if (handler != null) {
                handler.onValueChanged(playerData, instance);
            }
        }
    }

    public void syncInitial() {
        if (playerData.getEntity() instanceof ServerPlayer serverPlayer) {
            sendInitialPrivateAttributes(serverPlayer);
            AttributeMapExtension.setModificationHandler(serverPlayer, this::onAttributeModified);
        }
    }

    public void syncUpdates() {
        if (dirtyPrivates.isEmpty())
            return;

        if (playerData.getEntity() instanceof ServerPlayer serverPlayer) {
            // If not added to the world keep trying to sync
            if (!serverPlayer.isAddedToLevel())
                return;

            if (LOG_EN) {
                MKCore.LOGGER.debug("sending {} private attr updates to {}", dirtyPrivates.size(), serverPlayer);
            }
            sendAttributes(serverPlayer, dirtyPrivates);
            dirtyPrivates.clear();
        }
    }

    private static Set<Holder<Attribute>> buildInitialSyncSet() {
        ImmutableSet.Builder<Holder<Attribute>> builder = ImmutableSet.builder();
        AttributeSupplier playerSupplier = DefaultAttributes.getSupplier(EntityType.PLAYER);
        playerSupplier.instances.forEach((attr, instance) -> {
            if (attr.value() instanceof IMKAttribute mkAttribute &&
                    mkAttribute.getSyncType().needsInitialSync()) {
                builder.add(attr);
            }
        });
        return builder.build();
    }
}
