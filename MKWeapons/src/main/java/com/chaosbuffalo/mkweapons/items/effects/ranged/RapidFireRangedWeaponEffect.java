package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RapidFireRangedWeaponEffect extends BaseRangedWeaponEffect{
    private int maxHits;
    private float perHitReduction;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.rapid_fire");

    public RapidFireRangedWeaponEffect(int maxHits, float perHitReduction){
        this();
        this.maxHits = maxHits;
        this.perHitReduction = perHitReduction;
    }

    public RapidFireRangedWeaponEffect(){
        super(NAME, ChatFormatting.DARK_RED);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        maxHits = dynamic.get("maxHits").asInt(5);
        perHitReduction = dynamic.get("perHit").asFloat(.10f);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("maxHits"), ops.createInt(maxHits));
        builder.put(ops.createString("perHit"), ops.createFloat(perHitReduction));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn,
                               List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()){
            tooltip.add(new TextComponent(I18n.get("mkweapons.weapon_effect.rapid_fire.description",
                    perHitReduction * 100.0f, maxHits * perHitReduction * 100.0f)));
        }
    }

    @Override
    public float modifyDrawTime(float inTime, ItemStack item, LivingEntity entity) {
        return MKCore.getEntityData(entity).map(cap -> {
            int totalToReduce = Math.min(cap.getCombatExtension().getCurrentProjectileHitCount(), maxHits);
            float timeReduction = totalToReduce * perHitReduction;
            return (1.0f - timeReduction) * inTime;
        }).orElse(inTime);

    }
}
