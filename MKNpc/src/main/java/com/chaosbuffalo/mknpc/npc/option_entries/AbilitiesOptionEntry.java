package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.capabilities.CoreCapabilities;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.options.AbilitiesOption;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOptionEntry implements INpcOptionEntry {
    public static final Codec<AbilitiesOptionEntry> CODEC = Codec.list(NpcAbilityEntry.CODEC).xmap(AbilitiesOptionEntry::new, i -> i.abilities);

    private final List<NpcAbilityEntry> abilities;

    public AbilitiesOptionEntry(List<NpcAbilityEntry> entries) {
        this.abilities = entries;
    }

    @Override
    public ResourceLocation getOptionId() {
        return AbilitiesOption.NAME;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent((cap) -> {
                List<ResourceLocation> toUnlearn = new ArrayList<>();
                for (MKAbilityInfo ability : cap.getAbilities().getAllAbilities()) {
                    toUnlearn.add(ability.getId());
                }
                for (ResourceLocation loc : toUnlearn) {
                    cap.getAbilities().unlearnAbility(loc, AbilitySource.TRAINED);
                }
                for (NpcAbilityEntry entry : abilities) {
                    MKAbility ability = MKCoreRegistry.getAbility(entry.getAbilityId());
                    if (ability != null) {
                        cap.getAbilities().learnAbility(ability, entry.getPriority());
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
            abilitiesList.add(entry.serialize(NbtOps.INSTANCE));
        }
        tag.put("abilities", abilitiesList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag abilitiesList = nbt.getList("abilities", Tag.TAG_COMPOUND);
        abilities.clear();
        for (Tag tag : abilitiesList) {
            NpcAbilityEntry entry = NpcAbilityEntry.deserialize(NbtOps.INSTANCE, tag);
            abilities.add(entry);
        }
    }
}
