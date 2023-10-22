package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.loadout.ItemAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.loadout.PassiveAbilityGroup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class PlayerAbilityLoadout implements IPlayerSyncComponentProvider {

    private final PlayerSyncComponent sync = new PlayerSyncComponent("loadout");

    private final Map<AbilityGroupId, AbilityGroup> abilityGroups = new EnumMap<>(AbilityGroupId.class);
    private final PassiveAbilityGroup passiveAbilityGroup;
    private final AbilityGroup ultimateAbilityGroup;
    private final AbilityGroup basicAbilityGroup;
    private final ItemAbilityGroup itemAbilityGroup;

    public PlayerAbilityLoadout(MKPlayerData playerData) {
        basicAbilityGroup = new AbilityGroup(playerData, "basic", AbilityGroupId.Basic);
        passiveAbilityGroup = new PassiveAbilityGroup(playerData);
        ultimateAbilityGroup = new AbilityGroup(playerData, "ultimate", AbilityGroupId.Ultimate);
        itemAbilityGroup = new ItemAbilityGroup(playerData);
        registerAbilityGroup(AbilityGroupId.Basic, basicAbilityGroup);
        registerAbilityGroup(AbilityGroupId.Item, itemAbilityGroup);
        registerAbilityGroup(AbilityGroupId.Passive, passiveAbilityGroup);
        registerAbilityGroup(AbilityGroupId.Ultimate, ultimateAbilityGroup);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public PassiveAbilityGroup getPassiveAbilityGroup() {
        return passiveAbilityGroup;
    }

    @Nonnull
    public AbilityGroup getAbilityGroup(AbilityGroupId group) {
        return abilityGroups.get(group);
    }

    public Collection<AbilityGroup> getAbilityGroups() {
        return abilityGroups.values();
    }

    private void registerAbilityGroup(AbilityGroupId group, AbilityGroup abilityGroup) {
        abilityGroups.put(group, abilityGroup);
        addSyncChild(abilityGroup);
    }

    public ItemAbilityGroup getItemGroup() {
        return itemAbilityGroup;
    }

    void onAbilityLearned(MKAbilityInfo abilityInfo, AbilitySource source) {
        if (source.placeOnBarWhenLearned()) {
            for (Map.Entry<AbilityGroupId, AbilityGroup> entry : abilityGroups.entrySet()) {
                if (entry.getKey().fitsAbilityType(abilityInfo.getAbilityType()) &&
                        entry.getValue().tryEquip(abilityInfo.getId())) {
                    break;
                }
            }
        }
    }

    void onAbilityUnlearned(MKAbilityInfo abilityInfo) {
        abilityGroups.values().forEach(group -> {
            if (group.isEquipped(abilityInfo)) {
                group.onAbilityUnlearned(abilityInfo);
            }
        });
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

}
