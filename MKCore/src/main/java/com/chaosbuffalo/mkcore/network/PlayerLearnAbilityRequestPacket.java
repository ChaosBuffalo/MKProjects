package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities.training.EntityAbilityTrainer;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

        for (ResourceLocation loc : forgetting) {
            MKAbility ability = MKCoreRegistry.getAbility(loc);
            if (ability == null) {
                MKCore.LOGGER.error("Forget ability failed because ability with id {} is null for player: {}.", loc.toString(), player);
                return;
            }
        }

        Entity teacher = player.level().getEntity(entityId);
        if (teacher == null) {
            MKCore.LOGGER.error("Player {} tried to learn ability {} from invalid entity (id {})", player, learning, entityId);
            return;
        }

        var playerData = MKCore.getPlayerOrThrow(player);

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
            return;
        }

        int count = playerData.getAbilities().getSlotDeficitToLearnAnAbility();
        if (count != forgetting.size()) {
            MKCore.LOGGER.debug("Failed to learn ability {} from {} - a", learning, teacher);
            return;
        }
        for (ResourceLocation toForget : forgetting) {
            if (!playerData.getAbilities().unlearnAbility(toForget, AbilitySource.TRAINED)) {
                MKCore.LOGGER.debug("Failed to learn ability {} from {} - provided unlearned ability for forgetting {}", learning, teacher, toForget);
                return;
            }
        }

        if (playerData.getAbilities().learnAbility(entry.getAbility(), AbilitySource.TRAINED)) {
            entry.onAbilityLearned(playerData);
        }
    }
}
