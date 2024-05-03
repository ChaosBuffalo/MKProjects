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

import java.util.HashMap;
import java.util.Map;

public class StructureEventManager {

    public static final Map<ResourceLocation, Codec<? extends StructureEventRequirement>> REQ_CODECS = new HashMap<>();
    public static final Codec<StructureEventRequirement> REQ_CODEC = CommonCodecs
            .createMapBackedDispatch(ResourceLocation.CODEC, REQ_CODECS, StructureEventRequirement::getTypeName);

    public static final Map<ResourceLocation, Codec<? extends StructureEventCondition>> COND_CODECS = new HashMap<>();
    public static final Codec<StructureEventCondition> COND_CODEC = CommonCodecs
            .createMapBackedDispatch(ResourceLocation.CODEC, COND_CODECS, StructureEventCondition::getTypeName);

    public static final Map<ResourceLocation, Codec<? extends StructureEvent>> EVENT_CODECS = new HashMap<>();
    public static final Codec<StructureEvent> EVENT_CODEC = CommonCodecs
            .createMapBackedDispatch(ResourceLocation.CODEC, EVENT_CODECS, StructureEvent::getTypeName);

    public static void registerRequirement(ResourceLocation name,
                                           Codec<? extends StructureEventRequirement> codec) {
        REQ_CODECS.put(name, codec);
    }

    public static void registerCondition(ResourceLocation name, Codec<? extends StructureEventCondition> codec) {
        COND_CODECS.put(name, codec);
    }

    public static void registerEventType(ResourceLocation name, Codec<? extends StructureEvent> codec) {
        EVENT_CODECS.put(name, codec);
    }

    public static void setupDeserializers() {
        registerRequirement(StructureHasPoiRequirement.TYPE_NAME, StructureHasPoiRequirement.CODEC);
        registerRequirement(StructureHasNotableRequirement.TYPE_NAME, StructureHasNotableRequirement.CODEC);
        registerCondition(NotableDeadCondition.TYPE_NAME, NotableDeadCondition.CODEC);
        registerEventType(SpawnNpcDefinitionEvent.TYPE_NAME, SpawnNpcDefinitionEvent.CODEC);
    }
}
