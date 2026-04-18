package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter.ParticipantRelation;
import com.chaosbuffalo.mkcore.abilities2.runtime.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class AbilityRuntimeService {
    private final AbilityDefinitionResolver definitionResolver;
    private final AbilityStateStore stateStore;
    private final AbilityPowerResolver powerResolver;
    private final AbilityReactionBus reactionBus;
    private final SimpleAbilityEngine engine;

    public AbilityRuntimeService(AbilityDefinitionResolver definitionResolver) {
        this.definitionResolver = definitionResolver;
        this.stateStore = new MemoryAbilityStateStore();
        this.powerResolver = new MKAbilityPowerResolver();
        this.reactionBus = new AbilityReactionBus(
                this::currentGameTick,
                this::triggerReaction,
                this::evaluateRelation,
                this::matchesTag
        );
        this.engine = new SimpleAbilityEngine(definitionResolver, powerResolver, stateStore, reactionBus::emit);
    }

    public AbilityStateStore getStateStore() {
        return stateStore;
    }

    public AbilityPowerResolver getPowerResolver() {
        return powerResolver;
    }

    public AbilityReactionBus getReactionBus() {
        return reactionBus;
    }

    public AbilityEngine getEngine() {
        return engine;
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()) {
            engine.tickEntity(MKCore.getEntityDataOrThrow(living));
        }
    }

    private long currentGameTick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.overworld().getGameTime() : 0L;
    }

    private void triggerReaction(RegisteredReaction reaction, AbilityEventSnapshot event) {
        LivingEntity ownerEntity = findEntity(reaction.owner().ownerId());
        if (ownerEntity == null) {
            return;
        }
        InvocationResult result = engine.activateReaction(
                reaction,
                MKCore.getEntityDataOrThrow(ownerEntity),
                MKCore.getEntityDataOrThrow(ownerEntity),
                reaction.owner().stableSourceId(),
                event
        );
        if (!result.started()) {
            MKCore.LOGGER.debug("abilities2 reaction {} did not start: {}", reaction.handle(), result.failureReason());
        }
    }

    private boolean evaluateRelation(UUID ownerEntityId, UUID participantEntityId, ParticipantRelation relation) {
        if (relation == ParticipantRelation.IS_SELF) {
            return ownerEntityId.equals(participantEntityId);
        }

        LivingEntity owner = findEntity(ownerEntityId);
        LivingEntity participant = findEntity(participantEntityId);
        if (owner == null || participant == null) {
            return false;
        }

        return switch (relation) {
            case IS_SELF -> ownerEntityId.equals(participantEntityId);
            case IS_ALLY -> !ownerEntityId.equals(participantEntityId) && owner.isAlliedTo(participant);
            case IS_ENEMY -> !ownerEntityId.equals(participantEntityId) && !owner.isAlliedTo(participant);
        };
    }

    private boolean matchesTag(ResourceLocation value, ResourceLocation tag) {
        if (value.equals(tag)) {
            return true;
        }
        PatchedAbilityDefinition definition = definitionResolver.resolvePatched(value);
        return definition != null && definition.definition().data().tags().contains(tag);
    }

    private @Nullable LivingEntity findEntity(UUID entityId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            var entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }
}
