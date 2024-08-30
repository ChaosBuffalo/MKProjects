package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

import java.util.*;

public class SkillOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("skills");
    public static final MapCodec<SkillOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Attribute.CODEC.listOf().optionalFieldOf("minor_skills", List.of()).forGetter(i -> sortedList(i.minorSkills)),
            Attribute.CODEC.listOf().optionalFieldOf("major_skills", List.of()).forGetter(i -> sortedList(i.majorSkills)),
            Attribute.CODEC.listOf().optionalFieldOf("remedial_skills", List.of()).forGetter(i -> sortedList(i.remedialSkills))
    ).apply(builder, SkillOption::new));

    private final Set<Holder<Attribute>> minorSkills = new HashSet<>();
    private final Set<Holder<Attribute>> majorSkills = new HashSet<>();
    private final Set<Holder<Attribute>> remedialSkills = new HashSet<>();

    private SkillOption(List<Holder<Attribute>> minor, List<Holder<Attribute>> major, List<Holder<Attribute>> remedial) {
        super(NAME, ApplyOrder.EARLY);
        minorSkills.addAll(minor);
        majorSkills.addAll(major);
        remedialSkills.addAll(remedial);
    }

    public SkillOption() {
        super(NAME, ApplyOrder.EARLY);
    }

    private static List<Holder<Attribute>> sortedList(Collection<Holder<Attribute>> input) {
        Comparator<Holder<Attribute>> comp = Comparator.comparing(Holder::getRegisteredName);
        return input.stream().sorted(comp).toList();
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof LivingEntity living) {
            AttributeMap manager = living.getAttributes();
            for (Holder<Attribute> attr : remedialSkills) {
                AttributeInstance instance = manager.getInstance(attr);
                if (instance != null) {
                    instance.setBaseValue(difficultyLevel * .4);
                }
            }
            for (Holder<Attribute> attr : minorSkills) {
                AttributeInstance instance = manager.getInstance(attr);
                if (instance != null) {
                    instance.setBaseValue(difficultyLevel * .6);
                }
            }
            for (Holder<Attribute> attr : majorSkills) {
                AttributeInstance instance = manager.getInstance(attr);
                if (instance != null) {
                    instance.setBaseValue(difficultyLevel);
                }
            }
        }
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.SKILL.get();
    }

    public SkillOption addMajorSkill(Holder<Attribute> skill) {
        majorSkills.add(skill);
        return this;
    }

    public SkillOption addMinorSkill(Holder<Attribute> skill) {
        minorSkills.add(skill);
        return this;
    }

    public SkillOption addRemedialSkill(Holder<Attribute> skill) {
        remedialSkills.add(skill);
        return this;
    }
}
