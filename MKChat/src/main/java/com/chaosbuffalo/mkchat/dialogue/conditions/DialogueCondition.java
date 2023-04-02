package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class DialogueCondition implements IDynamicMapTypedSerializer {
    public static final String TYPE_ENTRY_NAME = "dialogueConditionType";
    private final ResourceLocation conditionType;
    private boolean invert;

    public DialogueCondition(ResourceLocation conditionType) {
        this.conditionType = conditionType;
        invert = false;
    }

    public abstract boolean meetsCondition(ServerPlayer player, LivingEntity source);

    public boolean checkCondition(ServerPlayer player, LivingEntity source) {
        boolean condition = meetsCondition(player, source);
        MKChat.LOGGER.debug("Player {} meets condition {} {}", player, getTypeName(), invert != condition);
        return invert != condition;
    }

    public DialogueCondition setInvert(boolean invert) {
        this.invert = invert;
        return this;
    }

    public abstract DialogueCondition copy();

    @Override
    public ResourceLocation getTypeName() {
        return conditionType;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        invert = dynamic.get("invert").asBoolean(false);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("invert"), ops.createBoolean(invert));
    }

    public static <D> Optional<ResourceLocation> getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME);
    }

    @Nonnull
    public static <D> DataResult<DialogueCondition> fromDynamic(Dynamic<D> dynamic) {
        Optional<ResourceLocation> type = getType(dynamic);
        if (!type.isPresent()) {
            return DataResult.error(String.format("Failed to decode dialogue condition id: %s", dynamic));
        }

        DialogueCondition cond = DialogueManager.getDialogueCondition(type.get());
        if (cond == null) {
            return DataResult.error(String.format("Unable to decode dialogue condition: %s", type.get()));
        }
        cond.deserialize(dynamic);
        return DataResult.success(cond);
    }
}
