package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class MKTalent {

    public abstract TalentType getTalentType();

    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new TalentNode(this, dynamic);
    }

    @Nonnull
    public ResourceLocation getTalentId() {
        return Objects.requireNonNull(MKCoreRegistry.TALENTS.getKey(this));
    }

    protected String getTalentNameKey(ResourceLocation talentId) {
        return talentId.toLanguageKey("talent", "name");
    }

    public Component getTalentName() {
        return Component.translatable(getTalentNameKey(getTalentId()));
    }

    public MutableComponent getTypeDescription() {
        return getTalentType().getDisplayName();
    }

    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        consumer.accept(getTalentDescription(record).withStyle(ChatFormatting.GRAY));
    }

    protected String getTalentDescriptionKey(ResourceLocation talentId) {
        return talentId.toLanguageKey("talent", "description");
    }

    public MutableComponent getTalentDescription(TalentRecord record) {
        return Component.translatable(getTalentDescriptionKey(getTalentId()));
    }

    public ResourceLocation getIcon() {
        ResourceLocation talentId = getTalentId();
        return talentId.withPath(path -> "textures/talents/" + path + "_icon.png");
    }

    public ResourceLocation getFilledIcon() {
        ResourceLocation talentId = getTalentId();
        return talentId.withPath(path -> "textures/talents/" + path + "_icon_filled.png");
    }
}
