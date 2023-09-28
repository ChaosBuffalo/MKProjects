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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogueNode extends DialogueObject {
    private final List<DialogueEffect> effects;

    public DialogueNode(String nodeId, String rawMessage) {
        this(nodeId, rawMessage, new ArrayList<>());
    }

    public DialogueNode(String nodeId) {
        this(nodeId, EMPTY_MSG);
    }

    private DialogueNode(String nodeId, String rawMessage, List<DialogueEffect> effects) {
        super(nodeId, rawMessage);
        this.effects = effects;
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

    public static Builder builder(String nodeId) {
        return new Builder(nodeId);
    }

    public static class Builder {
        private final String nodeId;
        private final StringBuilder builder;
        private final List<DialogueEffect> effects;
        private Consumer<DialogueNode> onBuild;

        public Builder(String nodeId) {
            this.nodeId = nodeId;
            builder = new StringBuilder(128);
            effects = new ArrayList<>();
        }

        public Builder onBuild(Consumer<DialogueNode> callback) {
            onBuild = callback;
            return this;
        }

        public Builder effect(DialogueEffect effect) {
            effects.add(effect);
            return this;
        }

        public Builder text(String text) {
            builder.append(text);
            return this;
        }

        public Builder text(String... text) {
            return text(Arrays.asList(text));
        }

        public Builder text(List<String> text) {
            text.forEach(this::text);
            return this;
        }

        public Builder context(String text) {
            builder.append(text);
            return this;
        }

        public Builder prompt(DialoguePrompt prompt) {
            builder.append(prompt.getPromptEmbed());
            return this;
        }

        public DialogueNode build() {
            DialogueNode newNode = new DialogueNode(nodeId, builder.toString(), effects);
            if (onBuild != null) {
                onBuild.accept(newNode);
            }
            return newNode;
        }
    }
}
