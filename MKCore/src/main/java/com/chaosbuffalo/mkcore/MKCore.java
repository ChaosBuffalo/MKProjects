package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.client.gui.MKOverlay;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.core.ICoreExtension;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.events.ClientEventHandler;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreItems;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;


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
    public static final String REGISTER_PLAYER_PAGE = "register_player_page";

    public static MKCore INSTANCE;

    public MKCore() {
        INSTANCE = this;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(EventPriority.LOWEST, this::loadComplete);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::modifyAttributesEvent);
        // Register the processIMC method for modloading
        modBus.addListener(this::processIMC);
        MKCoreRegistry.register(modBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        talentManager = new TalentManager();
        abilityManager = new AbilityManager();
        particleAnimationManager = new ParticleAnimationManager();

        MKConfig.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        PacketHandler.setupHandler();
        MKCommand.registerArguments();
        ParticleAnimationManager.setupDeserializers();
        AbilityManager.setupDeserializers();
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
        Attributes.ATTACK_DAMAGE.setSyncable(true);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new MKOverlay());
        ClientEventHandler.initKeybindings();
        PlayerPageRegistry.init();
        event.enqueueWork(CoreItems::registerItemProperties);
        ClientEventHandler.setupAttributeRenderers();
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, "skip health", MKOverlay::skipHealth);
        OverlayRegistry.enableOverlay(ForgeIngameGui.PLAYER_HEALTH_ELEMENT, false);
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
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> PlayerPageRegistry::checkClientIMC);
        event.getIMCStream().forEach(m -> {
            if (m.method().equals(PERSONA_EXTENSION)) {
                MKCore.LOGGER.debug("IMC register persona extension from mod {} {}", m.senderModId(), m.method());
                IPersonaExtensionProvider factory = (IPersonaExtensionProvider) m.messageSupplier().get();
                PersonaManager.registerExtension(factory);
            } else if (m.method().equals(CORE_EXTENSION)) {
                MKCore.LOGGER.debug("IMC core extension from mod {} {}", m.senderModId(), m.method());
                ICoreExtension extension = (ICoreExtension) m.messageSupplier().get();
                extension.register();
            }
        });
    }

    private void internalIMCStageSetup() {
        CoreParticles.handleEditorParticleRegistration();
    }

    public static ResourceLocation makeRL(String path) {
        return new ResourceLocation(MKCore.MOD_ID, path);
    }

    public static LazyOptional<MKPlayerData> getPlayer(Entity playerEntity) {
        return playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static MKPlayerData getPlayerOrNull(Entity playerEntity) {
        return playerEntity.getCapability(CoreCapabilities.PLAYER_CAPABILITY).orElse(null);
    }

    public static LazyOptional<? extends IMKEntityData> getEntityData(@Nullable Entity entity) {
        if (entity instanceof Player) {
            return entity.getCapability(CoreCapabilities.PLAYER_CAPABILITY);
        } else if (entity instanceof LivingEntity) {
            return entity.getCapability(CoreCapabilities.ENTITY_CAPABILITY);
        }
        return LazyOptional.empty();
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
