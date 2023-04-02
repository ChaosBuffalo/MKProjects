package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.IMKEntityKnowledge;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

public class EntityAbilityKnowledge implements IMKEntityKnowledge, IMKAbilityKnowledge, IMKSerializable<CompoundTag> {
    private final MKEntityData entityData;
    private final Map<ResourceLocation, MKAbilityInfo> abilityInfoMap = new HashMap<>();
    private final Map<ResourceLocation, Integer> abilityPriorities = new HashMap<>();
    private List<MKAbilityInfo> priorityOrder = new ArrayList<>();

    public EntityAbilityKnowledge(MKEntityData entityData) {
        this.entityData = entityData;
    }

    @Override
    public IMKAbilityKnowledge getAbilityKnowledge() {
        return this;
    }

    @Override
    public Collection<MKAbilityInfo> getAllAbilities() {
        return Collections.unmodifiableCollection(abilityInfoMap.values());
    }

    public void updatePriorityOrder() {
        priorityOrder = new ArrayList<>(getAllAbilities());
        priorityOrder.sort(Comparator.comparingInt((x) -> abilityPriorities.getOrDefault(x.getId(), 1)));
    }

    public List<MKAbilityInfo> getAbilitiesPriorityOrder() {
        return priorityOrder;
    }

    private boolean learnAbilityInternal(MKAbility ability, AbilitySource source) {
        MKAbilityInfo info = abilityInfoMap.get(ability.getAbilityId());
        if (info != null && info.isCurrentlyKnown()) {
            if (info.hasSource(source)) {
                // Already knows this ability from this source
                return true;
            }
            MKCore.LOGGER.warn("Entity {} updated known ability {} with new source {}", entityData.getEntity(), info, source);
            info.addSource(source);
            return true;
        }

        info = ability.createAbilityInfo();
        info.addSource(source);

        abilityInfoMap.put(ability.getAbilityId(), info);
        return true;
    }

    public boolean learnAbility(MKAbility ability, int priority) {
        boolean ret = learnAbilityInternal(ability, AbilitySource.TRAINED);
        if (ret) {
            abilityPriorities.put(ability.getAbilityId(), priority);
            updatePriorityOrder();
        }
        return ret;
    }

    public boolean learnAbility(MKAbility ability) {
        return learnAbility(ability, AbilitySource.TRAINED);
    }

    @Override
    public boolean learnAbility(MKAbility ability, AbilitySource source) {
        return learnAbility(ability, 1);
    }

    @Override
    public boolean unlearnAbility(ResourceLocation abilityId, AbilitySource source) {
        abilityInfoMap.remove(abilityId);
        abilityPriorities.remove(abilityId);
        updatePriorityOrder();
        return true;
    }

    @Override
    public boolean knowsAbility(ResourceLocation abilityId) {
        return abilityInfoMap.containsKey(abilityId);
    }

    @Nullable
    @Override
    public MKAbilityInfo getKnownAbility(ResourceLocation abilityId) {
        MKAbilityInfo info = abilityInfoMap.get(abilityId);
        if (info == null)
            return null;
        return info;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        CompoundTag abilityInfos = new CompoundTag();
        abilityInfoMap.forEach((key, value) -> abilityInfos.put(key.toString(), value.serialize()));
        tag.put("abilities", abilityInfos);
        CompoundTag priorities = new CompoundTag();
        abilityPriorities.forEach((key, value) -> priorities.putInt(key.toString(), value));
        tag.put("priorities", priorities);
        return tag;
    }

    private static MKAbilityInfo createAbilityInfo(ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null)
            return null;

        return ability.createAbilityInfo();
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        if (tag.contains("abilities")) {
            CompoundTag abilityInfo = tag.getCompound("abilities");
            for (String key : abilityInfo.getAllKeys()) {
                ResourceLocation loc = new ResourceLocation(key);
                MKAbilityInfo info = createAbilityInfo(loc);
                if (info != null) {
                    if (info.deserialize(abilityInfo.getCompound(key))) {
                        abilityInfoMap.put(loc, info);
                    }
                }
            }
        }
        if (tag.contains("priorities")) {
            CompoundTag priorityInfo = tag.getCompound("priorities");
            for (String key : priorityInfo.getAllKeys()) {
                ResourceLocation loc = new ResourceLocation(key);
                abilityPriorities.put(loc, priorityInfo.getInt(key));
            }
            updatePriorityOrder();
        }

        return true;
    }
}
