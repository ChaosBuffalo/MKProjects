package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mknpc.MKNpc;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HasTrainedAbilitiesCondition extends DialogueCondition {
    public static final ResourceLocation conditionTypeName = new ResourceLocation(MKNpc.MODID, "has_trained_abilities");
    private final List<ResourceLocation> abilities = new ArrayList<>();
    private boolean allMatch;

    public HasTrainedAbilitiesCondition(boolean allMatch, ResourceLocation... loc){
        super(conditionTypeName);
        abilities.addAll(Arrays.asList(loc));
        this.allMatch = allMatch;
    }

    public HasTrainedAbilitiesCondition(){
        super(conditionTypeName);
    }


    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        if (allMatch){
            return abilities.stream().allMatch(x -> MKCore.getPlayer(player).map(
                    pd -> pd.getAbilities().knowsAbility(x)).orElse(false));
        } else {
            return abilities.stream().anyMatch(x -> MKCore.getPlayer(player).map(
                    pd -> pd.getAbilities().knowsAbility(x)).orElse(false));
        }
    }

    @Override
    public HasTrainedAbilitiesCondition copy() {
        return new HasTrainedAbilitiesCondition(allMatch, abilities.toArray(new ResourceLocation[0]));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        this.allMatch = dynamic.get("allMatch").asBoolean(false);
        List<Optional<ResourceLocation>> locs = dynamic.get("abilities").asList(
                d -> d.asString().result().map(ResourceLocation::new));
        locs.forEach(loc -> loc.ifPresent(abilities::add));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("allMatch"), ops.createBoolean(allMatch));
        builder.put(ops.createString("abilities"),
                ops.createList(abilities.stream().map(x -> ops.createString(x.toString()))));
    }
}