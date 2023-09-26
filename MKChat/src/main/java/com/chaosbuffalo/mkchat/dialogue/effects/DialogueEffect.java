package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class DialogueEffect implements IDynamicMapTypedSerializer {
    private static final String TYPE_ENTRY_NAME = "dialogueEffectType";
    private final ResourceLocation effectType;

    public DialogueEffect(ResourceLocation effectType) {
        this.effectType = effectType;
    }

    public abstract DialogueEffect copy();

    public abstract void applyEffect(ServerPlayer player, LivingEntity source, DialogueNode node);

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    @Override
    public ResourceLocation getTypeName() {
        return effectType;
    }

    public static <D> Optional<ResourceLocation> getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME);
    }

    @Nonnull
    public static <D> DataResult<DialogueEffect> fromDynamic(Dynamic<D> dynamic) {
        Optional<ResourceLocation> type = getType(dynamic);
        if (type.isEmpty()) {
            return DataResult.error(() -> "Failed to decode dialogue effect id: " + dynamic);
        }

        DialogueEffect effect = ChatRegistries.createDialogueEffect(type.get());
        if (effect == null) {
            return DataResult.error(() -> "Unable to decode dialogue effect: " + type.get());
        }
        effect.deserialize(dynamic);
        return DataResult.success(effect);
    }
}
