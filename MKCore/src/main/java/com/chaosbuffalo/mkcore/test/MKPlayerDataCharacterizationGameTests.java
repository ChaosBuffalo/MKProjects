package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKPlayerDataCharacterizationGameTests {
    private static final ResourceKey<com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition> TEST_TREE =
            ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, MKCore.id("player_data_phase0"));
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
}
