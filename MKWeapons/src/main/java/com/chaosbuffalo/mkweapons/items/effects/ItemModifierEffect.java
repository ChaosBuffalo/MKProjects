package com.chaosbuffalo.mkweapons.items.effects;

import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemModifierEffect extends BaseItemEffect {
    protected final List<AttributeOptionEntry> modifiers;

    public ItemModifierEffect(ResourceLocation name, ChatFormatting color) {
        super(name, color);
        modifiers = new ArrayList<>();
    }


    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("modifiers"),
                ops.createList(modifiers.stream().map(mod -> mod.serialize(ops))));
    }

    public void addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier){
        modifiers.add(new AttributeOptionEntry(attribute, attributeModifier, attributeModifier.getAmount(), attributeModifier.getAmount()));
    }

    public List<AttributeOptionEntry> getModifiers() {
        return modifiers;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {

    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        List<AttributeOptionEntry> deserialized = dynamic.get("modifiers").asList(dyn -> {
            AttributeOptionEntry entry = new AttributeOptionEntry();
            entry.deserialize(dyn);
            return entry;
        });
        modifiers.clear();
        for (AttributeOptionEntry mod : deserialized){
            if (mod != null){
                modifiers.add(mod);
            }
        }
    }
}
