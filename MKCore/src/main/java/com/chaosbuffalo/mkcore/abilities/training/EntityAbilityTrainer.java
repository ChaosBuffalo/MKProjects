package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.network.OpenLearnAbilitiesGuiPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntityAbilityTrainer {
    private final Entity hostEntity;
    private final List<AbilityTrainingEntry> entries;

    public EntityAbilityTrainer(Entity entity) {
        entries = new ArrayList<>();
        hostEntity = entity;
    }

    public List<AbilityTrainingEntry> getTrainableAbilities(IMKEntityData entityData) {
        return entries;
    }

    public AbilityTrainingEntry getTrainingEntry(ResourceLocation abilityId) {
        return entries.stream().filter(entry -> entry.is(abilityId)).findFirst().orElse(null);
    }

    public void addTrainedAbility(MKAbility ability, List<AbilityTrainingRequirement> requirements) {
        AbilityTrainingEntry entry = new AbilityTrainingEntry(ability, requirements, AbilitySource.TRAINED.usesAbilityPool());
        entries.add(entry);
    }

    public void openTrainingGui(ServerPlayer playerEntity) {
        var playerData = MKCore.getPlayerOrThrow(playerEntity);

        List<AbilityTrainingEvaluation> abilities = new ArrayList<>(entries.size());
        for (AbilityTrainingEntry entry : getTrainableAbilities(playerData)) {
            AbilityTrainingEvaluation evaluation = entry.evaluate(playerData);
            abilities.add(evaluation);
        }
        PacketHandler.sendMessage(new OpenLearnAbilitiesGuiPacket(hostEntity.getId(), abilities), playerEntity);
    }
}
