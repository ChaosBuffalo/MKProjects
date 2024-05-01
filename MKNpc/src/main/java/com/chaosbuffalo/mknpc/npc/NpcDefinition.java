package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class NpcDefinition {
    private static final UUID HEALTH_SCALING_UUID = UUID.fromString("3508a0ad-a2d5-40f2-8ce7-110401cc1a2c");

    private final ResourceLocation definitionName;
    private final ResourceLocation parentName;
    private ResourceLocation entityType;
    private NpcDefinition parent;
    private final Map<ResourceLocation, NpcDefinitionOption> options;


    public NpcDefinition(ResourceLocation definitionName, ResourceLocation entityType, ResourceLocation parentName) {
        this.definitionName = definitionName;
        this.entityType = entityType;
        this.parentName = parentName;
        this.options = new HashMap<>();
    }

    public NpcDefinition(ResourceLocation definitionName, ResourceLocation entityType) {
        this(definitionName, entityType, null);
    }

    public ResourceLocation getDefinitionName() {
        return definitionName;
    }

    boolean hasParentName() {
        return parentName != null;
    }

    public ResourceLocation getParentName() {
        return parentName;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public NpcDefinition getParent() {
        return parent;
    }

    public boolean resolveParents() {
        if (hasParentName()) {
            parent = NpcDefinitionManager.getDefinition(parentName);
            return parent != null && parent.resolveParents();
        }
        return true;
    }

    public void resolveEntityType() {
        if (entityType == null) {
            entityType = getAncestor().getEntityType();
        }
    }

    public ResourceLocation getEntityType() {
        return entityType;
    }

    public NpcDefinition getAncestor() {
        if (!hasParent()) {
            return this;
        } else {
            return getParent().getAncestor();
        }
    }

    @Nullable
    public NpcDefinitionOption getOption(ResourceLocation optionName) {
        NpcDefinitionOption localOption = options.get(optionName);
        if (localOption != null) {
            return localOption;
        }
        else if (hasParent()) {
            return getParent().getOption(optionName);
        }
        return null;
    }

    public void addOption(NpcDefinitionOption option) {
        options.put(option.getName(), option);
    }

    public boolean isNotable() {
        if (getOption(NotableOption.NAME) instanceof NotableOption option) {
            return option.isNotable();
        }
        return false;
    }

    public ResourceLocation getFactionName() {
        if (getOption(FactionOption.NAME) instanceof FactionOption option) {
            return option.getValue();
        }
        return MKFaction.INVALID_FACTION;
    }

    @Nullable
    public String getDisplayName() {
        for (NpcDefinitionOption option : options.values()) {
            if (option instanceof INameProvider provider) {
                return provider.getDisplayName();
            }
        }
        if (hasParent()) {
            return getParent().getDisplayName();
        } else {
            return getEntityType().toString();
        }
    }

    public MutableComponent getNameForEntity(Level world, UUID spawnId) {
        for (NpcDefinitionOption option : options.values()) {
            if (option instanceof INameProvider provider) {
                return provider.getEntityName(this, world, spawnId);
            }
        }
        if (hasParent()) {
            return getParent().getNameForEntity(world, spawnId);
        } else {
            return Component.literal("Name Error");
        }
    }

    public void applyDefinition(Entity entity, double difficultyValue) {
        apply(entity, NpcDefinitionOption.ApplyOrder.EARLY, difficultyValue);
        apply(entity, NpcDefinitionOption.ApplyOrder.MIDDLE, difficultyValue);
        apply(entity, NpcDefinitionOption.ApplyOrder.LATE, difficultyValue);
        applyDifficultyScaling(entity, difficultyValue);

        //We need to apply equipment before the tick so that the following operations reflect correct values
        // hack to make sure we're at our new max health
        if (entity instanceof LivingEntity living) {
            living.setHealth(living.getMaxHealth());
            living.detectEquipmentUpdates();
        }
        MKCore.getEntityData(entity).ifPresent(cap -> {
            cap.getStats().setPoise(cap.getStats().getMaxPoise());
            cap.getStats().setMana(cap.getStats().getMaxMana());
        });
    }

    private void applyDifficultyScaling(Entity entity, double difficultyValue) {
        if (entity instanceof LivingEntity living) {
            double diffScale = difficultyValue / GameConstants.SKILL_POINTS_PER_LEVEL;
            AttributeInstance inst = living.getAttribute(Attributes.MAX_HEALTH);
            if (inst != null) {
                inst.addTransientModifier(new AttributeModifier(
                        HEALTH_SCALING_UUID, "Health Difficulty Scaling",
                        diffScale, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }


    private void apply(Entity entity, NpcDefinitionOption.ApplyOrder order, double difficultyValue) {
        if (hasParent()) {
            getParent().apply(entity, order, difficultyValue);
        }
        for (Map.Entry<ResourceLocation, NpcDefinitionOption> option : options.entrySet()) {
            if (option.getValue().getOrdering() == order) {
                option.getValue().applyToEntity(this, entity, difficultyValue);
            }
        }
    }

    @Nullable
    public Entity createEntity(Level world, Vec3 pos, double difficultyValue) {
        return createEntity(world, pos, UUID.randomUUID(), difficultyValue);
    }

    protected <D> D getDynamicType(DynamicOps<D> ops) {
        if (hasParentName()) {
            return ops.createMap(ImmutableMap.of(
                    ops.createString("parent"), ops.createString(getParentName().toString())
            ));
        } else {
            return ops.createMap(ImmutableMap.of(
                    ops.createString("entityType"), ops.createString(getEntityType().toString())
            ));
        }
    }

    public <D> D serialize(DynamicOps<D> ops) {
        D type = getDynamicType(ops);
        return ops.mergeToMap(type, ImmutableMap.of(
                        ops.createString("options"),
                        ops.createList(options.values().stream().flatMap(entry -> NpcDefinitionOption.CODEC.encodeStart(ops, entry).resultOrPartial(MKNpc.LOGGER::error).stream()))
                )
        ).result().orElse(type);
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        options.clear();
        dynamic.get("options").asStream().forEach(x -> {
            NpcDefinitionOption.CODEC.parse(x).resultOrPartial(MKNpc.LOGGER::error).ifPresent(o -> options.put(o.getName(), o));
        });
    }

    public static <D> NpcDefinition deserializeDefinitionFromDynamic(ResourceLocation name, Dynamic<D> dynamic) {
        ResourceLocation parentName = dynamic.get("parent").asString().result().map(ResourceLocation::new).orElse(null);
        ResourceLocation typeName = dynamic.get("entityType").asString().result().map(ResourceLocation::new).orElse(null);
        NpcDefinition def = new NpcDefinition(name, typeName, parentName);
        def.deserialize(dynamic);
        return def;
    }

    @Nullable
    public Entity createEntity(Level world, Vec3 pos, UUID uuid, double difficultyValue) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(getEntityType());
        if (type != null) {
            Entity entity = type.create(world);
            if (entity == null) {
                return null;
            }
            entity.setPos(pos.x(), pos.y(), pos.z());
            MKNpc.getNpcData(entity).ifPresent(cap -> {
                cap.setDefinition(this);
                cap.setSpawnID(uuid);
                cap.setDifficultyValue(difficultyValue);
            });
            applyDefinition(entity, difficultyValue);
            if (entity instanceof MKEntity mkEntity) {
                mkEntity.postDefinitionApply(this);
            }
            return entity;
        }
        return null;
    }
}
