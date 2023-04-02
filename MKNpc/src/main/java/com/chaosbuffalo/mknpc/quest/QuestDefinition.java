package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.CanStartQuestCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.StartQuestChainEffect;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QuestDefinition {
    public enum QuestMode {
        LINEAR,
        UNSORTED
    }


    private final ResourceLocation name;
    private final List<Quest> questChain;
    private final Map<String, Quest> questIndex;
    private boolean repeatable;
    private Component questName;
    private static final Component defaultQuestName = new TextComponent("Default");
    private final List<QuestRequirement> requirements;
    private QuestMode mode;
    private DialogueTree startQuestTree;

    public QuestDefinition(ResourceLocation name){
        this.name = name;
        this.questChain = new ArrayList<>();
        this.questIndex = new HashMap<>();
        this.requirements = new ArrayList<>();
        this.repeatable = false;
        this.mode = QuestMode.LINEAR;
        this.questName = defaultQuestName;
        startQuestTree = new DialogueTree(makeTreeId(name));
        DialoguePrompt hailPrompt = new DialoguePrompt("hail");
        startQuestTree.addPrompt(hailPrompt);
        startQuestTree.setHailPrompt(hailPrompt);
    }

    public QuestMode getMode() {
        return mode;
    }

    public void setMode(QuestMode mode) {
        this.mode = mode;
    }

    public List<QuestRequirement> getRequirements() {
        return requirements;
    }

    public void addRequirement(QuestRequirement requirement){
        this.requirements.add(requirement);
    }

    public void setQuestName(Component questName) {
        this.questName = questName;
    }

    public Component getQuestName() {
        return questName;
    }

    public void addStartNode(DialogueNode node){
        startQuestTree.addNode(node);
    }

    public void addStartPrompt(DialoguePrompt prompt){
        if (prompt.getId().equals("hail")){
            if (startQuestTree.getHailPrompt() != null){
                startQuestTree.getHailPrompt().merge(prompt);
            } else {
                startQuestTree.addPrompt(prompt);
                startQuestTree.setHailPrompt(prompt);
            }
        } else {
            startQuestTree.addPrompt(prompt);
        }
    }

    public void addHailResponse(DialogueNode node){
        startQuestTree.addNode(node);
        DialoguePrompt hail = startQuestTree.getHailPrompt();
        if (hail != null){
            DialogueResponse startResponse = new DialogueResponse(node)
                    .addCondition(new CanStartQuestCondition(Util.NIL_UUID, isRepeatable()));
            hail.addResponse(startResponse);
        }
    }

    private static ResourceLocation makeTreeId(ResourceLocation questName) {
        return new ResourceLocation(MKNpc.MODID, String.format("give_quest.%s.%s", questName.getNamespace(), questName.getPath()));
    }

    public DialogueTree getStartQuestTree() {
        return startQuestTree;
    }

    public void setupStartQuestResponse(DialogueNode startQuestResponse, DialoguePrompt prompt, DialogueCondition... extraConditions) {
        startQuestResponse.addEffect(new StartQuestChainEffect());
        addStartNode(startQuestResponse);
        DialogueResponse startResponse = new DialogueResponse(startQuestResponse)
                .addCondition(new CanStartQuestCondition(Util.NIL_UUID, isRepeatable()));
        for (DialogueCondition cond : extraConditions){
            startResponse.addCondition(cond);
        }
        prompt.addResponse(startResponse);
        addStartPrompt(prompt);
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public List<Quest> getFirstQuests(){
        switch (getMode()){
            case UNSORTED:
                return questChain;
            default:
            case LINEAR:
                return Collections.singletonList(questChain.get(0));
        }
    }

    public void addQuest(Quest quest){
        if (questIndex.containsKey(quest.getQuestName())){
            MKNpc.LOGGER.error("Trying to add quest with existing quest name {} to quest definition: {}", quest.getQuestName(), name.toString());
        } else {
            questChain.add(quest);
            questIndex.put(quest.getQuestName(), quest);
        }
    }

    @Nullable
    public Quest getQuest(String name){
        return questIndex.get(name);
    }

    public List<Quest> getQuestChain() {
        return questChain;
    }

    public ResourceLocation getName() {
        return name;
    }


    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("quests"), ops.createList(questChain.stream().map(x -> x.serialize(ops))));
        builder.put(ops.createString("repeatable"), ops.createBoolean(isRepeatable()));
        builder.put(ops.createString("questName"), ops.createString(Component.Serializer.toJson(questName)));
        builder.put(ops.createString("requirements"), ops.createList(requirements.stream().map(x -> x.serialize(ops))));
        builder.put(ops.createString("questMode"), ops.createInt(getMode().ordinal()));
        builder.put(ops.createString("dialogue"), startQuestTree.serialize(ops));
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        List<Quest> dQuests = dynamic.get("quests").asList(d -> {
            Quest q = new Quest();
            q.deserialize(d);
            return q;
        });
        questIndex.clear();
        questChain.clear();
        repeatable = dynamic.get("repeatable").asBoolean(false);
        for (Quest quest : dQuests) {
            addQuest(quest);
        }
        questName = Component.Serializer.fromJson(
                dynamic.get("questName").asString(Component.Serializer.toJson(defaultQuestName)));
        mode = QuestMode.values()[dynamic.get("questMode").asInt(0)];
        List<Optional<QuestRequirement>> reqs = dynamic.get("requirements").asList(x -> {
            ResourceLocation type = QuestRequirement.getType(x);
            Supplier<QuestRequirement> deserializer = QuestDefinitionManager.getRequirementDeserializer(type);
            if (deserializer == null){
                return Optional.empty();
            } else {
                QuestRequirement req = deserializer.get();
                req.deserialize(x);
                return Optional.of(req);
            }
        });
        reqs.forEach(x -> x.ifPresent(this::addRequirement));
        startQuestTree = DialogueTree.deserializeTreeFromDynamic(makeTreeId(getName()),
                dynamic.get("dialogue").result().orElseThrow(() -> new IllegalStateException(String.format(
                        "QuestDefinition: %s missing start quest dialogue", getName().toString()))));
    }

    public Map<ResourceLocation, Integer> getStructuresNeeded(){

        List<Pair<ResourceLocation, Integer>> allObjectives = questChain
                .stream()
                .map(Quest::getStructuresNeeded)
                .flatMap(Collection::stream).collect(Collectors.toList());

        Map<ResourceLocation, Integer> finals = new HashMap<>();
        for (Pair<ResourceLocation, Integer> pair : allObjectives){
            if (!finals.containsKey(pair.getFirst()) || finals.get(pair.getFirst()) < pair.getSecond()){
                finals.put(pair.getFirst(), pair.getSecond());
            }
        }
        return finals;
    }

    public boolean doesStructureMeetRequirements(MKStructureEntry entry){
        return questChain.stream().allMatch(x -> x.isStructureRelevant(entry));
    }

    public QuestChainInstance generate(Map<ResourceLocation, List<MKStructureEntry>> questStructures){
        QuestChainInstance instance = new QuestChainInstance(this, questStructures);
        return instance;
    }


}
