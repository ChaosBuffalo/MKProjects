package com.chaosbuffalo.mkcore.core.persona;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.PlayerKnowledge;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public class Persona implements IMKSerializable<CompoundTag> {
    private final String name;
    private final PlayerKnowledge knowledge;
    private final MKPlayerData data;
    private final Map<Class<? extends IPersonaExtension>, IPersonaExtension> extensions = new IdentityHashMap<>();
    private UUID personaId;

    public Persona(MKPlayerData playerData, String name) {
        this.name = name;
        knowledge = new PlayerKnowledge(playerData);
        data = playerData;
        personaId = UUID.randomUUID();
    }

    public String getName() {
        return name;
    }

    public PlayerKnowledge getKnowledge() {
        return knowledge;
    }

    public MKPlayerData getPlayerData() {
        return data;
    }

    public UUID getPersonaId() {
        return personaId;
    }

    void registerExtension(IPersonaExtension extension) {
        extensions.put(extension.getClass(), extension);
    }

    public <T extends IPersonaExtension> T getExtension(Class<T> clazz) {
//            MKCore.LOGGER.info("getExtension {} {}", extensions.size(), extensions.values().stream().map(Objects::toString).collect(Collectors.joining(",")));
        IPersonaExtension extension = extensions.get(clazz);
        return extension == null ? null : clazz.cast(extension);
    }

    public void activate() {
        knowledge.getSyncComponent().attach(data.getUpdateEngine());
        knowledge.onPersonaActivated();
        getPlayerData().onPersonaActivated();
        extensions.values().forEach(IPersonaExtension::onPersonaActivated);
    }

    public void deactivate() {
        knowledge.onPersonaDeactivated();
        knowledge.getSyncComponent().detach(data.getUpdateEngine());
        getPlayerData().onPersonaDeactivated();
        extensions.values().forEach(IPersonaExtension::onPersonaDeactivated);
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
        tag.put("knowledge", knowledge.serialize());
        tag.put("extensions", serializeExtensions());
        tag.putUUID("personaId", personaId);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        knowledge.deserialize(tag.getCompound("knowledge"));
        deserializeExtensions(tag.getCompound("extensions"));
        if (tag.contains("personaId")) {
            personaId = tag.getUUID("personaId");
        }
        return true;
    }
}
