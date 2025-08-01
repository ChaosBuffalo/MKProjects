package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.event;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.NotableDeadCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.NpcDeathCountCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.StructureEventCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureEventRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasNotableRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasNpcRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasPoiRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.*;

public class SpawnNpcDefinitionEvent extends StructureEvent {
    public final static ResourceLocation TYPE_NAME = MKNpc.id("struct_event.spawn_npc");
    public static final MapCodec<SpawnNpcDefinitionEvent> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return CommonCodecs.and(commonCodec(builder), builder.group(
                NpcDefinition.KEY_CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                Codec.STRING.fieldOf("poiTag").forGetter(i -> i.poiTag),
                Codec.STRING.fieldOf("faceTag").forGetter(i -> i.faceTag),
                MKEntity.NonCombatMoveType.CODEC.fieldOf("moveType").forGetter(i -> i.moveType)
        )).apply(builder, SpawnNpcDefinitionEvent::new);
    });

    protected final ResourceKey<NpcDefinition> npcDefinition;
    protected final String poiTag;
    protected final String faceTag;
    protected final MKEntity.NonCombatMoveType moveType;

    private SpawnNpcDefinitionEvent(String eventName, int cooldown, Set<EventTrigger> triggers,
                                    List<StructureEventRequirement> requirementList, List<StructureEventCondition> conditions,
                                    ResourceKey<NpcDefinition> npcDef, String poiTag, String faceTag, MKEntity.NonCombatMoveType moveType) {
        super(TYPE_NAME, eventName, cooldown, triggers, requirementList, conditions);
        this.npcDefinition = npcDef;
        this.poiTag = poiTag;
        this.faceTag = faceTag;
        this.moveType = moveType;
        startsCooldownImmediately = false;
        addRequirement(new StructureHasPoiRequirement(poiTag));
        addRequirement(new StructureHasPoiRequirement(faceTag));
    }

    public SpawnNpcDefinitionEvent(String eventName, ResourceKey<NpcDefinition> npcDef, String spawnLocation, String faceTagIn,
                                   MKEntity.NonCombatMoveType moveType) {
        this(eventName, DEFAULT_COOLDOWN, new HashSet<>(), List.of(), List.of(), npcDef, spawnLocation, faceTagIn, moveType);
    }

    public SpawnNpcDefinitionEvent addNotableDeadCondition(ResourceKey<NpcDefinition> notable, boolean killAll) {
        addRequirement(new StructureHasNotableRequirement(notable));
        addCondition(new NotableDeadCondition(notable, killAll));
        return this;
    }

    public SpawnNpcDefinitionEvent addNpcDeathCountCondition(ResourceKey<NpcDefinition> npcDefinition, int count, String name) {
        addRequirement(new StructureHasNpcRequirement(npcDefinition));
        addCondition(new NpcDeathCountCondition(npcDefinition, count, name));
        return this;
    }

    @Override
    public void onTrackedEntityDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure,
                                     IEntityNpcData npcData) {
        entry.getCooldownTracker().setTimer(getTimerName(), getCooldown());
    }

    @Override
    public boolean meetsConditions(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level level) {
        return !activeStructure.hasActiveEntity(entry.getCustomData().computeUUID(getEventName())) &&
                super.meetsConditions(entry, activeStructure, level);
    }

    @Override
    public void execute(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level level) {
        if (level.getServer() == null) {
            return;
        }
        NpcDefinition def = level.getServer().registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS).get(npcDefinition);
        if (def == null) {
            return;
        }
        entry.getFirstPoiWithTag(poiTag).ifPresent(x -> {
            UUID npcId = entry.getCustomData().computeUUID(getEventName());
            Vec3 pos = Vec3.atBottomCenterOf(x.getLocation().pos());
            double difficultyValue = WorldUtils.getDifficultyForGlobalPos(level, x.getLocation());
            Entity entity = def.createEntity(level, pos, npcId, difficultyValue);
            if (entity != null) {
                entry.getFirstPoiWithTag(faceTag).ifPresent(face -> {
                    entity.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(face.getLocation().pos()));
                });
                final double finDiff = difficultyValue;
                Optional<IEntityNpcData> npcCap = MKNpc.getNpcData(entity);
                npcCap.ifPresent(cap -> {
//                    cap.setMKSpawned(true);
                    cap.setSpawnPos(BlockPos.containing(pos).above());
                    cap.setNotableUUID(npcId);
                    cap.setStructureId(entry.getStructureId());
                    cap.setDifficultyValue(finDiff);
                });
                if (entity instanceof MKEntity mkEntity) {
                    mkEntity.setNonCombatMoveType(moveType);
                }
                if (entity instanceof Mob mobEnt && level instanceof ServerLevelAccessor serverLevel) {
                    EventHooks.finalizeMobSpawn(mobEnt, serverLevel,
                            serverLevel.getCurrentDifficultyAt(x.getLocation().pos()),
                            MobSpawnType.SPAWNER, null);
                }
                level.addFreshEntity(entity);
                npcCap.ifPresent(cap -> cap.setMKSpawned(true));
                activeStructure.addEntity(npcId, entity, getEventName());
            }
        });

    }
}
