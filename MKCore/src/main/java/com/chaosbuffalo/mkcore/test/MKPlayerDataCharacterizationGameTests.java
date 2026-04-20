package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities2.description.AbilityDefinitionDescriptions;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.core.AbilityDisplayEntry;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerKnownAbility;
import com.chaosbuffalo.mkcore.utils.text.IconTextComponent;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKPlayerDataCharacterizationGameTests {
    private static final ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> TEST_TREE =
            ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, MKCore.id("player_data_phase0"));
    private static final ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> TEST_ABILITIES2_TREE =
            ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, MKCore.id("player_data_abilities2_phase0"));
    private static final String TEST_LINE = "a";

    @GameTest(template = "player_data_phase0")
    public static void deserializedLoadoutResolvesTalentGrantedAbilityDuringActivation(GameTestHelper helper) {
        ResourceLocation abilityId = MKTestAbilities.TEST_EMBER.get().getAbilityId();
        MKServerPlayerData sourceData = createPlayerData(helper);
        setupTalentedLoadout(sourceData, abilityId);

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);
        removePersistedAbility(serialized, abilityId);
        setBasicSlots(serialized, 0);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);

        AbilityGroup restoredGroup = restoredData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        helper.assertFalse(restoredData.getAbilities().knowsAbility(abilityId), "Talent ability should not be restored before activation");
        helper.assertValueEqual(restoredGroup.getCurrentSlotCount(), 0, "pre-activation basic slot count");
        helper.assertTrue(restoredGroup.getAbilityInfo(0) == null, "Loadout should not resolve the talent ability before activation");

        restoredData.getPersonaManager().onJoinLevel();

        helper.assertTrue(restoredData.getAbilities().knowsAbility(abilityId), "Talent activation should restore the granted ability");
        helper.assertValueEqual(restoredGroup.getCurrentSlotCount(), 1, "post-activation basic slot count");
        helper.assertTrue(restoredGroup.getAbilityInfo(0) != null, "Loadout should resolve after talent and entitlement activation");
        helper.assertValueEqual(restoredGroup.getSlot(0), abilityId, "resolved basic slot");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void activationClearsUnknownSlottedAbility(GameTestHelper helper) {
        ResourceLocation abilityId = MKTestAbilities.TEST_EMBER.get().getAbilityId();
        MKServerPlayerData blankData = createPlayerData(helper);
        HolderLookup.Provider provider = blankData.getEntity().registryAccess();
        CompoundTag serialized = blankData.serializeNBT(provider);

        setBasicSlots(serialized, 1);
        setBasicAbilities(serialized, abilityId);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);
        helper.assertFalse(restoredData.getAbilities().knowsAbility(abilityId), "Player should still not know the injected ability");

        restoredData.getPersonaManager().onJoinLevel();

        AbilityGroup restoredGroup = restoredData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        helper.assertTrue(restoredGroup.getAbilityInfo(0) == null, "Activation should clear an unresolved slotted ability");
        helper.assertValueEqual(restoredGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY, "cleared slot value");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void clonePreservesTalentGrantedSlottedAbilityAfterActivation(GameTestHelper helper) {
        ResourceLocation abilityId = MKTestAbilities.TEST_EMBER.get().getAbilityId();
        MKServerPlayerData sourceData = createPlayerData(helper);
        setupTalentedLoadout(sourceData, abilityId);

        MKServerPlayerData cloneData = createPlayerData(helper);
        cloneData.clone(sourceData, false);

        AbilityGroup cloneGroup = cloneData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        helper.assertTrue(cloneData.getAbilities().knowsAbility(abilityId), "Clone should retain full ability state before activation");
        helper.assertValueEqual(cloneGroup.getSlot(0), abilityId, "cloned slotted ability before activation");
        helper.assertTrue(cloneGroup.getAbilityInfo(0) != null, "Clone should resolve the slotted talent ability before activation");

        cloneData.getPersonaManager().onJoinLevel();

        helper.assertTrue(cloneData.getAbilities().knowsAbility(abilityId), "Clone activation should replay talent-granted abilities");
        helper.assertValueEqual(cloneGroup.getCurrentSlotCount(), 1, "cloned basic slot count");
        helper.assertValueEqual(cloneGroup.getSlot(0), abilityId, "cloned slotted ability");
        helper.assertTrue(cloneGroup.getAbilityInfo(0) != null, "Cloned slotted ability should resolve after activation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void deserializedLoadoutRestoresTalentGrantedAbilities2DefinitionDuringActivation(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData sourceData = createPlayerData(helper);
        setupTalentedLoadout(sourceData, TEST_ABILITIES2_TREE, abilityId);

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);
        removePersistedAbility(serialized, abilityId);
        setBasicSlots(serialized, 0);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);

        AbilityGroup restoredGroup = restoredData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        helper.assertFalse(restoredData.getAbilities().knowsAbility(abilityId),
                "abilities2 talent definition should not be restored before activation");
        helper.assertValueEqual(restoredGroup.getCurrentSlotCount(), 0, "pre-activation abilities2 basic slot count");
        helper.assertTrue(restoredGroup.getAbilityInfo(0) == null,
                "abilities2 talent slot should not resolve as a legacy ability before activation");

        restoredData.getPersonaManager().onJoinLevel();

        helper.assertTrue(restoredData.getAbilities().knowsAbility(abilityId),
                "abilities2 talent activation should restore the granted definition");
        helper.assertValueEqual(restoredGroup.getCurrentSlotCount(), 1, "post-activation abilities2 basic slot count");
        helper.assertValueEqual(restoredGroup.getSlot(0), abilityId, "restored abilities2 talent slot");
        helper.assertTrue(restoredGroup.getAbilityInfo(0) == null,
                "abilities2 talent slot should remain definition-backed after activation");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void activationPreservesKnownSlottedAbilities2PassiveDefinition(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_spell_crit_passive");
        MKServerPlayerData sourceData = createPlayerData(helper);
        helper.assertTrue(sourceData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.ADMIN),
                "abilities2 passive definition should learn successfully");
        AbilityGroup sourceGroup = sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Passive);
        sourceGroup.setSlots(1);
        sourceGroup.setSlot(0, abilityId);

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);

        setPassiveSlots(serialized, 1);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);
        AbilityGroup restoredGroup = restoredData.getLoadout().getAbilityGroup(AbilityGroupId.Passive);

        helper.assertValueEqual(restoredGroup.getSlot(0), abilityId, "deserialized passive definition slot");
        helper.assertTrue(restoredGroup.getAbilityInfo(0) == null, "abilities2 passive should not resolve as a legacy ability");

        restoredData.getPersonaManager().onJoinLevel();

        helper.assertValueEqual(restoredGroup.getSlot(0), abilityId, "activation should preserve the abilities2 passive slot");
        helper.assertTrue(restoredGroup.getAbilityInfo(0) == null, "abilities2 passive should remain a definition-backed slot");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void activationClearsUnknownSlottedAbilities2PassiveDefinition(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_spell_crit_passive");
        MKServerPlayerData blankData = createPlayerData(helper);
        HolderLookup.Provider provider = blankData.getEntity().registryAccess();
        CompoundTag serialized = blankData.serializeNBT(provider);

        setPassiveSlots(serialized, 1);
        setPassiveAbilities(serialized, abilityId);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);
        AbilityGroup restoredGroup = restoredData.getLoadout().getAbilityGroup(AbilityGroupId.Passive);

        restoredData.getPersonaManager().onJoinLevel();

        helper.assertValueEqual(restoredGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY,
                "activation should clear an unknown slotted abilities2 passive definition");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void learnedAbilities2DefinitionPersistsAcrossSerialization(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData sourceData = createPlayerData(helper);
        helper.assertTrue(sourceData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.ADMIN),
                "abilities2 definition should learn successfully");

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);

        helper.assertTrue(restoredData.getAbilities().knowsAbility(abilityId),
                "deserialized player should still know the abilities2 definition");
        helper.assertTrue(restoredData.getAbilities().getAbilityInfo(abilityId) == null,
                "abilities2 definition should not deserialize as a legacy ability info");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void grantedAbilities2DefinitionAutoEquipsMatchingLoadoutGroup(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        basicGroup.setSlots(1);

        helper.assertTrue(playerData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.GRANTED),
                "abilities2 definition should learn successfully");
        helper.assertValueEqual(basicGroup.getSlot(0), abilityId,
                "granted abilities2 definition should auto-equip into the matching group");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void unlearningAbilities2DefinitionClearsSlottedEntry(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);

        helper.assertTrue(playerData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.ADMIN),
                "abilities2 definition should learn successfully");
        basicGroup.setSlot(0, abilityId);
        helper.assertValueEqual(basicGroup.getSlot(0), abilityId, "pre-unlearn slot state");

        helper.assertTrue(playerData.getAbilities().unlearnAbility(abilityId, AbilitySource.ADMIN),
                "abilities2 definition should unlearn successfully");
        helper.assertFalse(playerData.getAbilities().knowsAbility(abilityId),
                "abilities2 definition should no longer be known");
        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY,
                "unlearning should clear the slotted abilities2 definition");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void refundingTalentGrantedAbilities2DefinitionClearsKnowledgeAndSlot(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData playerData = createPlayerData(helper);
        setupTalentedLoadout(playerData, TEST_ABILITIES2_TREE, abilityId);

        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        helper.assertTrue(playerData.getAbilities().knowsAbility(abilityId),
                "abilities2 talent definition should be known after spending the talent");
        helper.assertValueEqual(basicGroup.getSlot(0), abilityId, "pre-refund abilities2 talent slot");

        helper.assertTrue(playerData.getTalents().refundTalentPoint(TEST_ABILITIES2_TREE, TEST_LINE, 1),
                "abilities2 talent should refund successfully");
        helper.assertFalse(playerData.getAbilities().knowsAbility(abilityId),
                "abilities2 talent refund should remove the granted definition");
        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY,
                "abilities2 talent refund should clear the slotted definition");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void trainedAbilities2DefinitionUsesTrainingBridge(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        basicGroup.setSlots(1);

        AbilityTrainingEntry entry = new AbilityTrainingEntry(abilityId, List.of(), AbilitySource.TRAINED.usesAbilityPool());
        helper.assertTrue(entry.learn(playerData, AbilitySource.TRAINED),
                "abilities2 definition should learn through the training bridge");
        helper.assertTrue(playerData.getAbilities().knowsAbility(abilityId),
                "trained abilities2 definition should become known");
        helper.assertValueEqual(playerData.getAbilities().getCurrentPoolCount(), 1,
                "trained abilities2 definition should consume one ability pool slot");
        helper.assertValueEqual(basicGroup.getSlot(0), abilityId,
                "trained abilities2 definition should auto-equip when a matching slot is available");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilities2DescriptionFormatterShowsPoolUsageForKnownPoolDefinition(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");
        MKServerPlayerData playerData = createPlayerData(helper);

        helper.assertTrue(playerData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.TRAINED),
                "abilities2 definition should learn successfully");
        PlayerKnownAbility knownAbility = playerData.getAbilities().getKnownAbility(abilityId);
        PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);

        helper.assertTrue(knownAbility != null, "trained definition should produce a known ability entry");
        helper.assertTrue(definition != null, "trained definition should resolve a patched description");

        List<Component> lines = AbilityDefinitionDescriptions.collectDescription(knownAbility, definition);
        helper.assertFalse(lines.isEmpty(), "description lines should not be empty");
        helper.assertTrue(lines.getFirst() instanceof IconTextComponent,
                "trained definitions should start with the uses-pool indicator");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilities2DescriptionFormatterSummarizesChannelBehaviorAndParameters(GameTestHelper helper) {
        ResourceLocation abilityId = MKCore.id("test_abilities2_mending_channel");
        PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);

        helper.assertTrue(definition != null, "channel definition should resolve a patched description");

        List<Component> lines = AbilityDefinitionDescriptions.collectDescription(null, definition);
        helper.assertTrue(containsLine(lines, "Cast: instant, channels every 1s"),
                "channel definitions should describe their pulse interval");
        helper.assertTrue(containsLine(lines, "Initial channel heal: 4"),
                "channel definitions should list the initial heal parameter");
        helper.assertTrue(containsLine(lines, "Per-pulse channel heal: 2"),
                "channel definitions should list the per-pulse heal parameter");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityDisplayEntryResolvesAbilities2DefinitionMetadata(GameTestHelper helper) {
        AbilityDisplayEntry display = AbilityDisplayEntry.resolve(MKCore.id("test_abilities2_self_heal"));

        helper.assertTrue(display.definitionBacked(), "abilities2 definitions should be marked definition-backed");
        helper.assertValueEqual(display.displayName().getString(), "Abilities2 Self Heal",
                "definition display name should come from the presentation block");
        helper.assertTrue(display.abilityType() != null, "definition display should expose a loadout ability type");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void setSlotSwapsWithExistingAbility(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        ResourceLocation emberId = learnAbility(playerData, MKTestAbilities.TEST_EMBER.get());
        ResourceLocation healId = learnAbility(playerData, MKTestAbilities.TEST_HEAL.get());

        basicGroup.setSlots(2);
        basicGroup.setSlot(0, emberId);
        basicGroup.setSlot(1, healId);
        basicGroup.setSlot(0, healId);

        helper.assertValueEqual(basicGroup.getSlot(0), healId, "swapped slot 0");
        helper.assertValueEqual(basicGroup.getSlot(1), emberId, "swapped slot 1");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void setSlotRejectsUnknownAbility(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        ResourceLocation emberId = MKTestAbilities.TEST_EMBER.get().getAbilityId();

        basicGroup.setSlots(1);
        basicGroup.setSlot(0, emberId);

        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY, "unknown ability should not be slotted");
        helper.assertTrue(basicGroup.getAbilityInfo(0) == null, "unknown ability should not resolve in the slot");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void setSlotRejectsWrongAbilityType(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        ResourceLocation passiveId = learnAbility(playerData, MKTestAbilities.TEST_NEW_BURNING_SOUL.get());

        basicGroup.setSlots(1);
        basicGroup.setSlot(0, passiveId);

        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY, "passive ability should not fit in basic slots");
        helper.assertTrue(basicGroup.getAbilityInfo(0) == null, "wrong-typed ability should not resolve in the slot");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void setSlotRejectsUnknownAbilities2Definition(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        ResourceLocation abilityId = MKCore.id("test_abilities2_self_heal");

        basicGroup.setSlots(1);
        basicGroup.setSlot(0, abilityId);

        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY,
                "unknown abilities2 definition should not be slotted");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void tryEquipUsesFirstFreeUnlockedSlotAndFailsWhenOnlyLockedSlotsRemain(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        ResourceLocation emberId = learnAbility(playerData, MKTestAbilities.TEST_EMBER.get());
        ResourceLocation healId = learnAbility(playerData, MKTestAbilities.TEST_HEAL.get());
        ResourceLocation newHealId = learnAbility(playerData, MKTestAbilities.TEST_NEW_HEAL.get());

        basicGroup.setSlots(2);

        helper.assertTrue(basicGroup.tryEquip(emberId), "first known ability should equip into the first free slot");
        helper.assertTrue(basicGroup.tryEquip(healId), "second known ability should equip into the next free slot");
        helper.assertTrue(basicGroup.tryEquip(emberId), "already equipped ability should report success");
        helper.assertFalse(basicGroup.tryEquip(newHealId), "full unlocked bar should reject another unslotted ability");
        helper.assertValueEqual(basicGroup.getSlot(0), emberId, "first free slot assignment");
        helper.assertValueEqual(basicGroup.getSlot(1), healId, "second free slot assignment");
        helper.assertTrue(basicGroup.getAbilitySlot(newHealId) == -1, "ability should remain unslotted when only locked slots are available");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void reducingSlotCountClearsLockedSlots(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        ResourceLocation emberId = learnAbility(playerData, MKTestAbilities.TEST_EMBER.get());
        ResourceLocation healId = learnAbility(playerData, MKTestAbilities.TEST_HEAL.get());

        basicGroup.setSlots(2);
        basicGroup.setSlot(0, emberId);
        basicGroup.setSlot(1, healId);

        helper.assertTrue(basicGroup.setSlots(1), "slot count should shrink");
        helper.assertValueEqual(basicGroup.getCurrentSlotCount(), 1, "shrunk slot count");
        helper.assertValueEqual(basicGroup.getSlot(0), emberId, "unlocked slot should remain unchanged");
        helper.assertValueEqual(basicGroup.getSlot(1), MKCoreRegistry.INVALID_ABILITY, "locked slot should be cleared");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void unlearningAbilityClearsSlottedEntry(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        MKAbility ember = MKTestAbilities.TEST_EMBER.get();
        ResourceLocation emberId = learnAbility(playerData, ember);

        basicGroup.setSlots(1);
        basicGroup.setSlot(0, emberId);
        helper.assertValueEqual(basicGroup.getSlot(0), emberId, "pre-unlearn slot state");

        helper.assertTrue(playerData.getAbilities().unlearnAbility(emberId, AbilitySource.ADMIN), "ability should unlearn successfully");
        helper.assertFalse(playerData.getAbilities().knowsAbility(emberId), "ability should no longer be known");
        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY, "unlearning should clear the slotted entry");
        helper.assertTrue(basicGroup.getAbilityInfo(0) == null, "cleared slot should not resolve an ability");
        helper.succeed();
    }

    private static MKServerPlayerData createPlayerData(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "phase0-test-player"), false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                cookie.gameProfile(),
                cookie.clientInformation()
        );
        return (MKServerPlayerData) MKCore.getPlayerOrThrow(player);
    }

    private static void setupTalentedLoadout(MKServerPlayerData playerData, ResourceLocation abilityId) {
        setupTalentedLoadout(playerData, TEST_TREE, abilityId);
    }

    private static void setupTalentedLoadout(MKServerPlayerData playerData,
                                             ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> treeId,
                                             ResourceLocation abilityId) {
        playerData.getTalents().grantTalentPoints(2);
        boolean unlocked = playerData.getTalents().unlockTree(treeId);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree " + treeId.location());
        }

        if (!playerData.getTalents().spendTalentPoint(treeId, TEST_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock slot-granting test talent");
        }
        if (!playerData.getTalents().spendTalentPoint(treeId, TEST_LINE, 1)) {
            throw new IllegalStateException("Failed to unlock ability-granting test talent");
        }

        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        basicGroup.setSlot(0, abilityId);
    }

    private static ResourceLocation learnAbility(MKServerPlayerData playerData, MKAbility ability) {
        if (!playerData.getAbilities().learnAbility(ability, AbilitySource.ADMIN)) {
            throw new IllegalStateException("Failed to learn test ability " + ability.getAbilityId());
        }
        return ability.getAbilityId();
    }

    private static boolean containsLine(List<Component> lines, String expected) {
        return lines.stream().map(Component::getString).anyMatch(expected::equals);
    }

    private static CompoundTag getDefaultPersonaTag(CompoundTag root) {
        return root.getCompound("persona")
                .getCompound("personas")
                .getCompound(PersonaManager.DEFAULT_PERSONA_NAME);
    }

    private static void removePersistedAbility(CompoundTag root, ResourceLocation abilityId) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        personaTag.getCompound("abilities").getCompound("known").remove(abilityId.toString());
    }

    private static void setBasicSlots(CompoundTag root, int slotCount) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        personaTag.getCompound("loadout").getCompound("basic").putInt("slots", slotCount);
    }

    private static void setBasicAbilities(CompoundTag root, ResourceLocation... abilityIds) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        ListTag list = new ListTag();
        for (ResourceLocation abilityId : abilityIds) {
            list.add(StringTag.valueOf(abilityId.toString()));
        }
        personaTag.getCompound("loadout").getCompound("basic").put("abilities", list);
    }

    private static void setPassiveSlots(CompoundTag root, int slotCount) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        personaTag.getCompound("loadout").getCompound("passive").putInt("slots", slotCount);
    }

    private static void setPassiveAbilities(CompoundTag root, ResourceLocation... abilityIds) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        ListTag list = new ListTag();
        for (ResourceLocation abilityId : abilityIds) {
            list.add(StringTag.valueOf(abilityId.toString()));
        }
        personaTag.getCompound("loadout").getCompound("passive").put("abilities", list);
    }
}
