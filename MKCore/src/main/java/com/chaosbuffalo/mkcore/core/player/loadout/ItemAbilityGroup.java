package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;

public class ItemAbilityGroup extends AbilityGroup {

    public ItemAbilityGroup(Persona persona) {
        super(persona, "item", AbilityGroupId.Item);
    }

    @Override
    protected boolean requiresAbilityKnown() {
        return false;
    }

    @Override
    public int getCurrentSlotCount() {
        // Only report nonzero if the mainhand slot is filled
        return !getSlot(EquipmentSlot.MAINHAND).equals(MKCoreRegistry.INVALID_ABILITY) ? 1 : 0;
    }

    private int slot2index(EquipmentSlot slot) {
        return slot.ordinal();
    }

    private EquipmentSlot index2slot(int index) {
        return EquipmentSlot.values()[index];
    }

    public ResourceLocation getSlot(EquipmentSlot slot) {
        return getSlot(slot2index(slot));
    }

    public void setSlot(EquipmentSlot slot, MKAbility ability) {
        setSlot(slot2index(slot), ability.getAbilityId());
    }

    public void clearSlot(EquipmentSlot slot) {
        clearSlot(slot2index(slot));
    }

    @Override
    protected void onAbilityAdded(int index, MKAbilityInfo abilityInfo) {
        super.onAbilityAdded(index, abilityInfo);
        MKAbility ability = abilityInfo.getAbility();
        EquipmentSlot slot = index2slot(index);
        if (slot.isArmor() && ability.getType() != AbilityType.Passive) {
            playerData.getAbilities().learnAbility(ability, AbilitySource.forEquipmentSlot(slot));
        }
    }

    @Override
    protected void onAbilityRemoved(int index, MKAbilityInfo abilityInfo) {
        super.onAbilityRemoved(index, abilityInfo);
        MKAbility ability = abilityInfo.getAbility();
        EquipmentSlot slot = index2slot(index);
        if (slot.isArmor() && ability.getType() != AbilityType.Passive) {
            playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forEquipmentSlot(slot));
        }
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

    @Override
    protected boolean clearLockedSlotsOnLoad() {
        return false;
    }
}
