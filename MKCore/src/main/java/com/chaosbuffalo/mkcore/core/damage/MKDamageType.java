package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;


public class MKDamageType extends ForgeRegistryEntry<MKDamageType> {
    private final Attribute damageAttribute;
    private final Attribute resistanceAttribute;
    private final Attribute critAttribute;
    private final Attribute critMultiplierAttribute;
    private final ResourceLocation iconLoc;
    private float critMultiplier;
    private boolean shouldDisplay;
    private final ChatFormatting formatting;

    public MKDamageType(ResourceLocation name, Attribute damageAttribute,
                        Attribute resistanceAttribute, Attribute critAttribute,
                        Attribute critMultiplierAttribute, ChatFormatting formatting) {
        setRegistryName(name);
        this.damageAttribute = damageAttribute;
        this.resistanceAttribute = resistanceAttribute;
        this.critMultiplierAttribute = critMultiplierAttribute;
        this.critAttribute = critAttribute;
        this.critMultiplier = 1.0f;
        this.shouldDisplay = true;
        this.formatting = formatting;
        iconLoc = new ResourceLocation(name.getNamespace(), String.format("textures/damage_types/%s.png",
                name.getPath().substring(7)));
    }

    @Nonnull
    public ResourceLocation getId() {
        return Objects.requireNonNull(MKCoreRegistry.DAMAGE_TYPES.getKey(this));
    }

    public MKDamageType setCritMultiplier(float value) {
        this.critMultiplier = value;
        return this;
    }

    public ChatFormatting getFormatting() {
        return formatting;
    }

    public MKDamageType setShouldDisplay(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
        return this;
    }

    public boolean shouldDisplay() {
        return shouldDisplay;
    }

    public MutableComponent getDisplayName() {
        ResourceLocation name = getId();
        return new TranslatableComponent(String.format("%s.%s.name", name.getNamespace(), name.getPath()));
    }

    public ResourceLocation getIcon() {
        return iconLoc;
    }

    public Attribute getDamageAttribute() {
        return damageAttribute;
    }

    public Attribute getCritChanceAttribute() {
        return critAttribute;
    }

    public Attribute getCritMultiplierAttribute() {
        return critMultiplierAttribute;
    }

    public Attribute getResistanceAttribute() {
        return resistanceAttribute;
    }

    public void registerAttributes(Consumer<Attribute> attributeMap) {
        attributeMap.accept(getDamageAttribute());
        attributeMap.accept(getResistanceAttribute());
    }

    public Component getEffectCritMessage(LivingEntity source, LivingEntity target, float damage,
                                          String damageType, boolean isSelf) {
        TranslatableComponent msg;
        if (isSelf) {
            msg = new TranslatableComponent("mkcore.crit.effect.self",
                    new TranslatableComponent(damageType),
                    target.getDisplayName(),
                    Math.round(damage));
        } else {
            msg = new TranslatableComponent("mkcore.crit.effect.other",
                    source.getDisplayName(),
                    new TranslatableComponent(damageType),
                    target.getDisplayName(),
                    Math.round(damage));
        }
        return msg.withStyle(ChatFormatting.DARK_PURPLE);
    }

    public Component getAbilityCritMessage(LivingEntity source, LivingEntity target, float damage,
                                           MKAbility ability, boolean isSelf) {
        TranslatableComponent msg;
        if (isSelf) {
            msg = new TranslatableComponent("mkcore.crit.ability.self",
                    ability.getAbilityName(),
                    target.getDisplayName(),
                    Math.round(damage));
        } else {
            msg = new TranslatableComponent("mkcore.crit.ability.other",
                    source.getDisplayName(),
                    ability.getAbilityName(),
                    target.getDisplayName(),
                    Math.round(damage));
        }
        return msg.withStyle(ChatFormatting.AQUA);
    }

    public float applyDamage(LivingEntity source, LivingEntity target, float originalDamage, float modifierScaling) {
        return applyDamage(source, target, source, originalDamage, modifierScaling);
    }

    public float applyDamage(LivingEntity source, LivingEntity target, Entity immediate, float originalDamage, float modifierScaling) {
        return (float) (originalDamage + source.getAttribute(getDamageAttribute()).getValue() * modifierScaling);
    }

    public float applyResistance(LivingEntity target, float originalDamage) {
        return (float) (originalDamage - (originalDamage * target.getAttribute(getResistanceAttribute()).getValue()));
    }

    protected boolean canCrit(LivingEntity source) {
        return source instanceof Player;
    }

    public boolean rollCrit(LivingEntity source, LivingEntity target) {
        return rollCrit(source, target, source);
    }

    public boolean rollCrit(LivingEntity source, LivingEntity target, Entity immediate) {
        if (canCrit(source)) {
            float critChance = getCritChance(source, target, immediate);
            return MKCombatFormulas.checkCrit(source, critChance);
        } else {
            return false;
        }
    }

    public float applyCritDamage(LivingEntity source, LivingEntity target, float originalDamage) {
        return applyCritDamage(source, target, source, originalDamage);
    }

    public float applyCritDamage(LivingEntity source, LivingEntity target, Entity immediate, float originalDamage) {
        return originalDamage * getCritMultiplier(source, target, immediate);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target) {
        return getCritMultiplier(source, target, source);
    }

    public float getCritMultiplier(LivingEntity source, LivingEntity target, Entity immediate) {
        return (float) source.getAttribute(getCritMultiplierAttribute()).getValue();
    }

    public float getCritChance(LivingEntity source, LivingEntity target) {
        return getCritChance(source, target, source);
    }

    public float getCritChance(LivingEntity source, LivingEntity target, Entity immediate) {
        return (float) source.getAttribute(getCritChanceAttribute()).getValue() * critMultiplier;
    }
}
