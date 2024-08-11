package com.chaosbuffalo.mknpc.npc;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.options.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NpcOptionTypes {

    public static final DeferredRegister<NpcOptionType<?>> REGISTRY = DeferredRegister.create(NpcRegistries.NPC_OPTION_TYPE_NAME, MKNpc.MODID);

    public static final Supplier<NpcOptionType<EquipmentOption>> EQUIPMENT = REGISTRY.register("equipment", () -> () -> EquipmentOption.CODEC);
    public static final Supplier<NpcOptionType<AbilitiesOption>> ABILITIES = REGISTRY.register("abilities", () -> () -> AbilitiesOption.CODEC);
    public static final Supplier<NpcOptionType<AttributesOption>> ATTRIBUTES  = REGISTRY.register("attributes", () -> () -> AttributesOption.CODEC);
    public static final Supplier<NpcOptionType<NameOption>> NAME = REGISTRY.register("name", () -> () -> NameOption.CODEC);
    public static final Supplier<NpcOptionType<ExperienceOption>> EXPERIENCE  = REGISTRY.register("experience", () -> () -> ExperienceOption.CODEC);
    public static final Supplier<NpcOptionType<FactionOption>> FACTION = REGISTRY.register("faction", () -> () -> FactionOption.CODEC);
    public static final Supplier<NpcOptionType<DialogueOption>> DIALOGUE  = REGISTRY.register("dialogue", () -> () -> DialogueOption.CODEC);
    public static final Supplier<NpcOptionType<FactionNameOption>> FACTION_NAME = REGISTRY.register("faction_name", () -> () -> FactionNameOption.CODEC);
    public static final Supplier<NpcOptionType<NotableOption>> NOTABLE  = REGISTRY.register("notable", () -> () -> NotableOption.CODEC);
    public static final Supplier<NpcOptionType<RenderGroupOption>> RENDER_GROUP = REGISTRY.register("render_group", () -> () -> RenderGroupOption.CODEC);
    public static final Supplier<NpcOptionType<MKSizeOption>> MK_SIZE  = REGISTRY.register("mk_size", () -> () -> MKSizeOption.CODEC);
    public static final Supplier<NpcOptionType<MKComboSettingsOption>> MK_COMBO = REGISTRY.register("mk_combo", () -> () -> MKComboSettingsOption.CODEC);
    public static final Supplier<NpcOptionType<LungeSpeedOption>> LUNGE_SPEED = REGISTRY.register("lunge_speed", () -> () -> LungeSpeedOption.CODEC);
    public static final Supplier<NpcOptionType<AbilityTrainingOption>> ABILITY_TRAINING  = REGISTRY.register("ability_trainings", () -> () -> AbilityTrainingOption.CODEC);
    public static final Supplier<NpcOptionType<ParticleEffectsOption>> PARTICLE_EFFECTS = REGISTRY.register("particle_effects", () -> () -> ParticleEffectsOption.CODEC);
    public static final Supplier<NpcOptionType<ExtraLootOption>> EXTRA_LOOT  = REGISTRY.register("extra_loot", () -> () -> ExtraLootOption.CODEC);
    public static final Supplier<NpcOptionType<QuestOfferingOption>> QUEST_OFFERING  = REGISTRY.register("offer_quests", () -> () -> QuestOfferingOption.CODEC);
    public static final Supplier<NpcOptionType<BossStageOption>> BOSS_STAGE = REGISTRY.register("boss_stage", () -> () -> BossStageOption.CODEC);
    public static final Supplier<NpcOptionType<TempAbilitiesOption>> TEMP_ABILITIES  = REGISTRY.register("temp_abilities", () -> () -> TempAbilitiesOption.CODEC);
    public static final Supplier<NpcOptionType<GhostOption>> GHOST = REGISTRY.register("ghost", () -> () -> GhostOption.CODEC);
    public static final Supplier<NpcOptionType<SkillOption>> SKILL = REGISTRY.register("skills", () -> () -> SkillOption.CODEC);
    public static final Supplier<NpcOptionType<FactionBattlecryOption>> FACTION_BATTLECRY  = REGISTRY.register("faction_battlecry", () -> () -> FactionBattlecryOption.CODEC);


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
