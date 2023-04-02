package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.mojang.serialization.Dynamic;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseItemEffect implements IItemEffect {
    public final static ResourceLocation INVALID_EFFECT_TYPE = new ResourceLocation(MKWeapons.MODID, "weapon_effect.error");
    private final ResourceLocation name;
    protected final ChatFormatting color;
    private static final String TYPE_ENTRY_NAME = "itemEffectType";

    public BaseItemEffect(ResourceLocation name, ChatFormatting color){
        this.name = name;
        this.color = color;
    }

    @Override
    public ResourceLocation getTypeName() {
        return name;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        tooltip.add(new TranslatableComponent(String.format("%s.%s.name",
                this.getTypeName().getNamespace(), this.getTypeName().getPath())).withStyle(color));
    }


    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME).orElse(INVALID_EFFECT_TYPE);
    }
}
