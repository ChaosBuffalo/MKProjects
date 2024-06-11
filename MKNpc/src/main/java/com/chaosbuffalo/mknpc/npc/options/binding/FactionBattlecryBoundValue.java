package com.chaosbuffalo.mknpc.npc.options.binding;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.options.FactionBattlecryOption;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FactionBattlecryBoundValue implements IBoundNpcOptionValue {
    public static final Codec<FactionBattlecryBoundValue> CODEC = ExtraCodecs.COMPONENT
            .xmap(FactionBattlecryBoundValue::new, FactionBattlecryBoundValue::getBattlecry);

    @Nonnull
    private final Component battlecry;

    public FactionBattlecryBoundValue(Component text) {
        this.battlecry = Objects.requireNonNull(text);
    }

    @Override
    public ResourceLocation getOptionId() {
        return FactionBattlecryOption.NAME;
    }

    @Nonnull
    public Component getBattlecry() {
        return battlecry;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setBattlecry(getBattlecry());
        }
    }
}
