package com.chaosbuffalo.mknpc.world.gen.feature.structure.events;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.StructureEventCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureEventRequirement;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Supplier;

public abstract class StructureEvent implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    private static final String TYPE_ENTRY_NAME = "structEventType";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKNpc.MODID, "struct_event.invalid");
    private final List<ISerializableAttribute<?>> attributes = new ArrayList<>();
    private final ResourceLocation typeName;
    private String eventName;
    protected final List<StructureEventRequirement> requirements = new ArrayList<>();
    protected final List<StructureEventCondition> conditions = new ArrayList<>();
    protected ResourceLocation timerName;
    protected final IntAttribute eventTimer = new IntAttribute("cooldown", 10 * GameConstants.TICKS_PER_SECOND * 60);
    public enum EventTrigger {
        ON_TICK,
        ON_DEATH,
        ON_ACTIVATE,
        ON_DEACTIVATE
    }
    protected final Set<EventTrigger> triggers = new HashSet<>();

    public StructureEvent(ResourceLocation typeName) {
        this.typeName = typeName;
        setEventName("name_not_found");
        addAttribute(eventTimer);
    }

    public StructureEvent setEventName(String eventName) {
        this.eventName = eventName;
        timerName = new ResourceLocation("event_timer", eventName);
        return this;
    }

    public boolean canTrigger(EventTrigger trigger) {
        return triggers.contains(trigger);
    }

    public StructureEvent addTrigger(EventTrigger trigger) {
        triggers.add(trigger);
        return this;
    }

    public int getCooldown(){
        return eventTimer.value();
    }

    public StructureEvent setCooldown(int ticks) {
        eventTimer.setValue(ticks);
        return this;
    }

    public ResourceLocation getTimerName() {
        return timerName;
    }

    @Override
    public ResourceLocation getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    public String getEventName() {
        return eventName;
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

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
        builder.put(ops.createString("requirements"),  ops.createList(requirements.stream().map(x -> x.serialize(ops))));
        builder.put(ops.createString("conditions"), ops.createList(conditions.stream().map(x -> x.serialize(ops))));
        builder.put(ops.createString("eventName"), ops.createString(getEventName()));
    }

    public List<StructureEventRequirement> getRequirements() {
        return requirements;
    }

    public void addRequirement(StructureEventRequirement requirement){
        this.requirements.add(requirement);
    }

    public void addCondition(StructureEventCondition condition) {
        this.conditions.add(condition);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        deserializeAttributeMap(dynamic, "attributes");
        List<Optional<StructureEventRequirement>> reqs = dynamic.get("requirements").asList(x -> {
            ResourceLocation type = StructureEventRequirement.getType(x);
            Supplier<StructureEventRequirement> deserializer = StructureEventManager.getRequirementDeserializer(type);
            if (deserializer == null){
                return Optional.empty();
            } else {
                StructureEventRequirement req = deserializer.get();
                req.deserialize(x);
                return Optional.of(req);
            }
        });
        reqs.forEach(x -> x.ifPresent(this::addRequirement));
        List<Optional<StructureEventCondition>> conds = dynamic.get("conditions").asList(x -> {
            ResourceLocation type = StructureEventCondition.getType(x);
            Supplier<StructureEventCondition> deserializer = StructureEventManager.getConditionDeserializer(type);
            if (deserializer == null) {
                return Optional.empty();
            } else {
                StructureEventCondition cond = deserializer.get();
                cond.deserialize(x);
                return Optional.of(cond);
            }
        });
        conds.forEach(x -> x.ifPresent(this::addCondition));
        setEventName(dynamic.get("eventName").asString("name_not_found"));
    }

    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return requirements.stream().allMatch(x -> x.meetsRequirements(entry, activeStructure, world));
    }

    public boolean meetsConditions(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return conditions.stream().allMatch(x -> x.meetsCondition(entry, activeStructure, world));
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic){
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME).orElse(INVALID_OPTION);
    }

    public abstract void execute(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world);

}
