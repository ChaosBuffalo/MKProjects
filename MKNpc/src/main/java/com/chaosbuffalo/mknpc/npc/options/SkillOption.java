package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "skills");
    private final Set<Attribute> minorSkills = new HashSet<>();
    private final Set<Attribute> majorSkills = new HashSet<>();
    private final Set<Attribute> remedialSkills = new HashSet<>();

    public SkillOption(ResourceLocation name, ApplyOrder order) {
        super(name, order);
    }

    public SkillOption() {
        super(NAME, ApplyOrder.EARLY);
    }


    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof LivingEntity) {
            AttributeMap manager =((LivingEntity) entity).getAttributes();
            for (Attribute attr : remedialSkills) {
                AttributeInstance instance = manager.getInstance(attr);
                if (instance != null) {
                    instance.setBaseValue(difficultyLevel * .4);
                }
            }
            for (Attribute attr : minorSkills) {
                AttributeInstance instance = manager.getInstance(attr);
                if (instance != null) {
                    instance.setBaseValue(difficultyLevel * .6);
                }
            }
            for (Attribute attr : majorSkills) {
                AttributeInstance instance = manager.getInstance(attr);
                if (instance != null) {
                    instance.setBaseValue(difficultyLevel);
                }
            }
        }
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<Attribute> minor_skill_entries = dynamic.get("minor_skills").asList(d ->
                ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(
                        d.asString("mknpc:invalid_attribute"))));
        minorSkills.clear();
        minorSkills.addAll(minor_skill_entries);
        List<Attribute> major_skill_entries = dynamic.get("major_skills").asList(d ->
                ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(
                        d.asString("mknpc:invalid_attribute"))));
        majorSkills.clear();
        majorSkills.addAll(major_skill_entries);
        List<Attribute> remedial_skill_entries = dynamic.get("remedial_skills").asList(d ->
                ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(
                        d.asString("mknpc:invalid_attribute"))));
        remedialSkills.clear();
        remedialSkills.addAll(remedial_skill_entries);
    }

    private static String skillId(Attribute attribute) {
        return ForgeRegistries.ATTRIBUTES.getKey(attribute).toString();
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("minor_skills"), ops.createList(minorSkills.stream()
                .map(x -> ops.createString(skillId(x)))));
        builder.put(ops.createString("major_skills"), ops.createList(majorSkills.stream()
                .map(x -> ops.createString(skillId(x)))));
        builder.put(ops.createString("remedial_skills"), ops.createList(remedialSkills.stream()
                .map(x -> ops.createString(skillId(x)))));
    }

    public SkillOption addMajorSkill(Attribute skill) {
        majorSkills.add(skill);
        return this;
    }

    public SkillOption addMinorSkill(Attribute skill) {
        minorSkills.add(skill);
        return this;
    }

    public SkillOption addRemedialSkill(Attribute skill) {
        remedialSkills.add(skill);
        return this;
    }
}
