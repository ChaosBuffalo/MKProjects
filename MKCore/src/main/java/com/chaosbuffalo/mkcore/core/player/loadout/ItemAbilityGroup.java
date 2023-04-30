package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityProvider;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemAbilityGroup extends AbilityGroup {

    public ItemAbilityGroup(MKPlayerData playerData) {
        super(playerData, "item", AbilityGroupId.Item);
    }

    @Override
    public boolean requiresAbilityKnown() {
        return false;
    }

    @Override
    public int getCurrentSlotCount() {
        // Only report nonzero if the slot is filled
        return isSlotFilled(0) ? 1 : 0;
    }

    public void setItemAbility(int index, MKAbilityInfo abilityInfo) {
        if (abilityInfo != null) {
            setSlot(index, abilityInfo.getId());
        } else {
            clearSlot(index);
        }
    }

    @Nullable
    @Override
    public MKAbilityInfo getAbilityInfo(int index) {
        ResourceLocation abilityId = getSlot(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return null;

        if (index == 0) {
            ItemStack stack = playerData.getEntity().getItemBySlot(EquipmentSlot.MAINHAND);
            if (!stack.isEmpty() && stack.getItem() instanceof IMKAbilityProvider provider) {
                return provider.getAbilityInfo(stack);
            }
        }

        return null;
    }
}
