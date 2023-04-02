package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.loadout.ItemAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.loadout.PassiveAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.loadout.UltimateAbilityGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

public class PlayerAbilityLoadout implements IPlayerSyncComponentProvider {

    private final SyncComponent sync = new SyncComponent("loadout");

    private final Map<AbilityGroupId, AbilityGroup> abilityGroups = new EnumMap<>(AbilityGroupId.class);
    private final PassiveAbilityGroup passiveAbilityGroup;
    private final UltimateAbilityGroup ultimateAbilityGroup;
    private final BasicAbilityGroup basicAbilityGroup;
    private final ItemAbilityGroup itemAbilityGroup;

    public PlayerAbilityLoadout(MKPlayerData playerData) {
        basicAbilityGroup = new BasicAbilityGroup(playerData);
        passiveAbilityGroup = new PassiveAbilityGroup(playerData);
        ultimateAbilityGroup = new UltimateAbilityGroup(playerData);
        itemAbilityGroup = new ItemAbilityGroup(playerData);
        registerAbilityGroup(AbilityGroupId.Basic, basicAbilityGroup);
        registerAbilityGroup(AbilityGroupId.Item, itemAbilityGroup);
        registerAbilityGroup(AbilityGroupId.Passive, passiveAbilityGroup);
        registerAbilityGroup(AbilityGroupId.Ultimate, ultimateAbilityGroup);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    @Nonnull
    public AbilityGroup getAbilityGroup(AbilityGroupId group) {
        return abilityGroups.get(group);
    }

    private void registerAbilityGroup(AbilityGroupId group, AbilityGroup abilityGroup) {
        abilityGroups.put(group, abilityGroup);
        addSyncChild(abilityGroup);
    }

    public ResourceLocation getAbilityInSlot(AbilityGroupId group, int slot) {
        return getAbilityGroup(group).getSlot(slot);
    }

    public PassiveAbilityGroup getPassiveGroup() {
        return passiveAbilityGroup;
    }

    public UltimateAbilityGroup getUltimateGroup() {
        return ultimateAbilityGroup;
    }

    public ItemAbilityGroup getItemGroup() {
        return itemAbilityGroup;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("basic", basicAbilityGroup.serializeNBT());
        tag.put("passive", passiveAbilityGroup.serializeNBT());
        tag.put("ultimate", ultimateAbilityGroup.serializeNBT());
        tag.put("item", itemAbilityGroup.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        basicAbilityGroup.deserializeNBT(tag.get("basic"));
        passiveAbilityGroup.deserializeNBT(tag.get("passive"));
        ultimateAbilityGroup.deserializeNBT(tag.get("ultimate"));
        itemAbilityGroup.deserializeNBT(tag.get("item"));
    }

    public void onJoinWorld() {
        abilityGroups.values().forEach(AbilityGroup::onJoinWorld);
    }

    public void onPersonaActivated() {
        abilityGroups.values().forEach(AbilityGroup::onPersonaActivated);
    }

    public void onPersonaDeactivated() {
        abilityGroups.values().forEach(AbilityGroup::onPersonaDeactivated);
    }

    public static class BasicAbilityGroup extends AbilityGroup {

        public BasicAbilityGroup(MKPlayerData playerData) {
            super(playerData, "basic", AbilityGroupId.Basic);
        }
    }

}
