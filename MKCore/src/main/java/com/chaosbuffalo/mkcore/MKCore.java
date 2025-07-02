package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.command.MKCommand;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtensionProvider;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.mojang.logging.LogUtils;
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
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.slf4j.Logger;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(MKCore.MOD_ID)
public class MKCore {
    public static final String MOD_ID = "mkcore";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogUtils.getLogger();
    private final AbilityManager abilityManager;
    private final TalentManager talentManager;
    private final ParticleAnimationManager particleAnimationManager;
    public static final String CORE_EXTENSION = "mk_core_extension";
    public static final String PERSONA_EXTENSION = "register_persona_extension";

    public static MKCore INSTANCE;

    public MKCore(IEventBus modBus, ModContainer modContainer) {
        INSTANCE = this;
        MKConfig.init(modContainer);
        modBus.addListener(EventPriority.LOWEST, this::loadComplete);
        modBus.addListener(this::modifyAttributesEvent);
        // Register the processIMC method for modloading
        modBus.addListener(this::processIMC);
        MKCoreRegistry.register(modBus);
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
        talentManager = new TalentManager();
        abilityManager = new AbilityManager();
        particleAnimationManager = new ParticleAnimationManager();
        AbilityManager.setupDeserializers();
        ParticleAnimationManager.setupDeserializers();
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

    public static Optional<MKPlayerData> getPlayer(Entity entity) {
        return entity instanceof Player player ? getPlayer(player) : Optional.empty();
    }

    // FIXME: this is a pointless Optional. All players will have this attachment
    public static Optional<MKPlayerData> getPlayer(Player playerEntity) {
        return Optional.of(playerEntity.getData(CoreAttachments.PLAYER_DATA_ATTACHMENT));
    }

    @Nullable
    public static MKPlayerData getPlayerOrNull(Entity entity) {
        return entity instanceof Player player ? getPlayerOrThrow(player) : null;
    }

    @Nonnull
    public static MKPlayerData getPlayerOrThrow(Player playerEntity) {
        return playerEntity.getData(CoreAttachments.PLAYER_DATA_ATTACHMENT);
    }

    public static Optional<? extends IMKEntityData> getEntityData(@Nullable Entity entity) {
        return entity instanceof LivingEntity living ? Optional.of(getEntityDataOrThrow(living)) : Optional.empty();
    }

    // FIXME: All LivingEntity will have the attachment so we don't need optionals here
    public static Optional<? extends IMKEntityData> getEntityData(@Nullable LivingEntity entity) {
        return Optional.ofNullable(getEntityDataOrNull(entity));
    }

    @Nonnull
    public static IMKEntityData getEntityDataOrThrow(@Nonnull LivingEntity entity) {
        if (entity instanceof Player) {
            return entity.getData(CoreAttachments.PLAYER_DATA_ATTACHMENT);
        }
        return entity.getData(CoreAttachments.ENTITY_DATA_ATTACHMENT);
    }

    @Nullable
    public static IMKEntityData getEntityDataOrNull(@Nullable Entity entity) {
        if (entity instanceof Player) {
            return entity.getData(CoreAttachments.PLAYER_DATA_ATTACHMENT);
        } else if (entity instanceof LivingEntity) {
            return entity.getData(CoreAttachments.ENTITY_DATA_ATTACHMENT);
        }
        return null;
    }

    public static Optional<MKEntityData> getEntitySpecificData(@Nullable LivingEntity entity) {
        if (entity instanceof Player) {
            return Optional.empty();
        }
        if (entity instanceof LivingEntity) {
            return Optional.of(entity.getData(CoreAttachments.ENTITY_DATA_ATTACHMENT));
        }
        return Optional.empty();
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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, path);
    }
}
