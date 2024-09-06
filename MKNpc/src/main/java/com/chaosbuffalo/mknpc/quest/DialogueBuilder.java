package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueResponse;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.objectives.TalkToNpcObjective;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogueBuilder {
    private final Map<String, String> nodeTemplates;
    private final Map<String, String> contextAliases;
    private final Map<String, List<DialogueEffect>> nodeEffects;
    private final Map<String, DialogueResponse> hailResponses;
    private final Map<String, Boolean> shouldCompletes;
    private final List<String> hailNodes;

    private static String sanitize_id(String id) {
        return id.replaceAll(" ", "_").replaceAll("[^\\w-.]", "");
    }

    public static class DialogueBuilderResult {
        public final Map<String, DialogueNode> nodes;
        public final Map<String, DialoguePrompt> prompts;
        public final Set<String> hailNodes;
        public final Map<String, Boolean> shouldCompletes;
        public final Map<String, DialogueResponse> hailResponses;

        public DialogueBuilderResult(Map<String, DialogueNode> nodes, Map<String, DialoguePrompt> prompts,
                                     Set<String> hailNodes, Map<String, Boolean> shouldCompletes, Map<String, DialogueResponse> hailResponses) {
            this.nodes = nodes;
            this.prompts = prompts;
            this.hailNodes = hailNodes;
            this.shouldCompletes = shouldCompletes;
            this.hailResponses = hailResponses;
        }
        public void populateStart(QuestDefinition def, String startResponse) {
            def.setupStartQuestResponse(nodes.get(sanitize_id(startResponse)), prompts.get(sanitize_id(startResponse)));
            for (var hail : hailNodes) {
                def.addHailResponse(nodes.get(hail));
            }
            for (var node : nodes.values()) {
                if (!hailNodes.contains(node.getId())) {
                    def.addStartNode(node);
                }
            }
            for (var prompt : prompts.values()) {
                if (!hailNodes.contains(prompt.getId())) {
                    def.addStartPrompt(prompt);
                }
            }
        }

        public void populateTalkObjective(TalkToNpcObjective objective, Quest quest) {
            for (var hail : hailNodes) {
                DialogueNode hailNode = nodes.get(hail).copyWithId(String.format("%s.%s", objective.getObjectiveName(), hail));
                if (shouldCompletes.containsKey(hail) && shouldCompletes.get(hail)) {
                    hailNode.addEffect(new ObjectiveCompleteEffect(objective.getObjectiveName(), quest.getQuestName()));
                }
                DialogueResponse resp;
                if (hailResponses.containsKey(hail)) {
                    DialogueResponse existing = hailResponses.get(hail);
                    resp = new DialogueResponse(hailNode.getId());
                    for (var cond : existing.getConditions()) {
                        resp.addCondition(cond.copy());
                    }

                } else {
                    resp = new DialogueResponse(hailNode.getId());
                }
                objective.withHailResponse(hailNode, resp);
            }

            for (var node : nodes.values()) {
                if (!hailNodes.contains(node.getId())) {
                    objective.withAdditionalNode(node);
                }
            }
            for (var prompt : prompts.values()) {
                if (!hailNodes.contains(prompt.getId())) {
                    objective.withAdditionalPrompts(prompt);
                }
            }
        }
    }

    public DialogueBuilder() {
        nodeTemplates = new HashMap<>();
        contextAliases = new HashMap<>();
        nodeEffects = new HashMap<>();
        hailResponses = new HashMap<>();
        hailNodes = new ArrayList<>();
        shouldCompletes = new HashMap<>();
    }

    public DialogueBuilderResult build() {
        Map<String, DialogueNode> nodes = new HashMap<>();
        Map<String, DialoguePrompt> prompts = new HashMap<>();
//        prompts.put("hail", new DialoguePrompt("hail"));
        for (var entry : nodeTemplates.entrySet()) {
            String start = entry.getValue();
            Matcher conM = Pattern.compile("\\{(.*?)}").matcher(start);
            String contexts = conM.replaceAll((res) -> contextAliases.get(sanitize_id(res.group(1))));
            Matcher m = Pattern.compile("\\[(.*?)]").matcher(contexts);
            while (m.find()) {
                String promptContent = m.group(1);
                var parts = promptContent.split("\\|");
                DialoguePrompt prompt;
                String promptId;
                if (parts.length == 1) {
                    promptId = parts[0];
                    prompt = new DialoguePrompt(sanitize_id(parts[0]), parts[0], parts[0], parts[0]);
                } else if (parts.length == 2) {
                    promptId = parts[0];
                    prompt = new DialoguePrompt(sanitize_id(parts[0]), parts[0], parts[1], parts[0]);
                } else if (parts.length == 3) {
                    promptId = parts[1];
                    prompt = new DialoguePrompt(sanitize_id(parts[1]), parts[1], parts[2], parts[0]);
                } else {
                    throw new IllegalArgumentException("Invalid prompt format: " + promptContent);
                }
                promptId = sanitize_id(promptId);
                prompt.addResponse(new DialogueResponse(promptId));
                prompts.put(promptId, prompt);
            }
            String nodeText = m.replaceAll((res) -> {
                var parts = res.group(1).split("\\|");
                if (parts.length == 2 || parts.length == 1) {
                    return String.format("{prompt:%s}", sanitize_id(parts[0]));
                } else if (parts.length == 3) {
                    return String.format("{prompt:%s}", sanitize_id(parts[1]));
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
        return new DialogueBuilderResult(nodes, prompts, new HashSet<>(hailNodes), shouldCompletes,  hailResponses);
    }

    public DialogueBuilder node(String key, String nodeText) {
        nodeTemplates.put(sanitize_id(key), nodeText);
        return this;
    }

    public DialogueBuilder effectNode(String key, String nodeText, DialogueEffect... effects) {
        node(sanitize_id(key), nodeText);
        nodeEffects.put(sanitize_id(key), Arrays.asList(effects));
        return this;
    }

    public DialogueBuilder shouldComplete(String key) {
        shouldCompletes.put(key, true);
        return this;
    }

    public DialogueBuilder context(String key, String contextKey) {
        contextAliases.put(sanitize_id(key), contextKey);
        return this;
    }

    public static DialogueBuilder hail(String hail) {
        return hail(hail, false);
    }

    public static DialogueBuilder hail(String hail, boolean shouldComplete) {
        DialogueBuilder builder = new DialogueBuilder();
        builder.hailNodes.add("hail");
        builder.node("hail", hail);
        if (shouldComplete){
            builder.shouldComplete("hail");
        }
        return builder;
    }

    public static DialogueBuilder hailWithCondition(String withCondition, String withoutCondition, DialogueCondition condition) {
        DialogueBuilder builder = new DialogueBuilder();
        builder.hailNodes.add("hail_w_cond");
        builder.hailNodes.add("hail_wo_cond");
        builder.hailResponses.put("hail_w_cond", new DialogueResponse("hail_w_cond").addCondition(condition));
        return builder.node("hail_w_cond", withCondition).node("hail_wo_cond", withoutCondition).shouldComplete("hail_w_cond");
    }
}
