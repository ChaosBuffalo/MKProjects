package com.chaosbuffalo.mknpc.world.gen.feature.structure.events;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.NotableDeadCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.StructureEventCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureEventRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasNotableRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasPoiRequirement;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StructureEventManager {

    public static final Map<ResourceLocation, Supplier<StructureEventRequirement>> REQ_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<StructureEventCondition>> COND_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<StructureEvent>> EVENT_DESERIALIZERS = new HashMap<>();

    public static void putRequirementDeserializer(ResourceLocation name,
                                                  Supplier<StructureEventRequirement> function){
        REQ_DESERIALIZERS.put(name, function);
    }

    public static void putConditionDeserializer(ResourceLocation name,
                                                Supplier<StructureEventCondition> function) {
        COND_DESERIALIZERS.put(name, function);
    }

    public static void putEventDeserializer(ResourceLocation name,
                                            Supplier<StructureEvent> function) {
        EVENT_DESERIALIZERS.put(name, function);
    }

    public static void setupDeserializers() {
        putRequirementDeserializer(StructureHasPoiRequirement.TYPE_NAME, StructureHasPoiRequirement::new);
        putRequirementDeserializer(StructureHasNotableRequirement.TYPE_NAME, StructureHasNotableRequirement::new);
        putConditionDeserializer(NotableDeadCondition.TYPE_NAME, NotableDeadCondition::new);
    }

    @Nullable
    public static Supplier<StructureEventRequirement> getRequirementDeserializer(ResourceLocation name){
        return REQ_DESERIALIZERS.get(name);
    }

    @Nullable
    public static Supplier<StructureEventCondition> getConditionDeserializer(ResourceLocation name) {
        return COND_DESERIALIZERS.get(name);
    }

    @Nullable
    public static Supplier<StructureEvent> getEventDeserializer(ResourceLocation name) {
        return EVENT_DESERIALIZERS.get(name);
    }
}
