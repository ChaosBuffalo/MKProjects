package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TempAbilitiesOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "temp_abilities");
    private final List<NpcAbilityEntry> abilities;

    public TempAbilitiesOption() {
        super(NAME, ApplyOrder.MIDDLE);
        abilities = new ArrayList<>();
    }

    protected void addAbilityEntry(NpcAbilityEntry entry) {
        abilities.add(entry);
    }

    public TempAbilitiesOption withAbilityOption(MKAbility ability, int priority, double chance) {
        addAbilityEntry(new NpcAbilityEntry(ability.getAbilityId(), priority, chance));
        return this;
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent((cap) -> {
                List<ResourceLocation> toUnlearn = new ArrayList<>();
                for (MKAbilityInfo ability : cap.getKnowledge().getAllAbilities()) {
                    toUnlearn.add(ability.getId());
                }
                for (ResourceLocation loc : toUnlearn) {
                    cap.getKnowledge().getAbilityKnowledge().unlearnAbility(loc, AbilitySource.TRAINED);
                }
                for (NpcAbilityEntry entry : abilities) {
                    MKAbility ability = MKCoreRegistry.getAbility(entry.getAbilityName());
                    if (ability != null && ((LivingEntity) entity).getRandom().nextDouble() <= entry.getChance()) {
                        cap.getKnowledge().learnAbility(ability, entry.getPriority());
                    }
                }
            });
        }
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("options"),
                ops.createList(abilities.stream().map(x -> x.serialize(ops))));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<NpcAbilityEntry> entries = dynamic.get("options").asList(d -> {
            NpcAbilityEntry entry = new NpcAbilityEntry();
            entry.deserialize(d);
            return entry;
        });
        abilities.clear();
        abilities.addAll(entries);
    }
}
