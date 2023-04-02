package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.client.effects.MKEffectRenderer;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class MKEffect extends ForgeRegistryEntry<MKEffect> {

    public static class Modifier {
        public final AttributeModifier attributeModifier;
        public final double base;
        @Nullable
        public final Attribute skill;

        public Modifier(UUID uuid, Supplier<String> nameProvider, double base, double amount,
                        AttributeModifier.Operation operation, @Nullable Attribute skill) {
            attributeModifier = new AttributeModifier(uuid, nameProvider, amount, operation);
            this.base = base;
            this.skill = skill;
        }
    }

    @Nullable
    protected String name;
    protected final Lazy<MobEffect> wrapperEffect = Lazy.of(() -> new WrapperEffect(this));
    protected final MobEffectCategory effectType;
    private final Map<Attribute, Modifier> attributeModifierMap = new HashMap<>();

    public MKEffect(MobEffectCategory effectType) {
        this.effectType = effectType;
    }

    @Nonnull
    public ResourceLocation getId() {
        return Objects.requireNonNull(MKCoreRegistry.EFFECTS.getKey(this));
    }

    protected String getOrCreateDescriptionId() {
        if (name == null) {
            name = Util.makeDescriptionId("mk_effect", getId());
        }
        return name;
    }

    public String getName() {
        return getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return new TranslatableComponent(getName());
    }

    public boolean isValidTarget(TargetingContext targetContext, IMKEntityData sourceData, IMKEntityData targetData) {
        return Targeting.isValidTarget(targetContext, sourceData.getEntity(), targetData.getEntity());
    }

    // Effect was added while the entity was in the world
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        if (hasAttributes()) {
            applyAttributesModifiers(targetData, newInstance);
        }
    }

    // Effect was updated while the entity was in the world
    public void onInstanceUpdated(IMKEntityData targetData, MKActiveEffect activeEffect) {
        if (hasAttributes()) {
            removeAttributesModifiers(targetData);
            applyAttributesModifiers(targetData, activeEffect);
        }
    }

    // Effect was removed while the entity was in the world
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        if (hasAttributes()) {
            removeAttributesModifiers(targetData);
        }
    }

    // Entity not yet in world when this is called. Called during deserialization from NBT
    public void onInstanceLoaded(IMKEntityData targetData, MKActiveEffect activeInstance) {
        MKCore.LOGGER.debug("MKEffect.onInstanceLoaded {}", activeInstance);
    }

    // Entity is about to be added to the world, but has NOT been added to the UUID map
    // Do not attempt to locate other entities here
    public void onInstanceReady(IMKEntityData targetData, MKActiveEffect activeInstance) {
        MKCore.LOGGER.debug("MKEffect.onInstanceReady {}", activeInstance);
    }

    public MKEffectBuilder<?> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public MKEffectBuilder<?> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public abstract MKEffectState makeState();

    public MKActiveEffect createInstance(UUID sourceId) {
        return new MKActiveEffect(this, sourceId);
    }

    public Map<Attribute, Modifier> getAttributeModifierMap() {
        return attributeModifierMap;
    }

    private boolean hasAttributes() {
        return attributeModifierMap.size() > 0;
    }

    public MKEffect addAttribute(Attribute attribute, UUID uuid, double amount, AttributeModifier.Operation operation) {
        return this.addAttribute(attribute, uuid, amount, amount, operation, null);
    }

    public MKEffect addAttribute(Attribute attribute, UUID uuid, double base, double amount,
                                 AttributeModifier.Operation operation, @Nullable Attribute skill) {
        attributeModifierMap.put(attribute, new Modifier(uuid, this::getName, base, amount, operation, skill));
        return this;
    }

    protected void removeAttributesModifiers(IMKEntityData targetData) {
        AttributeMap manager = targetData.getEntity().getAttributes();
        for (Map.Entry<Attribute, Modifier> entry : getAttributeModifierMap().entrySet()) {
            AttributeInstance attrInstance = manager.getInstance(entry.getKey());
            if (attrInstance != null) {
                attrInstance.removeModifier(entry.getValue().attributeModifier);
            }
        }
    }

    protected void applyAttributesModifiers(IMKEntityData targetData, MKActiveEffect activeEffect) {
        AttributeMap manager = targetData.getEntity().getAttributes();
        for (Map.Entry<Attribute, Modifier> entry : getAttributeModifierMap().entrySet()) {
            AttributeInstance attrInstance = manager.getInstance(entry.getKey());
            if (attrInstance != null) {
                Modifier template = entry.getValue();
                attrInstance.removeModifier(template.attributeModifier);
                attrInstance.addPermanentModifier(createModifier(template, activeEffect));
            }
        }
    }

    private AttributeModifier createModifier(Modifier template, MKActiveEffect activeEffect) {
        int stacks = activeEffect.getStackCount();

        double amount = calculateInstanceModifierValue(template, activeEffect);
        return new AttributeModifier(template.attributeModifier.getId(), () -> getName() + " " + stacks,
                amount, template.attributeModifier.getOperation());
    }

    public double calculateModifierValue(Modifier modifier, int stackCount, float skillLevel) {
        return modifier.base + (modifier.attributeModifier.getAmount() * stackCount * skillLevel);
    }

    protected double calculateInstanceModifierValue(Modifier modifier, MKActiveEffect activeEffect) {
        return calculateModifierValue(modifier, activeEffect.getStackCount(),
                modifier.skill != null ? activeEffect.getAttributeSkillLevel(modifier.skill) : 0.0f);
    }

    /**
     * If the effect should be displayed in the players inventory
     *
     * @param effect the active MKEffect
     * @return true to display it (default), false to hide it.
     */
    public boolean shouldRender(MKActiveEffect effect) {
        return true;
    }

    /**
     * If the standard text (name and duration) should be drawn when this potion is active.
     *
     * @param effect the active MKEffect
     * @return true to draw the standard text
     */
    public boolean shouldRenderInvText(MKActiveEffect effect) {
        return true;
    }

    /**
     * If the effect should be displayed in the player's ingame HUD
     *
     * @param effect the active MKEffect
     * @return true to display it (default), false to hide it.
     */
    public boolean shouldRenderHUD(MKActiveEffect effect) {
        return true;
    }

    @Override
    public String toString() {
        return "MKEffect{" + getId() + "}";
    }

    // Keep this package-private so no one calls it by accident
    MobEffect getVanillaWrapper() {
        return wrapperEffect.get();
    }

    public static class WrapperEffect extends MobEffect {

        private final MKEffect effect;
        private Object effectRendererReal;

        protected WrapperEffect(MKEffect effect) {
            super(effect.effectType, 0);
            this.effect = effect;
            initClientReal();
        }

        private void initClientReal() {
            // we gotta do this again
            // Minecraft instance isn't available in datagen, so don't call initializeClient if in datagen
            if (FMLEnvironment.dist == Dist.CLIENT && !FMLLoader.getLaunchHandler().isData()) {
                initializeClient(properties -> {
                    this.effectRendererReal = properties;
                });
            }
        }

        @Override
        public Object getEffectRendererInternal() {
            return effectRendererReal;
        }

        public MKEffect getMKEffect() {
            return effect;
        }

        @Nonnull
        @Override
        public String getDescriptionId() {
            return effect.getName();
        }

        @Nonnull
        @Override
        public Component getDisplayName() {
            return effect.getDisplayName();
        }

        @Override
        public List<ItemStack> getCurativeItems() {
            return Collections.emptyList();
        }

        public void initializeClient(Consumer<EffectRenderer> consumer) {
            consumer.accept(new MKEffectRenderer(effect));
        }
    }
}
