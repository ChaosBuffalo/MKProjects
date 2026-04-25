package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Method;
import java.util.UUID;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKPlayerDataCharacterizationGameTests {
    private static final ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> TEST_TREE =
            ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, MKCore.id("player_data_phase0"));
    private static final String TEST_LINE = "a";
    private static final String TEST_ATTRIBUTE_LINE = "b";
    private static final float FLOAT_EPSILON = 0.001f;

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

    @GameTest(template = "player_data_phase0")
    public static void spendingNegativeTalentIndexFailsWithoutChangingState(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(1);

        boolean unlocked = playerData.getTalents().unlockTree(TEST_TREE);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        helper.assertFalse(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, -1),
                "negative talent index should be rejected");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 1,
                "failed spend should not consume talent points");
        helper.assertTrue(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0) != null,
                "known test record should still resolve");
        helper.assertFalse(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0).isKnown(),
                "failed spend should not unlock any talent");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void spendingChildTalentBeforeParentFailsWithoutChangingState(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(1);

        boolean unlocked = playerData.getTalents().unlockTree(TEST_TREE);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        helper.assertFalse(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 1),
                "child talent should not unlock before its parent");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 1,
                "failed child spend should not consume a talent point");
        helper.assertTrue(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0) != null,
                "parent record should still resolve");
        helper.assertTrue(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 1) != null,
                "child record should still resolve");
        helper.assertFalse(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0).isKnown(),
                "failed child spend should not unlock the parent");
        helper.assertFalse(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 1).isKnown(),
                "failed child spend should not unlock the child");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void refundingParentTalentFailsWhileChildKnown(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(2);

        boolean unlocked = playerData.getTalents().unlockTree(TEST_TREE);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0),
                "parent talent should unlock");
        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 1),
                "child talent should unlock after parent");

        helper.assertFalse(playerData.getTalents().refundTalentPoint(TEST_TREE, TEST_LINE, 0),
                "parent talent should not refund while child is known");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 0,
                "failed refund should not restore a talent point");
        helper.assertTrue(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0) != null,
                "parent record should still resolve");
        helper.assertTrue(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0).isKnown(),
                "failed refund should leave the parent talent known");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void refundingAbilityGrantTalentUnlearnsAbilityAndClearsSlot(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(2);
        boolean unlocked = playerData.getTalents().unlockTree(TEST_TREE);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0),
                "slot talent should unlock");
        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 1),
                "ability talent should unlock");

        ResourceLocation emberId = MKTestAbilities.TEST_EMBER.get().getAbilityId();
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        basicGroup.setSlot(0, emberId);

        helper.assertTrue(playerData.getAbilities().knowsAbility(emberId),
                "ability talent should grant its ability");
        helper.assertValueEqual(basicGroup.getCurrentSlotCount(), 1,
                "slot-granting parent should unlock one basic slot");
        helper.assertValueEqual(basicGroup.getSlot(0), emberId,
                "granted ability should be slotted before refund");

        helper.assertTrue(playerData.getTalents().refundTalentPoint(TEST_TREE, TEST_LINE, 1),
                "ability talent should refund once its children are gone");
        helper.assertFalse(playerData.getAbilities().knowsAbility(emberId),
                "refunding the ability talent should unlearn its ability");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 1,
                "refunding the ability talent should restore one talent point");
        helper.assertTrue(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 1) != null,
                "ability talent record should still resolve");
        helper.assertFalse(playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 1).isKnown(),
                "ability talent should no longer be known after refund");
        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY,
                "refunding the ability talent should clear the slotted granted ability");
        helper.assertTrue(basicGroup.getAbilityInfo(0) == null,
                "refunding the ability talent should clear resolved slot info");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void refundingSlotGrantTalentShrinksLoadoutAndClearsSlottedAbility(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(2);
        boolean unlocked = playerData.getTalents().unlockTree(TEST_TREE);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        ResourceLocation healId = learnAbility(playerData, MKTestAbilities.TEST_HEAL.get());
        AbilityGroup basicGroup = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);

        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0),
                "slot talent should unlock");
        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 1),
                "ability talent should unlock");
        basicGroup.setSlot(0, healId);

        helper.assertValueEqual(basicGroup.getCurrentSlotCount(), 1,
                "slot talent should unlock one basic slot");
        helper.assertValueEqual(basicGroup.getSlot(0), healId,
                "trained ability should occupy the talent-granted slot");

        helper.assertTrue(playerData.getTalents().refundTalentPoint(TEST_TREE, TEST_LINE, 1),
                "child talent should refund before the parent");
        helper.assertTrue(playerData.getTalents().refundTalentPoint(TEST_TREE, TEST_LINE, 0),
                "slot talent should refund after its child is gone");

        helper.assertTrue(playerData.getAbilities().knowsAbility(healId),
                "trained ability should remain known after slot talent refund");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 2,
                "refunding both talents should restore both points");
        helper.assertValueEqual(basicGroup.getCurrentSlotCount(), 0,
                "slot talent refund should remove the granted basic slot");
        helper.assertValueEqual(basicGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY,
                "locking the slot should clear the slotted ability");
        helper.assertTrue(basicGroup.getAbilityInfo(0) == null,
                "locking the slot should clear resolved slot info");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void singleXpGrantAwardsMultipleTalentLevels(GameTestHelper helper) {
        int maxPoints = MKConfig.SERVER.maxTalentPoints.get();
        if (maxPoints > 0 && maxPoints < 2) {
            throw new IllegalStateException("Test requires max talent points >= 2");
        }

        MKServerPlayerData playerData = createPlayerData(helper);
        MKServerPlayerData thresholdProbe = createPlayerData(helper);
        int firstThreshold = thresholdProbe.getTalents().getXpToNextLevel();
        thresholdProbe.getTalents().grantTalentPoints(1);
        int secondThreshold = thresholdProbe.getTalents().getXpToNextLevel();

        playerData.getTalents().addTalentXp(firstThreshold + secondThreshold);

        helper.assertValueEqual(playerData.getTalents().getTotalTalentPoints(), 2,
                "single xp grant should award both talent points");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 2,
                "single xp grant should leave both earned points unspent");
        helper.assertValueEqual(playerData.getTalents().getTalentXp(), 0,
                "exact multi-level xp grant should fully consume its xp");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void invalidTalentTreeVersionDeserializeResetsTreeAndRefundsPoints(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        sourceData.getTalents().grantTalentPoints(1);
        if (!sourceData.getTalents().unlockTree(TEST_TREE)) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }
        if (!sourceData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock test talent");
        }

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);
        setTalentTreeVersion(serialized, TEST_TREE, 999);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);

        helper.assertTrue(restoredData.getTalents().getTree(TEST_TREE) != null,
                "invalid tree data should still leave the tree unlocked");
        helper.assertTrue(restoredData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0) != null,
                "blank replacement tree should still resolve known records");
        helper.assertFalse(restoredData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0).isKnown(),
                "invalid tree version should reset talent progress");
        helper.assertValueEqual(restoredData.getTalents().getTotalTalentPoints(), 1,
                "invalid tree version should preserve total earned talent points");
        helper.assertValueEqual(restoredData.getTalents().getUnspentTalentPoints(), 1,
                "invalid tree version should refund spent points");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void unaffordableTalentTreeDeserializeResetsTreeWithoutSpendingPoints(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        sourceData.getTalents().grantTalentPoints(2);
        if (!sourceData.getTalents().unlockTree(TEST_TREE)) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }
        if (!sourceData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock slot talent");
        }
        if (!sourceData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 1)) {
            throw new IllegalStateException("Failed to unlock ability talent");
        }

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);
        setTotalTalentPoints(serialized, 1);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);

        helper.assertTrue(restoredData.getTalents().getTree(TEST_TREE) != null,
                "unaffordable tree data should still leave the tree unlocked");
        helper.assertTrue(restoredData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0) != null,
                "replacement tree should still resolve the parent talent");
        helper.assertTrue(restoredData.getTalents().getRecord(TEST_TREE, TEST_LINE, 1) != null,
                "replacement tree should still resolve the child talent");
        helper.assertFalse(restoredData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0).isKnown(),
                "unaffordable tree data should reset the parent talent");
        helper.assertFalse(restoredData.getTalents().getRecord(TEST_TREE, TEST_LINE, 1).isKnown(),
                "unaffordable tree data should reset the child talent");
        helper.assertValueEqual(restoredData.getTalents().getTotalTalentPoints(), 1,
                "unaffordable tree data should keep the reduced total talent points");
        helper.assertValueEqual(restoredData.getTalents().getUnspentTalentPoints(), 1,
                "unaffordable tree data should not spend unavailable points");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void negativeTalentRankDeserializeIsRejected(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(1);
        if (!playerData.getTalents().unlockTree(TEST_TREE)) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }
        if (!playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock test talent");
        }

        TalentRecord record = playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0);
        if (record == null) {
            throw new IllegalStateException("Failed to resolve test talent");
        }

        CompoundTag invalidRank = new CompoundTag();
        invalidRank.putInt("rank", -1);

        helper.assertFalse(record.deserialize(new Dynamic<>(NbtOps.INSTANCE, invalidRank)),
                "negative talent rank should be rejected during deserialize");
        helper.assertValueEqual(record.getRank(), 1,
                "rejected deserialize should not mutate the existing rank");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void invalidTalentSyncRankIsIgnored(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(1);
        if (!playerData.getTalents().unlockTree(TEST_TREE)) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }
        if (!playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock test talent");
        }

        TalentTreeRecord treeRecord = playerData.getTalents().getTree(TEST_TREE);
        TalentRecord record = playerData.getTalents().getRecord(TEST_TREE, TEST_LINE, 0);
        if (treeRecord == null || record == null) {
            throw new IllegalStateException("Failed to resolve test talent tree state");
        }

        CompoundTag invalidUpdate = new CompoundTag();
        invalidUpdate.putIntArray(TEST_LINE, new int[]{0, -1});

        getTalentUpdater(treeRecord).handleUpdatePayload(null, invalidUpdate, SyncVisibility.Private);

        helper.assertValueEqual(record.getRank(), 1,
                "invalid sync rank should not mutate the existing rank");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void spendingAttributeTalentAppliesModifierAndRefundRemovesIt(GameTestHelper helper) {
        MKServerPlayerData playerData = createPlayerData(helper);
        playerData.getTalents().grantTalentPoints(1);
        if (!playerData.getTalents().unlockTree(TEST_TREE)) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        float baseMaxHealth = playerData.getEntity().getMaxHealth();

        helper.assertTrue(playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_ATTRIBUTE_LINE, 0),
                "attribute talent should unlock");
        assertFloatEquals(helper, playerData.getEntity().getMaxHealth(), baseMaxHealth + 1.0f, FLOAT_EPSILON,
                "attribute talent should immediately increase max health");

        helper.assertTrue(playerData.getTalents().refundTalentPoint(TEST_TREE, TEST_ATTRIBUTE_LINE, 0),
                "attribute talent should refund");
        helper.assertValueEqual(playerData.getTalents().getUnspentTalentPoints(), 1,
                "refunding the attribute talent should restore its point");
        assertFloatEquals(helper, playerData.getEntity().getMaxHealth(), baseMaxHealth, FLOAT_EPSILON,
                "refunding the attribute talent should remove the max health bonus");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void deserializedAttributeTalentAppliesOnActivationAndIsRemovedOnPersonaSwitch(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        sourceData.getTalents().grantTalentPoints(1);
        if (!sourceData.getTalents().unlockTree(TEST_TREE)) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        float baseMaxHealth = sourceData.getEntity().getMaxHealth();
        if (!sourceData.getTalents().spendTalentPoint(TEST_TREE, TEST_ATTRIBUTE_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock attribute talent");
        }

        HolderLookup.Provider provider = sourceData.getEntity().registryAccess();
        CompoundTag serialized = sourceData.serializeNBT(provider);

        MKServerPlayerData restoredData = createPlayerData(helper);
        restoredData.deserializeNBT(provider, serialized);
        assertFloatEquals(helper, restoredData.getEntity().getMaxHealth(), baseMaxHealth, FLOAT_EPSILON,
                "deserialized attribute talent should not apply before activation");

        restoredData.getPersonaManager().onJoinLevel();
        assertFloatEquals(helper, restoredData.getEntity().getMaxHealth(), baseMaxHealth + 1.0f, FLOAT_EPSILON,
                "activating the persona should apply the attribute talent");

        helper.assertTrue(restoredData.getPersonaManager().createPersona("other"),
                "secondary persona should be created for deactivation coverage");
        helper.assertTrue(restoredData.getPersonaManager().activatePersona("other"),
                "switching to another persona should deactivate the attribute talent");
        assertFloatEquals(helper, restoredData.getEntity().getMaxHealth(), baseMaxHealth, FLOAT_EPSILON,
                "deactivating the persona should remove the attribute talent bonus");

        helper.assertTrue(restoredData.getPersonaManager().activatePersona(PersonaManager.DEFAULT_PERSONA_NAME),
                "switching back to the default persona should reactivate the attribute talent");
        assertFloatEquals(helper, restoredData.getEntity().getMaxHealth(), baseMaxHealth + 1.0f, FLOAT_EPSILON,
                "reactivating the original persona should reapply the attribute talent bonus");
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
        playerData.getTalents().grantTalentPoints(2);
        boolean unlocked = playerData.getTalents().unlockTree(TEST_TREE);
        if (!unlocked) {
            throw new IllegalStateException("Failed to unlock test talent tree");
        }

        if (!playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 0)) {
            throw new IllegalStateException("Failed to unlock slot-granting test talent");
        }
        if (!playerData.getTalents().spendTalentPoint(TEST_TREE, TEST_LINE, 1)) {
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

    private static ISyncObject getTalentUpdater(TalentTreeRecord treeRecord) {
        try {
            Method method = TalentTreeRecord.class.getDeclaredMethod("getUpdater");
            method.setAccessible(true);
            return (ISyncObject) method.invoke(treeRecord);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to access talent tree updater", e);
        }
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

    private static CompoundTag getTalentTreeTag(CompoundTag root, ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> treeId) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        return personaTag.getCompound("talents")
                .getCompound("trees")
                .getCompound(treeId.location().toString());
    }

    private static void setTalentTreeVersion(CompoundTag root,
                                             ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> treeId,
                                             int version) {
        getTalentTreeTag(root, treeId).putInt("version", version);
    }

    private static void setTotalTalentPoints(CompoundTag root, int totalPoints) {
        CompoundTag personaTag = getDefaultPersonaTag(root);
        personaTag.getCompound("talents").putInt("totalPoints", totalPoints);
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

    private static void assertFloatEquals(GameTestHelper helper, float actual, float expected, float epsilon, String label) {
        helper.assertTrue(Math.abs(actual - expected) <= epsilon,
                label + ": expected " + expected + " but was " + actual);
    }
}
