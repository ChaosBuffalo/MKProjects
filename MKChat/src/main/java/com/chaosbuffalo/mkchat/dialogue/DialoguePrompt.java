package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DialoguePrompt extends DialogueObject {
    public static final String EMPTY_TRIGGER_PHRASE = "";
    public static final String EMPTY_SUGGESTION_TEXT = "";
    private final List<DialogueResponse> responses;
    private String triggerPhrase;
    private String suggestionFillText;

    public DialoguePrompt(String promptId, String triggerPhrase, String suggestionFillText, String highlightText) {
        super(promptId, highlightText);
        this.triggerPhrase = triggerPhrase;
        this.suggestionFillText = suggestionFillText;
        this.responses = new ArrayList<>();
    }

    public DialoguePrompt(String promptId) {
        this(promptId, EMPTY_TRIGGER_PHRASE, EMPTY_SUGGESTION_TEXT, EMPTY_MSG);
    }

    public DialoguePrompt addResponse(DialogueResponse response) {
        responses.add(response);
        return this;
    }

    public List<DialogueResponse> getResponses() {
        return responses;
    }

    public DialoguePrompt copy() {
        DialoguePrompt newPrompt = new DialoguePrompt(getId());
        responses.forEach(r -> newPrompt.addResponse(r.copy()));
        return newPrompt;
    }

    public void merge(DialoguePrompt other) {
        for (DialogueResponse resp : other.getResponses()) {
            addResponse(resp);
        }
    }

    public String getTriggerPhrase() {
        return triggerPhrase;
    }

    public String getSuggestion() {
        return suggestionFillText;
    }

    public Component getHighlightedText() {
        return getMessage();
    }

    public boolean willTriggerFrom(String input) {
        return !StringUtil.isNullOrEmpty(triggerPhrase) && input.contains(triggerPhrase);
    }

    public boolean willHandle(ServerPlayer player, LivingEntity source) {
        for (DialogueResponse response : responses) {
            if (response.doesMatchConditions(player, source)) {
                return true;
            }
        }
        MKChat.LOGGER.debug("No responses meet conditions for dialogue for player {}", player);
        return false;
    }

    public boolean handlePrompt(ServerPlayer player, LivingEntity source, DialogueTree tree,
                                @Nullable DialoguePrompt withAdditional) {
        for (DialogueResponse response : responses) {
            if (response.doesMatchConditions(player, source)) {
                DialogueNode responseNode = tree.getNode(response.getResponseNodeId());
                if (responseNode != null) {
                    if (withAdditional != null) {
                        responseNode.sendMessageWithSibling(player, source, withAdditional);
                    } else {
                        responseNode.sendMessage(player, source);
                    }
                    return true;
                } else {
                    throw new DialogueElementMissingException("Node '%s' was not found. Needed by prompt '%s' in tree '%s'",
                            response.getResponseNodeId(), getId(), tree.getDialogueName());
                }
            }
        }
        return false;
    }

    public String getPromptEmbed() {
        return String.format("{prompt:%s}", getId());
    }

    public Component getPromptLink() {
        return Component.literal("[")
                .append(getHighlightedText())
                .append("]")
                .withStyle(ChatFormatting.AQUA)
                .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getSuggestion())));
    }

    public Stream<String> getRequiredNodes() {
        return responses.stream().map(DialogueResponse::getResponseNodeId);
    }

    public static <D> DataResult<DialoguePrompt> fromDynamic(Dynamic<D> dynamic) {
        Optional<String> nameResult = decodeKey(dynamic);
        if (nameResult.isEmpty()) {
            return DataResult.error(() -> "Failed to decode dialogue response id");
        }

        DialoguePrompt prompt = new DialoguePrompt(nameResult.get());
        prompt.deserialize(dynamic);
        if (prompt.isValid()) {
            return DataResult.success(prompt);
        }
        return DataResult.error(() -> "Unable to decode dialogue prompt: " + nameResult.get());
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("triggerPhrase"), ops.createString(triggerPhrase));
        builder.put(ops.createString("suggestedText"), ops.createString(suggestionFillText));
        if (!responses.isEmpty()) {
            builder.put(ops.createString("responses"), ops.createList(responses.stream().map(x -> x.serialize(ops))));
        }
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        triggerPhrase = dynamic.get("triggerPhrase").asString()
                .resultOrPartial(DialogueUtils::throwParseException)
                .orElseThrow(IllegalStateException::new);
        suggestionFillText = dynamic.get("suggestedText").asString()
                .resultOrPartial(DialogueUtils::throwParseException)
                .orElseThrow(IllegalStateException::new);
        responses.clear();
        dynamic.get("responses").asList(DialogueResponse::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(responses::add));
    }

    public static Builder builder(String promptId) {
        return new Builder(promptId);
    }

    private void validate() {
        if (!getSuggestion().contains(getTriggerPhrase())) {
            throw new DialogueElementMissingException(String.format("Prompt '%s' failed to validate: Trigger '%s' was " +
                    "not found in suggestion '%s'", getId(), getTriggerPhrase(), getSuggestion()));
        }
    }

    public static class Builder {
        private final DialoguePrompt prompt;
        private Consumer<DialoguePrompt> onBuild;

        public Builder(String promptId) {
            prompt = new DialoguePrompt(promptId);
        }

        public Builder onBuild(Consumer<DialoguePrompt> callback) {
            onBuild = callback;
            return this;
        }

        public DialoguePrompt build() {
            prompt.validate();
            if (onBuild != null) {
                onBuild.accept(prompt);
            }
            return prompt;
        }

        public Builder trigger(String trigger) {
            prompt.triggerPhrase = trigger;
            return this;
        }

        public Builder suggest(String suggestion) {
            prompt.suggestionFillText = suggestion;
            return this;
        }

        public Builder highlight(String text) {
            prompt.setRawMessage(text);
            return this;
        }

        public Builder respondWith(DialogueResponse response) {
            prompt.addResponse(response);
            return this;
        }

        public Builder respondWith(DialogueNode response) {
            prompt.addResponse(new DialogueResponse(response));
            return this;
        }
    }
}
