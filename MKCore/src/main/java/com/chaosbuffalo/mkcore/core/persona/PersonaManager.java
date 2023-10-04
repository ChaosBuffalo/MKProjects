package com.chaosbuffalo.mkcore.core.persona;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.events.PersonaEvent;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

public class PersonaManager implements IMKSerializable<CompoundTag> {
    public static final String DEFAULT_PERSONA_NAME = "default";
    private static final List<IPersonaExtensionProvider> extensionProviders = new ArrayList<>(4);
    private final MKPlayerData playerData;
    private final Map<String, Persona> personas = new HashMap<>();
    protected Persona activePersona;

    public PersonaManager(MKPlayerData playerData) {
        this.playerData = playerData;
    }

    public Persona getActivePersona() {
        return activePersona;
    }

    protected void setActivePersona(Persona persona) {
        activePersona = Objects.requireNonNull(persona, "cannot activate a null persona");
    }

    public void onJoinWorld() {
        ensurePersonaLoaded();
        getActivePersona().onJoinWorld();
    }

    private void ensurePersonaLoaded() {
        if (activePersona == null) {
            // When creating a new character it comes to serialize first, so create the default persona here if none is active
            loadPersona(DEFAULT_PERSONA_NAME);
        }
        Objects.requireNonNull(activePersona, "Persona was required but not loaded");
    }

    private void loadPersona(String name) {
        // Look for the specified persona, or create a new persona if it does not exist
        Persona persona = personas.computeIfAbsent(name, this::createNewPersona);

        dispatchActivation(persona);
    }

    protected Persona createNewPersona(String name) {
        Persona persona = new Persona(playerData, name);
        extensionProviders.forEach(provider -> persona.registerExtension(provider.create(persona)));
        return persona;
    }

    public Collection<String> getPersonaNames() {
        return Collections.unmodifiableCollection(personas.keySet());
    }

    public Persona getPersona(String name) {
        return personas.get(name);
    }

    public boolean isPersonaActive(String name) {
        return getActivePersona().getName().equalsIgnoreCase(name);
    }

    public boolean hasPersona(String name) {
        return personas.containsKey(name);
    }

    public boolean createPersona(String name) {
        if (hasPersona(name)) {
            MKCore.LOGGER.error("Cannot create a persona named {} for {}! Persona with that name already exists.", name, playerData.getEntity());
            return false;
        }

        personas.put(name, createNewPersona(name));
        return true;
    }

    public boolean deletePersona(String name) {
        if (!hasPersona(name)) {
            MKCore.LOGGER.error("deletePersona({}) - persona does not exist!", name);
            return false;
        }

        if (isPersonaActive(name)) {
            MKCore.LOGGER.error("deletePersona({}) - cannot delete active persona!", name);
            return false;
        }

        personas.remove(name);
        return true;
    }

    public boolean activatePersona(String name) {
        Persona newPersona = getPersona(name);
        if (newPersona == null) {
            MKCore.LOGGER.error("Failed to activate unknown persona {}", name);
            return false;
        }

        Persona current = getActivePersona();
        if (current != newPersona) {
            dispatchDeactivation(current);

            dispatchActivation(newPersona);
        }

        return true;
    }

    private void dispatchActivation(Persona persona) {
        setActivePersona(persona);
        persona.activate();
        MinecraftForge.EVENT_BUS.post(new PersonaEvent.PersonaActivated(persona));
    }

    private void dispatchDeactivation(Persona current) {
        current.deactivate();
        MinecraftForge.EVENT_BUS.post(new PersonaEvent.PersonaDeactivated(current));
    }

    @Override
    public CompoundTag serialize() {
        ensurePersonaLoaded();

        CompoundTag tag = new CompoundTag();
        CompoundTag personaRoot = new CompoundTag();
        personas.forEach((name, persona) -> personaRoot.put(name, persona.serialize()));
        tag.put("personas", personaRoot);
        tag.putString("activePersona", getActivePersona().getName());
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        CompoundTag personaRoot = tag.getCompound("personas");
        for (String name : personaRoot.getAllKeys()) {
            CompoundTag personaTag = personaRoot.getCompound(name);
            Persona persona = createNewPersona(name);
            if (!persona.deserialize(personaTag)) {
                MKCore.LOGGER.error("Failed to deserialize persona {} for {}", name, playerData.getEntity());
                continue;
            }

            personas.put(name, persona);
        }

        String activePersonaName = tag.contains("activePersona") ?
                tag.getString("activePersona") :
                DEFAULT_PERSONA_NAME;

        loadPersona(activePersonaName);
        return true;
    }

    // The client only has a single persona that will be overwritten when the server changes
    public static class ClientPersonaManager extends PersonaManager {

        public ClientPersonaManager(MKPlayerData playerData) {
            super(playerData);

            Persona single = createNewPersona("client_persona");

            setActivePersona(single);
            getActivePersona().getSyncComponent().attach(playerData.getUpdateEngine());
        }
    }

    public static PersonaManager getPersonaManager(MKPlayerData playerData) {
        if (playerData.getEntity() instanceof ServerPlayer) {
            return new PersonaManager(playerData);
        } else {
            return new ClientPersonaManager(playerData);
        }
    }

    public static void registerExtension(IPersonaExtensionProvider provider) {
        Objects.requireNonNull(provider);
        extensionProviders.add(provider);
    }
}
