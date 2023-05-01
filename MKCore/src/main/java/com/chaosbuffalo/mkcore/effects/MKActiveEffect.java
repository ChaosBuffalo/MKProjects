package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class MKActiveEffect {

    private final UUID sourceId;
    private final MKEffect effect;
    private final Lazy<MobEffectInstance> displayEffectInstance = Lazy.of(() -> createDisplayEffectInstance(this));
    private final MKEffectState state;
    private final MKEffectBehaviour behaviour;
    private final Object2FloatMap<Attribute> attributeSkillSnapshot;
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

    private boolean needClientUpdate = false;

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
        Map<Attribute, MKEffect.Modifier> modifierMap = effect.getAttributeModifierMap();
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

    public boolean isTickReady() {
        return behaviour.isReady();
    }

    public boolean isTemporary() {
        return behaviour.isTemporary();
    }

    public boolean isTimed() {
        return behaviour.isTimed();
    }

    public boolean isExpired() {
        return behaviour.isExpired();
    }

    public int getDuration() {
        return behaviour.getDuration();
    }

    public void setDuration(int duration) {
        behaviour.setDuration(duration);
        needClientUpdate = true;
    }

    public void modifyDuration(int delta) {
        setDuration(getDuration() + delta);
    }

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int count) {
        stackCount = count;
        needClientUpdate = true;
    }

    public void setSkillLevel(float skillLevel) {
        this.skillLevel = skillLevel;
    }

    public float getSkillLevel() {
        return skillLevel;
    }

    public void modifyStackCount(int delta) {
        setStackCount(stackCount + delta);
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

    public MKEffectTickResult tick(IMKEntityData targetData) {
        MKEffectTickResult result = behaviour.behaviourTick(targetData, this);
        if (!result.sendsClientUpdate() && needClientUpdate) {
            result = MKEffectTickResult.Update;
        }
        needClientUpdate = false;
        return result;
    }

    public CompoundTag serializeClient() {
        CompoundTag stateTag = new CompoundTag();
        stateTag.put("behaviour", behaviour.serialize());
        stateTag.putInt("stacks", getStackCount());
        return stateTag;
    }

    public static MKActiveEffect deserializeClient(MKEffect effect, UUID sourceId, CompoundTag tag) {
        MKActiveEffect active = effect.createInstance(sourceId);
        active.behaviour.deserializeState(tag.getCompound("behaviour"));
        active.stackCount = tag.getInt("stacks");
        return active;
    }

    public CompoundTag serializeState() {
        CompoundTag stateTag = new CompoundTag();
        stateTag.put("behaviour", behaviour.serialize());
        stateTag.putInt("stacks", getStackCount());
        stateTag.putFloat("skillLevel", getSkillLevel());
        if (abilityId != null) {
            stateTag.putString("abilityId", abilityId.toString());
        }
        if (directUUID != null) {
            stateTag.putUUID("directEntity", directUUID);
        }
        if (!attributeSkillSnapshot.isEmpty()) {
            CompoundTag attrTag = new CompoundTag();
            attributeSkillSnapshot.object2FloatEntrySet().forEach(entry -> {
                ResourceLocation attrId = ForgeRegistries.ATTRIBUTES.getKey(entry.getKey());
                if (attrId != null) {
                    attrTag.putFloat(attrId.toString(), entry.getFloatValue());
                }
            });
            stateTag.put("attrSkills", attrTag);
        }

        return stateTag;
    }

    public float getAttributeSkillLevel(Attribute skill) {
        return attributeSkillSnapshot.getOrDefault(skill, 0f);
    }

    public void deserializeState(CompoundTag stateTag) {
        stackCount = stateTag.getInt("stacks");
        skillLevel = stateTag.getFloat("skillLevel");
        behaviour.deserializeState(stateTag.getCompound("behaviour"));
        if (stateTag.contains("abilityId")) {
            abilityId = ResourceLocation.tryParse(stateTag.getString("abilityId"));
        }
        if (stateTag.contains("state")) {
            state.deserializeStorage(stateTag.getCompound("state"));
        }
        if (stateTag.contains("directEntity")) {
            directUUID = stateTag.getUUID("directEntity");
        }
        if (stateTag.contains("attrSkills")) {
            CompoundTag attrTag = stateTag.getCompound("attrSkills");
            for (String key : attrTag.getAllKeys()) {
                ResourceLocation attrLoc = new ResourceLocation(key);
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attrLoc);
                if (attribute != null) {
                    float val = attrTag.getFloat(key);
                    attributeSkillSnapshot.put(attribute, val);
                }
            }
        }
    }

    public CompoundTag serializeStorage() {
        CompoundTag tag = serializeState();
        serializeId(tag);
        CompoundTag stateTag = new CompoundTag();
        state.serializeStorage(stateTag);
        if (!stateTag.isEmpty()) {
            tag.put("state", stateTag);
        }
        return tag;
    }

    public static MKActiveEffect deserializeStorage(UUID sourceId, CompoundTag tag) {
        ResourceLocation effectId = deserializeId(tag);

        MKEffect effect = MKCoreRegistry.EFFECTS.getValue(effectId);
        if (effect == null) {
            return null;
        }

        MKActiveEffect active = effect.createInstance(sourceId);
        active.deserializeState(tag);
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
        public MobEffect getEffect() {
            // Stop the call to the MobEffects registry
            return effectInstance.getEffect().getVanillaWrapper();
        }

        public MKActiveEffect getEffectInstance() {
            return effectInstance;
        }

        @Override
        public boolean isInfiniteDuration() {
            return effectInstance.behaviour.isInfinite();
        }

        @Override
        public int getDuration() {
            // Even though we override getIsPotionDurationMax we still need a large number so the
            // in-game GUI doesn't flash continuously
            if (isInfiniteDuration())
                return Integer.MAX_VALUE;
            return effectInstance.behaviour.getDuration();
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
