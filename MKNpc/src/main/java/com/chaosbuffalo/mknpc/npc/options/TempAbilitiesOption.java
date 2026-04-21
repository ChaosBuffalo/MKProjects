package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class TempAbilitiesOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("temp_abilities");
    public static final Codec<TempAbilitiesOption> CODEC = Codec.list(NpcAbilityEntry.CODEC).xmap(TempAbilitiesOption::new, i -> i.abilities);
    public static final MapCodec<TempAbilitiesOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NpcAbilityEntry.CODEC.listOf().fieldOf("abilities").forGetter(i -> i.abilities)
    ).apply(builder, TempAbilitiesOption::new));

    private final List<NpcAbilityEntry> abilities;

    private TempAbilitiesOption(List<NpcAbilityEntry> abilities) {
        super(NAME, ApplyOrder.MIDDLE);
        this.abilities = ImmutableList.copyOf(abilities);
    }

    public TempAbilitiesOption() {
        super(NAME, ApplyOrder.MIDDLE);
        abilities = new ArrayList<>();
    }

    protected void addAbilityEntry(NpcAbilityEntry entry) {
        abilities.add(entry);
    }

    public TempAbilitiesOption withAbilityOption(MKAbility ability, int priority, double chance) {
        addAbilityEntry(new NpcAbilityEntry(ability, priority, chance));
        return this;
    }

    public TempAbilitiesOption withAbilityDefinitionOption(ResourceLocation abilityId,
                                                           @org.jetbrains.annotations.Nullable String activationId,
                                                           int priority,
                                                           double chance) {
        addAbilityEntry(new NpcAbilityEntry(abilityId, activationId, priority, chance));
        return this;
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.TEMP_ABILITIES.get();
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof LivingEntity livingEntity) {
            MKCore.getEntitySpecificData(livingEntity).ifPresent((cap) -> {
                List<ResourceLocation> toUnlearn = cap.getAbilities().getKnownAbilityIds().toList();
                for (ResourceLocation loc : toUnlearn) {
                    cap.getAbilities().unlearnAbility(loc, AbilitySource.TRAINED);
                }
                for (NpcAbilityEntry entry : abilities) {
                    MKAbility ability = entry.getAbility();
                    if (entity.getRandom().nextDouble() > entry.getChance()) {
                        continue;
                    }
                    if (ability != null) {
                        cap.getAbilities().learnAbility(ability, entry.getPriority(), entry.getActivationId());
                    } else {
                        cap.getAbilities().learnAbilityDefinition(entry.getAbilityId(), entry.getPriority(),
                                entry.getActivationId());
                    }
                }
            });
        }
    }
}
