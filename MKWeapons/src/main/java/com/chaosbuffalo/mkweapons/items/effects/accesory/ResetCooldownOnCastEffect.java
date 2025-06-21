package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableDouble;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.IMKAccessory;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class ResetCooldownOnCastEffect extends BaseAccessoryEffect {
    public static final ResourceLocation NAME = MKWeapons.id("accessory_effect.reset_cooldown");
    public static final MapCodec<ResetCooldownOnCastEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ScalableDouble.MAP_CODEC.fieldOf("chance").forGetter(i -> i.chance),
            Attribute.CODEC.fieldOf("skill").forGetter(i -> i.skill)
    ).apply(builder, ResetCooldownOnCastEffect::new));

    protected final ScalableDouble chance;
    protected final Holder<Attribute> skill;

    private ResetCooldownOnCastEffect(ScalableDouble chance, Holder<Attribute> skill) {
        super(NAME, ChatFormatting.AQUA);
        this.chance = chance;
        this.skill = skill;
    }

    public ResetCooldownOnCastEffect(double chanceMin, double chanceMax, Holder<Attribute> skill) {
        super(NAME, ChatFormatting.AQUA);
        this.chance = new ScalableDouble(chanceMin, chanceMax);
        this.skill = skill;
    }

    public double getChance() {
        return chance.value();
    }

    public Holder<Attribute> getSkill() {
        return skill;
    }


    @Override
    public void livingCompleteAbility(IMKEntityData entityData, IMKAccessory accessory,
                                      ItemStack stack, MKAbility ability) {
        if (entityData.isServerSide() && entityData instanceof MKPlayerData playerData) {
            if (ability.getSkillAttributes().contains(getSkill())) {
                double roll = entityData.getEntity().getRandom().nextDouble();
                if (roll >= (1.0 - getChance())) {
                    playerData.getAbilityExecutor().setCooldown(ability.getAbilityId(), 0);
                    playerData.getEntity().sendSystemMessage(Component.translatable(
                            "mkweapons.accessory_effect.reset_cooldown.message",
                            stack.getHoverName()));
                }
            }

        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.accessory_effect.reset_cooldown.description",
                    MKAbility.PERCENT_FORMATTER.format(getChance()), I18n.get(getSkill().value().getDescriptionId())));
        }
    }

    @Override
    public ResetCooldownOnCastEffect createTunedEffect(double difficultyPercentage) {
        var newProc = chance.copyScaled(difficultyPercentage);
        return new ResetCooldownOnCastEffect(newProc, getSkill());
    }
}
