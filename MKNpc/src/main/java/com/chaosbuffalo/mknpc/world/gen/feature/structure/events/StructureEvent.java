package com.chaosbuffalo.mknpc.world.gen.feature.structure.events;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.StructureEventCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureEventRequirement;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class StructureEvent {
    public static final Codec<StructureEvent> CODEC = StructureEventManager.EVENT_CODEC;

    protected static <T extends StructureEvent> Products.P5<RecordCodecBuilder.Mu<T>, String, Integer,
            Set<EventTrigger>, List<StructureEventRequirement>,
            List<StructureEventCondition>> commonCodec(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.STRING.fieldOf("event_name").forGetter(StructureEvent::getEventName),
                Codec.INT.fieldOf("cooldown").forGetter(StructureEvent::getCooldown),
                CommonCodecs.sortedSet(EventTrigger.CODEC, EventTrigger.COMPARATOR).fieldOf("triggers").forGetter(i -> i.triggers),
                StructureEventRequirement.CODEC.listOf().fieldOf("requirements").forGetter(StructureEvent::getRequirements),
                StructureEventCondition.CODEC.listOf().fieldOf("conditions").forGetter(StructureEvent::getConditions)
        );
    }

    public static final int DEFAULT_COOLDOWN = 10 * GameConstants.TICKS_PER_SECOND * 60;

    private final ResourceLocation typeName;
    private final String eventName;
    protected final int eventTimer;
    protected final Set<EventTrigger> triggers;
    protected final List<StructureEventRequirement> requirements = new ArrayList<>();
    protected final List<StructureEventCondition> conditions = new ArrayList<>();
    protected final ResourceLocation timerName;

    protected boolean startsCooldownImmediately;

    public enum EventTrigger implements StringRepresentable {
        ON_TICK,
        ON_DEATH,
        ON_ACTIVATE,
        ON_DEACTIVATE;

        public static final Codec<EventTrigger> CODEC = StringRepresentable.fromEnum(EventTrigger::values);
        public static final Comparator<EventTrigger> COMPARATOR = Comparator.comparing(EventTrigger::getSerializedName);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    protected StructureEvent(ResourceLocation typeName, String eventName, int cooldown,
                             Set<EventTrigger> triggers,
                             List<StructureEventRequirement> requirements, List<StructureEventCondition> conditions) {
        this.typeName = typeName;
        this.eventName = eventName;
        timerName = new ResourceLocation("event_timer", eventName);
        eventTimer = cooldown;
        this.triggers = triggers;
        this.requirements.addAll(requirements);
        this.conditions.addAll(conditions);
        startsCooldownImmediately = true;
    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public String getEventName() {
        return eventName;
    }

    public int getCooldown() {
        return eventTimer;
    }

    public boolean startsCooldownImmediately() {
        return startsCooldownImmediately;
    }

    public ResourceLocation getTimerName() {
        return timerName;
    }

    public List<StructureEventRequirement> getRequirements() {
        return requirements;
    }

    public void addRequirement(StructureEventRequirement requirement) {
        this.requirements.add(requirement);
    }

    public List<StructureEventCondition> getConditions() {
        return conditions;
    }

    public void addCondition(StructureEventCondition condition) {
        this.conditions.add(condition);
    }

    public boolean canTrigger(EventTrigger trigger) {
        return triggers.contains(trigger);
    }

    public StructureEvent addTrigger(EventTrigger trigger) {
        triggers.add(trigger);
        return this;
    }

    public void onTrackedEntityDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure,
                                     IEntityNpcData npcData) {

    }

    public boolean meetsRequirements(MKStructureEntry entry,
                                     WorldStructureManager.ActiveStructure activeStructure, Level level) {
        return requirements.stream().allMatch(x -> x.meetsRequirements(entry, activeStructure, level));
    }

    public boolean meetsConditions(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level level) {
        return conditions.stream().allMatch(x -> x.meetsCondition(entry, activeStructure, level));
    }

    public abstract void execute(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level level);


    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
