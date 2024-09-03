package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueResponse;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogueBuilder {
    private final Map<String, String> nodeTemplates;
    private final Map<String, String> contextAliases;
    private final Map<String, List<DialogueEffect>> nodeEffects;

    public static class DialogueBuilderResult {
        public final Map<String, DialogueNode> nodes;
        public final Map<String, DialoguePrompt> prompts;

        public DialogueBuilderResult(Map<String, DialogueNode> nodes, Map<String, DialoguePrompt> prompts) {
            this.nodes = nodes;
            this.prompts = prompts;
        }
        public void populateStart(QuestDefinition def, String startResponse) {
            def.setupStartQuestResponse(nodes.get(startResponse), prompts.get(startResponse));
            def.addHailResponse(nodes.get("hail"));
            for (var node : nodes.values()) {
                if (!node.getId().equals("hail")) {
                    def.addStartNode(node);
                }
            }
            for (var prompt : prompts.values()) {
                if (!prompt.getId().equals("hail")) {
                    def.addStartPrompt(prompt);
                }
            }
        }

        public void populateTalkObjective(TalkToNpcObjective objective, boolean immediateComplete, Quest quest) {
            DialogueNode hailNode = nodes.get("hail");
            if (immediateComplete) {
                hailNode.addEffect(new ObjectiveCompleteEffect(objective.getObjectiveName(), quest.getQuestName()));
            }
            objective.withHailResponse(hailNode, new DialogueResponse(hailNode.getId()));
            for (var node : nodes.values()) {
                if (!node.getId().equals("hail")) {
                    objective.withAdditionalNode(node);
                }
            }
            for (var prompt : prompts.values()) {
                if (!prompt.getId().equals("hail")) {
                    objective.withAdditionalPrompts(prompt);
                }
            }
        }
    }

    public DialogueBuilder() {
        nodeTemplates = new HashMap<>();
        contextAliases = new HashMap<>();
        nodeEffects = new HashMap<>();
    }

    public DialogueBuilderResult build() {
        Map<String, DialogueNode> nodes = new HashMap<>();
        Map<String, DialoguePrompt> prompts = new HashMap<>();
        prompts.put("hail", new DialoguePrompt("hail"));
        for (var entry : nodeTemplates.entrySet()) {
            String start = entry.getValue();
            Matcher conM = Pattern.compile("\\{(.*?)}").matcher(start);
            String contexts = conM.replaceAll((res) -> contextAliases.get(res.group(1)));
            Matcher m = Pattern.compile("\\[(.*?)]").matcher(contexts);
            while (m.find()) {
                String promptContent = m.group(1);
                var parts = promptContent.split("\\|");
                DialoguePrompt prompt;
                String promptId;
                if (parts.length == 1) {
                    promptId = parts[0];
                    prompt = new DialoguePrompt(parts[0], parts[0], parts[0], parts[0]);
                } else if (parts.length == 2) {
                    promptId = parts[0];
                    prompt = new DialoguePrompt(parts[0], parts[0], parts[1], parts[0]);
                } else if (parts.length == 3) {
                    promptId = parts[1];
                    prompt = new DialoguePrompt(parts[1], parts[1], parts[2], parts[0]);
                } else {
                    throw new IllegalArgumentException("Invalid prompt format: " + promptContent);
                }
                prompt.addResponse(new DialogueResponse(promptId));
                prompts.put(promptId, prompt);
            }
            String nodeText = m.replaceAll((res) -> {
                var parts = res.group(1).split("\\|");
                if (parts.length == 2 || parts.length == 1) {
                    return String.format("{prompt:%s}", parts[0]);
                } else if (parts.length == 3) {
                    return String.format("{prompt:%s}", parts[1]);
                } else {
                    throw new IllegalArgumentException("Invalid prompt format: " + res.group(1));
                }
            });
            DialogueNode node = new DialogueNode(entry.getKey(), nodeText);
            if (nodeEffects.containsKey(entry.getKey())) {
                for (var effect : nodeEffects.get(entry.getKey())) {
                    node.addEffect(effect);
                }
            }
            nodes.put(entry.getKey(), node);
        }
        return new DialogueBuilderResult(nodes, prompts);
    }

    public DialogueBuilder node(String key, String nodeText) {
        nodeTemplates.put(key, nodeText);
        return this;
    }

    public DialogueBuilder effectNode(String key, String nodeText, DialogueEffect... effects) {
        node(key, nodeText);
        nodeEffects.put(key, Arrays.asList(effects));
        return this;
    }

    public DialogueBuilder context(String key, String contextKey) {
        contextAliases.put(key, contextKey);
        return this;
    }

    public static DialogueBuilder hail(String hail) {
        DialogueBuilder builder = new DialogueBuilder();
        return builder.node("hail", hail);
    }




}
