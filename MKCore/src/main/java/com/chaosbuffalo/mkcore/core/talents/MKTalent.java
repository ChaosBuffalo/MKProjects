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
import java.util.regex.Pattern;

public abstract class MKTalent {

    public abstract TalentType getTalentType();

    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new TalentNode(this, dynamic);
    }

    @Nonnull
    public ResourceLocation getTalentId() {
        return Objects.requireNonNull(MKCoreRegistry.TALENTS.getKey(this));
    }

    public Component getTalentName() {
        ResourceLocation talentId = getTalentId();
        return Component.translatable(String.format("%s.%s.name", talentId.getNamespace(), talentId.getPath()));
    }

    public MutableComponent getTypeDescription() {
        return getTalentType().getDisplayName();
    }

    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        consumer.accept(getTalentDescription(record).withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getTalentDescription(TalentRecord record) {
        ResourceLocation talentId = getTalentId();
        return Component.translatable(String.format("%s.%s.description", talentId.getNamespace(), talentId.getPath()));
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
