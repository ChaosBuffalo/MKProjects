package com.chaosbuffalo.mknpc.world.gen.feature.structure.events.event;

import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.StringAttribute;
import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.conditions.NotableDeadCondition;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasNotableRequirement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.requirements.StructureHasPoiRequirement;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.UUID;

public class SpawnNpcDefinitionEvent extends StructureEvent {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID,
            "struct_event.spawn_npc");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute(
            "npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);
    protected StringAttribute poiTag = new StringAttribute("poiTag", "invalid");
    protected StringAttribute faceTag = new StringAttribute("faceTag", "invalid");
    protected MKEntity.NonCombatMoveType moveType;


    public SpawnNpcDefinitionEvent() {
        super(TYPE_NAME);
        addAttributes(npcDefinition, poiTag, faceTag);
        startsCooldown = false;
    }

    public SpawnNpcDefinitionEvent(ResourceLocation npcDef, String spawnLocation, String faceTagIn,
                                   MKEntity.NonCombatMoveType moveType) {
        this();
        npcDefinition.setValue(npcDef);
        poiTag.setValue(spawnLocation);
        faceTag.setValue(faceTagIn);
        this.moveType = moveType;
        addRequirement(new StructureHasPoiRequirement(spawnLocation));
        addRequirement(new StructureHasPoiRequirement(faceTagIn));
    }

    public SpawnNpcDefinitionEvent addNotableDeadCondition(ResourceLocation notable, boolean killAll) {
        addRequirement(new StructureHasNotableRequirement(notable));
        addCondition(new NotableDeadCondition(notable, killAll));
        return this;
    }

    @Override
    public void onTrackedEntityDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure,
                                     IEntityNpcData npcData) {
        entry.getCooldownTracker().setTimer(getTimerName(), getCooldown());
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        moveType = MKEntity.NonCombatMoveType.values()[dynamic.get("moveType").asInt(0)];
    }

    @Override
    public boolean meetsConditions(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        return !activeStructure.hasActiveEntity(entry.getCustomData().computeUUID(getEventName())) &&
                super.meetsConditions(entry, activeStructure, world);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("moveType"), ops.createInt(moveType.ordinal()));
    }

    @Override
    public void execute(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        NpcDefinition def = NpcDefinitionManager.getDefinition(npcDefinition.getValue());
        if (def == null) {
            return;
        }
        entry.getFirstPoiWithTag(poiTag.getValue()).ifPresent(x -> {
            UUID npcId = entry.getCustomData().computeUUID(getEventName());
            Vec3 pos = Vec3.atBottomCenterOf(x.getLocation().pos());
            double difficultyValue = WorldUtils.getDifficultyForGlobalPos(x.getLocation());
            Entity entity = def.createEntity(world, pos, npcId, difficultyValue);
            if (entity != null) {
                entry.getFirstPoiWithTag(faceTag.getValue()).ifPresent(face -> {
                    entity.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(face.getLocation().pos()));
                });
                final double finDiff = difficultyValue;
                MKNpc.getNpcData(entity).ifPresent((cap) -> {
//                    cap.setMKSpawned(true);
                    cap.setSpawnPos(BlockPos.containing(pos).above());
                    cap.setNotableUUID(npcId);
                    cap.setStructureId(entry.getStructureId());
                    cap.setDifficultyValue(finDiff);
                });
                if (entity instanceof MKEntity mkEntity) {
                    mkEntity.setNonCombatMoveType(moveType);
                }
                if (entity instanceof Mob mobEnt && world instanceof ServerLevelAccessor serverLevel) {
                    ForgeEventFactory.onFinalizeSpawn(mobEnt, serverLevel,
                            serverLevel.getCurrentDifficultyAt(x.getLocation().pos()),
                            MobSpawnType.SPAWNER, null, null);
                }
                world.addFreshEntity(entity);
                MKNpc.getNpcData(entity).ifPresent(cap -> cap.setMKSpawned(true));
                activeStructure.addEntity(npcId, entity, getEventName());
            }
        });

    }
}
