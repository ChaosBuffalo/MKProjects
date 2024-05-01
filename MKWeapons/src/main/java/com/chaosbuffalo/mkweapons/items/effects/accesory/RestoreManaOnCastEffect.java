package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableDouble;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableFloat;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.IDifficultyAwareEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class RestoreManaOnCastEffect extends BaseAccessoryEffect implements IDifficultyAwareEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "accessory_effect.restore_mana");
    public static final Codec<RestoreManaOnCastEffect> CODEC = RecordCodecBuilder.<RestoreManaOnCastEffect>mapCodec(builder -> {
        return builder.group(
                ScalableDouble.CODEC.fieldOf("chance").forGetter(i -> i.chance),
                ScalableFloat.CODEC.fieldOf("percentage").forGetter(i -> i.percentage)
        ).apply(builder, RestoreManaOnCastEffect::new);
    }).codec();

    protected final ScalableDouble chance;
    protected final ScalableFloat percentage;

    private RestoreManaOnCastEffect(ScalableDouble chance, ScalableFloat percentage) {
        super(NAME, ChatFormatting.AQUA);
        this.chance = chance;
        this.percentage = percentage;
    }

    public RestoreManaOnCastEffect(double chanceMin, double chanceMax, float restorePercentageMin, float restorePercentageMax) {
        super(NAME, ChatFormatting.AQUA);
        this.chance = new ScalableDouble(chanceMin, chanceMax);
        this.percentage = new ScalableFloat(restorePercentageMin, restorePercentageMax);
    }

    public double getChance() {
        return chance.value();
    }

    public float getPercentage() {
        return percentage.value();
    }

    @Override
    public void livingCompleteAbility(IMKEntityData entityData, MKAccessory accessory,
                                      ItemStack stack, MKAbility ability) {
        if (entityData.isServerSide() && entityData instanceof MKPlayerData playerData) {
            double roll = entityData.getEntity().getRandom().nextDouble();
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
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.accessory_effect.restore_mana.description",
                    MKAbility.PERCENT_FORMATTER.format(getChance()), MKAbility.PERCENT_FORMATTER.format(getPercentage())));
        }
    }

    @Override
    public void tuneEffect(double difficultyPercentage) {
        chance.scale(difficultyPercentage);
        percentage.scale(difficultyPercentage);
    }
}
