package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.option_entries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NpcOptionEntryTypes {

    public static final DeferredRegister<NpcOptionEntryType<?>> REGISTRY = DeferredRegister.create(NpcRegistries.NPC_OPTION_ENTRY_TYPE_NAME, MKNpc.MODID);

    public static final Supplier<NpcOptionEntryType<AbilitiesOptionEntry>> ABILITIES = REGISTRY.register("abilities", () -> () -> AbilitiesOptionEntry.CODEC);
    public static final Supplier<NpcOptionEntryType<EquipmentOptionEntry>> EQUIPMENT = REGISTRY.register("equipment", () -> () -> EquipmentOptionEntry.CODEC);
    public static final Supplier<NpcOptionEntryType<FactionNameOptionEntry>> FACTION_NAME = REGISTRY.register("faction_name", () -> () -> FactionNameOptionEntry.CODEC);
    public static final Supplier<NpcOptionEntryType<QuestOptionEntry>> QUEST = REGISTRY.register("offer_quests", () -> () -> QuestOptionEntry.CODEC);
    public static final Supplier<NpcOptionEntryType<FactionBattlecryOptionEntry>> FACTION_BATTLECRY = REGISTRY.register("faction_battlecry", () -> () -> FactionBattlecryOptionEntry.CODEC);

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
