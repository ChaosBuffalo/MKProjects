package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.FactionGreetings;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.FactionBattlecryOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;

public class FactionBattlecryOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "faction_battlecry");


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

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, RandomSource random) {
        MKFaction faction = MKFactionRegistry.getFaction(definition.getFactionName());
        if (faction != null) {
            return faction.getGreetings().getGreetingsWithMembers(FactionGreetings.GreetingType.BATTLECRY).map(
                    x -> new FactionBattlecryOptionEntry(getRandomEntry(random, x))
            ).orElse(new FactionBattlecryOptionEntry());
        }
        return new FactionBattlecryOptionEntry();
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {

    }
}
