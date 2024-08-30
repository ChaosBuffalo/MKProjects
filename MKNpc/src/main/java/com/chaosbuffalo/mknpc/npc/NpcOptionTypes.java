package com.chaosbuffalo.mknpc.npc;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.options.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NpcOptionTypes {

    public static final DeferredRegister<NpcOptionType<?>> REGISTRY = DeferredRegister.create(NpcRegistries.NPC_OPTION_TYPE_NAME, MKNpc.MODID);

    public static final Supplier<NpcOptionType<EquipmentOption>> EQUIPMENT = REGISTRY.register("equipment", () -> () -> EquipmentOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<AbilitiesOption>> ABILITIES = REGISTRY.register("abilities", () -> () -> AbilitiesOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<AttributesOption>> ATTRIBUTES  = REGISTRY.register("attributes", () -> () -> AttributesOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<NameOption>> NAME = REGISTRY.register("name", () -> () -> NameOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<ExperienceOption>> EXPERIENCE  = REGISTRY.register("experience", () -> () -> ExperienceOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<FactionOption>> FACTION = REGISTRY.register("faction", () -> () -> FactionOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<DialogueOption>> DIALOGUE  = REGISTRY.register("dialogue", () -> () -> DialogueOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<FactionNameOption>> FACTION_NAME = REGISTRY.register("faction_name", () -> () -> FactionNameOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<NotableOption>> NOTABLE  = REGISTRY.register("notable", () -> () -> NotableOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<RenderGroupOption>> RENDER_GROUP = REGISTRY.register("render_group", () -> () -> RenderGroupOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<MKSizeOption>> MK_SIZE  = REGISTRY.register("mk_size", () -> () -> MKSizeOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<MKComboSettingsOption>> MK_COMBO = REGISTRY.register("mk_combo", () -> () -> MKComboSettingsOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<LungeSpeedOption>> LUNGE_SPEED = REGISTRY.register("lunge_speed", () -> () -> LungeSpeedOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<AbilityTrainingOption>> ABILITY_TRAINING  = REGISTRY.register("ability_trainings", () -> () -> AbilityTrainingOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<ParticleEffectsOption>> PARTICLE_EFFECTS = REGISTRY.register("particle_effects", () -> () -> ParticleEffectsOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<ExtraLootOption>> EXTRA_LOOT  = REGISTRY.register("extra_loot", () -> () -> ExtraLootOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<QuestOfferingOption>> QUEST_OFFERING  = REGISTRY.register("offer_quests", () -> () -> QuestOfferingOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<BossStageOption>> BOSS_STAGE = REGISTRY.register("boss_stage", () -> () -> BossStageOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<TempAbilitiesOption>> TEMP_ABILITIES  = REGISTRY.register("temp_abilities", () -> () -> TempAbilitiesOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<GhostOption>> GHOST = REGISTRY.register("ghost", () -> () -> GhostOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<SkillOption>> SKILL = REGISTRY.register("skills", () -> () -> SkillOption.MAP_CODEC);
    public static final Supplier<NpcOptionType<FactionBattlecryOption>> FACTION_BATTLECRY  = REGISTRY.register("faction_battlecry", () -> () -> FactionBattlecryOption.MAP_CODEC);


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
