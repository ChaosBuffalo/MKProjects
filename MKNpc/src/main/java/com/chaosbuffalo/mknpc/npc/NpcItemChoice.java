package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.MKNpc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class NpcItemChoice {
    public static final Codec<NpcItemChoice> CODEC = RecordCodecBuilder.<NpcItemChoice>mapCodec(builder -> {
        return builder.group(
                CommonCodecs.ITEM_STACK.fieldOf("item").forGetter(i -> i.item),
                Codec.DOUBLE.fieldOf("weight").forGetter(i -> i.weight),
                Codec.FLOAT.fieldOf("dropChance").forGetter(i -> i.dropChance)
        ).apply(builder, NpcItemChoice::new);
    }).codec();

    public final ItemStack item;
    public final double weight;
    public final float dropChance;

    public NpcItemChoice(ItemStack item, double weight, float dropChance) {
        this.item = item.isEmpty() ? item : item.copy();
        this.weight = weight;
        this.dropChance = dropChance;
    }

    public void equip(LivingEntity entity, EquipmentSlot slot) {
        entity.setItemSlot(slot, item.copy());
        if (entity instanceof Mob mobEntity) {
            mobEntity.setDropChance(slot, dropChance);
        }
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
