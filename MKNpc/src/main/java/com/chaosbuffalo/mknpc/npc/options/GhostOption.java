package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GhostOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "ghost");
    public static final Codec<GhostOption> CODEC = RecordCodecBuilder.<GhostOption>mapCodec(builder -> {
        return builder.group(
                Codec.FLOAT.fieldOf("ghost_translucency").forGetter(GhostOption::getGhostTranslucency),
                Codec.BOOL.fieldOf("ghost_armor").forGetter(GhostOption::getGhostArmor),
                Codec.FLOAT.fieldOf("armor_translucency").forGetter(GhostOption::getArmorTranslucency)
        ).apply(builder, GhostOption::new);
    }).codec();

    private float ghostTranslucency;
    private boolean doGhostArmor;
    private float armorTranslucency;

    private GhostOption(float ghostTranslucency, boolean ghostArmor, float armorTranslucency) {
        super(NAME, ApplyOrder.MIDDLE);
        setGhostTranslucency(ghostTranslucency);
        enableGhostArmor(ghostArmor);
        setArmorTranslucency(armorTranslucency);
    }

    public GhostOption() {
        super(NAME, ApplyOrder.MIDDLE);
        setGhostTranslucency(0.7f);
        enableGhostArmor(true);
        setArmorTranslucency(0.7f);
    }

    public GhostOption setGhostTranslucency(float value) {
        this.ghostTranslucency = value;
        return this;
    }

    public GhostOption enableGhostArmor(boolean doGhostArmor) {
        this.doGhostArmor = doGhostArmor;
        return this;
    }

    public GhostOption setArmorTranslucency(float translucency) {
        this.armorTranslucency = translucency;
        return this;
    }

    public float getGhostTranslucency() {
        return ghostTranslucency;
    }

    public boolean getGhostArmor() {
        return doGhostArmor;
    }

    public float getArmorTranslucency() {
        return this.armorTranslucency;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setGhost(true);
            mkEntity.setGhostTranslucency(getGhostTranslucency());
            mkEntity.setGhostArmor(getGhostArmor());
            mkEntity.setGhostArmorTranslucency(getArmorTranslucency());
        }
    }
}
