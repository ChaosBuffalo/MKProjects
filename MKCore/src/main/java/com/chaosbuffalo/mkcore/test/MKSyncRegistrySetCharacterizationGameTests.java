package com.chaosbuffalo.mkcore.test;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mkcore.init.CoreArmorClasses;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.adapters.SyncRegistrySet;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.UUID;

@GameTestHolder(MKCore.MOD_ID)
@PrefixGameTestTemplate(false)
public class MKSyncRegistrySetCharacterizationGameTests {

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

        Tag payload = sourceData.getEquipment().getSyncGroup().writeFullValue(context, SyncVisibility.Private);
        helper.assertTrue(payload instanceof CompoundTag, "Equipment sync payload should be a compound tag");

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
}
