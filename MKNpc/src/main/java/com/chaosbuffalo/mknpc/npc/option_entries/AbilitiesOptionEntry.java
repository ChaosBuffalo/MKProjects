package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOptionEntry implements INpcOptionEntry {

    private final List<NpcAbilityEntry> abilities;

    public AbilitiesOptionEntry(List<NpcAbilityEntry> entries) {
        this.abilities = entries;
    }

    public AbilitiesOptionEntry() {
        this(new ArrayList<>());
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent(cap -> {
                List<ResourceLocation> toUnlearn = new ArrayList<>();
                for (MKAbilityInfo ability : cap.getAbilities().getAllAbilities()) {
                    toUnlearn.add(ability.getId());
                }
                for (ResourceLocation abilityId : toUnlearn) {
                    cap.getAbilities().unlearnAbility(abilityId, AbilitySource.TRAINED);
                }
                for (NpcAbilityEntry entry : abilities) {
                    MKAbilityInfo abilityInfo = entry.getAbilityInfo();
                    if (abilityInfo != null) {
                        cap.getAbilities().learnAbility(abilityInfo, entry.getPriority());
                    }
                }
            });
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag abilitiesList = new ListTag();
        for (NpcAbilityEntry entry : abilities) {
            abilitiesList.add(entry.serializeNBT());
        }
        tag.put("abilities", abilitiesList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag abilitiesList = nbt.getList("abilities", Tag.TAG_COMPOUND);
        abilities.clear();
        for (Tag tag : abilitiesList) {
            NpcAbilityEntry entry = new NpcAbilityEntry();
            entry.deserializeNBT((CompoundTag) tag);
            abilities.add(entry);
        }
    }
}
