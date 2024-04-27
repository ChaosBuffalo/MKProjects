package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.function.Consumer;

public class DialogueNode extends DialogueObject {
    public static final Codec<DialogueNode> CODEC = RecordCodecBuilder.<DialogueNode>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("nodeId").forGetter(DialogueObject::getId),
                Codec.STRING.fieldOf("message").forGetter(DialogueObject::getRawMessage),
                Codec.list(DialogueEffect.CODEC).optionalFieldOf("effects", Collections.emptyList()).forGetter(i -> i.effects)
        ).apply(builder, DialogueNode::new);
    }).codec();

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
