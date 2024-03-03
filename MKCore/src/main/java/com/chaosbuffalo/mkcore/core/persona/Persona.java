package com.chaosbuffalo.mkcore.core.persona;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public class Persona implements IMKSerializable<CompoundTag>, IPlayerSyncComponentProvider {
    private final String name;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("knowledge");
    private final PlayerAbilityKnowledge abilities;
    private final PlayerTalentKnowledge talents;
    private final PlayerEntitlementKnowledge entitlements;
    private final PlayerAbilityLoadout loadout;
    private final PlayerSkills skills;
    private final MKPlayerData data;
    private final Map<Class<? extends IPersonaExtension>, IPersonaExtension> extensions = new IdentityHashMap<>();
    private UUID personaId;

    public Persona(MKPlayerData playerData, String name) {
        this.name = name;
        data = playerData;
        personaId = UUID.randomUUID();
        abilities = new PlayerAbilityKnowledge(playerData);
        talents = new PlayerTalentKnowledge(playerData);
        loadout = new PlayerAbilityLoadout(playerData);
        entitlements = new PlayerEntitlementKnowledge(playerData);
        addSyncChild(abilities);
        addSyncChild(talents);
        addSyncChild(loadout);
        skills = new PlayerSkills(playerData);
        skills.addCallback(loadout.getPassiveAbilityGroup()::onSkillUpdate);
    }

    public String getName() {
        return name;
    }

    public MKPlayerData getPlayerData() {
        return data;
    }

    public Player getEntity() {
        return data.getEntity();
    }

    public UUID getPersonaId() {
        return personaId;
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public PlayerSkills getSkills() {
        return skills;
    }

    public PlayerAbilityKnowledge getAbilities() {
        return abilities;
    }

    public PlayerAbilityLoadout getLoadout() {
        return loadout;
    }

    public PlayerTalentKnowledge getTalents() {
        return talents;
    }

    public PlayerEntitlementKnowledge getEntitlements() {
        return entitlements;
    }

    void registerExtension(IPersonaExtension extension) {
        extensions.put(extension.getClass(), extension);
    }

    public <T extends IPersonaExtension> T getExtension(Class<T> clazz) {
        IPersonaExtension extension = extensions.get(clazz);
        return extension == null ? null : clazz.cast(extension);
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerKnowledge.onPersonaActivated");
        entitlements.onPersonaActivated();
        talents.onPersonaActivated();
        skills.onPersonaActivated();
        loadout.onPersonaActivated();
    }

    public void onPersonaDeactivated() {
        MKCore.LOGGER.debug("PlayerKnowledge.onPersonaDeactivated");
        skills.onPersonaDeactivated();
        loadout.onPersonaDeactivated();
    }

    public void activate() {
        sync.attach(data.getSyncController());
        onPersonaActivated();
    }

    public void deactivate() {
        sync.detach(data.getSyncController());
        onPersonaDeactivated();
    }

    private CompoundTag serializeExtensions() {
        CompoundTag root = new CompoundTag();
        extensions.values().forEach(extension -> {
            CompoundTag output = extension.serialize();
            if (output != null) {
                root.put(extension.getName().toString(), output);
            }
        });
        return root;
    }

    private void deserializeExtensions(CompoundTag root) {
        if (root.isEmpty())
            return;

        extensions.values().forEach(extension -> {
            String name = extension.getName().toString();
            if (root.contains(name)) {
                extension.deserialize(root.getCompound(name));
            }
        });
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("personaId", personaId);
        tag.put("abilities", abilities.serialize());
        tag.put("talents", talents.serializeNBT());
        tag.put("entitlements", entitlements.serialize());
        tag.put("skills", skills.serialize());
        tag.put("loadout", loadout.serializeNBT());
        tag.put("extensions", serializeExtensions());
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        personaId = tag.getUUID("personaId");
        abilities.deserialize(tag.getCompound("abilities"));
        talents.deserializeNBT(tag.get("talents"));
        entitlements.deserialize(tag.getCompound("entitlements"));
        skills.deserialize(tag.getCompound("skills"));
        loadout.deserializeNBT(tag.getCompound("loadout"));
        deserializeExtensions(tag.getCompound("extensions"));
        return true;
    }
}
