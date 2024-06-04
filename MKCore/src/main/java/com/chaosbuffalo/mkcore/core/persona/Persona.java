package com.chaosbuffalo.mkcore.core.persona;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.player.events.EventPriorities;
import com.chaosbuffalo.mkcore.core.player.events.EventType;
import com.chaosbuffalo.mkcore.core.player.events.PersonaEventSubscription;
import com.chaosbuffalo.mkcore.core.player.events.PlayerEvent;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Persona implements IMKSerializable<CompoundTag>, IPlayerSyncComponentProvider {
    private final String name;
    private final PlayerSyncComponent sync = new PlayerSyncComponent("knowledge");
    private final PlayerAbilityKnowledge abilities;
    private final PlayerTalentKnowledge talents;
    private final PlayerEntitlements entitlements;
    private final PlayerAbilityLoadout loadout;
    private final PlayerSkills skills;
    private final MKPlayerData playerData;
    private final Map<Class<? extends IPersonaExtension>, IPersonaExtension> extensions = new IdentityHashMap<>();
    private UUID personaId;

    public Persona(MKPlayerData playerData, String name) {
        this.name = name;
        this.playerData = playerData;
        personaId = UUID.randomUUID();
        abilities = new PlayerAbilityKnowledge(this);
        talents = new PlayerTalentKnowledge(this);
        loadout = new PlayerAbilityLoadout(this);
        entitlements = new PlayerEntitlements(this);
        addSyncChild(abilities);
        addSyncChild(talents);
        addSyncChild(loadout);
        skills = new PlayerSkills(this);
    }

    public String getName() {
        return name;
    }

    public MKPlayerData getPlayerData() {
        return playerData;
    }

    public Player getEntity() {
        return playerData.getEntity();
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

    public PlayerEntitlements getEntitlements() {
        return entitlements;
    }

    void registerExtension(IPersonaExtension extension) {
        extensions.put(extension.getClass(), extension);
    }

    public <T extends IPersonaExtension> T getExtension(Class<T> clazz) {
        IPersonaExtension extension = extensions.get(clazz);
        return extension == null ? null : clazz.cast(extension);
    }

    public void activate() {
        sync.attach(playerData.getSyncController());
        MKCore.LOGGER.debug("Persona.activate");
        entitlements.onPersonaActivated();
        talents.onPersonaActivated();
        skills.onPersonaActivated();
        loadout.onPersonaActivated();
    }

    public void deactivate() {
        sync.detach(playerData.getSyncController());
        MKCore.LOGGER.debug("Persona.deactivate");
        skills.onPersonaDeactivated();
        loadout.onPersonaDeactivated();
    }

    public boolean isActive() {
        return playerData.getPersonaManager().getActivePersona() == this;
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function) {
        subscribe(eventType, uuid, function, EventPriorities.CONSUMER);
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function, int priority) {
        playerData.events().subscribe(eventType, () -> new PersonaEventSubscription<>(this, uuid, function, priority));
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

    @Override
    public String toString() {
        return "Persona{" +
                "name='" + name + '\'' +
                ", personaId=" + personaId +
                '}';
    }
}
