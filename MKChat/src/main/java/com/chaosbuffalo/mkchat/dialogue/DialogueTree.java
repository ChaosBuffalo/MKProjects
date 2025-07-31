package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.*;

public class DialogueTree {
    public static final Codec<ResourceKey<DialogueTree>> KEY_CODEC = ResourceKey.codec(ChatRegistries.DIALOGUE_TREES);
    public static final Codec<DialogueTree> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            KEY_CODEC.fieldOf("dialogueId").forGetter(i -> i.dialogueName),
            DialogueNode.CODEC.listOf().fieldOf("nodes").forGetter(i -> List.copyOf(i.nodes.values())),
            DialoguePrompt.CODEC.listOf().fieldOf("prompts").forGetter(i -> List.copyOf(i.prompts.values())),
            Codec.STRING.optionalFieldOf("hailPromptId").forGetter(i -> Optional.ofNullable(i.hailPromptId))
    ).apply(builder, DialogueTree::new));

    public static final Codec<Holder<DialogueTree>> REFERENCE_CODEC = RegistryFixedCodec.create(ChatRegistries.DIALOGUE_TREES);

    private final ResourceKey<DialogueTree> dialogueName;
    private final Map<String, DialogueNode> nodes;
    private final Map<String, DialoguePrompt> prompts;
    private String hailPromptId;

    private DialogueTree(ResourceKey<DialogueTree> dialogueName, Collection<DialogueNode> nodes, Collection<DialoguePrompt> prompts, Optional<String> hail) {
        this(dialogueName);
        nodes.forEach(this::addNode);
        prompts.forEach(this::addPrompt);
        hail.ifPresent(this::setHailPromptId);
    }

    public DialogueTree(ResourceLocation dialogueName) {
        this.dialogueName = ResourceKey.create(ChatRegistries.DIALOGUE_TREES, dialogueName);
        this.nodes = new HashMap<>();
        this.prompts = new HashMap<>();
        hailPromptId = null;
    }

    public DialogueTree(ResourceKey<DialogueTree> dialogueName) {
        this.dialogueName = dialogueName;
        this.nodes = new HashMap<>();
        this.prompts = new HashMap<>();
        hailPromptId = null;
    }

    public void addNode(DialogueNode node) {
        node.setDialogueTree(this);
        nodes.put(node.getId(), node);
    }

    @Nullable
    public DialogueNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public ResourceLocation getDialogueName() {
        return dialogueName.location();
    }

    @Nullable
    public DialoguePrompt getPrompt(String name) {
        return prompts.get(name);
    }

    public DialogueTree copy(ResourceLocation newTreeId) {
        DialogueTree newTree = new DialogueTree(newTreeId);
        nodes.values().forEach(n -> {
            DialogueNode newNode = n.copy();
            newTree.addNode(newNode);
        });
        prompts.values().forEach(p -> {
            DialoguePrompt newPrompt = p.copy();
            newTree.addPrompt(newPrompt);
        });
        newTree.setHailPromptId(hailPromptId);
        return newTree;
    }

    public DialogueTree copy() {
        return copy(dialogueName.location());
    }

    public void addPrompt(DialoguePrompt prompt) {
        prompt.setDialogueTree(this);
        prompt.getRequiredNodes().forEach(nodeId -> {
            DialogueNode node = getNode(nodeId);
//            if (node == null) {
//                throw new DialogueElementMissingException("Dialogue node '%s' needed by prompt '%s' was missing from tree '%s'", nodeId, prompt.getId(), getDialogueName());
//            }
        });
        prompts.put(prompt.getId(), prompt);
    }

    public Map<String, DialogueNode> getNodes() {
        return nodes;
    }

    public Map<String, DialoguePrompt> getPrompts() {
        return prompts;
    }

    @Nullable
    public DialoguePrompt getHailPrompt() {
        if (hailPromptId == null)
            return null;
        return getPrompt(hailPromptId);
    }

    public void setHailPromptId(@Nullable String hailPromptId) {
        if (hailPromptId == null) {
            this.hailPromptId = null;
            return;
        }
        DialoguePrompt prompt = getPrompt(hailPromptId);
        if (prompt != null) {
            this.hailPromptId = hailPromptId;
        } else {
            throw new DialogueElementMissingException("Hail prompt '%s' not found in tree '%s'", hailPromptId, getDialogueName());
        }
    }

    public void setHailPrompt(DialoguePrompt hailPrompt) {
        if (hailPrompt == null) {
            this.hailPromptId = null;
        } else {
            setHailPromptId(hailPrompt.getId());
        }
    }

    public boolean handlePlayerMessage(ServerPlayer player, String message, LivingEntity speaker) {
        for (DialoguePrompt prompt : prompts.values()) {
            if (prompt.willTriggerFrom(message)) {
                if (prompt.handlePrompt(player, speaker, this, null)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void internalMerge(DialogueTree other) {
        for (DialogueNode node : other.getNodes().values()) {
            addNode(node.copy());
        }

        for (DialoguePrompt prompt : other.getPrompts().values()) {
            DialoguePrompt existing = getPrompt(prompt.getId());
            if (existing == null) {
                addPrompt(prompt.copy());
            } else {
                existing.merge(prompt);
            }
        }

        if (hailPromptId == null && other.hailPromptId != null) {
            setHailPromptId(other.hailPromptId);
        }
    }

    public DialogueTree merge(DialogueTree other) {
        DialogueTree newTree = copy();
        newTree.internalMerge(other);
        return newTree;
    }

    public static Builder builder(ResourceKey<DialogueTree> treeId) {
        return new Builder(treeId);
    }

    public static class Builder {
        private final DialogueTree tree;

        public Builder(ResourceKey<DialogueTree> treeId) {
            tree = new DialogueTree(treeId);
        }

        public DialogueNode.Builder newNode(String nodeId) {
            return DialogueNode.builder(nodeId).onBuild(tree::addNode);
        }

        public Builder addNode(DialogueNode node) {
            tree.addNode(node);
            return this;
        }

        public DialoguePrompt.Builder newPrompt(String promptId) {
            return DialoguePrompt.builder(promptId).onBuild(tree::addPrompt);
        }

        public Builder hail(DialoguePrompt prompt) {
            tree.setHailPrompt(prompt);
            return this;
        }

        public DialogueTree build() {
            return tree;
        }
    }
}
