package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities.training.EntityAbilityTrainer;
import com.chaosbuffalo.mkcore.core.AbilityDisplayEntry;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityLoadout;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PlayerLearnAbilityRequestPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerLearnAbilityRequestPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("player_learn_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLearnAbilityRequestPacket> STREAM_CODEC = StreamCodec.ofMember(
            PlayerLearnAbilityRequestPacket::toBytes, PlayerLearnAbilityRequestPacket::new
    );

    private final int entityId;
    private final List<ResourceLocation> forgetting;
    private final ResourceLocation learning;

    public PlayerLearnAbilityRequestPacket(List<ResourceLocation> forgetting, ResourceLocation learning, int entityId) {
        this.entityId = entityId;
        this.forgetting = forgetting;
        this.learning = learning;
    }

    public PlayerLearnAbilityRequestPacket(ResourceLocation learning, int entityId) {
        this(new ArrayList<>(), learning, entityId);
    }

    public PlayerLearnAbilityRequestPacket(RegistryFriendlyByteBuf buffer) {
        entityId = buffer.readInt();
        learning = buffer.readResourceLocation();
        int count = buffer.readInt();
        forgetting = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            forgetting.add(buffer.readResourceLocation());
        }
    }

    public static void handle(PlayerLearnAbilityRequestPacket packet, IPayloadContext context) {
        packet.handle(context);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeResourceLocation(learning);
        buffer.writeInt(forgetting.size());
        for (ResourceLocation loc : forgetting) {
            buffer.writeResourceLocation(loc);
        }
    }

    private void handle(IPayloadContext ctx) {
        Player player = ctx.player();
        var playerData = MKCore.getPlayerOrThrow(player);
        Component abilityName = AbilityDisplayEntry.resolve(learning).displayName();

        for (ResourceLocation loc : forgetting) {
            if (!playerData.getAbilities().knowsAbility(loc)) {
                MKCore.LOGGER.error("Forget ability failed because ability with id {} is unknown for player: {}.", loc, player);
                return;
            }
        }

        Entity teacher = player.level().getEntity(entityId);
        if (teacher == null) {
            MKCore.LOGGER.error("Player {} tried to learn ability {} from invalid entity (id {})", player, learning, entityId);
            return;
        }
        EntityAbilityTrainer abilityTrainer = teacher.getExistingDataOrNull(CoreAttachments.ABILITY_TRAINER);
        if (abilityTrainer == null) {
            MKCore.LOGGER.error("Entity {} is not an ability trainer. Requested by {}", teacher, player);
            return;
        }

        AbilityTrainingEntry entry = abilityTrainer.getTrainingEntry(learning);
        if (entry == null) {
            MKCore.LOGGER.error("Trainer {} does not have requested ability {}. Requested by {}", teacher, learning, player);
            return;
        }
        if (!entry.checkRequirements(playerData)) {
            MKCore.LOGGER.debug("Failed to learn ability {} from {} - unmet requirements", learning, teacher);
            showLearnFeedback(player, Component.translatableWithFallback(
                    "mkcore.ability.feedback.learn_requirements",
                    "You do not meet the requirements for %s",
                    abilityName
            ).withStyle(ChatFormatting.RED));
            return;
        }

        int count = playerData.getAbilities().getSlotDeficitToLearnAnAbility();
        if (count != forgetting.size()) {
            MKCore.LOGGER.debug("Failed to learn ability {} from {} - a", learning, teacher);
            showLearnFeedback(player, Component.translatableWithFallback(
                    "mkcore.ability.feedback.learn_pool_full",
                    "You need %s free memory slots to learn %s",
                    count,
                    abilityName
            ).withStyle(ChatFormatting.RED));
            return;
        }
        for (ResourceLocation toForget : forgetting) {
            if (!playerData.getAbilities().unlearnAbility(toForget, AbilitySource.TRAINED)) {
                MKCore.LOGGER.debug("Failed to learn ability {} from {} - provided unlearned ability for forgetting {}", learning, teacher, toForget);
                return;
            }
        }

        if (!entry.learn(playerData, AbilitySource.TRAINED)) {
            showLearnFeedback(player, Component.translatableWithFallback(
                    "mkcore.ability.feedback.unavailable",
                    "%s cannot be used right now",
                    abilityName
            ).withStyle(ChatFormatting.RED));
            return;
        }

        PlayerAbilityLoadout loadout = playerData.getLoadout();
        PlayerAbilityLoadout.SlottedAbilityLocation location = loadout.findEquippedAbilityLocation(learning);
        if (location != null) {
            showLearnFeedback(player, Component.translatableWithFallback(
                    "mkcore.ability.feedback.learned_and_slotted",
                    "Learned %s and slotted it in %s %s",
                    abilityName,
                    location.groupId().getDisplayName(),
                    location.slotIndex() + 1
            ).withStyle(ChatFormatting.GOLD));
            return;
        }

        AbilityGroupId targetGroup = loadout.resolveLearnedAbilityGroup(learning);
        if (targetGroup != null) {
            showLearnFeedback(player, Component.translatableWithFallback(
                    "mkcore.ability.feedback.learned_manual_slot",
                    "Learned %s. Slot it into %s to cast it.",
                    abilityName,
                    targetGroup.getDisplayName()
            ).withStyle(ChatFormatting.GOLD));
            return;
        }

        showLearnFeedback(player, Component.translatableWithFallback(
                "mkcore.ability.feedback.learned",
                "Learned %s",
                abilityName
        ).withStyle(ChatFormatting.GOLD));
    }

    private static void showLearnFeedback(Player player, Component message) {
        player.displayClientMessage(message, true);
    }
}
