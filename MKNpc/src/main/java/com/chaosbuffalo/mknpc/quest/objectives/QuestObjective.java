package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.StringAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.ObjectiveInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.*;
import java.util.stream.Collectors;

public abstract class QuestObjective<T extends ObjectiveInstanceData>
        implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {

    private static final String TYPE_NAME_FIELD = "objectiveType";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKNpc.MODID, "quest_objective.invalid");
    protected static final MutableComponent defaultDescription = new TextComponent("Placeholder");

    private final List<ISerializableAttribute<?>> attributes;
    private final ResourceLocation typeName;
    protected final StringAttribute objectiveName = new StringAttribute("objectiveName", "invalid");
    protected List<MutableComponent> description = new ArrayList<>();

    public QuestObjective(ResourceLocation typeName, String name, MutableComponent... description){
        this.attributes = new ArrayList<>();
        addAttribute(objectiveName);
        objectiveName.setValue(name);
        this.typeName = typeName;
        this.description.addAll(Arrays.asList(description));
    }

    public void setDescription(MutableComponent... description) {
        this.setDescription(Arrays.asList(description));
    }

    public void setDescription(List<MutableComponent> description){
        this.description.clear();
        this.description.addAll(description);
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> iSerializableAttribute) {
        attributes.add(iSerializableAttribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... iSerializableAttributes) {
        attributes.addAll(Arrays.asList(iSerializableAttributes));
    }

    public String getObjectiveName() {
        return objectiveName.getValue();
    }

    // return true if it works or you dont care
    // return false only if this structure is needed but doesnt meet requirements
    public boolean isStructureRelevant(MKStructureEntry entry){
        return true;
    }

    public abstract T generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures);

    public abstract T instanceDataFactory();

    public T loadInstanceData(CompoundTag nbt){
        T data = instanceDataFactory();
        data.deserializeNBT(nbt);
        return data;
    }

    public abstract PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData);

    public abstract PlayerQuestObjectiveData playerDataFactory();

    public T getInstanceData(QuestData data){
        return (T) data.getObjective(getObjectiveName());
    }

    public void signalCompleted(PlayerQuestObjectiveData objectiveData) {
        objectiveData.putBool("isComplete", true);
    }


    public boolean isComplete(PlayerQuestObjectiveData playerData) {
        return playerData.getBool("isComplete");
    }

    public void createDataForQuest(QuestData data, Map<ResourceLocation, List<MKStructureEntry>> questStructures){
        data.putObjective(getObjectiveName(), generateInstanceData(questStructures));
    }

    public List<MutableComponent> getDescription(){
        return description;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("description"),
                ops.createList(description.stream()
                        .map(x -> ops.createString(Component.Serializer.toJson(x)))));
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        description = dynamic.get("description")
                .asList(x -> x.asString().resultOrPartial(MKNpc.LOGGER::error).orElseThrow(IllegalArgumentException::new))
                .stream()
                .map(Component.Serializer::fromJson)
                .collect(Collectors.toList());

        deserializeAttributeMap(dynamic, "attributes");
    }

    @Override
    public ResourceLocation getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_NAME_FIELD;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_NAME_FIELD).orElse(INVALID_OPTION);
    }
}
