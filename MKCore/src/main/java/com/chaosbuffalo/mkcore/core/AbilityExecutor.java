package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting;
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
    private final Map<ResourceLocation, MKAbilityInfo> activeToggleMap = new HashMap<>();
    private Consumer<MKAbilityInfo> startCastCallback;
    private Consumer<MKAbilityInfo> completeAbilityCallback;
    private BiConsumer<MKAbilityInfo, CastInterruptReason> interruptCastCallback;

    public AbilityExecutor(IMKEntityData entityData) {
        this.entityData = entityData;
        startCastCallback = null;
        completeAbilityCallback = null;
        interruptCastCallback = null;
    }

    public void setCompleteAbilityCallback(Consumer<MKAbilityInfo> completeAbilityCallback) {
        this.completeAbilityCallback = completeAbilityCallback;
    }

    public void setInterruptCastCallback(BiConsumer<MKAbilityInfo, CastInterruptReason> interruptCastCallback) {
        this.interruptCastCallback = interruptCastCallback;
    }

    public void setStartCastCallback(Consumer<MKAbilityInfo> startCastCallback) {
        this.startCastCallback = startCastCallback;
    }

    public void executeAbility(ResourceLocation abilityId) {
        executeAbilityWithContext(abilityId, null);
    }

    public void executeAbilityWithContext(ResourceLocation abilityId, AbilityContext context) {
        MKAbilityInfo info = entityData.getAbilities().getKnownAbility(abilityId);
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
                    MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a context that failed validation!", entityData.getEntity(), info.getId());
                    return;
                }
            }
            if (context != null) {
                ability.executeWithContext(entityData, context, info);
            } else {
                MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a null context!", entityData.getEntity(), info.getId());
            }
        }
    }

    public boolean canActivateAbility(MKAbilityInfo abilityInfo) {
        if (isCasting() || entityData.getEntity().isBlocking())
            return false;

        if (isOnGlobalCooldown())
            return false;

        return getCurrentAbilityCooldown(abilityInfo) <= 0;
    }

    public void tick() {
        updateCurrentCast();
    }

    public void onJoinWorld() {

    }

    public void setCooldown(ResourceLocation id, int ticks) {
        MKCore.LOGGER.debug("setCooldown({}, {})", id, ticks);

        if (!id.equals(MKCoreRegistry.INVALID_ABILITY)) {
            entityData.getStats().setTimer(id, ticks);
        }
    }

    public void setCooldown(MKAbilityInfo abilityInfo, int ticks) {
        if (abilityInfo != null) {
            entityData.getStats().setTimer(abilityInfo.getCooldownTimerId(), ticks);
        }
    }

    public int getCurrentAbilityCooldown(MKAbilityInfo abilityInfo) {
        return entityData.getStats().getTimer(abilityInfo.getCooldownTimerId());
    }

    public boolean isCasting() {
        return currentCast != null;
    }

    public int getCastTicks() {
        return currentCast != null ? currentCast.getCastTicks() : 0;
    }

    @Nullable
    public MKAbilityInfo getCastingAbility() {
        return currentCast != null ? currentCast.getAbilityInfo() : null;
    }

    private void clearCastingAbility() {
        currentCast = null;
    }

    private void startCast(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
        MKCore.LOGGER.debug("startCast {} {}", abilityInfo.getId(), castTime);
        currentCast = createServerCastingState(context, abilityInfo, castTime);
        currentCast.begin();
        PacketHandler.sendToTrackingAndSelf(EntityCastPacket.start(entityData, abilityInfo, castTime), entityData.getEntity());
    }

    public void startCastClient(MKAbilityInfo abilityInfo, int castTicks) {
        MKCore.LOGGER.debug("startCastClient {} {}", abilityInfo, castTicks);
        if (abilityInfo != null) {
            currentCast = createClientCastingState(abilityInfo, castTicks);
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

        MKCore.LOGGER.debug("{} interrupted by {} for {}", currentCast.getAbilityInfo(), reason, entityData.getEntity());

        if (reason.cannotBeBypassed() || currentCast.getAbilityInfo().getAbility().isInterruptedBy(entityData, reason)) {
            currentCast.interrupt(reason);
            if (interruptCastCallback != null) {
                interruptCastCallback.accept(currentCast.getAbilityInfo(), reason);
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
        if (isCasting()) {
            MKCore.LOGGER.warn("startAbility({}) failed - {} currently casting", info.getId(), entityData.getEntity());
            return false;
        }

        if (!info.getAbility().isExecutableContext(context)) {
            MKCore.LOGGER.error("Entity {} tried to execute ability {} with missing memories!", entityData.getEntity(), info.getId());
            return false;
        }

        startGlobalCooldown();
        int castTime = entityData.getStats().getAbilityCastTime(info);
        startCast(context, info, castTime);
        if (castTime > 0) {
            return true;
        } else {
            completeAbility(info, context);
        }
        return true;
    }

    protected void completeAbility(MKAbilityInfo info, AbilityContext context) {
        // Finish the cast
        consumeResource(info);
        MKAbility ability = info.getAbility();
        ability.endCast(entityData.getEntity(), entityData, context, info);
        if (completeAbilityCallback != null) {
            completeAbilityCallback.accept(info);
        }
        int cooldown = entityData.getStats().getAbilityCooldown(info);
        setCooldown(info, cooldown);
        SoundEvent sound = ability.getSpellCompleteSoundEvent();
        if (sound != null) {
            SoundUtils.serverPlaySoundAtEntity(entityData.getEntity(), sound, entityData.getEntity().getSoundSource());
        }
        clearCastingAbility();
        MinecraftForge.EVENT_BUS.post(new EntityAbilityEvent.EntityCompleteAbilityEvent(ability, entityData));
    }

    public void onAbilityUnlearned(MKAbilityInfo abilityInfo) {
        if (abilityInfo.getAbility() instanceof MKToggleAbility toggleAbility) {
            toggleAbility.removeEffect(entityData);
        }
    }

    protected ServerCastingState createServerCastingState(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
        return new ServerCastingState(context, this, abilityInfo, castTime);
    }

    protected ClientCastingState createClientCastingState(MKAbilityInfo abilityInfo, int castTicks) {
        return new ClientCastingState(this, abilityInfo, castTicks);
    }

    protected void consumeResource(MKAbilityInfo abilityInfo) {

    }

    static abstract class EntityCastingState {
        protected final MKAbilityInfo abilityInfo;
        protected final AbilityExecutor executor;
        protected int castTicks;

        public EntityCastingState(AbilityExecutor executor, MKAbilityInfo ability, int castTicks) {
            this.executor = executor;
            this.abilityInfo = ability;
            this.castTicks = castTicks;
        }

        public int getCastTicks() {
            return castTicks;
        }

        public MKAbilityInfo getAbilityInfo() {
            return abilityInfo;
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
                executor.startCastCallback.accept(abilityInfo);
            }
        }

        abstract void activeTick();

        public abstract void finish();

        void interrupt(CastInterruptReason reason) {
        }
    }

    static class ServerCastingState extends EntityCastingState {

        protected final AbilityContext abilityContext;

        public ServerCastingState(AbilityContext context, AbilityExecutor executor, MKAbilityInfo abilityInfo, int castTicks) {
            super(executor, abilityInfo, castTicks);
            abilityContext = context;
        }

        public AbilityContext getAbilityContext() {
            return abilityContext;
        }

        @Override
        void activeTick() {
            abilityInfo.getAbility().continueCast(executor.entityData.getEntity(), executor.entityData, castTicks,
                    abilityContext, abilityInfo);
        }

        @Override
        public void finish() {
            executor.completeAbility(abilityInfo, abilityContext);
        }

        @Override
        void interrupt(CastInterruptReason reason) {
            super.interrupt(reason);
            PacketHandler.sendToTrackingAndSelf(EntityCastPacket.interrupt(executor.entityData, reason), executor.entityData.getEntity());
        }
    }

    static class ClientCastingState extends EntityCastingState {
        protected MovingSoundCasting sound;
        protected boolean playing = false;

        public ClientCastingState(AbilityExecutor executor, MKAbilityInfo abilityInfo, int castTicks) {
            super(executor, abilityInfo, castTicks);
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
            SoundEvent event = abilityInfo.getAbility().getCastingSoundEvent();
            if (event != null) {
                sound = new MovingSoundCasting(executor.entityData.getEntity(), event, castTicks);
                Minecraft.getInstance().getSoundManager().play(sound);
                playing = true;
            }
        }

        @Override
        void activeTick() {
            abilityInfo.getAbility().continueCastClient(executor.entityData.getEntity(), executor.entityData, castTicks);
        }

        @Override
        public void finish() {
            stopSound();
            if (executor.completeAbilityCallback != null) {
                executor.completeAbilityCallback.accept(abilityInfo);
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

    public void setToggleGroupAbility(MKToggleAbility ability, MKAbilityInfo abilityInfo) {
        MKAbilityInfo current = activeToggleMap.get(ability.getToggleGroupId());
        // This can also be called when rebuilding the activeToggleMap after transferring dimensions and in that case
        // ability will be the same as current
        if (current != null && !current.equals(abilityInfo)) {
            if (current.getAbility() instanceof MKToggleAbility toggleAbility) {
                toggleAbility.removeEffect(entityData);
            }
            setCooldown(current, entityData.getStats().getAbilityCooldown(current));
        }
        activeToggleMap.put(ability.getToggleGroupId(), abilityInfo);
    }
}
