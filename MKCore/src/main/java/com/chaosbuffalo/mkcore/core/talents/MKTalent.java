package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class MKTalent extends ForgeRegistryEntry<MKTalent> {

    public abstract TalentType<?> getTalentType();

    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new TalentNode(this, dynamic);
    }

    @Nonnull
    public ResourceLocation getTalentId() {
        return Objects.requireNonNull(MKCoreRegistry.TALENTS.getKey(this));
    }

    public Component getTalentName() {
        ResourceLocation talentId = getTalentId();
        return new TranslatableComponent(String.format("%s.%s.name", talentId.getNamespace(), talentId.getPath()));
    }

    public Component getTalentDescription(TalentRecord record) {
        ResourceLocation talentId = getTalentId();
        TranslatableComponent comp = new TranslatableComponent(String.format("%s.%s.description", talentId.getNamespace(), talentId.getPath()));
        return comp.withStyle(ChatFormatting.GRAY);
    }

    public Component getTalentTypeName() {
        return getTalentType().getDisplayName().withStyle(ChatFormatting.GOLD);
    }

    public ResourceLocation getIcon() {
        ResourceLocation talentId = getTalentId();
        return new ResourceLocation(talentId.getNamespace(),
                String.format("textures/talents/%s_icon.png",
                        talentId.getPath().split(Pattern.quote("."))[1]));
    }

    public ResourceLocation getFilledIcon() {
        ResourceLocation talentId = getTalentId();
        return new ResourceLocation(talentId.getNamespace(),
                String.format("textures/talents/%s_icon_filled.png",
                        talentId.getPath().split(Pattern.quote("."))[1]));
    }
}
