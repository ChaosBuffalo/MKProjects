package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public abstract class StatPageBase extends PlayerPageBase {
    protected static final int NEGATIVE_COLOR = 13111115;
    protected static final int POSITIVE_COLOR = 3334475;
    protected static final int BASE_COLOR = 16777215;

    public StatPageBase(MKPlayerData playerData, Component title) {
        super(playerData, title);
    }

    protected MKText getTextForAttribute(MKPlayerData playerData, Holder<Attribute> attr) {
        AttributeInstance attribute = playerData.getEntity().getAttribute(attr);
        MKText textWidget = new MKText(minecraft.font, "").setMultiline(true);
        if (attribute != null) {
            textWidget.setText(() -> {
                double currentValue = attribute.getValue();

                var valueComp = attribute.getAttribute().value().toValueComponent(null, currentValue, TooltipFlag.NORMAL);

                var newComp = Component.translatable(attribute.getAttribute().value().getDescriptionId())
                        .append(": ")
                        .append(valueComp);

                double baseValue = attribute.getBaseValue();
                if (attr.equals(Attributes.ATTACK_SPEED) && minecraft.player != null) {
                    ItemStack itemInHand = minecraft.player.getMainHandItem();
                    if (!itemInHand.isEmpty()) {
                        var modifiers = itemInHand.getAttributeModifiers();
                        baseValue = modifiers.compute(4.0, EquipmentSlot.MAINHAND);
                    }
                }
                if (currentValue < baseValue) {
                    textWidget.setColor(NEGATIVE_COLOR);
                } else if (currentValue > baseValue) {
                    textWidget.setColor(POSITIVE_COLOR);
                } else {
                    textWidget.setColor(BASE_COLOR);
                }
                return newComp;
            });
        } else {
            String newText = String.format("%s: attr not found", I18n.get(attr.value().getDescriptionId()));
            textWidget.setText(newText);
        }

        return textWidget;
    }
}
