package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class SkillOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "skills");
    public static final Codec<SkillOption> CODEC = RecordCodecBuilder.<SkillOption>mapCodec(builder -> {
        return builder.group(
                ForgeRegistries.ATTRIBUTES.getCodec().listOf().optionalFieldOf("minor_skills", List.of()).forGetter(i -> sortedList(i.minorSkills)),
                ForgeRegistries.ATTRIBUTES.getCodec().listOf().optionalFieldOf("major_skills", List.of()).forGetter(i -> sortedList(i.majorSkills)),
                ForgeRegistries.ATTRIBUTES.getCodec().listOf().optionalFieldOf("remedial_skills", List.of()).forGetter(i -> sortedList(i.remedialSkills))
        ).apply(builder, SkillOption::new);
    }).codec();

    private final Set<Attribute> minorSkills = new HashSet<>();
    private final Set<Attribute> majorSkills = new HashSet<>();
    private final Set<Attribute> remedialSkills = new HashSet<>();

    private SkillOption(List<Attribute> minor, List<Attribute> major, List<Attribute> remedial) {
        super(NAME, ApplyOrder.EARLY);
        minorSkills.addAll(minor);
        majorSkills.addAll(major);
        remedialSkills.addAll(remedial);
    }

    public SkillOption() {
        super(NAME, ApplyOrder.EARLY);
    }

    private static List<Attribute> sortedList(Collection<Attribute> input) {
        Comparator<Attribute> comp = Comparator.comparing(ForgeRegistries.ATTRIBUTES::getKey);
        return input.stream().sorted(comp).toList();
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof LivingEntity living) {
            AttributeMap manager = living.getAttributes();
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
