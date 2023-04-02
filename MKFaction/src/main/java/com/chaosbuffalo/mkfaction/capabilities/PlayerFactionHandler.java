package com.chaosbuffalo.mkfaction.capabilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
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
    public Map<ResourceLocation, PlayerFactionEntry> getFactionMap() {
        return getPersonaData().getFactionMap();
    }

    public Optional<PlayerFactionEntry> getFactionEntry(ResourceLocation factionName) {
        return Optional.ofNullable(getPersonaData().getFactionEntry(factionName));
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
    public CompoundTag serializeNBT() {
        // This would be where global data that is shared across personas would be persisted.
        // Currently, there is none.
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }

    public static class PersonaFactionData implements IPersonaExtension {
        final static ResourceLocation NAME = new ResourceLocation(MKFactionMod.MODID, "faction_data");

        private final Map<ResourceLocation, PlayerFactionEntry> factionMap = new HashMap<>();
        private final SyncMapUpdater<ResourceLocation, PlayerFactionEntry> factionUpdater;
        private final Persona persona;

        public PersonaFactionData(Persona persona) {
            this.persona = persona;
            factionUpdater = new SyncMapUpdater<>("factions",
                    () -> factionMap,
                    ResourceLocation::toString,
                    ResourceLocation::tryParse,
                    this::createNewEntry
            );
            persona.getKnowledge().addSyncPrivate(factionUpdater);
        }

        private PlayerFactionEntry createNewEntry(ResourceLocation factionId) {
            if (factionId.equals(MKFaction.INVALID_FACTION)) {
                return null;
            }
            MKFaction faction = MKFactionRegistry.getFaction(factionId);
            if (faction == null) {
                return null;
            }
            return new PlayerFactionEntry(faction, this::onDirtyEntry);
        }

        public Map<ResourceLocation, PlayerFactionEntry> getFactionMap() {
            return factionMap;
        }

        @Nullable
        private PlayerFactionEntry getFactionEntry(ResourceLocation factionName) {
            if (factionName.equals(MKFaction.INVALID_FACTION))
                return null;

            return getFactionMap().computeIfAbsent(factionName, name -> {
                PlayerFactionEntry newEntry = createNewEntry(name);
                if (newEntry == null)
                    return null;
                newEntry.reset();
                return newEntry;
            });
        }

        private void onDirtyEntry(PlayerFactionEntry entry) {
            factionUpdater.markDirty(entry.getFactionName());
        }

        @Override
        public ResourceLocation getName() {
            return NAME;
        }

        @Override
        public void onPersonaActivated() {
//            MKFactionMod.LOGGER.info("PersonaFactionData.onPersonaActivated");
        }

        @Override
        public void onPersonaDeactivated() {
//            MKFactionMod.LOGGER.info("PersonaFactionData.onPersonaDeactivated");
        }

        @Override
        public CompoundTag serialize() {
//            MKFactionMod.LOGGER.info("PersonaFactionData.serialize");
            CompoundTag tag = new CompoundTag();
            tag.put("factions", factionUpdater.serializeStorage());
            return tag;
        }

        @Override
        public void deserialize(CompoundTag nbt) {
//            MKFactionMod.LOGGER.info("PersonaFactionData.deserialize {}", nbt);
            factionUpdater.deserializeStorage(nbt.getCompound("factions"));
        }
    }

    private static PersonaFactionData createNewPersonaData(Persona persona) {
        MKFactionMod.LOGGER.debug("MKFaction creating new persona data for {}", persona.getPlayerData().getEntity());
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

    public static class Provider extends FactionCapabilities.Provider<Player, IPlayerFaction> {

        public Provider(Player entity) {
            super(entity);
        }

        @Override
        IPlayerFaction makeData(Player attached) {
            return new PlayerFactionHandler(attached);
        }

        @Override
        Capability<IPlayerFaction> getCapability() {
            return FactionCapabilities.PLAYER_FACTION_CAPABILITY;
        }
    }
}
