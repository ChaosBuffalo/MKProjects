package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.sync.adapters.MapStorageCodec;
import com.chaosbuffalo.mkcore.sync.adapters.SyncMapUpdater;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.InterModComms;

import java.util.HashMap;
import java.util.Map;
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
        return getPersonaData().getNpcFactionOverride(spawnId);
    }

    @Override
    public void setNpcFactionOverride(UUID spawnId, int factionScore) {
        getPersonaData().setNpcFactionOverride(spawnId, factionScore);
    }

    @Override
    public void clearNpcFactionOverride(UUID spawnId) {
        getPersonaData().clearNpcFactionOverride(spawnId);
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
        private final Map<UUID, ScoreOverrideEntry> overrideMap = new HashMap<>();
        private final SyncMapUpdater<Holder<MKFaction>, PlayerFactionEntry> factionUpdater;
        private final SyncMapUpdater<UUID, ScoreOverrideEntry> overrideUpdater;
        private final MapStorageCodec<Holder<MKFaction>, PlayerFactionEntry> factionStorage;
        private final MapStorageCodec<UUID, ScoreOverrideEntry> overrideStorage;

        public PersonaFactionData(Persona persona) {
            factionUpdater = new SyncMapUpdater<>(
                    factionMap,
                    SyncMapUpdater.KeyCodec.registryHolders(MKFactionRegistry.FACTION_REGISTRY_KEY),
                    this::createNewEntry
            );
            overrideUpdater = new SyncMapUpdater<>(
                    overrideMap,
                    UUID::toString,
                    UUID::fromString,
                    this::createNewOverrideEntry
            );
            factionStorage = new MapStorageCodec<>(
                    factionMap,
                    holder -> holder.getKey().location().toString(),
                    PersonaFactionData::decodeFactionHolder,
                    this::createNewEntry
            );
            overrideStorage = new MapStorageCodec<>(
                    overrideMap,
                    UUID::toString,
                    UUID::fromString,
                    this::createNewOverrideEntry
            );
            persona.getSyncGroup().addPrivate("factions", factionUpdater);
            persona.getSyncGroup().addPrivate("npc_faction_overrides", overrideUpdater);
        }

        private PlayerFactionEntry createNewEntry(Holder<MKFaction> faction) {
            return new PlayerFactionEntry(faction, this::onDirtyEntry);
        }

        private static Holder<MKFaction> decodeFactionHolder(HolderLookup.Provider provider, String key) {
            ResourceLocation factionId = ResourceLocation.tryParse(key);
            if (factionId == null) {
                return null;
            }

            ResourceKey<MKFaction> factionKey = ResourceKey.create(MKFactionRegistry.FACTION_REGISTRY_KEY, factionId);
            return provider.lookupOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY)
                    .get(factionKey)
                    .orElse(null);
        }

        private ScoreOverrideEntry createNewOverrideEntry(UUID spawnId) {
            return new ScoreOverrideEntry(spawnId, this::onDirtyOverrideEntry);
        }

        public Map<Holder<MKFaction>, PlayerFactionEntry> getFactionMap() {
            return factionMap;
        }

        public OptionalInt getNpcFactionOverride(UUID spawnId) {
            ScoreOverrideEntry entry = overrideMap.get(spawnId);
            return entry == null ? OptionalInt.empty() : OptionalInt.of(entry.getFactionScore());
        }

        public void setNpcFactionOverride(UUID spawnId, int factionScore) {
            overrideMap.computeIfAbsent(spawnId, this::createNewOverrideEntry).setFactionScore(factionScore);
        }

        public void clearNpcFactionOverride(UUID spawnId) {
            if (overrideMap.remove(spawnId) != null) {
                overrideUpdater.markDirty(spawnId);
            }
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

        private void onDirtyOverrideEntry(ScoreOverrideEntry entry) {
            overrideUpdater.markDirty(entry.getSpawnId());
        }

        @Override
        public ResourceLocation getName() {
            return NAME;
        }

        @Override
        public CompoundTag serialize(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("factions", factionStorage.serialize(provider));
            tag.put("overrides", overrideStorage.serialize(provider));
            return tag;
        }

        @Override
        public void deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
            factionStorage.deserialize(provider, nbt.getCompound("factions"));
            overrideStorage.deserialize(provider, nbt.getCompound("overrides"));
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

    public static void registerPersonaExtension() {
        IPersonaExtensionProvider factory = PlayerFactionHandler::createNewPersonaData;
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("mkcore", "register_persona_extension", () -> {
            MKFactionMod.LOGGER.debug("Faction register persona by IMC");
            return factory;
        });
    }
}
