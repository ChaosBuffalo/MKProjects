package com.chaosbuffalo.mknpc.event;

import com.chaosbuffalo.mkchat.event.PlayerNpcDialogueTreeGatherEvent;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.healing.MKAbilityHealEvent;
import com.chaosbuffalo.mkcore.effects.EntityEffectBuilder;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
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
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        MKNpc.getNpcData(event.getEntity()).ifPresent((cap) -> {
            if (cap.wasMKSpawned()) {
                event.setCanceled(true);
            } else {
                if (cap.needsDefinitionApplied()) {
                    cap.applyDefinition();
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDamage(LivingDamageEvent event) {
        if (event.getSource() instanceof MKDamageSource) {
            if (event.getEntity() instanceof Player) {
                if (!(event.getSource().getEntity() instanceof Player)) {
                    event.setAmount((float) (event.getAmount() * MKNpc.getDifficultyScale(event.getEntity())));
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
                    server.overworld().getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY).ifPresent(x ->
                            x.queueChestForProcessing(GlobalPos.of(level.dimension(), pos)));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getEntity().level.isClientSide) {
            return;
        }
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) {
            return;
        }
        Level world = event.getLevel();
        BlockPos pos = event.getHitVec().getBlockPos();
        if (world.getBlockState(pos).getBlock() instanceof ChestBlock) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te == null) {
                return;
            }
            Level overWorld = server.getLevel(Level.OVERWORLD);
            if (overWorld != null) {
                te.getCapability(NpcCapabilities.CHEST_NPC_DATA_CAPABILITY).ifPresent(
                        chestCap -> {
                            overWorld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY).ifPresent(
                                    worldData -> processLootChestEvents(event.getEntity(), chestCap, worldData));
                            if (chestCap.hasQuestInventoryForPlayer(event.getEntity()) && !event.getEntity().isShiftKeyDown()) {
                                event.getEntity().openMenu(chestCap);
                                event.setCanceled(true);
                            }
                        });
            }
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        int bonusXp = MKNpc.getNpcData(event.getEntity()).map(IEntityNpcData::getBonusXp).orElse(0);
        event.setDroppedExperience(event.getDroppedExperience() + bonusXp);
    }

    private static void processLootChestEvents(Player player, IChestNpcData chestCap, IWorldNpcData worldData) {
        MKNpc.getPlayerQuestData(player).ifPresent(x -> x.getQuestChains().forEach(
                pQuestChain -> {
                    QuestChainInstance questChain = worldData.getQuest(pQuestChain.getQuestId());
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
                                    QuestData qData = questChain.getQuestChainData().getQuestData(currentQuest.getQuestName());
                                    if (containerObj.onLootChest(player, pObj, qData, chestCap)) {
                                        questChain.signalQuestProgress(worldData, x, currentQuest, pQuestChain, false);
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
        if (event.getEntity().level.isClientSide) {
            return;
        }
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) {
            return;
        }
        Level overWorld = server.getLevel(Level.OVERWORLD);
        MKNpc.LOGGER.debug("Setting up dialogue between {} and {}", event.getSpeaker(), event.getEntity());
        if (overWorld != null) {
            overWorld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY).ifPresent(
                    worldData -> MKNpc.getPlayerQuestData(event.getEntity()).ifPresent(x -> x.getQuestChains().forEach(
                            pQuestChain -> {
                                QuestChainInstance questChainInstance = worldData.getQuest(pQuestChain.getQuestId());
                                if (questChainInstance != null) {
                                    MKNpc.LOGGER.debug("Adding quest chain dialogue for {}", questChainInstance.getDefinition().getName());
                                    questChainInstance.getTreeForEntity(event.getSpeaker()).ifPresent(event::addTree);
                                }
                            })));
        }

    }

    private static void handleKillEntityForPlayer(Player player, LivingDeathEvent event, IWorldNpcData worldData) {
        MKNpc.getNpcData(event.getEntity()).ifPresent(x -> {
            if (x.getStructureId().isPresent()) {
                worldData.getStructureManager().onNpcDeath(x);
            }
            if (x.getDefinition() != null) {
                NpcDefinition def = x.getDefinition();
                MKNpc.getPlayerQuestData(player).ifPresent(pData -> pData.getQuestChains().forEach(
                        pQuestChain -> {
                            QuestChainInstance questChain = worldData.getQuest(pQuestChain.getQuestId());
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
                                            QuestData qData = questChain.getQuestChainData().getQuestData(currentQuest.getQuestName());
                                            if (killObj.onPlayerKillNpcDefEntity(player, pObj, def, event, qData, pQuestChain)) {
                                                questChain.signalQuestProgress(worldData, pData, currentQuest, pQuestChain, false);
                                            }
                                        }
                                    }
                                }
                            }
                        }));
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().level.isClientSide) {
            return;
        }
        MKNpc.getNpcData(event.getEntity()).ifPresent(npcData ->
                npcData.getDeathReceiver().ifPresent(receiver -> receiver.onEntityDeath(npcData, event)));
        if (event.getSource().getEntity() instanceof Player player) {
            MinecraftServer server = player.getServer();
            if (server == null) {
                return;
            }
            Level overWorld = server.getLevel(Level.OVERWORLD);
            if (overWorld == null) {
                return;
            }
            overWorld.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY).ifPresent(worldNpcData -> {
                handleKillEntityForPlayer(player, event, worldNpcData);
                Team team = player.getTeam();
                if (team != null) {
                    for (String s : team.getPlayers()) {
                        ServerPlayer member = server.getPlayerList().getPlayerByName(s);
                        if (member != null && !member.equals(player)) {
                            handleKillEntityForPlayer(member, event, worldNpcData);
                        }
                    }
                }
            });
        }
    }


    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        MKNpc.getNpcData(event.getEntity()).ifPresent(IEntityNpcData::tick);
    }

    @SubscribeEvent
    public static void onLootDrop(LivingDropsEvent event) {
        if (event.isRecentlyHit()) {
            MKNpc.getNpcData(event.getEntity()).ifPresent(x -> x.handleExtraLoot(
                    event.getLootingLevel(), event.getDrops(), event.getSource()));
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
