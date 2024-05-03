package com.chaosbuffalo.mknpc.world.gen.feature.structure.events;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.NotableDeadCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.StructureEventCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.event.SpawnNpcDefinitionEvent;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureEventRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasNotableRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasPoiRequirement;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StructureEventManager {

    public static final Map<ResourceLocation, Supplier<StructureEventRequirement>> REQ_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Codec<? extends StructureEventCondition>> COND_CODECS = new HashMap<>();
    public static final Codec<StructureEventCondition> COND_CODEC = CommonCodecs
            .createMapBackedDispatch(ResourceLocation.CODEC, COND_CODECS, StructureEventCondition::getTypeName);
    public static final Map<ResourceLocation, Supplier<StructureEvent>> EVENT_DESERIALIZERS = new HashMap<>();


    public static void putRequirementDeserializer(ResourceLocation name,
                                                  Supplier<StructureEventRequirement> function) {
        REQ_DESERIALIZERS.put(name, function);
    }

    public static void registerCondition(ResourceLocation name, Codec<? extends StructureEventCondition> codec) {
        COND_CODECS.put(name, codec);
    }

    public static void putEventDeserializer(ResourceLocation name,
                                            Supplier<StructureEvent> function) {
        EVENT_DESERIALIZERS.put(name, function);
    }

    public static void setupDeserializers() {
        putRequirementDeserializer(StructureHasPoiRequirement.TYPE_NAME, StructureHasPoiRequirement::new);
        putRequirementDeserializer(StructureHasNotableRequirement.TYPE_NAME, StructureHasNotableRequirement::new);
        registerCondition(NotableDeadCondition.TYPE_NAME, NotableDeadCondition.CODEC);
        putEventDeserializer(SpawnNpcDefinitionEvent.TYPE_NAME, SpawnNpcDefinitionEvent::new);
    }

    @Nullable
    public static Supplier<StructureEventRequirement> getRequirementDeserializer(ResourceLocation name) {
        return REQ_DESERIALIZERS.get(name);
    }

    @Nullable
    public static Supplier<StructureEvent> getEventDeserializer(ResourceLocation name) {
        return EVENT_DESERIALIZERS.get(name);
    }
}
