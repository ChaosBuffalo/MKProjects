package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RestoreManaOnCastEffect extends BaseAccessoryEffect {
    private double chance;
    private float percentage;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "accessory_effect.restore_mana");

    public RestoreManaOnCastEffect() {
        super(NAME, ChatFormatting.AQUA);
        chance = 0.0;
        percentage = 0.0f;
    }

    public RestoreManaOnCastEffect(double chance, float restorePercentage) {
        this();
        this.chance = chance;
        this.percentage = restorePercentage;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public float getPercentage() {
        return percentage;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("chance"), ops.createDouble(chance));
        builder.put(ops.createString("percentage"), ops.createFloat(percentage));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        chance = dynamic.get("chance").asDouble(0.0);
        percentage = dynamic.get("percentage").asFloat(0.0f);
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    @Override
    public void livingCompleteAbility(LivingEntity caster, IMKEntityData entityData, MKAccessory accessory,
                                      ItemStack stack, MKAbility ability) {
        if (!caster.getCommandSenderWorld().isClientSide() && entityData instanceof MKPlayerData){
            MKPlayerData playerData = (MKPlayerData) entityData;
            double roll = caster.getRandom().nextDouble();
            if (roll >= (1.0 - getChance())){
                float mana = ability.getManaCost(entityData) * getPercentage();
                playerData.getStats().addMana(mana);
                playerData.getEntity().sendMessage(new TranslatableComponent(
                        "mkweapons.accessory_effect.restore_mana.message",
                        stack.getHoverName()), Util.NIL_UUID);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("mkweapons.accessory_effect.restore_mana.description",
                    MKAbility.PERCENT_FORMATTER.format(getChance()), MKAbility.PERCENT_FORMATTER.format(getPercentage())));
        }
    }


}
