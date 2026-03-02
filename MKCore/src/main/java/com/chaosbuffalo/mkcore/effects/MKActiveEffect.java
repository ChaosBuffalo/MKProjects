package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class MKActiveEffect {

    private final UUID sourceId;
    private final MKEffect effect;
    private final Lazy<MobEffectInstance> displayEffectInstance = Lazy.of(() -> createDisplayEffectInstance(this));
    private final MKEffectState state;
    private final MKEffectBehaviour behaviour;
    private final Object2FloatMap<Holder<Attribute>> attributeSkillSnapshot;
    private int stackCount;
    private float skillLevel;
    @Nullable
    private ResourceLocation abilityId;
    @Nullable
    private LivingEntity sourceEntity;
    @Nullable
    private Entity directEntity;
    @Nullable
    private UUID directUUID;

    // Builder
    public MKActiveEffect(MKEffectBuilder<?> builder, MKEffectState state) {
        sourceId = builder.getSourceId();
        effect = builder.getEffect();
        this.behaviour = new MKEffectBehaviour(builder.getBehaviour());
        stackCount = builder.getInitialStackCount();
        skillLevel = builder.getSkillLevel();
        this.state = state;
        abilityId = builder.getAbilityId();
        sourceEntity = builder.getSourceEntity();
        directEntity = builder.getDirectEntity();
        Map<Holder<Attribute>, MKEffect.Modifier> modifierMap = effect.getAttributeModifierMap();
        attributeSkillSnapshot = new Object2FloatOpenHashMap<>(modifierMap.size());
        if (sourceEntity != null) {
            for (MKEffect.Modifier modifier : modifierMap.values()) {
                if (modifier.skill != null) {
                    attributeSkillSnapshot.put(modifier.skill, MKAbility.getSkillLevel(sourceEntity, modifier.skill));
                }
            }
        }
        if (directEntity != null) {
            directUUID = directEntity.getUUID();
        }
    }

    // Deserialize
    public MKActiveEffect(MKEffect effect, UUID sourceId) {
        this.sourceId = sourceId;
        this.effect = effect;
        this.behaviour = new MKEffectBehaviour();
        stackCount = 1;
        skillLevel = 0.0f;
        this.state = effect.makeState();
        attributeSkillSnapshot = new Object2FloatOpenHashMap<>(effect.getAttributeModifierMap().size());
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public MKEffectState getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    public <T extends MKEffectState> T getState(@SuppressWarnings("unused") TypeToken<T> typeBound) {
        return (T) getState();
    }

    public ResourceLocation getAbilityId() {
        if (abilityId == null)
            return MKCoreRegistry.INVALID_ABILITY;
        return abilityId;
    }

    public MKEffect getEffect() {
        return effect;
    }

    public MKEffectBehaviour getBehaviour() {
        return behaviour;
    }

    public MKEffectTickAction tick(IMKEntityData entityData) {
        return behaviour.behaviourTick(entityData, this);
    }

    public int getDuration() {
        return behaviour.getDuration();
    }

    public void setDuration(int duration) {
        behaviour.setDuration(duration);
    }

    public void modifyDuration(int delta) {
        behaviour.modifyDuration(delta);
    }

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int count) {
        stackCount = count;
    }

    public void setSkillLevel(float skillLevel) {
        this.skillLevel = skillLevel;
    }

    public float getSkillLevel() {
        return skillLevel;
    }

    public void modifyStackCount(int delta) {
        stackCount += delta;
    }

    @Nullable
    public LivingEntity getSourceEntity() {
        return sourceEntity;
    }

    public boolean hasSourceEntity() {
        return getSourceEntity() != null;
    }

    @Nullable
    public Entity getDirectEntity() {
        if (directEntity == null)
            return sourceEntity;
        return directEntity;
    }

    public boolean hasDirectEntity() {
        return getDirectEntity() != null;
    }

    public void recoverState(IMKEntityData targetData) {
        Entity rawSource = findEntity(sourceEntity, getSourceId(), targetData);
        if (rawSource instanceof LivingEntity) {
            sourceEntity = (LivingEntity) rawSource;
        }

        if (directEntity == null && directUUID != null) {
            directEntity = findEntity(null, directUUID, targetData);
        }
    }

    @Nullable
    protected Entity findEntity(Entity entity, UUID entityId, IMKEntityData targetData) {
        if (entity != null)
            return entity;
        Level world = targetData.getEntity().getCommandSenderWorld();
        if (!world.isClientSide()) {
            return ((ServerLevel) world).getEntity(entityId);
        }
        return null;
    }

    // Only need to share what is used by MKMobEffectInstance
    public CompoundTag serializeClient() {
        CompoundTag stateTag = new CompoundTag();
        stateTag.put("behaviour", behaviour.serialize());
        stateTag.putInt("stacks", getStackCount());
        return stateTag;
    }

    private void deserializeClient(CompoundTag stateTag) {
        behaviour.deserializeState(stateTag.getCompound("behaviour"));
        stackCount = stateTag.getInt("stacks");
    }

    public static MKActiveEffect deserializeClient(ResourceLocation effectId, UUID sourceId, CompoundTag tag) {
        MKEffect effect = MKCoreRegistry.EFFECTS.get(effectId);
        if (effect == null) {
            return null;
        }

        MKActiveEffect active = effect.createInstance(sourceId);
        active.deserializeClient(tag);
        return active;
    }

    public float getAttributeSkillLevel(Holder<Attribute> skill) {
        return attributeSkillSnapshot.getOrDefault(skill, 0f);
    }

    public CompoundTag serializeStorage() {
        CompoundTag tag = new CompoundTag();
        tag.put("behaviour", behaviour.serialize());
        tag.putInt("stacks", getStackCount());
        tag.putFloat("skillLevel", getSkillLevel());
        if (abilityId != null) {
            tag.putString("abilityId", abilityId.toString());
        }
        if (directUUID != null) {
            tag.putUUID("directEntity", directUUID);
        }
        if (!attributeSkillSnapshot.isEmpty()) {
            CompoundTag attrTag = new CompoundTag();
            // FIXME: We should make these serialize holders
            attributeSkillSnapshot.object2FloatEntrySet().forEach(entry -> {
                ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey().value());
                if (attrId != null) {
                    attrTag.putFloat(attrId.toString(), entry.getFloatValue());
                }
            });
            tag.put("attrSkills", attrTag);
        }

        serializeId(tag);
        CompoundTag stateTag = new CompoundTag();
        state.serializeStorage(stateTag);
        if (!stateTag.isEmpty()) {
            tag.put("state", stateTag);
        }
        return tag;
    }

    private void deserializeStorage(CompoundTag tag) {
        stackCount = tag.getInt("stacks");
        skillLevel = tag.getFloat("skillLevel");
        behaviour.deserializeState(tag.getCompound("behaviour"));
        if (tag.contains("abilityId")) {
            abilityId = ResourceLocation.tryParse(tag.getString("abilityId"));
        }
        if (tag.contains("state")) {
            state.deserializeStorage(tag.getCompound("state"));
        }
        if (tag.contains("directEntity")) {
            directUUID = tag.getUUID("directEntity");
        }
        if (tag.contains("attrSkills")) {
            CompoundTag attrTag = tag.getCompound("attrSkills");
            for (String key : attrTag.getAllKeys()) {
                ResourceLocation attrLoc = ResourceLocation.parse(key);
                // FIXME: We should make these serialize holders
                Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(ResourceKey.create(Registries.ATTRIBUTE, attrLoc));
                if (attribute.isBound()) {
                    float val = attrTag.getFloat(key);
                    attributeSkillSnapshot.put(attribute, val);
                }
            }
        }
    }

    public static MKActiveEffect deserializeStorage(UUID sourceId, CompoundTag tag) {
        ResourceLocation effectId = deserializeId(tag);

        MKEffect effect = MKCoreRegistry.EFFECTS.get(effectId);
        if (effect == null) {
            return null;
        }

        MKActiveEffect active = effect.createInstance(sourceId);
        active.deserializeStorage(tag);
        if (!active.getState().validateOnLoad(active)) {
            MKCore.LOGGER.warn("Effect {} failed load validation", active);
            return null;
        }
        return active;
    }

    private void serializeId(CompoundTag nbt) {
        MKNBTUtil.writeResourceLocation(nbt, "effectId", effect.getId());
    }

    private static ResourceLocation deserializeId(CompoundTag tag) {
        return MKNBTUtil.readResourceLocation(tag, "effectId");
    }

    @Override
    public String toString() {
        return "MKActiveEffect{" +
                "sourceId=" + sourceId +
                ", effect=" + effect +
                ", behaviour=" + behaviour +
                ", stackCount=" + stackCount +
                ", abilityId=" + getAbilityId() +
                ", skillLevel=" + skillLevel +
                '}';
    }

    // Only called for the local player on the client
    public MobEffectInstance getClientDisplayEffectInstance() {
        return displayEffectInstance.get();
    }

    public static class MKMobEffectInstance extends MobEffectInstance {
        protected final MKActiveEffect effectInstance;

        public MKMobEffectInstance(MKActiveEffect effectInstance) {
            super(effectInstance.getEffect().getVanillaWrapper());
            this.effectInstance = effectInstance;
        }

        @Override
        public Holder<MobEffect> getEffect() {
            // Stop the call to the MobEffects registry
            return effectInstance.getEffect().getVanillaWrapper();
        }

        public MKActiveEffect getEffectInstance() {
            return effectInstance;
        }

        @Override
        public boolean isInfiniteDuration() {
            return effectInstance.getBehaviour().isInfinite();
        }

        @Override
        public int getDuration() {
            // Even though we override getIsPotionDurationMax we still need a large number so the
            // in-game GUI doesn't flash continuously
            if (isInfiniteDuration())
                return Integer.MAX_VALUE;
            return effectInstance.getBehaviour().getDuration();
        }

        @Override
        public int getAmplifier() {
            // "Amplifier" in vanilla is the number of ranks above 1
            return Math.max(0, effectInstance.getStackCount() - 1);
        }
    }

    private static MobEffectInstance createDisplayEffectInstance(MKActiveEffect effectInstance) {
        return new MKMobEffectInstance(effectInstance);
    }
}
