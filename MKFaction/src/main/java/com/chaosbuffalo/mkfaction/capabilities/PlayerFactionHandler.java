package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.sync.adapters.SyncMapUpdater;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class PlayerFactionHandler implements IPlayerFaction {

    private final Player player;
    private MKPlayerData playerData;

    public PlayerFactionHandler(Player player) {
        // Do not attempt to access any persona-specific data here because at this time
        // it's impossible to get a copy of MKPlayerData
        this.player = player;
    }

    @Override
    public Map<Holder<MKFaction>, PlayerFactionEntry> getFactionMap() {
        return getPersonaData().getFactionMap();
    }

    @Override
    public Optional<PlayerFactionEntry> getFactionEntry(Holder<MKFaction> factionHolder) {
        return Optional.ofNullable(getPersonaData().getFactionEntry(factionHolder));
    }

    @Override
    public OptionalInt getNpcFactionOverride(UUID spawnId) {
        return getNpcOverrideData().getNpcFactionOverride(spawnId);
    }

    @Override
    public void setNpcFactionOverride(UUID spawnId, int factionScore) {
        getNpcOverrideData().setNpcFactionOverride(spawnId, factionScore);
    }

    @Override
    public void clearNpcFactionOverride(UUID spawnId) {
        getNpcOverrideData().clearNpcFactionOverride(spawnId);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private MKPlayerData getPlayerData() {
        if (playerData == null) {
            playerData = MKCore.getPlayer(player).orElseThrow(IllegalStateException::new);
        }
        return playerData;
    }

    private PersonaFactionData getPersonaData() {
        return getPlayerData().getPersonaExtension(PersonaFactionData.class);
    }

    private PersonaNpcFactionOverrideData getNpcOverrideData() {
        return getPlayerData().getPersonaExtension(PersonaNpcFactionOverrideData.class);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        // This would be where global data that is shared across personas would be persisted.
        // Currently, there is none.
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    }

    public static class PersonaFactionData implements IPersonaExtension {
        static final ResourceLocation NAME = MKFactionMod.id("faction_data");

        private final Map<Holder<MKFaction>, PlayerFactionEntry> factionMap = new HashMap<>();
        private final SyncMapUpdater<Holder<MKFaction>, PlayerFactionEntry> factionUpdater;
        private final Persona persona;

        public PersonaFactionData(Persona persona) {
            this.persona = persona;
            factionUpdater = new SyncMapUpdater<>(
                    factionMap,
                    this::holderToId,
                    this::idToHolder,
                    this::createNewEntry
            );
            persona.getSyncGroup().addPrivate("factions", factionUpdater);
        }

        private <T> String holderToId(Holder<T> factionHolder) {
            return Objects.requireNonNull(factionHolder.getKey()).location().toString();
        }

        @Nullable
        private Holder<MKFaction> idToHolder(String key) {
            var factionKey = ResourceLocation.tryParse(key);
            if (factionKey == null)
                return null;
            return MKFactionRegistry.getFactionHolder(persona.getEntity().registryAccess(), factionKey)
                    .orElse(null);
        }

        private PlayerFactionEntry createNewEntry(Holder<MKFaction> faction) {
            return new PlayerFactionEntry(faction, this::onDirtyEntry);
        }

        public Map<Holder<MKFaction>, PlayerFactionEntry> getFactionMap() {
            return factionMap;
        }

        private PlayerFactionEntry getFactionEntry(Holder<MKFaction> factionName) {
            return getFactionMap().computeIfAbsent(factionName, name -> {
                PlayerFactionEntry newEntry = createNewEntry(name);
                newEntry.reset();
                return newEntry;
            });
        }

        private void onDirtyEntry(PlayerFactionEntry entry) {
            factionUpdater.markDirty(entry.getFaction());
        }

        @Override
        public ResourceLocation getName() {
            return NAME;
        }

        @Override
        public CompoundTag serialize(HolderLookup.Provider provider) {
//            MKFactionMod.LOGGER.info("PersonaFactionData.serialize");
            CompoundTag tag = new CompoundTag();
            tag.put("factions", factionUpdater.serializeStorage(provider));
            return tag;
        }

        @Override
        public void deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
//            MKFactionMod.LOGGER.info("PersonaFactionData.deserialize {}", nbt);
            factionUpdater.deserializeStorage(provider, nbt.getCompound("factions"));
        }
    }

    public static class PersonaNpcFactionOverrideData implements IPersonaExtension {
        static final ResourceLocation NAME = MKFactionMod.id("npc_faction_override_data");

        private final Map<UUID, ScoreOverrideEntry> overrideMap = new HashMap<>();
        private final SyncMapUpdater<UUID, ScoreOverrideEntry> overrideUpdater;
        private final Persona persona;

        public PersonaNpcFactionOverrideData(Persona persona) {
            this.persona = persona;
            overrideUpdater = new SyncMapUpdater<>(
                    overrideMap,
                    UUID::toString,
                    UUID::fromString,
                    this::createNewEntry
            );
            persona.getSyncGroup().addPrivate("npc_faction_overrides", overrideUpdater);
        }

        private ScoreOverrideEntry createNewEntry(UUID spawnId) {
            return new ScoreOverrideEntry(spawnId, this::onDirtyEntry);
        }

        public OptionalInt getNpcFactionOverride(UUID spawnId) {
            ScoreOverrideEntry entry = overrideMap.get(spawnId);
            return entry == null ? OptionalInt.empty() : OptionalInt.of(entry.getFactionScore());
        }

        public void setNpcFactionOverride(UUID spawnId, int factionScore) {
            overrideMap.computeIfAbsent(spawnId, this::createNewEntry).setFactionScore(factionScore);
        }

        public void clearNpcFactionOverride(UUID spawnId) {
            if (overrideMap.remove(spawnId) != null) {
                overrideUpdater.markDirty(spawnId);
            }
        }

        private void onDirtyEntry(ScoreOverrideEntry entry) {
            overrideUpdater.markDirty(entry.getSpawnId());
        }

        @Override
        public ResourceLocation getName() {
            return NAME;
        }

        @Override
        public CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("overrides", overrideUpdater.serializeStorage(provider));
            return tag;
        }

        @Override
        public void deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
            overrideUpdater.deserializeStorage(provider, nbt.getCompound("overrides"));
        }
    }

    public static class ScoreOverrideEntry implements com.chaosbuffalo.mkcore.sync.IMKSerializable<CompoundTag> {
        private final UUID spawnId;
        private final java.util.function.Consumer<ScoreOverrideEntry> dirtyNotifier;
        private int factionScore;

        public ScoreOverrideEntry(UUID spawnId, java.util.function.Consumer<ScoreOverrideEntry> dirtyNotifier) {
            this.spawnId = spawnId;
            this.dirtyNotifier = dirtyNotifier;
        }

        public UUID getSpawnId() {
            return spawnId;
        }

        public int getFactionScore() {
            return factionScore;
        }

        public void setFactionScore(int factionScore) {
            this.factionScore = factionScore;
            markDirty();
        }

        @Override
        public CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("factionScore", factionScore);
            return tag;
        }

        @Override
        public boolean deserialize(HolderLookup.Provider provider, CompoundTag tag) {
            if (tag.contains("factionScore")) {
                factionScore = tag.getInt("factionScore");
            }
            return true;
        }

        private void markDirty() {
            if (dirtyNotifier != null) {
                dirtyNotifier.accept(this);
            }
        }
    }

    private static PersonaFactionData createNewPersonaData(Persona persona) {
        return new PersonaFactionData(persona);
    }

    private static PersonaNpcFactionOverrideData createNewNpcOverrideData(Persona persona) {
        return new PersonaNpcFactionOverrideData(persona);
    }

    public static void registerPersonaExtension() {
        IPersonaExtensionProvider factory = PlayerFactionHandler::createNewPersonaData;
        IPersonaExtensionProvider overrideFactory = PlayerFactionHandler::createNewNpcOverrideData;
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("mkcore", "register_persona_extension", () -> {
            MKFactionMod.LOGGER.debug("Faction register persona by IMC");
            return factory;
        });
        InterModComms.sendTo("mkcore", "register_persona_extension", () -> {
            MKFactionMod.LOGGER.debug("Faction register npc override persona by IMC");
            return overrideFactory;
        });
    }
}
