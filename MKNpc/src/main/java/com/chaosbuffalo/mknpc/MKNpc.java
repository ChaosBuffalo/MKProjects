package com.chaosbuffalo.mknpc;

import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.chaosbuffalo.mknpc.client.gui.screens.QuestPage;
import com.chaosbuffalo.mknpc.command.NpcCommands;
import com.chaosbuffalo.mknpc.components.NpcComponents;
import com.chaosbuffalo.mknpc.dialogue.NPCDialogueExtension;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.entity.ai.sensor.MKSensorTypes;
import com.chaosbuffalo.mknpc.init.*;
import com.chaosbuffalo.mknpc.npc.*;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.chaosbuffalo.mknpc.quest.dialogue.NpcDialogueUtils;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueEffectTypes;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEventManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


@Mod(MKNpc.MODID)
public class MKNpc {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean DEV_LOGGING = Boolean.parseBoolean(System.getProperty("mknpc.enable_debug_log", "false"));
    public static final String MODID = "mknpc";
    public static final String REGISTER_NPC_OPTIONS_EXTENSION = "register_npc_extension";

    public MKNpc(IEventBus modBus) {
        modBus.addListener(this::setup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::enqueueIMC);
        modBus.addListener(this::processIMC);
        setupRegistries(modBus);
        NeoForge.EVENT_BUS.register(this);
        NpcDialogueUtils.setupMKNpcHandlers();
    }

    private void setupRegistries(IEventBus modBus) {
        MKNpcAttachments.register(modBus);
        MKNpcAttributes.register(modBus);
        MKNpcBlocks.register(modBus);
        NpcCommands.register(modBus);
        MKNpcBlockEntityTypes.register(modBus);
        MKNpcEntityTypes.register(modBus);
        MKNpcEffects.register(modBus);
        MKMemoryModuleTypes.register(modBus);
        MKSensorTypes.register(modBus);
        MKNpcWorldGen.register(modBus);
        NpcDialogueEffectTypes.REGISTRY.register(modBus);
        NpcDialogueConditionTypes.REGISTRY.register(modBus);
        QuestRegistries.register(modBus);
        NpcRegistries.register(modBus);
        NpcOptionTypes.register(modBus);
        NpcOptionEntryTypes.register(modBus);
        StructureEventManager.setupDeserializers();
        NpcComponents.register(modBus);

    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        NPCDialogueExtension.sendExtension();
        PlayerQuestingDataHandler.registerPersonaExtension();
    }

    private void processIMC(final InterModProcessEvent event) {
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(REGISTER_NPC_OPTIONS_EXTENSION)) {
                LOGGER.info("IMC register npc option extension from mod {} {}", m.senderModId(),
                        m.method());
                IMKNpcExtension ext = (IMKNpcExtension) m.messageSupplier().get();
                ext.registerNpcExtension();
            }
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NpcCommands.register(event.getDispatcher());
    }


    private void setup(final FMLCommonSetupEvent event) {
//        MKNpcWorldGen.registerStructurePoolTypes();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(QuestPage::registerPlayerPage);
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

    public static Optional<IEntityNpcData> getNpcData(Entity entity) {
        return IEntityNpcData.get(entity);
    }

    public static Optional<IPlayerQuestingData> getPlayerQuestData(Player entity) {
        return IPlayerQuestingData.get(entity);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

}
