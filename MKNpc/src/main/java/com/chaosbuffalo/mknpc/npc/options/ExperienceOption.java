package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ExperienceOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("experience");
    public static final Codec<ExperienceOption> CODEC = Codec.INT.xmap(ExperienceOption::new, ExperienceOption::getBonusXp);
    public static final MapCodec<ExperienceOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("experience").forGetter(i -> i.bonusXp)
    ).apply(builder, ExperienceOption::new));

    private final int bonusXp;

    public ExperienceOption(int bonusXp) {
        super(NAME, ApplyOrder.MIDDLE);
        this.bonusXp = bonusXp;
    }

    public int getBonusXp() {
        return bonusXp;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        MKNpc.getNpcData(entity).ifPresent(cap -> cap.setBonusXp(bonusXp));
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.EXPERIENCE.get();
    }
}
