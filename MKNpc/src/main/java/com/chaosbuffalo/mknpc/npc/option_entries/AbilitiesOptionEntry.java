package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOptionEntry implements INpcOptionEntry {
    public static final Codec<AbilitiesOptionEntry> CODEC = Codec.list(NpcAbilityEntry.CODEC).xmap(AbilitiesOptionEntry::new, i -> i.abilities);
    public static final MapCodec<AbilitiesOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NpcAbilityEntry.CODEC.listOf().fieldOf("abilities").forGetter(i -> i.abilities)
    ).apply(builder, AbilitiesOptionEntry::new));

    private final List<NpcAbilityEntry> abilities;

    public AbilitiesOptionEntry(List<NpcAbilityEntry> entries) {
        this.abilities = entries;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            MKCore.getEntitySpecificData(livingEntity).ifPresent((cap) -> {
                List<ResourceLocation> toUnlearn = new ArrayList<>();
                for (MKAbilityInfo ability : cap.getAbilities().getAllAbilities()) {
                    toUnlearn.add(ability.getId());
                }
                for (ResourceLocation loc : toUnlearn) {
                    cap.getAbilities().unlearnAbility(loc, AbilitySource.TRAINED);
                }
                for (NpcAbilityEntry entry : abilities) {
                    MKAbility ability = entry.getAbility();
                    if (ability != null) {
                        cap.getAbilities().learnAbility(ability, entry.getPriority());
                    }
                }
            });
        }
    }

    @Override
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.ABILITIES.get();
    }
}
