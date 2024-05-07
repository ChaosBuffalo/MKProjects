package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class ItemAbilityGroup extends AbilityGroup {

    public ItemAbilityGroup(MKPlayerData playerData) {
        super(playerData, "item", AbilityGroupId.Item);
    }

    @Override
    protected boolean requiresAbilityKnown() {
        return false;
    }

    @Override
    public int getCurrentSlotCount() {
        // Only report nonzero if the slot is filled
        return !getSlot(0).equals(MKCoreRegistry.INVALID_ABILITY) ? 1 : 0;
    }

    @Nullable
    @Override
    public MKAbilityInfo getAbilityInfo(int index) {
        ResourceLocation abilityId = getSlot(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return null;

        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            return ability.createAbilityInfo();
        }
        return null;
    }
}
