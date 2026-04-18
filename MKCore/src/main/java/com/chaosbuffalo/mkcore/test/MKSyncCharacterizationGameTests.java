package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerKnownAbility;
import com.chaosbuffalo.mkcore.init.CoreArmorClasses;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.adapters.SyncArrayListUpdater;
import com.chaosbuffalo.mkcore.sync.adapters.SyncRegistrySet;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import com.chaosbuffalo.mkcore.sync.types.SyncInt;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKSyncCharacterizationGameTests {

    @GameTest(template = "player_data_phase0")
    public static void syncRegistrySetWritesCompactFullAndDirtyPayloads(GameTestHelper helper) {
        SyncContext context = new SyncContext(helper.getLevel().registryAccess());

        SyncRegistrySet<ResourceKey<ArmorClass>, ArmorClass> source =
                SyncRegistrySet.resourceKeys(new HashSet<>(), MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY);
        SyncRegistrySet<ResourceKey<ArmorClass>, ArmorClass> target =
                SyncRegistrySet.resourceKeys(new HashSet<>(), MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY);

        source.add(CoreArmorClasses.ROBES_ARMOR);
        source.add(CoreArmorClasses.HEAVY_ARMOR);

        Tag fullPayload = source.writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(fullPayload instanceof CompoundTag, "Full sync payload should be a compound tag");
        CompoundTag fullTag = (CompoundTag) fullPayload;
        helper.assertTrue(fullTag.contains("v", Tag.TAG_INT_ARRAY), "Full sync should use a compact int array");
        helper.assertFalse(fullTag.contains("l"), "Full sync should not fall back to string list payloads");

        target.handleUpdatePayload(context, fullTag, SyncVisibility.Private);
        helper.assertTrue(target.contains(CoreArmorClasses.ROBES_ARMOR), "Full sync should include robes mastery");
        helper.assertTrue(target.contains(CoreArmorClasses.HEAVY_ARMOR), "Full sync should include heavy mastery");
        helper.assertValueEqual(target.view().size(), 2, "full sync size");

        source.clearDirty();
        source.remove(CoreArmorClasses.ROBES_ARMOR);
        source.add(CoreArmorClasses.LIGHT_ARMOR);

        Tag dirtyPayload = source.writeDirtyValue(context, SyncVisibility.Private);
        helper.assertTrue(dirtyPayload instanceof CompoundTag, "Dirty sync payload should be a compound tag");
        CompoundTag dirtyTag = (CompoundTag) dirtyPayload;
        helper.assertTrue(dirtyTag.contains("a", Tag.TAG_INT_ARRAY), "Dirty sync should encode additions as int arrays");
        helper.assertTrue(dirtyTag.contains("r", Tag.TAG_INT_ARRAY), "Dirty sync should encode removals as int arrays");

        target.handleUpdatePayload(context, dirtyTag, SyncVisibility.Private);
        helper.assertFalse(target.contains(CoreArmorClasses.ROBES_ARMOR), "Dirty sync should remove robes mastery");
        helper.assertTrue(target.contains(CoreArmorClasses.HEAVY_ARMOR), "Dirty sync should retain unchanged mastery");
        helper.assertTrue(target.contains(CoreArmorClasses.LIGHT_ARMOR), "Dirty sync should add light mastery");
        helper.assertValueEqual(target.view().size(), 2, "dirty sync size");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void playerEquipmentArmorMasteryRoundTripsThroughSyncGroup(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        MKServerPlayerData targetData = createPlayerData(helper);
        SyncContext context = new SyncContext(sourceData.getEntity().registryAccess());

        sourceData.getEquipment().enableArmorMastery(CoreArmorClasses.ROBES_ARMOR, true);
        sourceData.getEquipment().enableArmorMastery(CoreArmorClasses.HEAVY_ARMOR, true);

        CompoundTag payload = sourceData.getEquipment().getSyncGroup().writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(payload != null, "Equipment sync payload should be present");

        targetData.getEquipment().getSyncGroup().handleUpdatePayload(context, payload, SyncVisibility.Private);

        Holder<ArmorClass> robesHolder = helper.getLevel().registryAccess()
                .registryOrThrow(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY)
                .getHolderOrThrow(CoreArmorClasses.ROBES_ARMOR);
        Holder<ArmorClass> heavyHolder = helper.getLevel().registryAccess()
                .registryOrThrow(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY)
                .getHolderOrThrow(CoreArmorClasses.HEAVY_ARMOR);

        helper.assertTrue(targetData.getEquipment().isArmorClassMastered(robesHolder), "Robes mastery should round-trip through equipment sync");
        helper.assertTrue(targetData.getEquipment().isArmorClassMastered(heavyHolder), "Heavy mastery should round-trip through equipment sync");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityGroupSyncUsesCompactRegistryIds(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        MKServerPlayerData targetData = createPlayerData(helper);
        SyncContext context = new SyncContext(sourceData.getEntity().registryAccess());
        ResourceLocation abilityId = learnAbility(sourceData, MKTestAbilities.TEST_EMBER.get());
        ResourceLocation secondAbilityId = learnAbility(sourceData, MKTestAbilities.TEST_HEAL.get());

        AbilityGroup sourceGroup = sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        sourceGroup.setSlot(0, abilityId);

        CompoundTag payload = sourceGroup.getSyncGroup().writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(payload != null, "Ability group sync payload should be present");
        CompoundTag activeTag = payload.getCompound("active");
        helper.assertTrue(activeTag.contains("s", Tag.TAG_LIST), "Default-backed slot sync should use sparse entry payloads");
        CompoundTag firstEntry = activeTag.getList("s", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(firstEntry.contains("v", Tag.TAG_INT), "Ability slot payloads should use compact registry ids");
        helper.assertFalse(firstEntry.contains("v", Tag.TAG_STRING), "Ability slot payloads should no longer send string ids");

        AbilityGroup targetGroup = targetData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        targetGroup.getSyncGroup().handleUpdatePayload(context, payload, SyncVisibility.Private);
        helper.assertValueEqual(targetGroup.getSlot(0), abilityId, "synced ability slot");

        sourceGroup.getSyncGroup().clearDirty();
        sourceGroup.clearSlot(0);
        sourceGroup.setSlot(1, secondAbilityId);

        CompoundTag dirtyPayload = sourceGroup.getSyncGroup().writeDirtyValue(context, SyncVisibility.Private);
        helper.assertTrue(dirtyPayload != null, "Ability group dirty sync payload should be present");
        CompoundTag dirtyActiveTag = dirtyPayload.getCompound("active");
        helper.assertFalse(dirtyActiveTag.getBoolean("f"), "Clearing a default-backed slot should stay incremental");
        var dirtyEntries = dirtyActiveTag.getList("s", Tag.TAG_COMPOUND);
        helper.assertValueEqual(dirtyEntries.size(), 2, "dirty slot update count");
        CompoundTag clearedEntry = dirtyEntries.getCompound(0);
        helper.assertValueEqual(clearedEntry.getInt("i"), 0, "cleared slot index");
        helper.assertValueEqual(clearedEntry.getInt("v"), -1, "cleared slot sentinel");
        CompoundTag dirtyEntry = dirtyEntries.getCompound(1);
        helper.assertValueEqual(dirtyEntry.getInt("i"), 1, "set slot index");
        helper.assertTrue(dirtyEntry.contains("v", Tag.TAG_INT), "Dirty slot payloads should also use compact registry ids");

        targetGroup.getSyncGroup().handleUpdatePayload(context, dirtyPayload, SyncVisibility.Private);
        helper.assertValueEqual(targetGroup.getSlot(0), MKCoreRegistry.INVALID_ABILITY, "dirty sync cleared slot");
        helper.assertValueEqual(targetGroup.getSlot(1), secondAbilityId, "dirty sync set slot");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityGroupFullSyncClearsOmittedDefaultBackedSlots(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        MKServerPlayerData targetData = createPlayerData(helper);
        SyncContext context = new SyncContext(sourceData.getEntity().registryAccess());
        ResourceLocation emberId = learnAbility(sourceData, MKTestAbilities.TEST_EMBER.get());
        ResourceLocation healId = learnAbility(targetData, MKTestAbilities.TEST_HEAL.get());
        learnAbility(targetData, MKTestAbilities.TEST_EMBER.get());

        AbilityGroup sourceGroup = sourceData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        AbilityGroup targetGroup = targetData.getLoadout().getAbilityGroup(AbilityGroupId.Basic);
        sourceGroup.setSlot(0, emberId);
        targetGroup.setSlot(0, healId);
        targetGroup.setSlot(1, emberId);

        CompoundTag payload = sourceGroup.getSyncGroup().writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(payload != null, "Ability group full sync payload should be present");
        CompoundTag activeTag = payload.getCompound("active");
        helper.assertTrue(activeTag.getBoolean("f"), "Sparse full sync should be marked as a full refresh");
        helper.assertTrue(activeTag.contains("n", Tag.TAG_INT), "Sparse full sync should carry the target list size");
        helper.assertValueEqual(activeTag.getList("s", Tag.TAG_COMPOUND).size(), 1,
                "Sparse full sync should omit default-valued slots");

        targetGroup.getSyncGroup().handleUpdatePayload(context, payload, SyncVisibility.Private);
        helper.assertValueEqual(targetGroup.getSlot(0), emberId, "Full sync should update the populated slot");
        helper.assertValueEqual(targetGroup.getSlot(1), MKCoreRegistry.INVALID_ABILITY,
                "Full sync should clear stale slots omitted from the sparse payload");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityKnowledgeSyncUsesCompactRegistryKeys(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        MKServerPlayerData targetData = createPlayerData(helper);
        SyncContext context = new SyncContext(sourceData.getEntity().registryAccess());

        MKAbility ember = MKTestAbilities.TEST_EMBER.get();
        MKAbility heal = MKTestAbilities.TEST_HEAL.get();
        sourceData.getAbilities().learnAbility(ember, AbilitySource.ADMIN);
        sourceData.getAbilities().learnAbility(heal, AbilitySource.ADMIN);

        CompoundTag fullPayload = sourceData.getAbilities().getSyncGroup().writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(fullPayload != null, "Knowledge sync payload should be present");
        CompoundTag knownTag = fullPayload.getCompound("known");
        helper.assertTrue(knownTag.contains("l", Tag.TAG_LIST),
                "Registry-backed map sync should use list-of-entries format");
        helper.assertFalse(knownTag.contains("l", Tag.TAG_COMPOUND),
                "Registry-backed map sync should not use string-keyed compound format");
        CompoundTag firstEntry = knownTag.getList("l", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(firstEntry.contains("k", Tag.TAG_INT),
                "Map entry keys should be compact registry ids");
        helper.assertTrue(firstEntry.contains("v", Tag.TAG_COMPOUND),
                "Map entry values should be compound tags");

        targetData.getAbilities().getSyncGroup().handleUpdatePayload(context, fullPayload, SyncVisibility.Private);
        helper.assertTrue(targetData.getAbilities().knowsAbility(ember.getAbilityId()),
                "Full sync should transfer ember knowledge");
        helper.assertTrue(targetData.getAbilities().knowsAbility(heal.getAbilityId()),
                "Full sync should transfer heal knowledge");

        sourceData.getAbilities().getSyncGroup().clearDirty();
        sourceData.getAbilities().unlearnAbility(ember.getAbilityId(), AbilitySource.ADMIN);

        CompoundTag dirtyPayload = sourceData.getAbilities().getSyncGroup().writeDirtyValue(context, SyncVisibility.Private);
        helper.assertTrue(dirtyPayload != null, "Dirty sync payload should be present");
        CompoundTag dirtyKnownTag = dirtyPayload.getCompound("known");
        helper.assertTrue(dirtyKnownTag.contains("r", Tag.TAG_INT_ARRAY),
                "Registry-backed map removals should use int array encoding");

        targetData.getAbilities().getSyncGroup().handleUpdatePayload(context, dirtyPayload, SyncVisibility.Private);
        helper.assertFalse(targetData.getAbilities().knowsAbility(ember.getAbilityId()),
                "Dirty sync should remove ember knowledge");
        helper.assertTrue(targetData.getAbilities().knowsAbility(heal.getAbilityId()),
                "Dirty sync should retain heal knowledge");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityKnowledgeFullSyncClearsStaleEntriesAndDirtySyncUpdatesExistingEntries(
            GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        MKServerPlayerData targetData = createPlayerData(helper);
        SyncContext context = new SyncContext(sourceData.getEntity().registryAccess());
        MKAbility ember = MKTestAbilities.TEST_EMBER.get();
        MKAbility heal = MKTestAbilities.TEST_HEAL.get();

        sourceData.getAbilities().learnAbility(ember, AbilitySource.ADMIN);
        targetData.getAbilities().learnAbility(ember, AbilitySource.ADMIN);
        targetData.getAbilities().learnAbility(heal, AbilitySource.ADMIN);

        CompoundTag fullPayload = sourceData.getAbilities().getSyncGroup().writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(fullPayload != null, "Knowledge full sync payload should be present");
        helper.assertTrue(fullPayload.getCompound("known").getBoolean("f"),
                "Knowledge full sync should clear target state before applying entries");

        targetData.getAbilities().getSyncGroup().handleUpdatePayload(context, fullPayload, SyncVisibility.Private);
        helper.assertTrue(targetData.getAbilities().knowsAbility(ember.getAbilityId()),
                "Full sync should retain synced knowledge");
        helper.assertFalse(targetData.getAbilities().knowsAbility(heal.getAbilityId()),
                "Full sync should remove stale known abilities absent from the source");

        sourceData.getAbilities().getSyncGroup().clearDirty();
        sourceData.getAbilities().learnAbility(ember, AbilitySource.GRANTED);

        CompoundTag dirtyPayload = sourceData.getAbilities().getSyncGroup().writeDirtyValue(context, SyncVisibility.Private);
        helper.assertTrue(dirtyPayload != null, "Knowledge dirty sync payload should be present");
        CompoundTag dirtyKnownTag = dirtyPayload.getCompound("known");
        helper.assertTrue(dirtyKnownTag.contains("l", Tag.TAG_LIST),
                "Dirty entry updates should serialize as map entries");
        helper.assertFalse(dirtyKnownTag.contains("r"),
                "Dirty entry updates should not encode removals when the key still exists");

        targetData.getAbilities().getSyncGroup().handleUpdatePayload(context, dirtyPayload, SyncVisibility.Private);
        PlayerKnownAbility syncedAbility = targetData.getAbilities().getKnownAbility(ember.getAbilityId());
        helper.assertTrue(syncedAbility != null, "Dirty sync should preserve the existing known ability entry");
        helper.assertTrue(syncedAbility.hasSource(AbilitySource.ADMIN),
                "Dirty sync should retain previously known sources");
        helper.assertTrue(syncedAbility.hasSource(AbilitySource.GRANTED),
                "Dirty sync should update the existing entry in place");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void abilityKnowledgeStorageUsesStableKeysAndClearsMissingEntries(GameTestHelper helper) {
        MKServerPlayerData sourceData = createPlayerData(helper);
        MKServerPlayerData targetData = createPlayerData(helper);
        ResourceLocation emberId = learnAbility(sourceData, MKTestAbilities.TEST_EMBER.get());
        ResourceLocation healId = learnAbility(targetData, MKTestAbilities.TEST_HEAL.get());

        CompoundTag serialized = sourceData.getAbilities().serialize(helper.getLevel().registryAccess());
        CompoundTag knownStorage = serialized.getCompound("known");
        helper.assertTrue(knownStorage.contains(emberId.toString(), Tag.TAG_COMPOUND),
                "Storage serialization should use stable string keys");
        helper.assertFalse(knownStorage.contains("l"),
                "Storage serialization should not use sync list payloads");

        targetData.getAbilities().deserialize(helper.getLevel().registryAccess(), serialized);
        helper.assertTrue(targetData.getAbilities().knowsAbility(emberId),
                "Storage deserialize should restore the serialized knowledge entry");
        helper.assertFalse(targetData.getAbilities().knowsAbility(healId),
                "Storage deserialize should clear entries that are missing from disk state");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void registryBackedListRejectsUnknownStringIdsWithoutClearingSlots(GameTestHelper helper) {
        SyncContext context = new SyncContext(helper.getLevel().registryAccess());
        ResourceLocation knownAbility = MKTestAbilities.TEST_EMBER.get().getAbilityId();
        List<ResourceLocation> target = NonNullList.withSize(2, MKCoreRegistry.INVALID_ABILITY);
        target.set(0, knownAbility);
        SyncArrayListUpdater<ResourceLocation> updater = SyncArrayListUpdater.registryResourceLocations(
                target,
                MKCoreRegistry.ABILITY_REGISTRY_KEY,
                MKCoreRegistry.INVALID_ABILITY
        );

        CompoundTag payload = new CompoundTag();
        CompoundTag entry = new CompoundTag();
        entry.putInt("i", 0);
        entry.put("v", StringTag.valueOf("mkcore:missing_ability"));
        ListTag sparseList = new ListTag();
        sparseList.add(entry);
        payload.put("s", sparseList);
        updater.handleUpdatePayload(context, payload, SyncVisibility.Private);

        helper.assertValueEqual(target.get(0), knownAbility, "invalid registry-backed update should not clear slot");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void syncGroupSeparatesVisibilityAcrossNestedChildGroups(GameTestHelper helper) {
        SyncContext context = new SyncContext(helper.getLevel().registryAccess());
        SyncGroup sourceRoot = new SyncGroup();
        SyncInt sourcePublic = new SyncInt(1);
        SyncGroup sourceChild = new SyncGroup();
        SyncInt sourcePrivate = new SyncInt(10);
        sourceRoot.addPublic("publicCount", sourcePublic);
        sourceChild.addPrivate("privateCount", sourcePrivate);
        sourceRoot.addChild("child", sourceChild);

        SyncGroup targetRoot = new SyncGroup();
        SyncInt targetPublic = new SyncInt(0);
        SyncGroup targetChild = new SyncGroup();
        SyncInt targetPrivate = new SyncInt(0);
        targetRoot.addPublic("publicCount", targetPublic);
        targetChild.addPrivate("privateCount", targetPrivate);
        targetRoot.addChild("child", targetChild);

        sourceRoot.clearDirty();
        sourcePublic.set(5);
        sourcePrivate.set(15);
        helper.assertTrue(sourceRoot.isDirty(SyncVisibility.Public), "Root group should track public dirtiness");
        helper.assertTrue(sourceRoot.isDirty(SyncVisibility.Private), "Root group should track child private dirtiness");

        CompoundTag publicPayload = sourceRoot.writeDirtyValue(context, SyncVisibility.Public);
        helper.assertTrue(publicPayload != null, "Public dirty payload should be present");
        helper.assertTrue(publicPayload.contains("publicCount", Tag.TAG_INT), "Public dirty payload should include public member");
        helper.assertFalse(publicPayload.contains("child"), "Public dirty payload should not include private child data");
        helper.assertFalse(sourceRoot.isDirty(SyncVisibility.Public), "Writing public dirtiness should clear only public state");
        helper.assertTrue(sourceRoot.isDirty(SyncVisibility.Private), "Private child dirtiness should remain queued");

        targetRoot.handleUpdatePayload(context, publicPayload, SyncVisibility.Public);
        helper.assertValueEqual(targetPublic.get(), 5, "Public dirty payload should update the target root");
        helper.assertValueEqual(targetPrivate.get(), 0, "Public dirty payload should not update private child state");

        CompoundTag privatePayload = sourceRoot.writeDirtyValue(context, SyncVisibility.Private);
        helper.assertTrue(privatePayload != null, "Private dirty payload should be present");
        helper.assertTrue(privatePayload.contains("child", Tag.TAG_COMPOUND),
                "Private dirty payload should include nested child payloads");
        helper.assertTrue(privatePayload.getCompound("child").contains("privateCount", Tag.TAG_INT),
                "Nested child payload should include the private member");

        targetRoot.handleUpdatePayload(context, privatePayload, SyncVisibility.Private);
        helper.assertValueEqual(targetPrivate.get(), 15, "Private dirty payload should update the target child group");
        helper.succeed();
    }

    @GameTest(template = "player_data_phase0")
    public static void syncGroupRejectsDirtyMembersThatReturnNull(GameTestHelper helper) {
        SyncContext context = new SyncContext(helper.getLevel().registryAccess());
        SyncGroup group = new SyncGroup();
        NullDirtySyncObject object = new NullDirtySyncObject();
        group.addPrivate("flaky", object);

        object.markDirty();
        try {
            group.writeDirtyValue(context, SyncVisibility.Private);
            throw new IllegalStateException("Expected dirty sync member to be rejected when it returns null");
        } catch (IllegalStateException e) {
            helper.assertTrue(e.getMessage().contains("returned null"),
                    "SyncGroup should reject dirty members that violate the writeDirtyValue contract");
        }
        helper.succeed();
    }

    private static MKServerPlayerData createPlayerData(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "sync-set-test-player"), false);
        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                cookie.gameProfile(),
                cookie.clientInformation()
        );
        return (MKServerPlayerData) MKCore.getPlayerOrThrow(player);
    }

    private static ResourceLocation learnAbility(MKServerPlayerData playerData, MKAbility ability) {
        if (!playerData.getAbilities().learnAbility(ability, AbilitySource.ADMIN)) {
            throw new IllegalStateException("Failed to learn test ability " + ability.getAbilityId());
        }
        return ability.getAbilityId();
    }

    private static class NullDirtySyncObject implements ISyncObject {
        private ISyncNotifier notifier = ISyncNotifier.NONE;
        private boolean dirty;

        public void markDirty() {
            dirty = true;
            notifier.notifyUpdate();
        }

        @Override
        public void setSyncUpdateNotifier(ISyncNotifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public boolean isDirty() {
            return dirty;
        }

        @Override
        public void clearDirty() {
            dirty = false;
        }

        @Override
        public Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
            return writeDirtyValue(context, visibility);
        }

        @Override
        public Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
            return null;
        }

        @Override
        public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        }
    }
}
