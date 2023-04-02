package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class NpcDefinition {
    private ResourceLocation entityType;
    private final ResourceLocation definitionName;
    private final ResourceLocation parentName;
    public static final NpcDefinitionOption INVALID_OPTION = new InvalidOption();
    private NpcDefinition parent;
    private final Map<ResourceLocation, NpcDefinitionOption> options;
    private static final List<NpcDefinitionOption.ApplyOrder> orders = new ArrayList<>();
    private static final UUID HEALTH_SCALING_UUID = UUID.fromString("3508a0ad-a2d5-40f2-8ce7-110401cc1a2c");
    static {
        orders.add(NpcDefinitionOption.ApplyOrder.EARLY);
        orders.add(NpcDefinitionOption.ApplyOrder.MIDDLE);
        orders.add(NpcDefinitionOption.ApplyOrder.LATE);
    }

    boolean hasParentName(){
        return parentName != null;
    }

    public NpcDefinition(ResourceLocation definitionName, ResourceLocation entityType, ResourceLocation parentName){
        this.definitionName = definitionName;
        this.entityType = entityType;
        this.parentName = parentName;
        this.options = new HashMap<>();
    }


    public boolean isNotable() {
        if (hasOption(NotableOption.NAME)){
            NotableOption option = (NotableOption) getOption(NotableOption.NAME);
            return option.getValue();
        }
        return false;
    }

    public boolean hasParent(){
        return parent != null;
    }

    public boolean isWorldPermanent(){
        for (NpcDefinitionOption option : options.values()){
            if (option instanceof WorldPermanentOption){
                return true;
            }
        }
        if (hasParent()){
            return getParent().isWorldPermanent();
        }
        return false;
    }

    public NpcDefinition getParent() {
        return parent;
    }

    public boolean resolveParents(){
        if (hasParentName()){
            parent = NpcDefinitionManager.getDefinition(parentName);
            return parent != null && parent.resolveParents();
        }
        return true;
    }

    public void resolveEntityType(){
        if (entityType == null){
            entityType = getAncestor().getEntityType();
        }
    }

    public NpcDefinition getAncestor(){
        if (!hasParent()){
            return this;
        } else {
            return getParent().getAncestor();
        }
    }

    public ResourceLocation getParentName() {
        return parentName;
    }

    public ResourceLocation getDefinitionName() {
        return definitionName;
    }

    public boolean hasOption(ResourceLocation optionName){
        return options.containsKey(optionName) ||  (hasParent() && parent.hasOption(optionName));
    }

    public NpcDefinitionOption getOption(ResourceLocation optionName){
        if (!hasOption(optionName)){
            return null;
        }
        if (options.containsKey(optionName)){
            return options.get(optionName);
        } else if (hasParent()){
            return getParent().getOption(optionName);
        }
        return null;
    }

    public void addOption(NpcDefinitionOption option){
        options.put(option.getName(), option);
    }

    public ResourceLocation getFactionName(){
        if (hasOption(FactionOption.NAME)){
            FactionOption option = (FactionOption) getOption(FactionOption.NAME);
            return option.getValue();
        }
        return MKFaction.INVALID_FACTION;
    }

    @Nullable
    public String getDisplayName(){
        for (NpcDefinitionOption option : options.values()){
            if (option instanceof INameProvider){
                return ((INameProvider) option).getDisplayName();
            }
        }
        if (hasParent()){
            return getParent().getDisplayName();
        } else {
            return getEntityType().toString();
        }
    }


    public ResourceLocation getEntityType() {
        return entityType;
    }

    public TextComponent getNameForEntity(Level world, UUID spawnId){
        for (NpcDefinitionOption option : options.values()){
            if (option instanceof INameProvider){
                return ((INameProvider) option).getEntityName(this, world, spawnId);
            }
        }
        if (hasParent()){
            return getParent().getNameForEntity(world, spawnId);
        } else {
            return new TextComponent("Name Error");
        }
    }

    public void applyDefinition(Entity entity, double difficultyValue){
        for (NpcDefinitionOption.ApplyOrder order : orders){
            apply(entity, order, difficultyValue);
        }
        applyDifficultyScaling(entity, difficultyValue);
        // hack to make sure we're at our new max health
        if (entity instanceof LivingEntity){
            ((LivingEntity) entity).setHealth(((LivingEntity) entity).getMaxHealth());
        }
    }

    private void applyDifficultyScaling(Entity entity, double difficultyValue) {
        if (entity instanceof LivingEntity) {
            double diffScale = difficultyValue / GameConstants.SKILL_POINTS_PER_LEVEL;
            AttributeMap manager =((LivingEntity) entity).getAttributes();
            AttributeInstance inst = manager.getInstance(Attributes.MAX_HEALTH);
            if (inst != null) {
                inst.addTransientModifier(new AttributeModifier(
                        HEALTH_SCALING_UUID, "Health Difficulty Scaling",
                        diffScale, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }


    private void apply(Entity entity, NpcDefinitionOption.ApplyOrder order, double difficultyValue){
        if (hasParent()){
            getParent().apply(entity, order, difficultyValue);
        }
        for (Map.Entry<ResourceLocation, NpcDefinitionOption> option : options.entrySet()){
            if (option.getValue().getOrdering() == order){
                option.getValue().applyToEntity(this, entity, difficultyValue);
            }
        }
    }

    @Nullable
    public Entity createEntity(Level world, Vec3 pos, double difficultyValue){
        return createEntity(world, pos, UUID.randomUUID(), difficultyValue);
    }

    protected <D> D getDynamicType(DynamicOps<D> ops){
        if (hasParentName()){
            return ops.createMap(ImmutableMap.of(
                    ops.createString("parent"), ops.createString(getParentName().toString())
            ));
        } else {
            return ops.createMap(ImmutableMap.of(
                    ops.createString("entityType"), ops.createString(getEntityType().toString())
            ));
        }
    }

    public <D> D serialize(DynamicOps<D> ops){
        D type = getDynamicType(ops);
        return ops.mergeToMap(type, ImmutableMap.of(
                ops.createString("options"),
                ops.createList(options.values().stream().map(entry -> entry.serialize(ops)))
                )
        ).result().orElse(type);

    }

    public <D> void deserialize(Dynamic<D> dynamic){
        List<NpcDefinitionOption> newOptions = dynamic.get("options").asList(valueD -> {
            ResourceLocation type = NpcDefinitionOption.getType(valueD);
            NpcDefinitionOption opt = NpcDefinitionManager.getNpcOption(type);
            if (opt != null){
                opt.deserialize(valueD);
            }
            return opt != null ? opt : INVALID_OPTION;
        });
        options.clear();
        for (NpcDefinitionOption option : newOptions){
            if (!option.getName().equals(NpcDefinitionOption.INVALID_OPTION) && !option.equals(INVALID_OPTION)){
                options.put(option.getName(), option);
            }
        }
    }

    public static <D> NpcDefinition deserializeDefinitionFromDynamic(ResourceLocation name, Dynamic<D> dynamic){
        ResourceLocation parentName = dynamic.get("parent").asString().result().map(ResourceLocation::new).orElse(null);
        ResourceLocation typeName = dynamic.get("entityType").asString().result().map(ResourceLocation::new).orElse(null);
        NpcDefinition def = new NpcDefinition(name, typeName, parentName);
        def.deserialize(dynamic);
        return def;
    }

    @Nullable
    public Entity createEntity(Level world, Vec3 pos, UUID uuid, double difficultyValue){
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(getEntityType());
        if (type != null){
            Entity entity = type.create(world);
            if (entity == null){
                return null;
            }
            entity.setPos(pos.x(), pos.y(), pos.z());
            MKNpc.getNpcData(entity).ifPresent(cap -> {
                cap.setDefinition(this);
                cap.setSpawnID(uuid);
            });
            applyDefinition(entity, difficultyValue);
            if (entity instanceof MKEntity){
                ((MKEntity) entity).postDefinitionApply(this);
            }
            return entity;
        }
        return null;
    }
}
