package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalent;
import com.chaosbuffalo.mkcore.core.talents.talent_types.EntitlementGrantTalent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public class CoreTalents {

    public static final DeferredRegister<MKTalent> TALENTS =
            DeferredRegister.create(MKCoreRegistry.TALENT_REGISTRY_KEY, MKCore.MOD_ID);

    public static final DeferredHolder<MKTalent, AttributeTalent> MAX_HEALTH_TALENT = TALENTS.register("max_health",
            () -> new AttributeTalent(
                    Attributes.MAX_HEALTH,
                    UUID.fromString("5d95bcd4-a06e-415a-add0-f1f85e20b18b"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> ARMOR_TALENT = TALENTS.register("armor",
            () -> new AttributeTalent(
                    Attributes.ARMOR,
                    UUID.fromString("1f917d51-efa1-43ee-8af0-b49175c97c0b"))
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> MOVEMENT_SPEED_TALENT = TALENTS.register("movement_speed",
            () -> new AttributeTalent(
                    Attributes.MOVEMENT_SPEED,
                    UUID.fromString("95fcf4d0-aaa9-413f-8362-7706e29412f7"))
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));


    public static final DeferredHolder<MKTalent, AttributeTalent> ATTACK_DAMAGE_TALENT = TALENTS.register("attack_damage",
            () -> new AttributeTalent(
                    Attributes.ATTACK_DAMAGE,
                    UUID.fromString("752d8f70-a5de-4111-af81-6bd1020b9433"))
                    .setOp(AttributeModifier.Operation.ADD_VALUE)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> ATTACK_SPEED_TALENT = TALENTS.register("attack_speed",
            () -> new AttributeTalent(
                    Attributes.ATTACK_SPEED,
                    UUID.fromString("e8d4945f-7435-4b1b-990d-3f32815687ff"))
                    .setDisplayAsPercentage(true)
                    .setOp(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> MAX_MANA_TALENT = TALENTS.register("max_mana",
            () -> new AttributeTalent(
                    MKAttributes.MAX_MANA,
                    UUID.fromString("50338dba-eaca-4ec8-a71f-13b5924496f4"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> MANA_REGEN_TALENT = TALENTS.register("mana_regen",
            () -> new AttributeTalent(
                    MKAttributes.MANA_REGEN,
                    UUID.fromString("87cd1a11-682f-4635-97db-4fedf6a7496b"))
                    .setDefaultPerRank(0.5f));

    public static final DeferredHolder<MKTalent, AttributeTalent> MELEE_CRIT_TALENT = TALENTS.register("melee_crit",
            () -> new AttributeTalent(
                    MKAttributes.MELEE_CRIT,
                    UUID.fromString("3b9ea27d-61ca-47b4-9bba-e82679b74ddd"))
                    .setDisplayAsPercentage(true));

    public static final DeferredHolder<MKTalent, AttributeTalent> SPELL_CRIT_TALENT = TALENTS.register("spell_crit",
            () -> new AttributeTalent(
                    MKAttributes.SPELL_CRIT,
                    UUID.fromString("9fbc7b94-4836-45ca-933a-4edaabcf2c6a"))
                    .setDisplayAsPercentage(true));

    public static final DeferredHolder<MKTalent, AttributeTalent> MELEE_CRIT_MULTIPLIER_TALENT = TALENTS.register("melee_crit_multiplier",
            () -> new AttributeTalent(
                    MKAttributes.MELEE_CRIT_MULTIPLIER,
                    UUID.fromString("0032d49a-ed71-4dfb-a9f5-f0d3dd183e96"))
                    .setDisplayAsPercentage(true));

    public static final DeferredHolder<MKTalent, AttributeTalent> SPELL_CRIT_MULTIPLIER_TALENT = TALENTS.register("spell_crit_multiplier",
            () -> new AttributeTalent(
                    MKAttributes.SPELL_CRIT_MULTIPLIER,
                    UUID.fromString("a9d6069c-98b9-454d-b59f-c5a6e81966d5"))
                    .setDisplayAsPercentage(true));

    public static final DeferredHolder<MKTalent, AttributeTalent> COOLDOWN_REDUCTION_TALENT = TALENTS.register("cooldown_reduction",
            () -> new AttributeTalent(
                    MKAttributes.COOLDOWN,
                    UUID.fromString("5378ff4c-0606-4781-abc0-c7d3e945b378"))
                    .setOp(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .setDisplayAsPercentage(true));

    public static final DeferredHolder<MKTalent, AttributeTalent> HEAL_BONUS_TALENT = TALENTS.register("heal_bonus",
            () -> new AttributeTalent(
                    MKAttributes.HEAL_BONUS,
                    UUID.fromString("711e57c3-cf2a-4fb5-a503-3dff0a1e007d")));

    public static final DeferredHolder<MKTalent, AttributeTalent> MAX_POISE_TALENT = TALENTS.register("max_poise",
            () -> new AttributeTalent(
                    MKAttributes.MAX_POISE,
                    UUID.fromString("71f00038-664a-484b-b543-6429f1478212"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> POISE_REGEN_TALENT = TALENTS.register("poise_regen",
            () -> new AttributeTalent(
                    MKAttributes.POISE_REGEN,
                    UUID.fromString("449864cf-64f0-4f00-8009-cf2a9aa2e46f"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(0.25));

    public static final DeferredHolder<MKTalent, AttributeTalent> BLOCK_EFFICIENCY_TALENT = TALENTS.register("block_efficiency",
            () -> new AttributeTalent(
                    MKAttributes.BLOCK_EFFICIENCY,
                    UUID.fromString("5e56c509-9f69-4fae-b0eb-368a4c80545a"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> POISE_BREAK_CD_TALENT = TALENTS.register("poise_break_cd",
            () -> new AttributeTalent(
                    MKAttributes.POISE_BREAK_CD,
                    UUID.fromString("6fa0e9ce-20f8-47a6-90ef-aa89012d05fc"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(-0.25));

    public static final DeferredHolder<MKTalent, AttributeTalent> HEAL_EFFICIENCY_TALENT = TALENTS.register("heal_efficiency",
            () -> new AttributeTalent(
                    MKAttributes.HEAL_EFFICIENCY,
                    UUID.fromString("c8db3f1f-e8f5-44ef-a34c-01f615748160"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> NATURE_DAMAGE_TALENT = TALENTS.register("nature_damage",
            () -> new AttributeTalent(
                    MKAttributes.NATURE_DAMAGE,
                    UUID.fromString("15e92270-e033-4de9-85ca-55f3195ab808"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> SHADOW_DAMAGE_TALENT = TALENTS.register("shadow_damage",
            () -> new AttributeTalent(
                    MKAttributes.SHADOW_DAMAGE,
                    UUID.fromString("5c1c4b81-5cb3-4e23-81b7-27a10b0e559a"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> FIRE_DAMAGE_TALENT = TALENTS.register("fire_damage",
            () -> new AttributeTalent(
                    MKAttributes.FIRE_DAMAGE,
                    UUID.fromString("04bd899c-2d67-433f-99e4-0419beb5c2cc"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> FROST_DAMAGE_TALENT = TALENTS.register("frost_damage",
            () -> new AttributeTalent(
                    MKAttributes.FROST_DAMAGE,
                    UUID.fromString("de9136d1-ff8d-4aff-8f28-f3216b73f5fc"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> HOLY_DAMAGE_TALENT = TALENTS.register("holy_damage",
            () -> new AttributeTalent(
                    MKAttributes.HOLY_DAMAGE,
                    UUID.fromString("449f91dd-4be6-4a32-8d1c-e0b18f9a6464"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> ARCANE_DAMAGE_TALENT = TALENTS.register("arcane_damage",
            () -> new AttributeTalent(
                    MKAttributes.ARCANE_DAMAGE,
                    UUID.fromString("1f8b52fe-994e-4d8e-9d94-3c6551fe8fea"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> POISON_DAMAGE_TALENT = TALENTS.register("poison_damage",
            () -> new AttributeTalent(
                    MKAttributes.POISON_DAMAGE,
                    UUID.fromString("a89e3452-9f37-4bc5-8b75-95892f1b3da7"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> BLEED_DAMAGE_TALENT = TALENTS.register("bleed_damage",
            () -> new AttributeTalent(
                    MKAttributes.BLEED_DAMAGE,
                    UUID.fromString("55333945-634b-400f-b93c-5cc6d37923eb"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> RANGED_DAMAGE_TALENT = TALENTS.register("ranged_damage",
            () -> new AttributeTalent(
                    MKAttributes.RANGED_DAMAGE,
                    UUID.fromString("162f29c1-ca25-4304-936e-9823f2cdab03"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final DeferredHolder<MKTalent, AttributeTalent> BLEED_RESISTANCE_TALENT = TALENTS.register("bleed_resistance",
            () -> new AttributeTalent(
                    MKAttributes.BLEED_RESISTANCE,
                    UUID.fromString("d9501c4f-7a7c-4c27-a834-0639a31b6bfe"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> HOLY_RESISTANCE_TALENT = TALENTS.register("holy_resistance",
            () -> new AttributeTalent(
                    MKAttributes.HOLY_RESISTANCE,
                    UUID.fromString("fd524571-6147-4661-8eb3-199a533f9bc8"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> FROST_RESISTANCE_TALENT = TALENTS.register("frost_resistance",
            () -> new AttributeTalent(
                    MKAttributes.FROST_RESISTANCE,
                    UUID.fromString("e512ef85-ef65-476b-bc04-c504c863fdb0"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> FIRE_RESISTANCE_TALENT = TALENTS.register("fire_resistance",
            () -> new AttributeTalent(
                    MKAttributes.FIRE_RESISTANCE,
                    UUID.fromString("ed6fc985-520b-45dd-89bc-21e997a5be32"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> ARCANE_RESISTANCE_TALENT = TALENTS.register("arcane_resistance",
            () -> new AttributeTalent(
                    MKAttributes.ARCANE_RESISTANCE,
                    UUID.fromString("039abddc-4665-4288-a0c0-00fd71315a3e"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> POISON_RESISTANCE_TALENT = TALENTS.register("poison_resistance",
            () -> new AttributeTalent(
                    MKAttributes.POISON_RESISTANCE,
                    UUID.fromString("c431f0d3-2c25-4d9e-8678-eed060291464"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> SHADOW_RESISTANCE_TALENT = TALENTS.register("shadow_resistance",
            () -> new AttributeTalent(
                    MKAttributes.SHADOW_RESISTANCE,
                    UUID.fromString("dccbf0d9-39f6-48d4-947d-423782232a84"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> RANGED_RESISTANCE_TALENT = TALENTS.register("ranged_resistance",
            () -> new AttributeTalent(
                    MKAttributes.RANGED_RESISTANCE,
                    UUID.fromString("98c02c1c-21b1-4613-a37d-d32be6f57b4a"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));

    public static final DeferredHolder<MKTalent, AttributeTalent> NATURE_RESISTANCE_TALENT = TALENTS.register("nature_resistance",
            () -> new AttributeTalent(
                    MKAttributes.NATURE_RESISTANCE,
                    UUID.fromString("7a1d447c-655c-4d8c-87f5-63cb7f0977b9"))
                    .setRequiresStatRefresh(true)
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));


    public static final DeferredHolder<MKTalent, EntitlementGrantTalent> ABILITY_SLOT_TALENT = TALENTS.register("ability_slot",
            () -> new EntitlementGrantTalent(TalentType.BASIC_SLOT));

    public static final DeferredHolder<MKTalent, EntitlementGrantTalent> PASSIVE_ABILITY_SLOT_TALENT = TALENTS.register("passive_ability_slot",
            () -> new EntitlementGrantTalent(TalentType.PASSIVE_SLOT));

    public static final DeferredHolder<MKTalent, EntitlementGrantTalent> ULTIMATE_ABILITY_SLOT_TALENT = TALENTS.register("ultimate_ability_slot",
            () -> new EntitlementGrantTalent(TalentType.ULTIMATE_SLOT));

    public static final DeferredHolder<MKTalent, EntitlementGrantTalent> POOL_COUNT_TALENT = TALENTS.register("pool_count",
            () -> new EntitlementGrantTalent(TalentType.POOL_COUNT));


    public static void register(IEventBus modBus) {
        TALENTS.register(modBus);
    }
}
