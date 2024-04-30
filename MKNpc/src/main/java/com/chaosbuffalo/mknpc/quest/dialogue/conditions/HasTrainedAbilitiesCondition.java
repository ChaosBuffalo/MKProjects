package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HasTrainedAbilitiesCondition extends DialogueCondition {
    public static final Codec<HasTrainedAbilitiesCondition> CODEC = RecordCodecBuilder.<HasTrainedAbilitiesCondition>mapCodec(builder ->
            builder.group(
                    Codec.list(ResourceLocation.CODEC).fieldOf("abilities").forGetter(i -> i.abilities),
                    Codec.BOOL.fieldOf("allMatch").forGetter(i -> i.allMatch)
            ).apply(builder, HasTrainedAbilitiesCondition::new)
    ).codec();

    private final List<ResourceLocation> abilities = new ArrayList<>();
    private final boolean allMatch;

    private HasTrainedAbilitiesCondition(List<ResourceLocation> abilities, boolean allMatch) {
        this.abilities.addAll(abilities);
        this.allMatch = allMatch;
    }

    public HasTrainedAbilitiesCondition(boolean allMatch, ResourceLocation... loc) {
        abilities.addAll(Arrays.asList(loc));
        this.allMatch = allMatch;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.HAS_TRAINED_ABILITIES.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        if (allMatch) {
            return abilities.stream().allMatch(x -> MKCore.getPlayer(player).map(
                    pd -> pd.getAbilities().knowsAbility(x)).orElse(false));
        } else {
            return abilities.stream().anyMatch(x -> MKCore.getPlayer(player).map(
                    pd -> pd.getAbilities().knowsAbility(x)).orElse(false));
        }
    }

    @Override
    public HasTrainedAbilitiesCondition copy() {
        return new HasTrainedAbilitiesCondition(ImmutableList.copyOf(abilities), allMatch);
    }
}
