package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.capabilities.IPlayerDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialogueUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class AddFlag extends DialogueEffect {
    public static ResourceLocation effectTypeName = new ResourceLocation(MKChat.MODID, "dialogue_effect.add_flag");
    public static ResourceLocation INVALID_FLAG = new ResourceLocation(MKChat.MODID, "invalid_flag");
    private ResourceLocation flagName;

    public AddFlag(ResourceLocation flagName) {
        super(effectTypeName);
        this.flagName = flagName;
    }

    public AddFlag() {
        this(INVALID_FLAG);
    }

    @Override
    public AddFlag copy() {
        // No runtime mutable state
        return new AddFlag(flagName);
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity source, DialogueNode node) {
        if (flagName.equals(INVALID_FLAG)) {
            return;
        }
        IPlayerDialogue.get(player).ifPresent(cap -> cap.getConversationMemory(source).setBoolFlag(flagName, true));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        flagName = dynamic.get("flagName").asString()
                .resultOrPartial(DialogueUtils::throwParseException)
                .map(ResourceLocation::new)
                .orElse(INVALID_FLAG);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("flagName"), ops.createString(flagName.toString()));
    }
}
