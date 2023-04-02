package com.chaosbuffalo.mknpc;

import com.chaosbuffalo.mknpc.capabilities.*;
import com.chaosbuffalo.mknpc.client.gui.screens.QuestPage;
import com.chaosbuffalo.mknpc.command.NpcCommands;
import com.chaosbuffalo.mknpc.dialogue.NPCDialogueExtension;
import com.chaosbuffalo.mknpc.event.NpcClientEventHandler;
import com.chaosbuffalo.mknpc.init.MKNpcBlocks;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mknpc.init.MKNpcTileEntityTypes;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.network.PacketHandler;
import com.chaosbuffalo.mknpc.npc.IMKNpcExtension;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.quest.QuestDefinitionManager;
import com.chaosbuffalo.mknpc.quest.dialogue.NpcDialogueUtils;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.TestJigsawStructurePools;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEventManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(MKNpc.MODID)
public class MKNpc {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "mknpc";
    public static final String REGISTER_NPC_OPTIONS_EXTENSION = "register_npc_extension";
    private NpcDefinitionManager npcDefinitionManager;
    private QuestDefinitionManager questDefinitionManager;

    public MKNpc() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
//
        MKNpcBlocks.register();
        MKNpcTileEntityTypes.register();
        MKNpcEntityTypes.register();
        NpcDialogueUtils.setupMKNpcHandlers();
        npcDefinitionManager = new NpcDefinitionManager();
        questDefinitionManager = new QuestDefinitionManager();
        MKNpcWorldGen.register();
//        MKNpcWorldGen.registerStructurePieces();
//        TestJigsawStructurePools.registerPatterns();
//        MinecraftForge.EVENT_BUS.addListener(MKNpcWorldGen::biomeSetup);
//        MinecraftForge.EVENT_BUS.addListener(MKNpcWorldGen::worldSetup);
        //make sure not to class load server specific events on client
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        NPCDialogueExtension.sendExtension();
        PlayerQuestingDataHandler.registerPersonaExtension();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuestPage::registerPlayerPage);
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("MKNpc.processIMC");
        internalIMCSetup();
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_NPC_OPTIONS_EXTENSION)) {
                LOGGER.info("IMC register npc option extension from mod {} {}", m.senderModId(),
                        m.method());
                IMKNpcExtension ext = (IMKNpcExtension) m.messageSupplier().get();
                ext.registerNpcExtension();
            }
        });
    }

    private void internalIMCSetup() {
        NpcDefinitionManager.setupDeserializers();
        QuestDefinitionManager.setupDeserializers();
        StructureEventManager.setupDeserializers();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NpcCommands.register(event.getDispatcher());
    }


    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.setupHandler();
        NpcCommands.registerArguments();
//        MKNpcWorldGen.registerStructurePoolTypes();
    }



    public static double getDifficultyScale(LivingEntity entity) {
        switch (entity.getCommandSenderWorld().getDifficulty()) {
            case EASY:
                return 0.5;
            case NORMAL:
                return 0.75;
            case HARD:
                return 1.0;
            case PEACEFUL:
            default:
                return 0.25;
        }
    }

    private void doClientStuff(final FMLClientSetupEvent event){
        NpcClientEventHandler.initKeybindings();
    }

    public static LazyOptional<? extends IEntityNpcData> getNpcData(Entity entity){
        return entity.getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY);
    }

    public static LazyOptional<? extends IPlayerQuestingData> getPlayerQuestData(Player entity){
        return entity.getCapability(NpcCapabilities.PLAYER_QUEST_DATA_CAPABILITY);
    }

    public static LazyOptional<? extends IWorldNpcData> getWorldNpcData(Level world){
        return world.getCapability(NpcCapabilities.WORLD_NPC_DATA_CAPABILITY);
    }
}
