package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mkchat.event.PlayerNpcDialogueTreeGatherEvent;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.healing.MKAbilityHealEvent;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.effects.HealingThreatEffect;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.chaosbuffalo.mknpc.quest.objectives.IContainerObjectiveHandler;
import com.chaosbuffalo.mknpc.quest.objectives.IKillObjectiveHandler;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjective;
import com.chaosbuffalo.mknpc.utils.NpcConstants;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Optional;


@SuppressWarnings("unused")
@EventBusSubscriber(modid = MKNpc.MODID)
public class EntityHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        MKNpc.getNpcData(event.getEntity()).ifPresent((cap) -> {
            if (cap.needsDefinitionApplied()) {
                cap.applyDefinition();
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDamage(LivingDamageEvent.Pre event) {
        if (event.getSource() instanceof MKDamageSource) {
            if (event.getEntity() instanceof Player) {
                if (!(event.getSource().getEntity() instanceof Player)) {
                    event.setNewDamage((float) (event.getNewDamage() * MKNpc.getDifficultyScale(event.getEntity())));
                }
                //add threat to pets here

            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            MinecraftServer server = event.getLevel().getServer();
            Level level = (Level) event.getLevel();
            if (server == null || level == null) {
                return;
            }
            event.getChunk().getBlockEntitiesPos().forEach(pos -> {
                BlockEntity entity = event.getChunk().getBlockEntity(pos);
                if (entity != null) {
                    ContentDB.getPrimaryData().queueChestForProcessing(GlobalPos.of(level.dimension(), pos));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = event.getHitVec().getBlockPos();
        if (level.getBlockState(pos).getBlock() instanceof ChestBlock) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te == null) {
                return;
            }
            IChestNpcData.get(te).ifPresent(chestCap -> {
                Player player = event.getEntity();
                processLootChestEvents(player, chestCap);
                if (!player.isShiftKeyDown() && chestCap.hasQuestInventoryForPlayer(player)) {
                    player.openMenu(chestCap);
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        int bonusXp = MKNpc.getNpcData(event.getEntity()).map(IEntityNpcData::getBonusXp).orElse(0);
        event.setDroppedExperience(event.getDroppedExperience() + bonusXp);
    }

    private static void processLootChestEvents(Player player, IChestNpcData chestCap) {
        MKNpc.getPlayerQuestData(player).ifPresent(x -> x.getQuestChains().forEach(
                pQuestChain -> {
                    QuestChainInstance questChain = ContentDB.getQuestInstance(pQuestChain.getQuestId());
                    if (questChain == null) {
                        return;
                    }
                    for (String questName : pQuestChain.getCurrentQuests()) {
                        Quest currentQuest = questChain.getDefinition().getQuest(questName);
                        if (currentQuest != null) {
                            for (QuestObjective<?> obj : currentQuest.getObjectives()) {
                                if (obj instanceof IContainerObjectiveHandler containerObj) {
                                    PlayerQuestData pQuest = pQuestChain.getQuestData(currentQuest.getQuestName());
                                    PlayerQuestObjectiveData pObj = pQuest.getObjective(obj.getObjectiveName());
                                    QuestData qData = questChain.getQuestData(currentQuest);
                                    if (containerObj.onLootChest(player, pObj, qData, chestCap)) {
                                        questChain.signalQuestProgress(ContentDB.getQuestDB(), x, currentQuest, pQuestChain, false);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }));
    }

    @SubscribeEvent
    public static void onSetupDialogue(PlayerNpcDialogueTreeGatherEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (MKNpc.DEV_LOGGING) {
            MKNpc.LOGGER.debug("Setting up dialogue between {} and {}", event.getSpeaker(), event.getEntity());
        }
        MKNpc.getPlayerQuestData(event.getEntity()).ifPresent(x -> x.getQuestChains().forEach(
                pQuestChain -> {
                    QuestChainInstance questChainInstance = ContentDB.getQuestInstance(pQuestChain.getQuestId());
                    if (questChainInstance != null) {
                        if (MKNpc.DEV_LOGGING) {
                            MKNpc.LOGGER.debug("Adding quest chain dialogue for {}", questChainInstance.getDefinition().getName());
                        }
                        questChainInstance.getTreeForEntity(event.getSpeaker()).ifPresent(event::addTree);
                    }
                }));
    }

    private static void handleNpcInStructureDeath(IEntityNpcData npcData) {
        if (npcData.getStructureId().isPresent()) {
            ContentDB.getPrimaryData().getStructureManager().onNpcDeath(npcData);
        }
    }

    private static void handleKillEntityForPlayer(Player player, LivingDeathEvent event, IEntityNpcData npcData) {
        NpcDefinition def = npcData.getDefinition();
        MKNpc.getPlayerQuestData(player).ifPresent(pData -> pData.getQuestChains().forEach(
                pQuestChain -> {
                    QuestChainInstance questChain = ContentDB.getQuestInstance(pQuestChain.getQuestId());
                    if (questChain == null) {
                        return;
                    }
                    for (String questName : pQuestChain.getCurrentQuests()) {
                        Quest currentQuest = questChain.getDefinition().getQuest(questName);
                        if (currentQuest != null) {
                            for (QuestObjective<?> obj : currentQuest.getObjectives()) {
                                if (obj instanceof IKillObjectiveHandler killObj) {
                                    PlayerQuestData pQuest = pQuestChain.getQuestData(currentQuest.getQuestName());
                                    PlayerQuestObjectiveData pObj = pQuest.getObjective(obj.getObjectiveName());
                                    QuestData qData = questChain.getQuestData(currentQuest);
                                    if (killObj.onPlayerKillNpcDefEntity(player, pObj, def, event, qData, pQuestChain)) {
                                        questChain.signalQuestProgress(ContentDB.getQuestDB(), pData, currentQuest, pQuestChain, false);
                                    }
                                }
                            }
                        }
                    }
                }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        Optional<IEntityNpcData> npcCap = MKNpc.getNpcData(event.getEntity());
        npcCap.ifPresent(npcData -> {
            npcData.getDeathReceiver().ifPresent(receiver -> receiver.onEntityDeath(npcData, event));
            handleNpcInStructureDeath(npcData);
        });
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.getServer();
            if (server == null) {
                return;
            }
            npcCap.ifPresent(npcData -> {
                handleKillEntityForPlayer(player, event, npcData);
                Team team = player.getTeam();
                if (team != null) {
                    for (String s : team.getPlayers()) {
                        ServerPlayer member = server.getPlayerList().getPlayerByName(s);
                        if (member != null && !member.equals(player)) {
                            handleKillEntityForPlayer(member, event, npcData);
                        }
                    }
                }
            });
        }
    }


    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity) {
            MKNpc.getNpcData(event.getEntity()).ifPresent(IEntityNpcData::tick);
        }
    }

    @SubscribeEvent
    public static void onLootDrop(LivingDropsEvent event) {
        if (event.isRecentlyHit()) {
            // FIXME: Since the enchantment rework event.getLootingLevel() doesn't exist
            MKNpc.getNpcData(event.getEntity()).ifPresent(x -> x.handleExtraLoot(
                    0, event.getDrops(), event.getSource()));
        }
    }

    @SubscribeEvent
    public static void onHealEvent(MKAbilityHealEvent event) {
        LivingEntity healed = event.getEntity();
        LivingEntity source = event.getHealSource().getSourceEntity();
        if (source != null && !(source instanceof Player player && player.isCreative())) {
            EntityEffectBuilder.PointEffectBuilder.createPointEffectOnEntity(source, healed, Vec3.ZERO)
                    .radius(10.0f)
                    .effect(HealingThreatEffect.from(source, healed, event.getAmount() * NpcConstants.HEALING_THREAT_MULTIPLIER),
                            TargetingContexts.ENEMY)
                    .spawn();
        }
    }
}
