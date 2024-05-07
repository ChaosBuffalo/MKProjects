package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.events.EntityAbilityEvent;
import com.chaosbuffalo.mkcore.network.EntityCastPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AbilityExecutor {
    public static final ResourceLocation GCD_TIMER = MKCore.makeRL("timer.gcd");
    protected final IMKEntityData entityData;
    private EntityCastingState currentCast;
    private final Map<ResourceLocation, MKToggleAbility> activeToggleMap = new HashMap<>();
    private BiConsumer<MKAbility, Integer> startCastCallback;
    private Consumer<MKAbility> completeAbilityCallback;
    private BiConsumer<MKAbility, CastInterruptReason> interruptCastCallback;

    public AbilityExecutor(IMKEntityData entityData) {
        this.entityData = entityData;
        startCastCallback = null;
        completeAbilityCallback = null;
        interruptCastCallback = null;
    }

    public void setCompleteAbilityCallback(Consumer<MKAbility> completeAbilityCallback) {
        this.completeAbilityCallback = completeAbilityCallback;
    }

    public void setInterruptCastCallback(BiConsumer<MKAbility, CastInterruptReason> interruptCastCallback) {
        this.interruptCastCallback = interruptCastCallback;
    }

    public void setStartCastCallback(BiConsumer<MKAbility, Integer> startCastCallback) {
        this.startCastCallback = startCastCallback;
    }

    public void executeAbility(ResourceLocation abilityId) {
        executeAbilityWithContext(abilityId, null);
    }

    public void executeAbilityWithContext(ResourceLocation abilityId, AbilityContext context) {
        MKAbilityInfo info = entityData.getAbilities().getAbilityInfo(abilityId);
        if (info == null)
            return;

        executeAbilityInfoWithContext(info, context);
    }

    public void executeAbilityInfoWithContext(MKAbilityInfo info, AbilityContext context) {
        MKAbility ability = info.getAbility();
        if (ability.meetsCastingRequirements(entityData, info)) {
            if (context == null) {
                context = ability.getTargetSelector().createContext(entityData, info);
            } else {
                boolean validContext = ability.getTargetSelector().validateContext(entityData, context);
                if (!validContext) {
                    MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a context that failed validation!", entityData.getEntity(), info.getAbility().getAbilityId());
                    return;
                }
            }
            if (context != null) {
                ability.executeWithContext(entityData, context, info);
            } else {
                MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a null context!", entityData.getEntity(), info.getAbility().getAbilityId());
            }
        }
    }

    public boolean canActivateAbility(MKAbility ability) {
        if (isCasting() || entityData.getEntity().isBlocking())
            return false;

        if (isOnGlobalCooldown())
            return false;

        return getCurrentAbilityCooldown(ability.getAbilityId()) <= 0;
    }

    public void tick() {
        updateCurrentCast();
    }

    public void setCooldown(ResourceLocation id, int ticks) {
//        MKCore.LOGGER.debug("setCooldown({}, {})", id, ticks);

        if (!id.equals(MKCoreRegistry.INVALID_ABILITY)) {
            entityData.getStats().setTimer(id, ticks);
        }
    }

    public int getCurrentAbilityCooldown(ResourceLocation abilityId) {
        return entityData.getStats().getTimer(abilityId);
    }

    public boolean isCasting() {
        return currentCast != null;
    }

    public int getCastTicks() {
        return currentCast != null ? currentCast.getCastTicks() : 0;
    }

    @Nullable
    public MKAbility getCastingAbility() {
        return currentCast != null ? currentCast.getAbility() : null;
    }

    private void clearCastingAbility() {
        currentCast = null;
    }

    private void startCast(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
//        MKCore.LOGGER.debug("startCast {} {}", abilityInfo.getId(), castTime);
        currentCast = createServerCastingState(context, abilityInfo, castTime);
        currentCast.begin();
        PacketHandler.sendToTrackingAndSelf(EntityCastPacket.start(entityData, abilityInfo.getId(), castTime, context), entityData.getEntity());
    }

    public void startCastClient(ResourceLocation abilityId, int castTicks, @Nullable AbilityClientState clientState) {
//        MKCore.LOGGER.debug("startCastClient {} {}", abilityId, castTicks);
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            currentCast = createClientCastingState(ability, castTicks, clientState);
            currentCast.begin();
            if (castTicks <= 0) {
                currentCast.finish();
            }
        } else {
            clearCastingAbility();
        }
    }

    public void interruptCast(CastInterruptReason reason) {
        if (!isCasting())
            return;

        MKCore.LOGGER.debug("{} interrupted by {} for {}", currentCast.getAbility(), reason, entityData.getEntity());

        if (reason.cannotBeBypassed() || currentCast.getAbility().isInterruptedBy(entityData, reason)) {
            currentCast.interrupt(reason);
            if (interruptCastCallback != null) {
                interruptCastCallback.accept(currentCast.getAbility(), reason);
            }
            clearCastingAbility();
        }
    }

    private void updateCurrentCast() {
        if (!isCasting())
            return;

        if (!currentCast.tick()) {
            clearCastingAbility();
        }
    }

    public void startGlobalCooldown() {
        entityData.getStats().setLocalTimer(GCD_TIMER, GameConstants.GLOBAL_COOLDOWN_TICKS);
    }

    public boolean isOnGlobalCooldown() {
        return entityData.getStats().getTimer(GCD_TIMER) > 0;
    }

    public float getGlobalCooldownPercent(float partialTick) {
        return entityData.getStats().getTimerPercent(GCD_TIMER, partialTick);
    }

    public boolean startAbility(AbilityContext context, MKAbilityInfo info) {
        MKAbility ability = info.getAbility();
        if (isCasting()) {
            MKCore.LOGGER.warn("startAbility({}) failed - {} currently casting", ability.getAbilityId(), entityData.getEntity());
            return false;
        }

        if (!ability.isExecutableContext(context)) {
            MKCore.LOGGER.error("Entity {} tried to execute ability {} with missing memories!", entityData.getEntity(), ability.getAbilityId());
            return false;
        }

        startGlobalCooldown();
        int castTime = entityData.getStats().getAbilityCastTime(ability);
        startCast(context, info, castTime);
        if (castTime > 0) {
            return true;
        } else {
            completeAbility(ability, info, context);
        }
        return true;
    }

    protected void completeAbility(MKAbility ability, MKAbilityInfo info, AbilityContext context) {
        // Finish the cast
        consumeResource(ability);
        ability.endCast(entityData.getEntity(), entityData, context);
        if (completeAbilityCallback != null) {
            completeAbilityCallback.accept(ability);
        }
        int cooldown = entityData.getStats().getAbilityCooldown(ability);
        setCooldown(ability.getAbilityId(), cooldown);
        SoundEvent sound = ability.getSpellCompleteSoundEvent();
        if (sound != null) {
            SoundUtils.serverPlaySoundAtEntity(entityData.getEntity(), sound, entityData.getEntity().getSoundSource());
        }
        clearCastingAbility();
        MinecraftForge.EVENT_BUS.post(new EntityAbilityEvent.EntityCompleteAbilityEvent(ability, entityData));
    }

    protected EntityCastingState createServerCastingState(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
        return new ServerCastingState(context, this, abilityInfo, castTime);
    }

    protected EntityCastingState createClientCastingState(MKAbility ability, int castTicks, @Nullable AbilityClientState state) {
        return new ClientCastingState(this, ability, castTicks, state);
    }

    protected void consumeResource(MKAbility ability) {

    }

    protected static abstract class EntityCastingState {
        protected final MKAbility ability;
        protected final AbilityExecutor executor;
        protected int castTicks;
        protected int totalTicks;

        public EntityCastingState(AbilityExecutor executor, MKAbility ability, int castTicks) {
            this.executor = executor;
            this.ability = ability;
            this.castTicks = castTicks;
            this.totalTicks = castTicks;
        }

        public int getCastTicks() {
            return castTicks;
        }

        public MKAbility getAbility() {
            return ability;
        }

        public ResourceLocation getAbilityId() {
            return ability.getAbilityId();
        }

        public boolean tick() {
            if (castTicks <= 0)
                return false;

            activeTick();
            castTicks--;
            boolean active = castTicks > 0;
            if (!active) {
                finish();
            }
            return active;
        }

        void begin() {
            if (executor.startCastCallback != null) {
                executor.startCastCallback.accept(ability, totalTicks);
            }
        }

        abstract void activeTick();

        public abstract void finish();

        void interrupt(CastInterruptReason reason) {
        }
    }

    static class ServerCastingState extends EntityCastingState {
        protected final MKAbilityInfo info;
        protected final AbilityContext abilityContext;

        public ServerCastingState(AbilityContext context, AbilityExecutor executor, MKAbilityInfo abilityInfo, int castTicks) {
            super(executor, abilityInfo.getAbility(), castTicks);
            this.info = abilityInfo;
            abilityContext = context;
        }

        @Override
        void begin() {
            super.begin();
            ability.startCast(executor.entityData, totalTicks, abilityContext);
        }

        public AbilityContext getAbilityContext() {
            return abilityContext;
        }

        @Override
        void activeTick() {
            ability.continueCast(executor.entityData.getEntity(), executor.entityData, castTicks, totalTicks, abilityContext);
        }

        @Override
        public void finish() {
            executor.completeAbility(ability, info, abilityContext);
        }

        @Override
        void interrupt(CastInterruptReason reason) {
            super.interrupt(reason);
            ability.interruptCast(reason, executor.entityData, abilityContext);
            PacketHandler.sendToTrackingAndSelf(EntityCastPacket.interrupt(executor.entityData, reason), executor.entityData.getEntity());
        }
    }

    static class ClientCastingState extends EntityCastingState {
        protected MovingSoundCasting sound;
        protected boolean playing = false;
        @Nullable
        protected final AbilityClientState clientState;

        public ClientCastingState(AbilityExecutor executor, MKAbility ability, int castTicks, @Nullable AbilityClientState clientState) {
            super(executor, ability, castTicks);
            this.clientState = clientState;
        }

        private void stopSound() {
            if (playing && sound != null) {
                Minecraft.getInstance().getSoundManager().stop(sound);
                playing = false;
            }
        }

        @Override
        void begin() {
            super.begin();
            SoundEvent event = ability.getCastingSoundEvent();
            if (event != null) {
                sound = new MovingSoundCasting(executor.entityData.getEntity(), event, castTicks);
                Minecraft.getInstance().getSoundManager().play(sound);
                playing = true;
            }
        }

        @Override
        void activeTick() {
            ability.continueCastClient(executor.entityData.getEntity(), executor.entityData, castTicks, totalTicks, clientState);
        }

        @Override
        public void finish() {
            stopSound();
            ability.endCastClient(executor.entityData, clientState);
            if (executor.completeAbilityCallback != null) {
                executor.completeAbilityCallback.accept(ability);
            }
        }

        @Override
        public void interrupt(CastInterruptReason reason) {
            super.interrupt(reason);
            stopSound();
        }
    }

    public void clearToggleGroupAbility(ResourceLocation groupId) {
        activeToggleMap.remove(groupId);
    }

    public void setToggleGroupAbility(ResourceLocation groupId, MKToggleAbility ability) {
        MKToggleAbility current = activeToggleMap.get(ability.getToggleGroupId());
        // This can also be called when rebuilding the activeToggleMap after transferring dimensions and in that case
        // ability will be the same as current
        if (current != null && current != ability) {
            current.removeEffect(entityData);
            setCooldown(current.getAbilityId(), entityData.getStats().getAbilityCooldown(current));
        }
        activeToggleMap.put(groupId, ability);
    }
}
