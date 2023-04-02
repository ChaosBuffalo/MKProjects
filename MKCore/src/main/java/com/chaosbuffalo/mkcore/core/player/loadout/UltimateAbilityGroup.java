package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;

public class UltimateAbilityGroup extends AbilityGroup {

    public UltimateAbilityGroup(MKPlayerData playerData) {
        super(playerData, "ultimate", AbilityGroupId.Ultimate);
    }
}
