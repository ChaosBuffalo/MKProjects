package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.editor.PlayerEditorModule;
import com.chaosbuffalo.mkcore.core.player.PlayerEntitlements;
import com.chaosbuffalo.mkcore.core.entity.EntityRiderModule;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.controllers.PlayerSyncController;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class MKPlayerData implements IMKEntityData {
    protected final Player player;
    private final PlayerAbilityExecutor abilityExecutor;
    private final PlayerStats stats;
    private final PersonaManager personaManager;
    protected final PlayerSyncController syncController;
    private final PlayerAnimationModule animationModule;
    private final PlayerEquipment equipment;
    protected final PlayerCombatExtensionModule combatExtensionModule;
    private final PlayerEditorModule editorModule;
    private final PlayerEffectHandler effectHandler;
    private final EntityPetModule pets;
    protected final PlayerAttributeMonitor attributeMonitor;
    private final PlayerEventDispatcher events;
    private final EntityRiderModule riders;

    public MKPlayerData(Player playerEntity) {
        player = Objects.requireNonNull(playerEntity);
        events = new PlayerEventDispatcher(this);
        syncController = new PlayerSyncController(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        combatExtensionModule = new PlayerCombatExtensionModule(this);
        attributeMonitor = new PlayerAttributeMonitor(this);
        stats = new PlayerStats(this);

        animationModule = new PlayerAnimationModule(this);
        abilityExecutor.setStartCastCallback(animationModule::startCast);
        abilityExecutor.setCompleteAbilityCallback(this::completeAbility);
        abilityExecutor.setInterruptCastCallback(animationModule::interruptCast);

        equipment = new PlayerEquipment(this);
        editorModule = new PlayerEditorModule(this);
        effectHandler = new PlayerEffectHandler(this);
        pets = new EntityPetModule(this);
        riders = new EntityRiderModule(this);
        attachUpdateEngine(syncController);
    }

    private Persona getPersona() {
        return getPersonaManager().getActivePersona();
    }

    @Override
    public PlayerStats getStats() {
        return stats;
    }

    @Override
    public PlayerCombatExtensionModule getCombatExtension() {
        return combatExtensionModule;
    }

    public PlayerEventDispatcher events() {
        return events;
    }

    @Override
    public PlayerAbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    public PlayerAbilityLoadout getLoadout() {
        return getPersona().getLoadout();
    }

    @Override
    public PlayerAbilityKnowledge getAbilities() {
        return getPersona().getAbilities();
    }

    public PlayerSkills getSkills() {
        return getPersona().getSkills();
    }

    public PlayerSyncController getSyncController() {
        return syncController;
    }

    public PersonaManager getPersonaManager() {
        return personaManager;
    }

    public PlayerTalentKnowledge getTalents() {
        return getPersona().getTalents();
    }

    public PlayerEntitlements getEntitlements() {
        return getPersona().getEntitlements();
    }

    @Override
    public PlayerEquipment getEquipment() {
        return equipment;
    }

    @Nonnull
    @Override
    public Player getEntity() {
        return player;
    }

    public PlayerAnimationModule getAnimationModule() {
        return animationModule;
    }

    @Override
    public EntityPetModule getPets() {
        return pets;
    }

    @Override
    public EntityRiderModule getRiders() {
        return riders;
    }

    public PlayerEditorModule getEditor() {
        return editorModule;
    }

    @Override
    public Optional<ParticleEffectInstanceTracker> getParticleEffectTracker() {
        return Optional.of(getAnimationModule().getEffectInstanceTracker());
    }

    @Override
    public PlayerEffectHandler getEffects() {
        return effectHandler;
    }

    public PlayerAttributeMonitor getAttributeMonitor() {
        return attributeMonitor;
    }

    private void completeAbility(MKAbility ability) {
        animationModule.endCast(ability);
        if (isServerSide()) {
            getSkills().onCastAbility(ability);
        }
    }

    @Override
    public void onJoinWorld() {
        getStats().onJoinLevel();
        if (isServerSide()) {
            getPersonaManager().onJoinWorld();
            getEffects().onJoinLevel();
        }
    }

    private void onDeath() {
        getEffects().onDeath();
        getPets().onDeath(Entity.RemovalReason.KILLED);
    }

    @Override
    public void update() {
        getEffects().tick();
        getStats().tick();
        getAbilityExecutor().tick();
        getAnimationModule().tick();
        getCombatExtension().tick();
    }

    public void clone(MKPlayerData previous, boolean death) {
        if (death) {
            previous.onDeath();
        }
        var prov = player.registryAccess();
        CompoundTag tag = previous.serializeNBT(prov);
        deserializeNBT(prov, tag);
    }

    @Override
    public void onPlayerStartTracking(ServerPlayer otherPlayer) {
        syncController.sendFullSync(otherPlayer);
        getEffects().sendAllEffectsToPlayer(otherPlayer);
    }

    public void attachUpdateEngine(SyncController engine) {
        animationModule.getSyncComponent().attach(engine);
        combatExtensionModule.getSyncComponent().attach(engine);
        stats.getSyncComponent().attach(engine);
        editorModule.getSyncComponent().attach(engine);
        pets.getSyncComponent().attach(engine);
        riders.getSyncComponent().attach(engine);
    }

    public <T extends IPersonaExtension> T getPersonaExtension(Class<T> clazz) {
        return getPersona().getExtension(clazz);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("persona", personaManager.serialize(provider));
        tag.put("stats", getStats().serialize(provider));
        tag.put("editor", getEditor().serialize(provider));
        tag.put("effects", getEffects().serialize(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        personaManager.deserialize(provider, tag.getCompound("persona"));
        getStats().deserialize(provider, tag.getCompound("stats"));
        getEditor().deserialize(provider, tag.getCompound("editor"));
        getEffects().deserialize(provider, tag.getCompound("effects"));
    }
}
