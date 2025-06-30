package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.item.IReceivesSkillChange;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.DoubleUnaryOperator;

public class PlayerSkills implements IMKSerializable<CompoundTag> {

    protected interface SkillChangeHandler {
        void onSkillChange(MKPlayerData playerData, double value);
    }

    private final Persona persona;
    private final Object2DoubleMap<Holder<Attribute>> skillValues = new Object2DoubleOpenHashMap<>();

    private static final Map<Holder<Attribute>, SkillChangeHandler> skillChangeHandlers = Util.make(() -> {
        Map<Holder<Attribute>, SkillChangeHandler> map = new HashMap<>(8);
        map.put(MKAttributes.ONE_HAND_BLUNT, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.TWO_HAND_BLUNT, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.ONE_HAND_SLASH, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.TWO_HAND_SLASH, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.ONE_HAND_PIERCE, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.TWO_HAND_PIERCE, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.MARKSMANSHIP, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.HAND_TO_HAND, PlayerSkills::onUnarmedSkillChange);
        return map;
    });

    public PlayerSkills(Persona persona) {
        this.persona = persona;
    }

    private static void onWeaponSkillChange(MKPlayerData playerData, double value) {
        ItemStack mainHand = playerData.getEntity().getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainHand.getItem() instanceof IReceivesSkillChange receiver) {
            receiver.onSkillChange(mainHand, playerData.getEntity());
        }
    }

    private static void onUnarmedSkillChange(MKPlayerData playerData, double value) {
        ItemStack mainHand = playerData.getEntity().getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainHand.getItem() instanceof IReceivesSkillChange receiver) {
            receiver.onSkillChange(mainHand, playerData.getEntity());
        } else if (mainHand.isEmpty()) {
            playerData.getEquipment().removeUnarmedModifier();
            playerData.getEquipment().addUnarmedModifier();
        }
    }

    public void onCastAbility(MKAbility cast) {
        for (Holder<Attribute> attribute : cast.getSkillAttributes()) {
            tryIncreaseSkill(attribute);
        }
    }

    public void onPersonaActivated() {
        for (Object2DoubleMap.Entry<Holder<Attribute>> entry : skillValues.object2DoubleEntrySet()) {
            setSkill(entry.getKey(), entry.getDoubleValue(), false);
        }
    }

    public void onPersonaDeactivated() {
        for (Holder<Attribute> key : skillValues.keySet()) {
            setSkill(key, 0.0, false);
        }
    }

    public void setSkill(Holder<Attribute> attribute, double skillLevel) {
        setSkill(attribute, skillLevel, true);
    }

    private void setSkill(Holder<Attribute> attribute, double skillLevel, boolean updateMapValue) {
        AttributeInstance attrInst = persona.getEntity().getAttribute(attribute);
        if (attrInst == null) {
            return;
        }

        attrInst.setBaseValue(skillLevel);
        if (updateMapValue) {
            skillValues.put(attribute, skillLevel);
        }

        MKPlayerData playerData = persona.getPlayerData();
        SkillChangeHandler handler = skillChangeHandlers.get(attribute);
        if (handler != null) {
            handler.onSkillChange(playerData, skillLevel);
        }
        playerData.events().tryTrigger(PlayerEvents.SKILL_LEVEL_CHANGE, () -> new PlayerEvents.SkillEvent(persona.getPlayerData(), attrInst));
    }

    private double getSkillValue(Holder<Attribute> attribute) {
        return skillValues.getOrDefault(attribute, 0.0);
    }

    public void tryIncreaseSkill(Holder<Attribute> attribute) {
        tryIncreaseSkill(attribute, this::getDefaultSkillIncreaseChance);
    }

    public void tryIncreaseSkill(Holder<Attribute> attribute, double flatChance) {
        tryIncreaseSkill(attribute, current -> flatChance);
    }

    public void tryIncreaseSkill(Holder<Attribute> attribute, DoubleUnaryOperator chanceFormula) {
        double currentSkill = getSkillValue(attribute);
        if (currentSkill < GameConstants.NATURAL_SKILL_MAX) {
            Player player = persona.getEntity();
            if (player.getRandom().nextDouble() <= chanceFormula.applyAsDouble(currentSkill)) {
                player.sendSystemMessage(Component.translatable("mkcore.skill.increase",
                                Component.translatable(attribute.value().getDescriptionId()), currentSkill + 1.0)
                        .withStyle(ChatFormatting.AQUA));
                setSkill(attribute, currentSkill + 1.0);
            }
        }
    }

    public void tryScaledIncreaseSkill(Holder<Attribute> attribute, double scale) {
        tryIncreaseSkill(attribute, current -> getDefaultSkillIncreaseChance(current) * scale);
    }

    private double getDefaultSkillIncreaseChance(double currentSkill) {
        return 1.0 / (5.0 + currentSkill);
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag skillsNbt = new CompoundTag();
        for (Object2DoubleMap.Entry<Holder<Attribute>> entry : skillValues.object2DoubleEntrySet()) {
            ResourceLocation attrId = Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getKey(entry.getKey().value()));
            skillsNbt.putDouble(attrId.toString(), entry.getDoubleValue());
        }
        tag.put("skills", skillsNbt);
        return tag;
    }

    @Override
    public boolean deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        CompoundTag skillsNbt = tag.getCompound("skills");
        for (String key : skillsNbt.getAllKeys()) {
            BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(key))
                    .ifPresent(attr -> skillValues.put(attr, skillsNbt.getDouble(key)));

        }
        return true;
    }
}
