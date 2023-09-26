package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogueNode extends DialogueObject {
    private final List<DialogueEffect> effects;

    public DialogueNode(String nodeId, String rawMessage) {
        super(nodeId, rawMessage);
        this.effects = new ArrayList<>();
    }

    public DialogueNode(String nodeId) {
        this(nodeId, EMPTY_MSG);
    }

    public DialogueNode copy() {
        DialogueNode newNode = new DialogueNode(getId(), getRawMessage());
        effects.forEach(e -> newNode.addEffect(e.copy()));
        return newNode;
    }

    public List<DialogueEffect> getEffects() {
        return effects;
    }

    public void addEffect(DialogueEffect effect) {
        this.effects.add(effect);
    }

    public MutableComponent getSpeakerMessage(LivingEntity speaker, ServerPlayer player) {
        DialogueContext context = new DialogueContext(speaker, player, this);
        Component body = context.evaluate(getMessage());
        return DialogueUtils.formatSpeakerMessage(speaker, body);
    }

    public void sendMessage(ServerPlayer player, LivingEntity source) {
        sendMessage(player, source, getSpeakerMessage(source, player));
    }

    public void sendMessageWithSibling(ServerPlayer player, LivingEntity source,
                                       DialoguePrompt withAdditional) {
        MutableComponent message = getSpeakerMessage(source, player)
                .append(" ")
                .append(withAdditional.getPromptLink());

        sendMessage(player, source, message);
    }

    private void sendMessage(ServerPlayer player, LivingEntity source, Component message) {
        if (player.getServer() != null) {
            DialogueUtils.sendMessageToAllAround(source, message);
            for (DialogueEffect effect : effects) {
                effect.applyEffect(player, source, this);
            }
        }
    }

    public static <D> DataResult<DialogueNode> fromDynamic(Dynamic<D> dynamic) {
        Optional<String> name = decodeKey(dynamic);
        if (name.isEmpty()) {
            return DataResult.error(() -> "Failed to decode dialogue node id: " + dynamic);
        }

        DialogueNode prompt = new DialogueNode(name.get());
        prompt.deserialize(dynamic);
        if (prompt.isValid()) {
            return DataResult.success(prompt);
        }
        return DataResult.error(() -> "Unable to decode dialogue node: " + name.get());
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        if (!effects.isEmpty()) {
            builder.put(ops.createString("effects"), ops.createList(effects.stream().map(x -> x.serialize(ops))));
        }
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        effects.clear();
        dynamic.get("effects")
                .asList(DialogueEffect::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(effects::add));
    }
}
