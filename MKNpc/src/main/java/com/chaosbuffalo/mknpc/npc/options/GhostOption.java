package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GhostOption extends FloatOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "ghost");

    private boolean doGhostArmor;
    private float armorTranslucency;
    public GhostOption() {
        super(NAME);
        setValue(0.7f);
        ghostArmor(true);
        armorTranslucency(0.7f);
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, Float value) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setGhost(true);
            mkEntity.setGhostTranslucency(getValue());
            mkEntity.setGhostArmor(getGhostArmor());
            mkEntity.setGhostArmorTranslucency(getArmorTranslucency());
        }
    }
    public GhostOption ghostArmor(boolean doGhostArmor) {
        this.doGhostArmor = doGhostArmor;
        return this;
    }

    public GhostOption armorTranslucency(float translucency) {
        this.armorTranslucency = translucency;
        return this;
    }

    public boolean getGhostArmor() {
        return doGhostArmor;
    }

    public float getArmorTranslucency() {
        return this.armorTranslucency;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("ghost_armor"), ops.createBoolean(getGhostArmor()));
        builder.put(ops.createString("armor_translucency"), ops.createFloat(getArmorTranslucency()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        setValue(dynamic.get("value").asFloat(0.70f));
        ghostArmor(dynamic.get("ghost_armor").asBoolean(false));
        armorTranslucency(dynamic.get("armor_translucency").asFloat(0.70f));
    }
}
