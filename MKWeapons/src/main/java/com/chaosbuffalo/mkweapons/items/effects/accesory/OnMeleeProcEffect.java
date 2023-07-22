package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.EntityTargetingAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableDoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ScalableFloatAttribute;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class OnMeleeProcEffect extends BaseAccessoryEffect {
    protected ScalableDoubleAttribute procChance = new ScalableDoubleAttribute("proc_chance", 0.05, 0.05);

    protected ScalableFloatAttribute skillLevel = new ScalableFloatAttribute("skill_level", 0.0f, 0.0f);
    private Supplier<? extends EntityTargetingAbility> abilitySupplier;

    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID,
            "accessory_effect.on_hit_ability");

    public OnMeleeProcEffect(double procChanceMin, double procChanceMax, float skillLevelMin, float skillLevelMax,
                             Supplier<? extends EntityTargetingAbility> abilitySupplier) {
        this();
        this.procChance.setValue(procChanceMin);
        this.procChance.setMin(procChanceMin);
        this.procChance.setMax(procChanceMax);
        this.skillLevel.setMin(skillLevelMin);
        this.skillLevel.setMax(skillLevelMax);
        this.skillLevel.setValue(skillLevelMin);
        this.abilitySupplier = abilitySupplier;
    }

    public OnMeleeProcEffect() {
        super(NAME, ChatFormatting.GOLD);
        addAttributes(procChance, skillLevel);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        dynamic.get("ability").asString().result()
                .ifPresent(abilityName -> {
                    MKAbility ability = MKCoreRegistry.ABILITIES.getValue(new ResourceLocation(abilityName));
                    if (ability instanceof EntityTargetingAbility entAbility) {
                        abilitySupplier = Lazy.of(() -> entAbility);
                    }
                });
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        if (abilitySupplier.get() != null) {
            builder.put(ops.createString("ability"),
                    ops.createString(abilitySupplier.get().getAbilityId().toString()));
        }

    }

    @Override
    public void onMeleeHit(IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, IMKEntityData attackerData) {
        if (attackerData.getEntity().getRandom().nextDouble() >= (1.0 - procChance.value())) {
            abilitySupplier.get().castAtEntity(attackerData, target, attr -> skillLevel.value());
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        MKAbility ability = abilitySupplier.get();
        if (ability == null)
            return;

        tooltip.add(Component.translatable(String.format("%s.%s.name",
                this.getTypeName().getNamespace(), this.getTypeName().getPath()), ability.getAbilityName()).withStyle(color));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(String.format("%s.%s.description",
                            this.getTypeName().getNamespace(), this.getTypeName().getPath()),
                    MKAbility.PERCENT_FORMATTER.format(procChance.value()), MKAbility.NUMBER_FORMATTER.format(skillLevel.value())));
            if (player != null) {
                MKCore.getEntityData(player).ifPresent(entityData -> {
                    tooltip.add(ability.getAbilityDescription(entityData, attr -> skillLevel.value()));
                });
            }
        }
    }
}
