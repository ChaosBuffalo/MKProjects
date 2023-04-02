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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class CoreTalents {

    public static final DeferredRegister<MKTalent> TALENTS =
            DeferredRegister.create(MKCoreRegistry.TALENT_REGISTRY_NAME, MKCore.MOD_ID);

    public static final RegistryObject<AttributeTalent> MAX_HEALTH_TALENT = TALENTS.register("talent.max_health",
            () -> new AttributeTalent(
                    Attributes.MAX_HEALTH,
                    UUID.fromString("5d95bcd4-a06e-415a-add0-f1f85e20b18b"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final RegistryObject<AttributeTalent> ARMOR_TALENT = TALENTS.register("talent.armor",
            () -> new AttributeTalent(
                    Attributes.ARMOR,
                    UUID.fromString("1f917d51-efa1-43ee-8af0-b49175c97c0b"))
                    .setDefaultPerRank(1));

    public static final RegistryObject<AttributeTalent> MOVEMENT_SPEED_TALENT = TALENTS.register("talent.movement_speed",
            () -> new AttributeTalent(
                    Attributes.MOVEMENT_SPEED,
                    UUID.fromString("95fcf4d0-aaa9-413f-8362-7706e29412f7"))
                    .setDisplayAsPercentage(true)
                    .setDefaultPerRank(0.01));


    public static final RegistryObject<AttributeTalent> ATTACK_DAMAGE_TALENT = TALENTS.register("talent.attack_damage",
            () -> new AttributeTalent(
                    Attributes.ATTACK_DAMAGE,
                    UUID.fromString("752d8f70-a5de-4111-af81-6bd1020b9433"))
                    .setOp(AttributeModifier.Operation.ADDITION)
                    .setDefaultPerRank(1));

    public static final RegistryObject<AttributeTalent> ATTACK_SPEED_TALENT = TALENTS.register("talent.attack_speed",
            () -> new AttributeTalent(
                    Attributes.ATTACK_SPEED,
                    UUID.fromString("e8d4945f-7435-4b1b-990d-3f32815687ff"))
                    .setDisplayAsPercentage(true)
                    .setOp(AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .setDefaultPerRank(0.01));

    public static final RegistryObject<AttributeTalent> MAX_MANA_TALENT = TALENTS.register("talent.max_mana",
            () -> new AttributeTalent(
                    MKAttributes.MAX_MANA,
                    UUID.fromString("50338dba-eaca-4ec8-a71f-13b5924496f4"))
                    .setRequiresStatRefresh(true)
                    .setDefaultPerRank(1));

    public static final RegistryObject<AttributeTalent> MANA_REGEN_TALENT = TALENTS.register("talent.mana_regen",
            () -> new AttributeTalent(
                    MKAttributes.MANA_REGEN,
                    UUID.fromString("87cd1a11-682f-4635-97db-4fedf6a7496b"))
                    .setDefaultPerRank(0.5f));

    public static final RegistryObject<AttributeTalent> MELEE_CRIT_TALENT = TALENTS.register("talent.melee_crit",
            () -> new AttributeTalent(
                    MKAttributes.MELEE_CRIT,
                    UUID.fromString("3b9ea27d-61ca-47b4-9bba-e82679b74ddd"))
                    .setDisplayAsPercentage(true));

    public static final RegistryObject<AttributeTalent> SPELL_CRIT_TALENT = TALENTS.register("talent.spell_crit",
            () -> new AttributeTalent(
                    MKAttributes.SPELL_CRIT,
                    UUID.fromString("9fbc7b94-4836-45ca-933a-4edaabcf2c6a"))
                    .setDisplayAsPercentage(true));

    public static final RegistryObject<AttributeTalent> MELEE_CRIT_MULTIPLIER_TALENT = TALENTS.register("talent.melee_crit_multiplier",
            () -> new AttributeTalent(
                    MKAttributes.MELEE_CRIT_MULTIPLIER,
                    UUID.fromString("0032d49a-ed71-4dfb-a9f5-f0d3dd183e96"))
                    .setDisplayAsPercentage(true));

    public static final RegistryObject<AttributeTalent> SPELL_CRIT_MULTIPLIER_TALENT = TALENTS.register("talent.spell_crit_multiplier",
            () -> new AttributeTalent(
                    MKAttributes.SPELL_CRIT_MULTIPLIER,
                    UUID.fromString("a9d6069c-98b9-454d-b59f-c5a6e81966d5"))
                    .setDisplayAsPercentage(true));

    public static final RegistryObject<AttributeTalent> COOLDOWN_REDUCTION_TALENT = TALENTS.register("talent.cooldown_reduction",
            () -> new AttributeTalent(
                    MKAttributes.COOLDOWN,
                    UUID.fromString("5378ff4c-0606-4781-abc0-c7d3e945b378"))
                    .setOp(AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .setDisplayAsPercentage(true));

    public static final RegistryObject<AttributeTalent> HEAL_BONUS_TALENT = TALENTS.register("talent.heal_bonus",
            () -> new AttributeTalent(
                    MKAttributes.HEAL_BONUS,
                    UUID.fromString("711e57c3-cf2a-4fb5-a503-3dff0a1e007d")));


    public static final RegistryObject<EntitlementGrantTalent> ABILITY_SLOT_TALENT = TALENTS.register("talent.ability_slot",
            () -> new EntitlementGrantTalent(TalentType.BASIC_SLOT));

    public static final RegistryObject<EntitlementGrantTalent> PASSIVE_ABILITY_SLOT_TALENT = TALENTS.register("talent.passive_ability_slot",
            () -> new EntitlementGrantTalent(TalentType.PASSIVE_SLOT));

    public static final RegistryObject<EntitlementGrantTalent> ULTIMATE_ABILITY_SLOT_TALENT = TALENTS.register("talent.ultimate_ability_slot",
            () -> new EntitlementGrantTalent(TalentType.ULTIMATE_SLOT));

    public static final RegistryObject<EntitlementGrantTalent> POOL_COUNT_TALENT = TALENTS.register("talent.pool_count",
            () -> new EntitlementGrantTalent(TalentType.POOL_COUNT));


    public static void register(IEventBus modBus) {
        TALENTS.register(modBus);
    }
}
