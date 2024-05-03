package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.chaosbuffalo.mknpc.quest.dialogue.NpcDialogueUtils;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.OnQuestCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.IReceivesChainId;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TalkToNpcObjective extends QuestObjective<UUIDInstanceData> {
    public static final Codec<TalkToNpcObjective> CODEC = RecordCodecBuilder.<TalkToNpcObjective>mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                Codec.list(ExtraCodecs.COMPONENT).fieldOf("description").forGetter(i -> i.description),
                DialogueTree.CODEC.fieldOf("dialogue").forGetter(i -> i.tree)
        ).apply(builder, TalkToNpcObjective::new);
    }).codec();

    protected final ResourceLocation npcDefinition;
    protected final List<Component> description;
    protected final DialogueTree tree;

    private TalkToNpcObjective(String name, QuestStructureLocation structureLocation, ResourceLocation npcDefinition, List<Component> description, DialogueTree tree) {
        super(name, structureLocation);
        this.npcDefinition = npcDefinition;
        this.description = ImmutableList.copyOf(description);
        this.tree = tree;
    }

    public TalkToNpcObjective(String name, QuestStructureLocation structureLocation, ResourceLocation npcDefinition, Component description) {
        super(name, structureLocation);
        this.npcDefinition = npcDefinition;
        this.description = List.of(description);
        tree = new DialogueTree(makeDialogueTreeId(name));
        DialoguePrompt hailPrompt = new DialoguePrompt("hail");
        tree.addPrompt(hailPrompt);
        tree.setHailPrompt(hailPrompt);
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.TALK_TO_NPC.get();
    }

    public ResourceLocation getNpcDefinition() {
        return npcDefinition;
    }

    @Override
    public List<Component> getDescription() {
        return description;
    }

    private ResourceLocation makeDialogueTreeId(String name) {
        return new ResourceLocation(MKNpc.MODID, String.format("quest.dialogue.%s", name));
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        MKStructureEntry entry = questStructures.get(location.getStructureId()).get(location.getIndex());
        Optional<NotableNpcEntry> npcOpt = entry.getFirstNotableOfType(npcDefinition);
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    public TalkToNpcObjective withHailResponse(DialogueNode hailNode, DialogueResponse hailResponse) {
        if (tree.getHailPrompt() != null) {
            tree.getHailPrompt().addResponse(hailResponse);
        } else {
            MKNpc.LOGGER.error("TalkToNpcObjective Dialogue Tree is Missing hail prompt");
        }
        tree.addNode(hailNode);
        return this;
    }

    public TalkToNpcObjective withAdditionalNode(DialogueNode node) {
        tree.addNode(node);
        return this;
    }

    public TalkToNpcObjective withAdditionalPrompts(DialoguePrompt prompt) {
        tree.addPrompt(prompt);
        return this;
    }

    public static void handleQuestRawMessageManipulation(DialogueObject dialogueObj,
                                                         Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                                         QuestChainInstance questChain) {
        String rawMsg = dialogueObj.getRawMessage();
        String newMsg = NpcDialogueUtils.parseQuestDialogueMessage(rawMsg, questStructures, questChain);
        dialogueObj.setRawMessage(newMsg);
    }

    private DialogueTree specializeTree(Quest quest, QuestChainInstance questChain, Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        DialogueTree specializedTree = tree.copy();
        for (DialogueNode node : specializedTree.getNodes().values()) {
            for (DialogueEffect effect : node.getEffects()) {
                if (effect instanceof IReceivesChainId advEffect) {
                    advEffect.setChainId(questChain.getQuestId());
                }
            }
            handleQuestRawMessageManipulation(node, questStructures, questChain);
        }
        for (DialoguePrompt prompt : specializedTree.getPrompts().values()) {
            for (DialogueResponse resp : prompt.getResponses()) {
                for (DialogueCondition condition : resp.getConditions()) {
                    if (condition instanceof IReceivesChainId receiver) {
                        receiver.setChainId(questChain.getQuestId());
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
                                               QuestDefinition definition) {
        return tree.merge(specializeTree(quest, questChain, questStructures));
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }

    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return location.getStructureId().equals(entry.getStructureName()) && entry.hasNotableOfType(npcDefinition);
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
        NotableNpcEntry entry = worldData.getNotableNpc(objData.getUUID());
        if (entry != null) {
            newObj.putBlockPos("npcPos", entry.getLocation());
        }

        newObj.putBool("hasSpoken", false);
        return newObj;
    }

    @Override
    public void signalCompleted(PlayerQuestObjectiveData objectiveData) {
        super.signalCompleted(objectiveData);
        objectiveData.putBool("hasSpoken", true);
    }
}
