package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.loadout.ItemAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.loadout.PassiveAbilityGroup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class PlayerAbilityLoadout implements IPlayerSyncComponentProvider {
    public static final UUID EV_ID = UUID.fromString("a44c1f13-50d8-427f-865e-00bb4daf6931");

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

        playerData.events().subscribe(PlayerEvents.ABILITY_LEARNED, EV_ID, this::onAbilityLearn);
        playerData.events().subscribe(PlayerEvents.ABILITY_UNLEARNED, EV_ID, this::onAbilityUnlearn);
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

    private void onAbilityLearn(PlayerEvents.AbilityLearnEvent event) {
        MKAbilityInfo abilityInfo = event.getAbilityInfo();
        if (event.getSource().placeOnBarWhenLearned()) {
            for (Map.Entry<AbilityGroupId, AbilityGroup> entry : abilityGroups.entrySet()) {
                if (entry.getKey().fitsAbilityType(abilityInfo.getAbilityType()) &&
                        entry.getValue().tryEquip(abilityInfo.getId())) {
                    break;
                }
            }
        }
    }

    private void onAbilityUnlearn(PlayerEvents.AbilityUnlearnEvent event) {
        MKAbilityInfo abilityInfo = event.getAbilityInfo();
        for (AbilityGroup group : abilityGroups.values()) {
            if (group.isEquipped(abilityInfo)) {
                group.onAbilityUnlearned(abilityInfo);
            }
        }
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

    public void onPersonaActivated() {
        abilityGroups.values().forEach(AbilityGroup::onPersonaActivated);
    }

    public void onPersonaDeactivated() {
        abilityGroups.values().forEach(AbilityGroup::onPersonaDeactivated);
    }

}
