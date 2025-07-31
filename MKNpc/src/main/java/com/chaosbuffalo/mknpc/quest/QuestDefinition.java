package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueResponse;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.CanStartQuestCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.StartQuestChainEffect;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class QuestDefinition {
    public enum QuestMode implements StringRepresentable {
        LINEAR,
        UNSORTED;

        public static final Codec<QuestMode> CODEC = StringRepresentable.fromValues(QuestMode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record AdditionalNotable(QuestStructureLocation location, ResourceKey<NpcDefinition> notableDef) {
        public static final Codec<AdditionalNotable> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                QuestStructureLocation.CODEC.fieldOf("location").forGetter(AdditionalNotable::location),
                NpcDefinition.KEY_CODEC.fieldOf("notableDefs").forGetter(AdditionalNotable::notableDef)
        ).apply(builder, AdditionalNotable::new));
    }

    public static final Codec<QuestDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("questTemplateId").forGetter(i -> i.name),
            ComponentSerialization.CODEC.fieldOf("questName").forGetter(i -> i.questName),
            Quest.CODEC.listOf().fieldOf("quests").forGetter(i -> i.questChain),
            Codec.BOOL.fieldOf("repeatable").forGetter(i -> i.repeatable),
            QuestRequirement.CODEC.listOf().fieldOf("requirements").forGetter(i -> i.requirements),
            QuestMode.CODEC.fieldOf("questMode").forGetter(i -> i.mode),
            DialogueTree.CODEC.fieldOf("dialogue").forGetter(i -> i.startQuestTree),
            AdditionalNotable.CODEC.listOf().optionalFieldOf("additionalNotables", List.of()).forGetter(i -> i.additionalNotables)
    ).apply(builder, QuestDefinition::new));

    public static final Codec<Holder<QuestDefinition>> REFERENCE_CODEC = RegistryFixedCodec.create(QuestRegistries.QUEST_DEFINITIONS);
    public static final Codec<ResourceKey<QuestDefinition>> KEY_CODEC = ResourceKey.codec(QuestRegistries.QUEST_DEFINITIONS);

    private final ResourceLocation name;
    private final List<Quest> questChain;
    private final Map<String, Quest> questIndex;
    private boolean repeatable;
    private Component questName;
    private static final Component defaultQuestName = Component.literal("Default");
    private final List<QuestRequirement> requirements;
    private QuestMode mode;
    private final DialogueTree startQuestTree;
    private final List<AdditionalNotable> additionalNotables;


    private QuestDefinition(ResourceLocation name, Component questName, List<Quest> quests, boolean repeatable,
                            List<QuestRequirement> requirements, QuestMode mode, DialogueTree startQuestTree,
                            List<AdditionalNotable> additionalNotables) {
        this.name = name;
        this.questName = questName;
        this.questChain = quests;
        this.repeatable = repeatable;
        this.requirements = requirements;
        this.mode = mode;
        this.startQuestTree = startQuestTree;
        this.additionalNotables = additionalNotables;
        questIndex = new HashMap<>(quests.size());
        quests.forEach(quest -> questIndex.put(quest.getQuestName(), quest));
    }

    public QuestDefinition(ResourceKey<QuestDefinition> name) {
        this.name = name.location();
        this.questChain = new ArrayList<>();
        this.questIndex = new HashMap<>();
        this.requirements = new ArrayList<>();
        this.additionalNotables = new ArrayList<>();
        this.repeatable = false;
        this.mode = QuestMode.LINEAR;
        this.questName = defaultQuestName;
        startQuestTree = new DialogueTree(makeTreeId(name.location()));
        DialoguePrompt hailPrompt = new DialoguePrompt("hail");
        startQuestTree.addPrompt(hailPrompt);
        startQuestTree.setHailPrompt(hailPrompt);
    }

    public void addAdditionalNotable(QuestStructureLocation location, ResourceKey<NpcDefinition> notable) {
        additionalNotables.add(new AdditionalNotable(location, notable));
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

    public void addRequirement(QuestRequirement requirement) {
        this.requirements.add(requirement);
    }

    public void setQuestName(Component questName) {
        this.questName = questName;
    }

    public Component getQuestName() {
        return questName;
    }

    public void addStartNode(DialogueNode node) {
        startQuestTree.addNode(node);
    }

    public void addStartPrompt(DialoguePrompt prompt) {
        if (prompt.getId().equals("hail")) {
            if (startQuestTree.getHailPrompt() != null) {
                startQuestTree.getHailPrompt().merge(prompt);
            } else {
                startQuestTree.addPrompt(prompt);
                startQuestTree.setHailPrompt(prompt);
            }
        } else {
            startQuestTree.addPrompt(prompt);
        }
    }

    public void addHailResponse(DialogueNode node) {
        startQuestTree.addNode(node);
        DialoguePrompt hail = startQuestTree.getHailPrompt();
        if (hail != null) {
            DialogueResponse startResponse = new DialogueResponse(node)
                    .addCondition(new CanStartQuestCondition(Util.NIL_UUID, isRepeatable()));
            hail.addResponse(startResponse);
        }
    }

    private static ResourceLocation makeTreeId(ResourceLocation questName) {
        return MKNpc.id(String.format("give_quest.%s.%s", questName.getNamespace(), questName.getPath()));
    }

    public DialogueTree getStartQuestTree() {
        return startQuestTree;
    }

    public void setupStartQuestResponse(DialogueNode startQuestResponse, DialoguePrompt prompt, DialogueCondition... extraConditions) {
        startQuestResponse.addEffect(new StartQuestChainEffect(Util.NIL_UUID));
        addStartNode(startQuestResponse);
        DialogueResponse startResponse = new DialogueResponse(startQuestResponse)
                .addCondition(new CanStartQuestCondition(Util.NIL_UUID, isRepeatable()));
        for (DialogueCondition cond : extraConditions) {
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

    public List<Quest> getFirstQuests() {
        switch (getMode()) {
            case UNSORTED:
                return questChain;
            default:
            case LINEAR:
                return Collections.singletonList(questChain.get(0));
        }
    }

    public void addQuest(Quest quest) {
        if (questIndex.containsKey(quest.getQuestName())) {
            MKNpc.LOGGER.error("Trying to add quest with existing quest name {} to quest definition: {}", quest.getQuestName(), name.toString());
        } else {
            questChain.add(quest);
            questIndex.put(quest.getQuestName(), quest);
        }
    }

    @Nullable
    public Quest getQuest(String name) {
        return questIndex.get(name);
    }

    public List<Quest> getQuestChain() {
        return questChain;
    }

    public ResourceLocation getName() {
        return name;
    }

    public Set<QuestStructureLocation> getStructuresNeeded() {
        return questChain
                .stream()
                .flatMap(x -> x.getStructuresNeeded().stream())
                .collect(Collectors.toSet());
    }

    public boolean doesStructureMeetRequirements(QuestStructureLocation location, MKStructureEntry entry) {
        if (entry == null) {
            return false;
        }
        boolean meetsRequires = questChain.stream()
                .flatMap(x -> x.getObjectives().stream())
                .filter(x -> x.getLocation() != null && x.getLocation().equals(location))
                .allMatch(x -> x.isStructureRelevant(entry));
        if (!meetsRequires) {
            return false;
        }
        for (AdditionalNotable not : additionalNotables) {
            if (not.location.equals(location)) {
                if (!entry.hasNotableOfType(not.notableDef, entry.getWorldData().getWorld().registryAccess())) {
                    return false;
                }
            }
        }
        return true;
    }

    public QuestChainInstance generate(Map<QuestStructureLocation, MKStructureEntry> questStructures, Level level) {
        QuestChainInstance instance = new QuestChainInstance(this, questStructures, level);
        return instance;
    }


}
