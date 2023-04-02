package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.chaosbuffalo.mknpc.quest.dialogue.NpcDialogueUtils;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.OnQuestCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.IReceivesChainId;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public class TalkToNpcObjective extends StructureInstanceObjective<UUIDInstanceData> {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.talk_to_npc");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute("npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);
    protected DialogueTree tree;

    public TalkToNpcObjective(String name, ResourceLocation structure, int index, ResourceLocation npcDefinition, MutableComponent... description){
        super(NAME, name, structure, index, description);
        addAttribute(this.npcDefinition);
        this.npcDefinition.setValue(npcDefinition);
        tree = new DialogueTree(new ResourceLocation(MKNpc.MODID, String.format("quest.dialogue.%s", name)));
        DialoguePrompt hailPrompt = new DialoguePrompt("hail");
        tree.addPrompt(hailPrompt);
        tree.setHailPrompt(hailPrompt);
    }

    public TalkToNpcObjective(){
        super(NAME, "invalid", defaultDescription);
        addAttribute(this.npcDefinition);
    }

    public ResourceLocation getNpcDefinition(){
        return npcDefinition.getValue();
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        MKStructureEntry entry = questStructures.get(getStructureName()).get(structureIndex.value());
        Optional<NotableNpcEntry> npcOpt = entry.getFirstNotableOfType(npcDefinition.getValue());
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    public TalkToNpcObjective withHailResponse(DialogueNode hailNode, DialogueResponse hailResponse){
        if (tree.getHailPrompt() != null){
            tree.getHailPrompt().addResponse(hailResponse);
        } else {
            MKNpc.LOGGER.error("TalkToNpcObjective Dialogue Tree is Missing hail prompt");
        }
        tree.addNode(hailNode);
        return this;
    }

    public TalkToNpcObjective withAdditionalNode(DialogueNode node){
        tree.addNode(node);
        return this;
    }

    public TalkToNpcObjective withAdditionalPrompts(DialoguePrompt prompt){
        tree.addPrompt(prompt);
        return this;
    }

    public TalkToNpcObjective withTree(DialogueTree tree){
        this.tree = tree;
        return this;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("dialogue"), tree.serialize(ops));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        tree = DialogueTree.deserializeTreeFromDynamic(new ResourceLocation(MKNpc.MODID, String.format("quest.dialogue.%s", getObjectiveName())),
                dynamic.get("dialogue").result().orElseThrow(() -> new IllegalStateException(String.format("TalkToNpcObjective: %s missing dialogue", getObjectiveName()))));

    }

    public static void handleQuestRawMessageManipulation(DialogueObject dialogueObj,
                                                   Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                                   QuestChainInstance questChain){
        String rawMsg = dialogueObj.getRawMessage();
        String newMsg = NpcDialogueUtils.parseQuestDialogueMessage(rawMsg, questStructures, questChain);
        dialogueObj.setRawMessage(newMsg);
    }

    private DialogueTree specializeTree(Quest quest, QuestChainInstance questChain, Map<ResourceLocation, List<MKStructureEntry>> questStructures){
        DialogueTree specializedTree = tree.copy();
        for (DialogueNode node : specializedTree.getNodes().values()){
            for (DialogueEffect effect : node.getEffects()){
                if (effect instanceof IReceivesChainId){
                    IReceivesChainId advEffect = (IReceivesChainId) effect;
                    advEffect.setChainId(questChain.getQuestId());
                }
            }
            handleQuestRawMessageManipulation(node, questStructures, questChain);
        }
        for (DialoguePrompt prompt : specializedTree.getPrompts().values()){
            for (DialogueResponse resp : prompt.getResponses()){
                for (DialogueCondition condition : resp.getConditions()){
                    if (condition instanceof IReceivesChainId){
                        ((IReceivesChainId) condition).setChainId(questChain.getQuestId());
                    }
                }
                resp.addCondition(new OnQuestCondition(questChain.getQuestId(), quest.getQuestName()));
            }
        }
        return specializedTree;
    }

    public DialogueTree generateDialogueForNpc(Quest quest, QuestChainInstance questChain, ResourceLocation npcDefinitionName,
                                       UUID npcId, DialogueTree tree,
                                       Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                       QuestDefinition definition){
        return tree.merge(specializeTree(quest, questChain, questStructures));
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }

    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return structureName.getValue().equals(entry.getStructureName()) && entry.hasNotableOfType(npcDefinition.getValue());
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = playerDataFactory();
        NotableNpcEntry entry = worldData.getNotableNpc(objData.getUuid());
        if (entry != null) {
            newObj.putBlockPos("npcPos", entry.getLocation());
        }

        newObj.putBool("hasSpoken", false);
        return newObj;
    }

    @Override
    public PlayerQuestObjectiveData playerDataFactory() {
        return new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
    }

    @Override
    public void signalCompleted(PlayerQuestObjectiveData objectiveData) {
        super.signalCompleted(objectiveData);
        objectiveData.putBool("hasSpoken", true);

    }
}
