package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.InterModComms;

import java.util.*;

public class PlayerQuestingDataHandler implements IPlayerQuestingData {

    public enum QuestStatus {
        NOT_ON,
        IN_PROGRESS,
        COMPLETED
    }

    private final Player player;
    private MKPlayerData playerData;

    public PlayerQuestingDataHandler(Player player) {
        // Do not attempt to access any persona-specific data here because at this time
        // it's impossible to get a copy of MKPlayerData
        this.player = player;
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

    @Override
    public void advanceQuestChain(IWorldNpcData worldHandler, QuestChainInstance questChainInstance, Quest currentQuest){
        getPersonaData().advanceQuestChain(worldHandler, questChainInstance, currentQuest, this);
    }

    @Override
    public void questProgression(IWorldNpcData worldHandler, QuestChainInstance questChainInstance) {
        getPersonaData().questProgression(worldHandler, questChainInstance);
    }


    public Collection<PlayerQuestChainInstance> getQuestChains(){
        return getPersonaData().questChains.values();
    }

    @Override
    public Optional<PlayerQuestChainInstance> getQuestChain(UUID questId) {
        return getPersonaData().getChain(questId);
    }

    @Override
    public void startQuest(IWorldNpcData worldHandler, UUID questId) {
        QuestChainInstance chain = worldHandler.getQuest(questId);
        if (chain != null){
            MKNpc.LOGGER.info("Player {} started quest {}", getPlayer(), chain);
            getPersonaData().startQuest(playerData.getEntity(), worldHandler, chain);
        } else {
            MKNpc.LOGGER.warn("Tried to start quest with id {} but it doesn't exist in the world data", questId);
        }

    }

    @Override
    public QuestStatus getQuestStatus(UUID questId) {
        return getPersonaData().getQuestStatus(questId);
    }

    @Override
    public Optional<List<String>> getCurrentQuestSteps(UUID questId) {
        return getPersonaData().getCurrentQuestSteps(questId);
    }

    private PersonaQuestData getPersonaData(){
        return getPlayerData().getPersonaExtension(PersonaQuestData.class);
    }

    @Override
    public CompoundTag serializeNBT() {
        // This would be where global data that is shared across personas would be persisted.
        // Currently there is none.
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }

    public static class PersonaQuestData implements IPersonaExtension {
        final static ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "player_quest_data");
        private final Map<UUID, PlayerQuestChainInstance> questChains = new HashMap<>();
        private final SyncMapUpdater<UUID, PlayerQuestChainInstance> questChainUpdater;
        private final Set<UUID> completedQuests = new HashSet<>();

        private final Persona persona;

        public PersonaQuestData(Persona persona) {
            this.persona = persona;
            questChainUpdater = new SyncMapUpdater<>(
                    "questChains",
                    () -> questChains,
                    UUID::toString,
                    UUID::fromString,
                    this::createNewEntry
            );
            persona.getKnowledge().addSyncPrivate(questChainUpdater);
        }

        public Optional<PlayerQuestChainInstance> getChain(UUID questId){
            PlayerQuestChainInstance questChain = questChains.get(questId);
            if (questChain == null){
                return Optional.empty();
            }
            return Optional.of(questChain);
        }

        public Optional<List<String>> getCurrentQuestSteps(UUID questId){
            PlayerQuestChainInstance questChain = questChains.get(questId);
            if (questChain == null){
                return Optional.empty();
            }
            return Optional.of(questChain.getCurrentQuests());
        }

        public QuestStatus getQuestStatus(UUID questId){
            PlayerQuestChainInstance questChain = questChains.get(questId);
            if (questChain == null){
                return QuestStatus.NOT_ON;
            }
            if (questChain.isQuestComplete()) {
                return QuestStatus.COMPLETED;
            }
            return QuestStatus.IN_PROGRESS;
        }

        public void startQuest(Player player, IWorldNpcData worldHandler, QuestChainInstance questChain){
            if (!questChain.getDefinition().isRepeatable() && completedQuests.contains(questChain.getQuestId())){
                MKNpc.LOGGER.info("Can't start quest with definition {} for {} already completed {}",
                        questChain.getDefinition().getName(), persona.getPlayerData().getEntity(), questChain.getQuestId());
                player.sendMessage(new TranslatableComponent("mknpc.quest.cant_start_quest", questChain.getDefinition().getQuestName()).withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);
                return;
            }
            if (!questChain.getDefinition().getRequirements().stream().allMatch(requirement ->
                    requirement.meetsRequirements(player))){
                player.sendMessage(new TranslatableComponent("mknpc.quest.cant_start_quest", questChain.getDefinition().getQuestName()).withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);
                return;
            }
            PlayerQuestChainInstance quest = createNewEntry(questChain.getQuestId());
            quest.setupQuestChain(questChain);
            quest.setCurrentQuests(questChain.getStartingQuestNames());
            for (Quest q : questChain.getDefinition().getFirstQuests()){
                PlayerQuestData questData = q.generatePlayerQuestData(
                        worldHandler, questChain.getQuestChainData().getQuestData(q.getQuestName()));
                quest.addQuestData(questData);
                questChains.put(questChain.getQuestId(), quest);
                questChainUpdater.markDirty(questChain.getQuestId());
            }

            player.sendMessage(new TranslatableComponent("mknpc.quest.start_quest", questChain.getDefinition().getQuestName()).withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
        }

        public void questProgression(IWorldNpcData worldHandler, QuestChainInstance questChainInstance){
            questChainUpdater.markDirty(questChainInstance.getQuestId());
        }

        private void completeChain(PlayerQuestChainInstance chain, Player player){
            chain.setQuestComplete(true);
            completedQuests.add(chain.getQuestId());
            player.sendMessage(new TranslatableComponent("mknpc.quest.complete_chain", chain.getQuestName()).withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
        }

        public void advanceQuestChain(IWorldNpcData worldHandler, QuestChainInstance questChainInstance, Quest currentQuest, IPlayerQuestingData questingData){
            PlayerQuestChainInstance chain = questChains.get(questChainInstance.getQuestId());
            if (chain != null && currentQuest != null) {
                currentQuest.grantRewards(questingData);
                SoundUtils.serverPlaySoundAtEntity(questingData.getPlayer(), CoreSounds.quest_complete_sound.get(), SoundSource.PLAYERS);
                switch (questChainInstance.getDefinition().getMode()) {
                    case LINEAR:
                        String currentQuestName = currentQuest.getQuestName();
                        Optional<Quest> nextQuest = questChainInstance.getNextQuest(currentQuestName);
                        if (nextQuest.isPresent()) {
                            Quest quest = nextQuest.get();
                            chain.addQuestData(quest.generatePlayerQuestData(worldHandler,
                                    questChainInstance.getQuestChainData().getQuestData(quest.getQuestName())));
                            chain.setCurrentQuests(Collections.singletonList(quest.getQuestName()));
                        } else {
                            completeChain(chain, questingData.getPlayer());
                        }
                        break;
                    case UNSORTED:
                        boolean allComplete = chain.getCurrentQuests().stream().allMatch(questName -> {
                            Quest quest = questChainInstance.getDefinition().getQuest(questName);
                            if (quest != null){
                                PlayerQuestData questData = chain.getQuestData(questName);
                                if (questData == null){
                                    return false;
                                }
                                return quest.isComplete(questData);
                            } else {
                                return false;
                            }
                        });
                        if (allComplete){
                            completeChain(chain, questingData.getPlayer());
                        }
                        break;
                }
                questChainUpdater.markDirty(chain.getQuestId());
            }
        }

        private void onDirtyEntry(PlayerQuestChainInstance entry) {
            questChainUpdater.markDirty(entry.getQuestId());
        }

        private PlayerQuestChainInstance createNewEntry(UUID id) {
            PlayerQuestChainInstance entry = new PlayerQuestChainInstance(id);
            entry.setDirtyNotifier(this::onDirtyEntry);
            return entry;
        }

        @Override
        public ResourceLocation getName() {
            return NAME;
        }

        @Override
        public void onPersonaActivated() {

        }

        @Override
        public void onPersonaDeactivated() {

        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            ListTag chainsNbt = new ListTag();
            for (PlayerQuestChainInstance chain : questChains.values()){
                chainsNbt.add(chain.serialize());
            }
            tag.put("chains", chainsNbt);
            return tag;
        }

        @Override
        public void deserialize(CompoundTag nbt) {
            ListTag chainsNbt = nbt.getList("chains", Tag.TAG_COMPOUND);
            for (Tag chainNbt : chainsNbt){
                PlayerQuestChainInstance newChain = new PlayerQuestChainInstance((CompoundTag) chainNbt);
                newChain.setDirtyNotifier(this::onDirtyEntry);
                questChains.put(newChain.getQuestId(), newChain);
                if (newChain.isQuestComplete()){
                    completedQuests.add(newChain.getQuestId());
                }
            }
        }
    }

    private static PersonaQuestData createNewPersonaData(Persona persona){
        return new PersonaQuestData(persona);
    }


    public static void registerPersonaExtension() {
        IPersonaExtensionProvider factory = PlayerQuestingDataHandler::createNewPersonaData;
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("mkcore", "register_persona_extension", () -> {
            MKNpc.LOGGER.info("MK NPC register player quest persona by IMC");
            return factory;
        });
    }
}
