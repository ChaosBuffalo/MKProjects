package com.chaosbuffalo.mkcore.utils.text;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class IconTextComponent implements Component {

    private final ResourceLocation icon;
    private final MutableComponent component;

    public IconTextComponent(ResourceLocation icon, String translationKey) {
        this(icon, translationKey, TranslatableContents.NO_ARGS);
    }

    public IconTextComponent(ResourceLocation icon, String translationKey, Object... args) {
        this(icon, Component.translatable(translationKey, args));
    }

    public IconTextComponent(ResourceLocation icon, MutableComponent component) {
        this.component = component;
        this.icon = icon;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public Style getStyle() {
        return component.getStyle();
    }

    @Override
    public ComponentContents getContents() {
        return component.getContents();
    }

    @Override
    public List<Component> getSiblings() {
        return component.getSiblings();
    }

//    @Override
//    public MutableComponent plainCopy() {
//        return new IconTextComponent(icon, component.plainCopy());
//    }

    public IconTextComponent withStyle(Style pStyle) {
        this.component.setStyle(pStyle.applyTo(this.getStyle()));
        return this;
    }

    public IconTextComponent withStyle(ChatFormatting... pFormats) {
        this.component.setStyle(this.getStyle().applyFormats(pFormats));
        return this;
    }

    public IconTextComponent withStyle(ChatFormatting pFormat) {
        this.component.setStyle(this.getStyle().applyFormat(pFormat));
        return this;
    }

    //    public IconTextComponent plainCopy() {
//        return new IconTextComponent(getIcon(), getKey(), getArgs());
//    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return component.getVisualOrderText();
    }
}
