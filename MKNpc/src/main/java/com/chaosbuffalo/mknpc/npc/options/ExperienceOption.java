package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ExperienceOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "experience");
    public static final Codec<ExperienceOption> CODEC = Codec.INT.xmap(ExperienceOption::new, ExperienceOption::getBonusXp);

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
}
