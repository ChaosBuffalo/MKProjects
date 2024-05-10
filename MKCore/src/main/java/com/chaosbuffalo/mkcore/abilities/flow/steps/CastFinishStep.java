package com.chaosbuffalo.mkcore.abilities.flow.steps;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.flow.AbilityFlowStep;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.entity.LivingEntity;

public class CastFinishStep extends AbilityFlowStep {

    private EndCastServerFn serverCallback;

    public interface EndCastServerFn {
        void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context);
    }

    public CastFinishStep() {
    }

    public CastFinishStep onServer(EndCastServerFn cast) {
        this.serverCallback = cast;
        return this;
    }

    @Override
    public CastFinishStep onClient(String tag) {
        super.onClient(tag);
        return this;
    }

    @Override
    public void serverEvaluate(IMKEntityData casterData, MKAbilityInfo abilityInfo, AbilityContext context) {
        if (serverCallback != null) {
            serverCallback.endCast(casterData.getEntity(), casterData, context);
        }
    }
}
