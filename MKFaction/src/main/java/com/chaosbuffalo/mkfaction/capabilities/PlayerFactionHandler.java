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
