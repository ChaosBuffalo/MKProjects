package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableDoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableFloatAttribute;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class RestoreManaOnCastEffect extends BaseAccessoryEffect {

    protected final ScalableDoubleAttribute chance = new ScalableDoubleAttribute("chance", 0.0, 0.0);
    protected final ScalableFloatAttribute percentage = new ScalableFloatAttribute("percentage", 0.0f, 1.0f);

    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "accessory_effect.restore_mana");

    public RestoreManaOnCastEffect() {
        super(NAME, ChatFormatting.AQUA);
        addAttributes(chance, percentage);
    }

    public RestoreManaOnCastEffect(double chanceMin, double chanceMax, float restorePercentageMin, float restorePercentageMax) {
        this();
        this.chance.setValue(chanceMin);
        this.chance.setMin(chanceMin);
        this.chance.setMax(chanceMax);
        this.percentage.setValue(restorePercentageMin);
        this.percentage.setMin(restorePercentageMin);
        this.percentage.setMax(restorePercentageMax);
    }

    public double getChance() {
        return chance.value();
    }

    public void setChance(double chance) {
        this.chance.setValue(chance);
    }

    public float getPercentage() {
        return percentage.value();
    }


    public void setPercentage(float percentage) {
        this.percentage.setValue(percentage);
    }

    @Override
    public void livingCompleteAbility(LivingEntity caster, IMKEntityData entityData, MKAccessory accessory,
                                      ItemStack stack, MKAbility ability) {
        if (!caster.getCommandSenderWorld().isClientSide() && entityData instanceof MKPlayerData) {
            MKPlayerData playerData = (MKPlayerData) entityData;
            double roll = caster.getRandom().nextDouble();
            if (roll >= (1.0 - getChance())) {
                float mana = ability.getManaCost(entityData) * getPercentage();
                playerData.getStats().addMana(mana);
                playerData.getEntity().sendSystemMessage(Component.translatable(
                        "mkweapons.accessory_effect.restore_mana.message",
                        stack.getHoverName()));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.accessory_effect.restore_mana.description",
                    MKAbility.PERCENT_FORMATTER.format(getChance()), MKAbility.PERCENT_FORMATTER.format(getPercentage())));
        }
    }


}
