package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class MobAbilityKnowledge implements IMKAbilityKnowledge, IMKSerializable<CompoundTag> {
    private final MKEntityData entityData;
    private final Map<ResourceLocation, MobKnownAbility> knownAbilities = new HashMap<>();
    private List<MobKnownAbility> priorityOrder = new ArrayList<>();

    public MobAbilityKnowledge(MKEntityData entityData) {
        this.entityData = entityData;
    }

    @Override
    public Collection<MKAbilityInfo> getAllAbilities() {
        return priorityOrder.stream()
                .map(MobKnownAbility::getAbilityInfo)
                .filter(Objects::nonNull)
                .toList();
    }

    public void updatePriorityOrder() {
        Comparator<MobKnownAbility> comp = Comparator.comparingInt(MobKnownAbility::getPriority);
        priorityOrder = knownAbilities.values().stream().sorted(comp).toList();
    }

    public List<MobKnownAbility> getAbilitiesPriorityOrder() {
        return priorityOrder;
    }

    public boolean learnAbility(MKAbility ability, int priority) {
        return learnAbility(ability, priority, null);
    }

    public boolean learnAbility(MKAbility ability, int priority, @Nullable String activationId) {
        MobKnownAbility info = knownAbilities.get(ability.getAbilityId());
        if (info == null) {
            info = new MobKnownAbility(ability.createAbilityInfo(), priority, activationId);
            knownAbilities.put(ability.getAbilityId(), info);
        } else {
            info.setPriority(priority);
            info.setActivationId(activationId);
        }

        updatePriorityOrder();
        return true;
    }

    @Override
    public boolean learnAbility(MKAbility ability, AbilitySource source) {
        return learnAbility(ability, 1);
    }

    public boolean learnAbilityDefinition(ResourceLocation abilityId, int priority, @Nullable String activationId) {
        if (MKCore.getAbilityDefinitionService().getDefinition(abilityId) == null) {
            return false;
        }

        MobKnownAbility info = knownAbilities.get(abilityId);
        if (info == null) {
            info = new MobKnownAbility(abilityId, null, priority, activationId);
            knownAbilities.put(abilityId, info);
        } else {
            info.setPriority(priority);
            info.setActivationId(activationId);
        }

        updatePriorityOrder();
        return true;
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId, AbilitySource source) {
        knownAbilities.remove(abilityId);
        updatePriorityOrder();
        return true;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return knownAbilities.containsKey(abilityId);
    }

    @Override
    public Stream<ResourceLocation> getKnownAbilityIds() {
        return knownAbilities.keySet().stream();
    }

    @Nullable
    @Override
    public MKAbilityInfo getAbilityInfo(ResourceLocation abilityId) {
        MobKnownAbility info = knownAbilities.get(abilityId);
        if (info == null)
            return null;
        return info.getAbilityInfo();
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag abilityInfos = new CompoundTag();
        knownAbilities.forEach((key, value) -> abilityInfos.put(key.toString(), value.serialize(provider)));
        tag.put("abilities", abilityInfos);
        return tag;
    }

    @Override
    public boolean deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("abilities")) {
            CompoundTag abilityInfo = tag.getCompound("abilities");
            for (String key : abilityInfo.getAllKeys()) {
                ResourceLocation abilityId = ResourceLocation.parse(key);
                MobKnownAbility info = createKnownAbility(abilityId);
                if (info != null && info.deserialize(provider, abilityInfo.getCompound(key))) {
                    knownAbilities.put(abilityId, info);
                }
            }
            updatePriorityOrder();
        }

        return true;
    }

    private static MobKnownAbility createKnownAbility(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            return new MobKnownAbility(ability.createAbilityInfo(), 1, null);
        }

        if (MKCore.getAbilityDefinitionService().getDefinition(abilityId) == null) {
            return null;
        }

        return new MobKnownAbility(abilityId, null, 1, null);
    }
}
