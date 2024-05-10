package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.network.EntityCastPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;

class ServerCastingState extends EntityCastingState {
    protected final MKAbilityInfo info;
    protected final AbilityContext abilityContext;
    private final FinishCallback onFinish;

    public interface FinishCallback {
        void finish(MKAbilityInfo abilityInfo, AbilityContext context);
    }

    public ServerCastingState(AbilityContext context, AbilityExecutor executor, MKAbilityInfo abilityInfo, int castTicks, FinishCallback onFinish) {
        super(executor, abilityInfo.getAbility(), castTicks);
        this.info = abilityInfo;
        abilityContext = context;
        this.onFinish = onFinish;
    }

    @Override
    void begin() {
        ability.startCast(executor.entityData, totalTicks, abilityContext);
    }

    public AbilityContext getAbilityContext() {
        return abilityContext;
    }

    @Override
    void activeTick() {
        ability.continueCast(executor.entityData, castTicks, totalTicks, abilityContext);
    }

    @Override
    public void finish() {
        if (onFinish != null) {
            onFinish.finish(info, abilityContext);
        }
    }

    @Override
    void interrupt(CastInterruptReason reason) {
        super.interrupt(reason);
        ability.interruptCast(executor.entityData, reason, abilityContext);
        PacketHandler.sendToTrackingAndSelf(EntityCastPacket.interrupt(executor.entityData, reason), executor.entityData.getEntity());
    }
}
