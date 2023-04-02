package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.IPlayerDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueUtils;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class HasBoolFlagCondition extends DialogueCondition {
    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKChat.MODID, "dialogue_condition.has_bool_flag");
    private ResourceLocation flagName;

    public HasBoolFlagCondition(ResourceLocation flagName) {
        super(conditionTypeName);
        this.flagName = flagName;
    }

    public HasBoolFlagCondition() {
        this(AddFlag.INVALID_FLAG);
    }

    @Override
    public HasBoolFlagCondition copy() {
        return new HasBoolFlagCondition(flagName);
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        if (flagName.equals(AddFlag.INVALID_FLAG)) {
            return false;
        }
        return IPlayerDialogue.get(player)
                .map(cap -> cap.getConversationMemory(source).getBoolFlag(flagName))
                .orElse(false);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        flagName = dynamic.get("flagName").asString()
                .resultOrPartial(DialogueUtils::throwParseException)
                .map(ResourceLocation::new)
                .orElse(AddFlag.INVALID_FLAG);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("flagName"), ops.createString(flagName.toString()));
    }
}
