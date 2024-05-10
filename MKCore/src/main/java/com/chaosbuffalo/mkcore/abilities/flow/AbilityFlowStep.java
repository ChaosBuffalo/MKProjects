package com.chaosbuffalo.mkcore.abilities.flow;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

import javax.annotation.Nullable;

public class AbilityFlowStep {

    protected String clientTag;

    public AbilityFlowStep onClient(String tag) {

        return this;
    }


    public void serverEvaluate(IMKEntityData casterData, MKAbilityInfo abilityInfo, AbilityContext context) {

    }

    public void clientEvaluate(IMKEntityData casterData, MKAbility ability, int ticks, int totalTicks, @Nullable AbilityClientState state) {
        if (clientTag != null) {
            ability.clientFlowHandler(casterData, clientTag, ticks, totalTicks, state);
        }
    }
}
