package com.chaosbuffalo.mkcore.core.player.loadout;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityGrantSource;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityReference;
import com.chaosbuffalo.mkcore.abilities2.runtime.GrantedAbility;
import com.chaosbuffalo.mkcore.abilities2.runtime.PatchedAbilityDefinition;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.item.AbilitySourceOverride;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemAbilityGroup extends AbilityGroup {
    private static final AbilityGrantSource ITEM_GRANT_SOURCE = new AbilityGrantSource(
            MKCore.makeRL("grant_source.item"),
            MKCore.makeRL("loadout_group.item")
    );
    private static final List<EquipmentSlot> TRACKED_SLOTS = List.of(
            EquipmentSlot.MAINHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    );
    private boolean suppressAbility2PassiveRefresh = false;

    public ItemAbilityGroup(Persona persona) {
        super(persona, AbilityGroupId.Item);
    }

    @Override
    protected boolean requiresAbilityKnown() {
        return false;
    }

    @Override
    public int getCurrentSlotCount() {
        // Only report nonzero if the mainhand slot is filled
        return !getEquippedAbilityId(EquipmentSlot.MAINHAND).equals(MKCoreRegistry.INVALID_ABILITY) ? 1 : 0;
    }

    private int slot2index(EquipmentSlot slot) {
        return slot.ordinal();
    }

    private EquipmentSlot index2slot(int index) {
        return EquipmentSlot.values()[index];
    }

    public ResourceLocation getSlot(EquipmentSlot slot) {
        return getSlot(slot2index(slot));
    }

    public void setSlot(EquipmentSlot slot, MKAbility ability) {
        setSlot(slot2index(slot), ability.getAbilityId());
    }

    public void setSlot(EquipmentSlot slot, ResourceLocation abilityId) {
        setSlot(slot2index(slot), abilityId);
    }

    public void clearSlot(EquipmentSlot slot) {
        clearSlot(slot2index(slot));
    }

    @Override
    protected void onAbilityAdded(int index, MKAbilityInfo abilityInfo) {
        if (abilityInfo == null) {
            return;
        }
        super.onAbilityAdded(index, abilityInfo);
        MKAbility ability = abilityInfo.getAbility();
        EquipmentSlot slot = index2slot(index);
        if (slot.isArmor() && ability.getType() != AbilityType.Passive) {
            playerData.getAbilities().learnAbility(ability, AbilitySource.forEquipmentSlot(slot));
        }
    }

    @Override
    protected void onAbilityRemoved(int index, MKAbilityInfo abilityInfo) {
        if (abilityInfo == null) {
            return;
        }
        super.onAbilityRemoved(index, abilityInfo);
        MKAbility ability = abilityInfo.getAbility();
        EquipmentSlot slot = index2slot(index);
        if (slot.isArmor() && ability.getType() != AbilityType.Passive) {
            playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forEquipmentSlot(slot));
        }
    }

    @Override
    protected void onAbilityDefinitionAdded(int index, ResourceLocation abilityId) {
        if (!suppressAbility2PassiveRefresh) {
            refreshAbility2Passives();
        }
    }

    @Override
    protected void onAbilityDefinitionRemoved(int index, ResourceLocation abilityId) {
        if (!suppressAbility2PassiveRefresh) {
            refreshAbility2Passives();
        }
    }

    @Nullable
    @Override
    public MKAbilityInfo getAbilityInfo(int index) {
        EquipmentSlot slot = index2slot(index);
        ResourceLocation abilityId = getEquippedAbilityId(slot);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY))
            return null;

        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            return ability.createAbilityInfo();
        }
        return null;
    }

    @Override
    protected @Nullable PatchedAbilityDefinition resolveAbilityDefinition(ResourceLocation abilityId) {
        PatchedAbilityDefinition definition = MKCore.getAbilityDefinitionService().getResolver().resolvePatched(abilityId);
        if (definition == null) {
            return null;
        }

        ResourceLocation slotFamily = definition.definition().data().slotFamily();
        if (AbilityDatagenKeys.SLOT_FAMILY_BASIC.equals(slotFamily)
                || AbilityDatagenKeys.SLOT_FAMILY_PASSIVE.equals(slotFamily)
                || AbilityDatagenKeys.SLOT_FAMILY_ULTIMATE.equals(slotFamily)) {
            return definition;
        }
        return null;
    }

    @Override
    public void executeSlot(int index) {
        ResourceLocation abilityId = getSlot(index);
        if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            return;
        }

        MKAbilityInfo abilityInfo = getAbilityInfo(index);
        if (abilityInfo != null) {
            playerData.getAbilityExecutor().executeAbilityInfoWithContext(abilityInfo, null);
            return;
        }

        EquipmentSlot slot = index2slot(index);
        UUID sourceId = getEquippedAbilitySourceId(slot, abilityId);
        if (sourceId == null) {
            return;
        }

        MKCore.getAbilityRuntimeService().executeLoadoutAbility(
                playerData,
                playerData,
                groupId,
                new AbilityReference(abilityId, sourceId),
                sourceId
        );
    }

    @Override
    public void onPersonaActivated() {
        suppressAbility2PassiveRefresh = true;
        try {
            super.onPersonaActivated();
        } finally {
            suppressAbility2PassiveRefresh = false;
        }
        refreshAbility2Passives();
    }

    @Override
    public void onPersonaDeactivated() {
        suppressAbility2PassiveRefresh = true;
        try {
            clearAbility2Passives();
            super.onPersonaDeactivated();
        } finally {
            suppressAbility2PassiveRefresh = false;
        }
    }

    @Override
    protected boolean clearLockedSlotsOnLoad() {
        return false;
    }

    private void refreshAbility2Passives() {
        MKCore.getAbilityRuntimeService().refreshPassives(
                playerData,
                playerData,
                ITEM_GRANT_SOURCE,
                collectAbilities2Passives()
        );
    }

    private void clearAbility2Passives() {
        MKCore.getAbilityRuntimeService().refreshPassives(
                playerData,
                playerData,
                ITEM_GRANT_SOURCE,
                List.of()
        );
    }

    private List<GrantedAbility> collectAbilities2Passives() {
        List<GrantedAbility> desired = new ArrayList<>();
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            ResourceLocation abilityId = getEquippedAbilityId(slot);
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
                continue;
            }

            PatchedAbilityDefinition definition = resolveAbilityDefinition(abilityId);
            if (definition == null
                    || !AbilityDatagenKeys.SLOT_FAMILY_PASSIVE.equals(definition.definition().data().slotFamily())) {
                continue;
            }

            UUID sourceId = getEquippedAbilitySourceId(slot, abilityId);
            if (sourceId == null) {
                continue;
            }

            desired.add(new GrantedAbility(
                    sourceId,
                    abilityId,
                    Map.of(),
                    ITEM_GRANT_SOURCE
            ));
        }
        return desired;
    }

    private ResourceLocation getEquippedAbilityId(EquipmentSlot slot) {
        ResourceLocation slottedAbilityId = getSlot(slot2index(slot));
        if (slottedAbilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            return MKCoreRegistry.INVALID_ABILITY;
        }

        ItemGrantedAbility itemAbility = getEquippedGrantedAbility(slot);
        if (itemAbility == null || !slottedAbilityId.equals(itemAbility.abilityId())) {
            return MKCoreRegistry.INVALID_ABILITY;
        }

        return slottedAbilityId;
    }

    private @Nullable UUID getEquippedAbilitySourceId(EquipmentSlot slot, ResourceLocation abilityId) {
        ItemGrantedAbility itemAbility = getEquippedGrantedAbility(slot);
        if (itemAbility == null || !abilityId.equals(itemAbility.abilityId())) {
            return null;
        }
        return AbilitySourceOverride.getSource(playerData.getEntity().getItemBySlot(slot));
    }

    private @Nullable ItemGrantedAbility getEquippedGrantedAbility(EquipmentSlot slot) {
        ItemStack stack = playerData.getEntity().getItemBySlot(slot);
        if (stack.isEmpty()) {
            return null;
        }
        return stack.get(CoreItemComponents.ITEM_ABILITY);
    }
}
