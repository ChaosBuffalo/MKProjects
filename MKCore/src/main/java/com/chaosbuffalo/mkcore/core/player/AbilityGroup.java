package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.ResourceListUpdater;
import com.chaosbuffalo.mkcore.sync.SyncInt;
import com.chaosbuffalo.mkcore.sync.SyncListUpdater;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class AbilityGroup implements IPlayerSyncComponentProvider {
    protected final MKPlayerData playerData;
    protected final SyncComponent sync;
    protected final String name;
    private final List<ResourceLocation> activeAbilities;
    private final SyncListUpdater<ResourceLocation> activeUpdater;
    private final SyncInt slots;
    protected final AbilityGroupId groupId;

    public AbilityGroup(MKPlayerData playerData, String name, AbilityGroupId groupId) {
        this(playerData, name, groupId, groupId.getDefaultSlots(), groupId.getMaxSlots());
    }

    public AbilityGroup(MKPlayerData playerData, String name, AbilityGroupId groupId, int defaultSize, int max) {
        sync = new SyncComponent(name);
        this.playerData = playerData;
        this.name = name;
        this.groupId = groupId;
        activeAbilities = NonNullList.withSize(max, MKCoreRegistry.INVALID_ABILITY);
        activeUpdater = new ResourceListUpdater("active", () -> activeAbilities);
        slots = new SyncInt("slots", defaultSize);
        addSyncPrivate(activeUpdater);
        addSyncPrivate(slots);
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public List<ResourceLocation> getAbilities() {
        return Collections.unmodifiableList(activeAbilities);
    }

    public int getCurrentSlotCount() {
        return slots.get();
    }

    public int getMaximumSlotCount() {
        return activeAbilities.size();
    }

    public boolean setSlots(int newSlotCount) {
        if (newSlotCount < 0 || newSlotCount > getMaximumSlotCount()) {
            MKCore.LOGGER.error("setSlots({}, {}) - bad count", newSlotCount, getMaximumSlotCount());
            return false;
        }

        int currentCount = getCurrentSlotCount();
        slots.set(newSlotCount);

        if (newSlotCount > currentCount) {
            for (int i = currentCount; i < newSlotCount; i++) {
                onSlotUnlocked(i);
            }
        } else if (newSlotCount < currentCount) {
            for (int i = newSlotCount; i < currentCount; i++) {
                onSlotLocked(i);
            }
        }
        return true;
    }

    protected void onSlotLocked(int slot) {
        clearSlot(slot);
    }

    protected void onSlotUnlocked(int slot) {

    }

    protected int getFirstFreeAbilitySlot() {
        return getAbilitySlot(MKCoreRegistry.INVALID_ABILITY);
    }

    public int tryEquip(ResourceLocation abilityId) {
        int slot = getAbilitySlot(abilityId);
        if (slot == -1) {
            // Ability was just learned so let's try to put it on the bar
            slot = getFirstFreeAbilitySlot();
            if (slot != -1 && slot < getCurrentSlotCount()) {
                setSlot(slot, abilityId);
            }
        }

        return slot;
    }

    public int getAbilitySlot(ResourceLocation abilityId) {
        return activeAbilities.indexOf(abilityId);
    }

    @Nonnull
    public ResourceLocation getSlot(int slot) {
        if (slot < activeAbilities.size()) {
            return activeAbilities.get(slot);
        }
        return MKCoreRegistry.INVALID_ABILITY;
    }

    protected void onAbilityAdded(ResourceLocation abilityId) {
        MKCore.LOGGER.debug("onAbilityAdded({})", abilityId);
    }

    protected void onAbilityRemoved(ResourceLocation abilityId) {
        MKCore.LOGGER.debug("onAbilityRemoved({})", abilityId);
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability instanceof MKToggleAbility) {
            ((MKToggleAbility) ability).removeEffect(playerData.getEntity(), playerData);
        }
    }

    private void setIndex(int index, ResourceLocation abilityId) {
        activeAbilities.set(index, abilityId);
        activeUpdater.setDirty(index);
    }

    public void setSlot(int index, ResourceLocation abilityId) {
        MKCore.LOGGER.debug("AbilityGroup.setSlot({}, {}, {})", groupId, index, abilityId);

        ResourceLocation currentAbilityId = activeAbilities.get(index);
        // No need to do anything if it's already in the target slot
        if (abilityId.equals(currentAbilityId))
            return;

        // Clearing slot - no validity checks required
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
//            MKCore.LOGGER.info("setSlot - clearing {} from {}", index, currentAbilityId);
            setIndex(index, abilityId);
            onAbilityRemoved(currentAbilityId);
            return;
        }

        // Check if it's already present in another slot
        int newExistingSlot = getAbilitySlot(abilityId);
        if (newExistingSlot != -1) {
            // Ability was already in another slot, and we know it's not already at 'index'
//            MKCore.LOGGER.info("swapping {}:{} with {}:{}", newExistingSlot, abilityId, index, currentAbilityId);
            setIndex(index, abilityId);
            setIndex(newExistingSlot, currentAbilityId);
            return;
        }

        // abilityId was not already slotted - run the validity checks
        if (!validateAbilityForSlot(index, abilityId))
            return;

        // abilityId was not slotted and is being inserted into an empty slot
        if (currentAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            setIndex(index, abilityId);
            onAbilityAdded(abilityId);
            return;
        }

        // New ability is not current slotted and is replacing an existing ability
        setIndex(index, abilityId);
        onAbilityRemoved(currentAbilityId);
        onAbilityAdded(abilityId);
    }

    private boolean validateAbilityForSlot(int index, ResourceLocation abilityId) {
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability == null) {
            // not an ability
            return false;
        }

        if (groupId.requiresAbilityKnown() && !playerData.getAbilities().knowsAbility(abilityId)) {
            MKCore.LOGGER.error("setSlot({}, {}, {}) - player does not know ability!", groupId, index, abilityId);
            return false;
        }

        if (!groupId.fitsAbilityType(ability.getType())) {
            MKCore.LOGGER.error("setSlot({}, {}, {}) - ability does not fit in group", groupId, index, abilityId);
            return false;
        }
        return true;
    }

    public boolean isSlotUnlocked(int slot) {
        return slot < getCurrentSlotCount();
    }

    public void resetSlots() {
        for (int i = 0; i < activeAbilities.size(); i++) {
            clearSlot(i);
        }
    }

    public void clearAbility(ResourceLocation abilityId) {
        int slot = getAbilitySlot(abilityId);
        if (slot != -1) {
            clearSlot(slot);
        }
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo(int index) {
        ResourceLocation abilityId = getSlot(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return null;

        return playerData.getAbilities().getKnownAbility(abilityId);
    }

    public void executeSlot(int index) {
        MKAbilityInfo info = getAbilityInfo(index);
        if (info == null)
            return;

        playerData.getAbilityExecutor().executeAbilityInfoWithContext(info, null);
    }

    public void clearSlot(int slot) {
        setSlot(slot, MKCoreRegistry.INVALID_ABILITY);
    }

    public void onAbilityLearned(MKAbilityInfo info) {

    }

    public void onAbilityUnlearned(MKAbilityInfo info) {
        clearAbility(info.getId());
    }

    private void ensureValidAbility(ResourceLocation abilityId) {
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return;

        if (!groupId.requiresAbilityKnown() || playerData.getAbilities().knowsAbility(abilityId))
            return;

        MKCore.LOGGER.debug("ensureValidAbility({}, {}) - bad", groupId, abilityId);
        clearAbility(abilityId);
    }

    public void onJoinWorld() {

    }

    public void onPersonaActivated() {
        onPersonaSwitch();
    }

    public void onPersonaDeactivated() {

    }

    protected void onPersonaSwitch() {
        activeAbilities.forEach(this::ensureValidAbility);
    }

    protected <T> T serialize(DynamicOps<T> ops) {
        return ops.createMap(
                ImmutableMap.of(
                        ops.createString("slots"),
                        ops.createInt(getCurrentSlotCount()),
                        ops.createString("abilities"),
                        ops.createList(activeAbilities.stream().map(ResourceLocation::toString).map(ops::createString))
                )
        );
    }

    protected <T> void deserialize(Dynamic<T> dynamic) {
        slots.set(dynamic.get("slots").asInt(getCurrentSlotCount()));
        deserializeAbilityList(dynamic.get("abilities").orElseEmptyList(), this::setIndex);
    }

    public Tag serializeNBT() {
        return serialize(NbtOps.INSTANCE);
    }

    public void deserializeNBT(Tag tag) {
        deserialize(new Dynamic<>(NbtOps.INSTANCE, tag));
    }

    private <T> void deserializeAbilityList(Dynamic<T> dynamic, BiConsumer<Integer, ResourceLocation> consumer) {
        List<DataResult<String>> passives = dynamic.asList(Dynamic::asString);
        for (int i = 0; i < passives.size(); i++) {
            int index = i;
            passives.get(i).resultOrPartial(MKCore.LOGGER::error).ifPresent(idString -> {
                ResourceLocation abilityId = new ResourceLocation(idString);
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability != null) {
                    consumer.accept(index, abilityId);
                }
            });
        }
    }
}
