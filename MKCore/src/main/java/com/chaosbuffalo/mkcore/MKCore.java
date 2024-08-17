package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.events.ClientEventHandler;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import com.chaosbuffalo.mkcore.init.CoreItems;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(MKCore.MOD_ID)
public class MKCore {
    public static final String MOD_ID = "mkcore";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    private final AbilityManager abilityManager;
    private final TalentManager talentManager;
    private final ParticleAnimationManager particleAnimationManager;
    public static final String CORE_EXTENSION = "mk_core_extension";
    public static final String PERSONA_EXTENSION = "register_persona_extension";

    public static MKCore INSTANCE;

    public MKCore(IEventBus modBus, ModContainer modContainer) {
        INSTANCE = this;
        modBus.addListener(this::registerLayers);
        modBus.addListener(this::setup);
        modBus.addListener(EventPriority.LOWEST, this::loadComplete);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::modifyAttributesEvent);
        // Register the processIMC method for modloading
        modBus.addListener(this::processIMC);
        MKCoreRegistry.register(modBus);
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
        talentManager = new TalentManager();
        abilityManager = new AbilityManager();
        particleAnimationManager = new ParticleAnimationManager();

        MKConfig.init();
        AbilityManager.setupDeserializers();
        ParticleAnimationManager.setupDeserializers();
    }

    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.setupHandler();
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        event.enqueueWork(this::registerAttributes);
    }

    public void modifyAttributesEvent(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entityType -> {
            if (entityType == EntityType.PLAYER) {
                MKAttributes.iteratePlayerAttributes(attr -> event.add(entityType, attr));
            }
            MKAttributes.iterateEntityAttributes((attr) -> event.add(entityType, attr));
        });
    }

    private void registerAttributes() {
        Attributes.ATTACK_DAMAGE.value().setSyncable(true);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PlayerPageRegistry.init();
        event.enqueueWork(CoreItems::registerItemProperties);
        ClientEventHandler.setupAttributeRenderers();
    }

    public void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "mk"), MKOverlay.INSTANCE);
    }


    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        MKCommand.registerCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(abilityManager);
        event.addListener(talentManager);
        event.addListener(particleAnimationManager);
    }

    private void processIMC(final InterModProcessEvent event) {
        MKCore.LOGGER.debug("MKCore.processIMC");
        internalIMCStageSetup();
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(PERSONA_EXTENSION)) {
                MKCore.LOGGER.debug("IMC register persona extension from mod {} {}", m.senderModId(), m.method());
                IPersonaExtensionProvider factory = (IPersonaExtensionProvider) m.messageSupplier().get();
                PersonaManager.registerExtension(factory);
            }
        });
    }

    private void internalIMCStageSetup() {
        CoreParticles.handleEditorParticleRegistration();
    }

    public static ResourceLocation makeRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, path);
    }

    public static Optional<MKPlayerData> getPlayer(Entity playerEntity) {
        return playerEntity.getExistingData(CoreAttachments.PLAYER_DATA_ATTACHMENT);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static MKPlayerData getPlayerOrNull(Entity playerEntity) {
        return playerEntity.getData(CoreAttachments.PLAYER_DATA_ATTACHMENT);
    }

    public static Optional<? extends IMKEntityData> getEntityData(@Nullable Entity entity) {
        if (entity instanceof Player) {
            return entity.getExistingData(CoreAttachments.PLAYER_DATA_ATTACHMENT);
        } else if (entity instanceof LivingEntity) {
            return entity.getExistingData(CoreAttachments.ENTITY_DATA_ATTACHMENT);
        }
        return Optional.empty();
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static IMKEntityData getEntityDataOrNull(@Nullable Entity entity) {
        return getEntityData(entity).orElse(null);
    }

    public static TalentManager getTalentManager() {
        return INSTANCE.talentManager;
    }

    public static AbilityManager getAbilityManager() {
        return INSTANCE.abilityManager;
    }

    public static ParticleAnimationManager getAnimationManager() {
        return INSTANCE.particleAnimationManager;
    }
}
