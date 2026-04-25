package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.core.AbilityDisplayEntry;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.loadout.ItemAbilityGroup;
import com.chaosbuffalo.mkcore.core.player.loadout.PassiveAbilityGroup;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class PlayerAbilityLoadout implements ISyncGroupProvider {
    public record SlottedAbilityLocation(AbilityGroupId groupId, int slotIndex) {
    }

    private final SyncGroup syncGroup = new SyncGroup();

    private final Map<AbilityGroupId, AbilityGroup> abilityGroups = new EnumMap<>(AbilityGroupId.class);
    private final PassiveAbilityGroup passiveAbilityGroup;
    private final AbilityGroup ultimateAbilityGroup;
    private final AbilityGroup basicAbilityGroup;
    private final ItemAbilityGroup itemAbilityGroup;

    public PlayerAbilityLoadout(Persona persona) {
        basicAbilityGroup = new AbilityGroup(persona, AbilityGroupId.Basic);
        passiveAbilityGroup = new PassiveAbilityGroup(persona);
        ultimateAbilityGroup = new AbilityGroup(persona, AbilityGroupId.Ultimate);
        itemAbilityGroup = new ItemAbilityGroup(persona);
        registerAbilityGroup("basic", AbilityGroupId.Basic, basicAbilityGroup);
        registerAbilityGroup("item", AbilityGroupId.Item, itemAbilityGroup);
        registerAbilityGroup("passive", AbilityGroupId.Passive, passiveAbilityGroup);
        registerAbilityGroup("ultimate", AbilityGroupId.Ultimate, ultimateAbilityGroup);
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
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

    private void registerAbilityGroup(String name, AbilityGroupId group, AbilityGroup abilityGroup) {
        abilityGroups.put(group, abilityGroup);
        syncGroup.addChild(name, abilityGroup);
    }

    public ItemAbilityGroup getItemGroup() {
        return itemAbilityGroup;
    }

    public @Nullable SlottedAbilityLocation findEquippedAbilityLocation(ResourceLocation abilityId) {
        for (Map.Entry<AbilityGroupId, AbilityGroup> entry : abilityGroups.entrySet()) {
            int slotIndex = entry.getValue().getAbilitySlot(abilityId);
            if (slotIndex != -1) {
                return new SlottedAbilityLocation(entry.getKey(), slotIndex);
            }
        }
        return null;
    }

    public @Nullable SlottedAbilityLocation previewAutoEquipLocation(ResourceLocation abilityId) {
        SlottedAbilityLocation existing = findEquippedAbilityLocation(abilityId);
        if (existing != null) {
            return existing;
        }

        AbilityGroupId targetGroup = resolveLearnedAbilityGroup(abilityId);
        if (targetGroup == null) {
            return null;
        }

        AbilityGroup group = abilityGroups.get(targetGroup);
        if (group == null) {
            return null;
        }

        int freeSlot = group.findFirstFreeAbilitySlot();
        return freeSlot != -1 ? new SlottedAbilityLocation(targetGroup, freeSlot) : null;
    }

    public @Nullable AbilityGroupId resolveLearnedAbilityGroup(ResourceLocation abilityId) {
        AbilityType abilityType = AbilityDisplayEntry.resolve(abilityId).abilityType();
        if (abilityType == null) {
            return getAbilityDefinitionGroup(abilityId);
        }

        return switch (abilityType) {
            case Basic -> AbilityGroupId.Basic;
            case Passive -> AbilityGroupId.Passive;
            case Ultimate -> AbilityGroupId.Ultimate;
            case Npc, Structure -> null;
        };
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

    void onAbilityDefinitionLearned(ResourceLocation abilityId, AbilitySource source) {
        if (!source.placeOnBarWhenLearned()) {
            return;
        }

        AbilityGroupId targetGroup = resolveLearnedAbilityGroup(abilityId);
        if (targetGroup == null) {
            return;
        }

        AbilityGroup group = abilityGroups.get(targetGroup);
        if (group != null) {
            group.tryEquip(abilityId);
        }
    }

    void onAbilityUnlearned(MKAbilityInfo abilityInfo) {
        for (AbilityGroup group : abilityGroups.values()) {
            if (group.isEquipped(abilityInfo)) {
                group.onAbilityUnlearned(abilityInfo);
            }
        }
    }

    void onAbilityDefinitionUnlearned(ResourceLocation abilityId) {
        for (AbilityGroup group : abilityGroups.values()) {
            group.clearAbility(abilityId);
        }
    }

    private @Nullable AbilityGroupId getAbilityDefinitionGroup(ResourceLocation abilityId) {
        PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);
        if (definition == null) {
            return null;
        }

        return AbilityDisplayEntry.resolveAbilityGroup(definition.definition().data().slotFamily());
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
