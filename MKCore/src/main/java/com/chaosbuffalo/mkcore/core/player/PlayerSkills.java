package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.item.IReceivesSkillChange;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenCustomHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

public class PlayerSkills implements IMKSerializable<CompoundTag> {
    private static final UUID blockScalerUUID = UUID.fromString("8cabfe08-4ad3-4b8a-9b94-cb146f743c36");

    protected interface SkillChangeHandler {
        void onSkillChange(MKPlayerData playerData, double value);
    }

    private final MKPlayerData playerData;
    private final Object2DoubleMap<Attribute> skillValues = new Object2DoubleOpenCustomHashMap<>(Util.identityStrategy());
    private static final Map<Attribute, SkillChangeHandler> skillChangeHandlers = Util.make(() -> {
        Map<Attribute, SkillChangeHandler> map = new HashMap<>(8);
        map.put(MKAttributes.BLOCK, PlayerSkills::onBlockChange);
        map.put(MKAttributes.ONE_HAND_BLUNT, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.TWO_HAND_BLUNT, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.ONE_HAND_SLASH, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.TWO_HAND_SLASH, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.ONE_HAND_PIERCE, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.TWO_HAND_PIERCE, PlayerSkills::onWeaponSkillChange);
        map.put(MKAttributes.MARKSMANSHIP, PlayerSkills::onWeaponSkillChange);
        return map;
    });

    public PlayerSkills(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    private static void onBlockChange(MKPlayerData playerData, double value) {
        AttributeInstance inst = playerData.getEntity().getAttribute(MKAttributes.MAX_POISE);
        if (inst != null) {
            inst.removeModifier(blockScalerUUID);
            inst.addTransientModifier(new AttributeModifier(blockScalerUUID, "block skill",
                    MKAbility.convertSkillToMultiplier(value), AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static void onWeaponSkillChange(MKPlayerData playerData, double value) {
        ItemStack mainHand = playerData.getEntity().getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainHand.getItem() instanceof IReceivesSkillChange) {
            ((IReceivesSkillChange) mainHand.getItem()).onSkillChange(mainHand, playerData.getEntity());
        }
    }

    public void onCastAbility(MKAbility cast) {
        for (Attribute attribute : cast.getSkillAttributes()) {
            tryIncreaseSkill(attribute);
        }
    }

    public void onPersonaActivated() {
        for (Object2DoubleMap.Entry<Attribute> entry : skillValues.object2DoubleEntrySet()) {
            setSkill(entry.getKey(), entry.getDoubleValue(), false);
        }
    }

    public void onPersonaDeactivated() {
        for (Attribute key : skillValues.keySet()) {
            setSkill(key, 0.0, false);
        }
    }

    public void setSkill(Attribute attribute, double skillLevel) {
        setSkill(attribute, skillLevel, true);
    }

    private void setSkill(Attribute attribute, double skillLevel, boolean updateMapValue) {
        AttributeInstance attrInst = playerData.getEntity().getAttribute(attribute);
        if (attrInst == null) {
            return;
        }

        attrInst.setBaseValue(skillLevel);
        if (updateMapValue) {
            skillValues.put(attribute, skillLevel);
        }

        SkillChangeHandler handler = skillChangeHandlers.get(attribute);
        if (handler != null) {
            handler.onSkillChange(playerData, skillLevel);
        }
    }

    private double getSkillValue(Attribute attribute) {
        return skillValues.getOrDefault(attribute, 0.0);
    }

    public void tryIncreaseSkill(Attribute attribute) {
        tryIncreaseSkill(attribute, this::getDefaultSkillIncreaseChance);
    }

    public void tryIncreaseSkill(Attribute attribute, double flatChance) {
        tryIncreaseSkill(attribute, current -> flatChance);
    }

    public void tryIncreaseSkill(Attribute attribute, DoubleUnaryOperator chanceFormula) {
        double currentSkill = getSkillValue(attribute);
        if (currentSkill < GameConstants.NATURAL_SKILL_MAX) {
            Player player = playerData.getEntity();
            if (player.getRandom().nextDouble() <= chanceFormula.applyAsDouble(currentSkill)) {
                player.sendMessage(new TranslatableComponent("mkcore.skill.increase",
                        new TranslatableComponent(attribute.getDescriptionId()), currentSkill + 1.0)
                        .withStyle(ChatFormatting.AQUA), Util.NIL_UUID);
                setSkill(attribute, currentSkill + 1.0);
            }
        }
    }

    public void tryScaledIncreaseSkill(Attribute attribute, double scale) {
        tryIncreaseSkill(attribute, current -> getDefaultSkillIncreaseChance(current) * scale);
    }

    private double getDefaultSkillIncreaseChance(double currentSkill) {
        return 1.0 / (5.0 + currentSkill);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        CompoundTag skillsNbt = new CompoundTag();
        for (Object2DoubleMap.Entry<Attribute> entry : skillValues.object2DoubleEntrySet()) {
            ResourceLocation attrId = Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getKey(entry.getKey()));
            skillsNbt.putDouble(attrId.toString(), entry.getDoubleValue());
        }
        tag.put("skills", skillsNbt);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        CompoundTag skillsNbt = tag.getCompound("skills");
        for (String key : skillsNbt.getAllKeys()) {
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
            if (attr != null) {
                skillValues.put(attr, skillsNbt.getDouble(key));
            }
        }
        return true;
    }
}
