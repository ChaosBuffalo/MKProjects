package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueResponse;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.CanStartQuestCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.StartQuestChainEffect;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.chaosbuffalo.mkweapons.components.RangedEffectsComponent;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class QuestDefinition {
    public enum QuestMode {
        LINEAR,
        UNSORTED
    }

    public record AdditionalNotable(QuestStructureLocation location, ResourceLocation notableDef) {
        public static final Codec<AdditionalNotable> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                QuestStructureLocation.CODEC.fieldOf("location").forGetter(AdditionalNotable::location),
                ResourceLocation.CODEC.fieldOf("notableDefs").forGetter(AdditionalNotable::notableDef)
        ).apply(builder, AdditionalNotable::new));
    }


    private final ResourceLocation name;
    private final List<Quest> questChain;
    private final Map<String, Quest> questIndex;
    private boolean repeatable;
    private Component questName;
    private static final Component defaultQuestName = Component.literal("Default");
    private final List<QuestRequirement> requirements;
    private QuestMode mode;
    private DialogueTree startQuestTree;

    private final List<AdditionalNotable> additionalNotables;



    public QuestDefinition(ResourceLocation name) {
        this.name = name;
        this.questChain = new ArrayList<>();
        this.questIndex = new HashMap<>();
        this.requirements = new ArrayList<>();
        this.additionalNotables = new ArrayList<>();
        this.repeatable = false;
        this.mode = QuestMode.LINEAR;
        this.questName = defaultQuestName;
        startQuestTree = new DialogueTree(makeTreeId(name));
        DialoguePrompt hailPrompt = new DialoguePrompt("hail");
        startQuestTree.addPrompt(hailPrompt);
        startQuestTree.setHailPrompt(hailPrompt);
    }

    public void addAdditionalNotable(QuestStructureLocation location, ResourceLocation notable) {
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


    public <D> D serialize(DynamicOps<D> ops, HolderLookup.Provider provider) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("quests"), ops.createList(questChain.stream().map(x -> x.serialize(ops, provider))));
        builder.put(ops.createString("repeatable"), ops.createBoolean(isRepeatable()));
        builder.put(ops.createString("questName"), ops.createString(Component.Serializer.toJson(questName, provider)));
        builder.put(ops.createString("requirements"), ops.createList(requirements.stream().flatMap(x -> QuestRequirement.CODEC.encodeStart(ops, x).resultOrPartial(MKNpc.LOGGER::error).stream())));
        builder.put(ops.createString("questMode"), ops.createInt(getMode().ordinal()));
        builder.put(ops.createString("dialogue"), startQuestTree.serialize(ops));
        builder.put(ops.createString("additionalNotables"), ops.createList(additionalNotables.stream().flatMap(x -> AdditionalNotable.CODEC.encodeStart(ops, x).resultOrPartial(MKNpc.LOGGER::error).stream())));
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic, HolderLookup.Provider provider) {
        List<Quest> dQuests = dynamic.get("quests").asList(d -> {
            Quest q = new Quest();
            q.deserialize(d, provider);
            return q;
        });
        questIndex.clear();
        questChain.clear();
        repeatable = dynamic.get("repeatable").asBoolean(false);
        for (Quest quest : dQuests) {
            addQuest(quest);
        }
        questName = Component.Serializer.fromJson(
                dynamic.get("questName").asString(Component.Serializer.toJson(defaultQuestName, provider)), provider);
        mode = QuestMode.values()[dynamic.get("questMode").asInt(0)];
        dynamic.get("requirements").asStream().forEach(x -> {
            QuestRequirement.CODEC.parse(x).resultOrPartial(MKNpc.LOGGER::error).ifPresent(this::addRequirement);
        });
        dynamic.get("additionalNotables").asStream().forEach(x -> {
            AdditionalNotable.CODEC.parse(x).resultOrPartial(MKNpc.LOGGER::error).ifPresent(not -> addAdditionalNotable(not.location, not.notableDef));
        });
        startQuestTree = DialogueTree.deserialize(makeTreeId(getName()),
                dynamic.get("dialogue").result().orElseThrow(() -> new IllegalStateException(String.format(
                        "QuestDefinition: %s missing start quest dialogue", getName().toString()))));
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
