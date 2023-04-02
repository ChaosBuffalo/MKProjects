package com.chaosbuffalo.mkchat.dialogue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DialogueTree {
    private final ResourceLocation dialogueName;
    private final Map<String, DialogueNode> nodes;
    private final Map<String, DialoguePrompt> prompts;
    private String hailPromptId;

    public DialogueTree(ResourceLocation dialogueName) {
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
        return dialogueName;
    }

    @Nullable
    public DialoguePrompt getPrompt(String name) {
        return prompts.get(name);
    }

    public DialogueTree copy() {
        DialogueTree newTree = new DialogueTree(dialogueName);
        Tag nbt = serialize(NbtOps.INSTANCE);
        newTree.deserialize(new Dynamic<>(NbtOps.INSTANCE, nbt));
        return newTree;
    }

    public void addPrompt(DialoguePrompt prompt) {
        prompt.setDialogueTree(this);
        prompt.getRequiredNodes().forEach(nodeId -> {
            DialogueNode node = getNode(nodeId);
            if (node == null) {
                throw new DialogueElementMissingException("Dialogue node '%s' needed by prompt '%s' was missing from tree '%s'", nodeId, prompt.getId(), getDialogueName());
            }
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

    public void setHailPromptId(String hailPromptId) {
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

    private <T extends DialogueObject, D> D serializeList(DynamicOps<D> ops, Map<String, T> nodes) {
        ImmutableList.Builder<D> builder = ImmutableList.builder();
        nodes.forEach((key, value) -> builder.add(value.serialize(ops)));
        return ops.createList(builder.build().stream());
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("nodes"), serializeList(ops, nodes));
        builder.put(ops.createString("prompts"), serializeList(ops, prompts));

        if (getHailPrompt() != null) {
            builder.put(ops.createString("hailPrompt"), ops.createString(getHailPrompt().getId()));
        }
        return ops.createMap(builder.build());
    }

    public static <D> DialogueTree deserializeTreeFromDynamic(ResourceLocation name, Dynamic<D> dynamic) {
        DialogueTree tree = new DialogueTree(name);
        tree.deserialize(dynamic);
        return tree;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        nodes.clear();
        dynamic.get("nodes").asList(DialogueNode::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(this::addNode));

        prompts.clear();
        dynamic.get("prompts").asList(DialoguePrompt::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(this::addPrompt));

        // Optional
        dynamic.get("hailPrompt").asString()
                .result()
                .ifPresent(this::setHailPromptId);
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
}
