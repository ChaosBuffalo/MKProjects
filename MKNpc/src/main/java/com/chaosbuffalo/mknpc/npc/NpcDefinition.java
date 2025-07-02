package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class NpcDefinition {
    private static final ResourceLocation HEALTH_SCALING_MOD_ID = MKNpc.id("health_difficulty_scaling");
    public static final Codec<NpcDefinition> CODEC = RecordCodecBuilder.<NpcDefinition>mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(NpcDefinition::getDefinitionName),
            NeoForgeExtraCodecs.xor(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType"),
                    ResourceLocation.CODEC.fieldOf("parent")
            ).forGetter(i -> i.parentName != null ? Either.right(i.parentName) : Either.left(i.entityType)),
            NpcDefinitionOption.OPTION_MAP_CODEC.fieldOf("options").forGetter(i -> i.options)
    ).apply(builder, NpcDefinition::new)).codec();

    private final ResourceLocation definitionName;
    @Nullable
    private final ResourceLocation parentName;
    @Nullable
    private EntityType<?> entityType;
    private NpcDefinition parent;
    private final Map<NpcOptionType<?>, NpcDefinitionOption> options;

    public NpcDefinition(ResourceLocation definitionName, EntityType<?> entityType) {
        this(definitionName, Either.left(entityType), new HashMap<>());
    }

    public NpcDefinition(ResourceLocation definitionName, Holder<EntityType<?>> entityType) {
        this(definitionName, entityType.value());
    }

    public NpcDefinition(ResourceLocation definitionName, Either<EntityType<?>, ResourceLocation> either,
                          Map<NpcOptionType<?>, NpcDefinitionOption> options) {
        this.definitionName = definitionName;

        this.entityType = either.left().orElse(null);
        this.parentName = either.right().orElse(null);
        this.options = new HashMap<>(options);
    }

    public static NpcDefinition derived(ResourceLocation definitionName, ResourceLocation parentName) {
        return new NpcDefinition(definitionName, Either.right(parentName), Map.of());
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

    public boolean resolveParents(Registry<NpcDefinition> npcRegistry) {
        if (hasParentName()) {
            parent = npcRegistry.get(parentName);
            return parent != null && parent.resolveParents(npcRegistry);
        }
        return true;
    }

    public void resolveEntityType() {
        if (entityType == null) {
            entityType = getAncestor().getEntityType();
        }
    }

    public EntityType<?> getEntityType() {
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
    public NpcDefinitionOption getOption(NpcOptionType<?> optionName) {
        NpcDefinitionOption localOption = options.get(optionName);
        if (localOption != null) {
            return localOption;
        } else if (hasParent()) {
            return getParent().getOption(optionName);
        }
        return null;
    }

    @Nullable
    public <T extends NpcDefinitionOption> T getOption(Supplier<NpcOptionType<T>> optionName) {
        //noinspection unchecked
        return (T) getOption(optionName.get());
    }

    public void addOption(NpcDefinitionOption option) {
        options.put(option.getType(), option);
    }

    public boolean isNotable() {
        if (getOption(NpcOptionTypes.NOTABLE) instanceof NotableOption option) {
            return option.isNotable();
        }
        return false;
    }

    public ResourceLocation getFactionName() {
        if (getOption(NpcOptionTypes.FACTION) instanceof FactionOption option) {
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

            var entityData = MKCore.getEntityDataOrThrow(living);
            entityData.getStats().setPoise(entityData.getStats().getMaxPoise());
            entityData.getStats().setMana(entityData.getStats().getMaxMana());
        }
    }

    private void applyDifficultyScaling(Entity entity, double difficultyValue) {
        if (entity instanceof LivingEntity living) {
            double diffScale = difficultyValue / GameConstants.SKILL_POINTS_PER_LEVEL;
            AttributeInstance inst = living.getAttribute(Attributes.MAX_HEALTH);
            if (inst != null) {
                inst.addTransientModifier(new AttributeModifier(
                        HEALTH_SCALING_MOD_ID, diffScale, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }


    private void apply(Entity entity, NpcDefinitionOption.ApplyOrder order, double difficultyValue) {
        if (hasParent()) {
            getParent().apply(entity, order, difficultyValue);
        }
        for (NpcDefinitionOption option : options.values()) {
            if (option.getOrdering() == order) {
                option.applyToEntity(this, entity, difficultyValue);
            }
        }
    }

    @Nullable
    public Entity createEntity(Level world, Vec3 pos, double difficultyValue) {
        return createEntity(world, pos, UUID.randomUUID(), difficultyValue);
    }

    @Nullable
    public Entity createEntity(Level world, Vec3 pos, UUID uuid, double difficultyValue) {
        EntityType<?> type = getEntityType();
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
