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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

public class PlayerSkills implements IMKSerializable<CompoundTag> {

    protected interface SkillChangeHandler {
        void onSkillChange(MKPlayerData playerData, double value);
    }

    private final MKPlayerData playerData;
    private final Object2DoubleMap<Attribute> skillValues = new Object2DoubleOpenCustomHashMap<>(Util.identityStrategy());

    private final List<Consumer<Attribute>> skillChangeCallbacks = new ArrayList<>();
    private static final Map<Attribute, SkillChangeHandler> skillChangeHandlers = Util.make(() -> {
        Map<Attribute, SkillChangeHandler> map = new HashMap<>(8);
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

    public void addCallback(Consumer<Attribute> cb) {
        skillChangeCallbacks.add(cb);
    }

    private static void onWeaponSkillChange(MKPlayerData playerData, double value) {
        ItemStack mainHand = playerData.getEntity().getItemBySlot(EquipmentSlot.MAINHAND);
        if (mainHand.getItem() instanceof IReceivesSkillChange receiver) {
            receiver.onSkillChange(mainHand, playerData.getEntity());
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
        skillChangeCallbacks.forEach(x -> x.accept(attribute));
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
                player.sendSystemMessage(Component.translatable("mkcore.skill.increase",
                                Component.translatable(attribute.getDescriptionId()), currentSkill + 1.0)
                        .withStyle(ChatFormatting.AQUA));
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
