package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.abilities.flow.AbilityFlow;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.events.EntityAbilityEvent;
import com.chaosbuffalo.mkcore.network.EntityCastPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
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

    public void executeAbilityInfoWithContext(MKAbilityInfo info, @Nullable AbilityContext context) {
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
                startAbility(context, info);
            } else {
                MKCore.LOGGER.warn("Entity {} tried to execute ability {} with a null context!", entityData.getEntity(), info.getId());
            }
        }
    }

    public boolean canActivateAbility(MKAbilityInfo ability) {
        if (isCasting() || entityData.getEntity().isBlocking())
            return false;

        if (isOnGlobalCooldown())
            return false;

        return getCurrentAbilityCooldown(ability.getId()) <= 0;
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

    public void startAbilityClient(ResourceLocation abilityId, int castTicks, @Nullable AbilityClientState clientState) {
//        MKCore.LOGGER.debug("startCastClient {} {}", abilityId, castTicks);
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            currentCast = createClientCastingState(ability, castTicks, clientState);
            currentCast.begin();
            if (startCastCallback != null) {
                startCastCallback.accept(ability, castTicks);
            }
            if (castTicks <= 0) {
                currentCast.finish();
                if (completeAbilityCallback != null) {
                    completeAbilityCallback.accept(ability);
                }
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
            MKCore.LOGGER.warn("startAbility({}) failed - {} currently casting", info.getId(), entityData.getEntity());
            return false;
        }

        if (!ability.isExecutableContext(context)) {
            MKCore.LOGGER.error("Entity {} tried to execute ability {} with missing memories!", entityData.getEntity(), info.getId());
            return false;
        }

        if (ability.usesFlow()) {
            AbilityFlow flow = ability.createFlow(entityData, info, context);


            return true;
        } else {
            return legacyStart(info, context);
        }
    }

    protected boolean legacyStart(MKAbilityInfo info, AbilityContext context) {
        MKAbility ability = info.getAbility();
        startGlobalCooldown();
        int castTime = entityData.getStats().getAbilityCastTime(ability);
        currentCast = createServerCastingState(context, info, castTime);
        currentCast.begin();
        if (startCastCallback != null) {
            startCastCallback.accept(ability, castTime);
        }
        PacketHandler.sendToTrackingAndSelf(EntityCastPacket.start(entityData, info.getId(), castTime, context), entityData.getEntity());
        if (castTime <= 0) {
            completeAbility(info, context);
        } else {
            return true;
        }
        return true;
    }

    protected void completeAbility(MKAbilityInfo info, AbilityContext context) {
        MKAbility ability = info.getAbility();
        // Finish the cast
        consumeResource(info);
        ability.endCast(entityData.getEntity(), entityData, context);
        if (completeAbilityCallback != null) {
            completeAbilityCallback.accept(ability);
        }
        int cooldown = entityData.getStats().getAbilityCooldown(ability);
        setCooldown(info.getId(), cooldown);
        SoundEvent sound = ability.getSpellCompleteSoundEvent();
        if (sound != null) {
            SoundUtils.serverPlaySoundAtEntity(entityData.getEntity(), sound, entityData.getEntity().getSoundSource());
        }
        clearCastingAbility();
        MinecraftForge.EVENT_BUS.post(new EntityAbilityEvent.EntityCompleteAbilityEvent(ability, entityData));
    }

    protected EntityCastingState createServerCastingState(AbilityContext context, MKAbilityInfo abilityInfo, int castTime) {
        return new ServerCastingState(context, this, abilityInfo, castTime, this::completeAbility);
    }

    protected EntityCastingState createClientCastingState(MKAbility ability, int castTicks, @Nullable AbilityClientState state) {
        return new ClientCastingState(this, ability, castTicks, state);
    }

    protected void consumeResource(MKAbilityInfo abilityInfo) {

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
