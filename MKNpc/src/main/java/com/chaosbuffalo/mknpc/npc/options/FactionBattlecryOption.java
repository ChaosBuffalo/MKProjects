package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.FactionGreetings;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.binding.FactionBattlecryBoundValue;
import com.chaosbuffalo.mknpc.npc.options.binding.IBoundNpcOptionValue;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;

public class FactionBattlecryOption extends BindingNpcOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "faction_battlecry");
    public static final FactionBattlecryOption INSTANCE = new FactionBattlecryOption();
    public static final Codec<FactionBattlecryOption> CODEC = Codec.unit(INSTANCE);

    public FactionBattlecryOption() {
        super(NAME, ApplyOrder.LATE);
    }

    @Nullable
    private static <T> T getRandomEntry(RandomSource random, List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    @Nullable
    @Override
    protected IBoundNpcOptionValue generateBoundValue(NpcDefinition definition, RandomSource random) {
        MKFaction faction = MKFactionRegistry.getFaction(definition.getFactionName());
        if (faction != null) {
            return faction.getGreetings().getGreetingsWithMembers(FactionGreetings.GreetingType.BATTLECRY)
                    .map(x -> getRandomEntry(random, x))
                    .map(FactionBattlecryBoundValue::new)
                    .orElse(null);
        }
        return null;
    }
}
