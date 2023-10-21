package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.editor.PlayerEditorModule;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.PlayerUpdateEngine;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class MKPlayerData implements IMKEntityData {
    private final Player player;
    private final PlayerAbilityExecutor abilityExecutor;
    private final PlayerStats stats;
    private final PersonaManager personaManager;
    private final PlayerUpdateEngine updateEngine;
    private final PlayerAnimationModule animationModule;
    private final PlayerEquipment equipment;
    private final PlayerCombatExtensionModule combatExtensionModule;
    private final PlayerEditorModule editorModule;
    private final PlayerEffectHandler effectHandler;
    private final EntityPetModule pets;
    private final PlayerAttributeMonitor attributes;
    private final Set<BooleanSupplier> tickCallbacks = new ObjectArraySet<>(4);

    public MKPlayerData(Player playerEntity) {
        player = Objects.requireNonNull(playerEntity);
        updateEngine = new PlayerUpdateEngine(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        combatExtensionModule = new PlayerCombatExtensionModule(this);
        attributes = new PlayerAttributeMonitor(this, this::enqueueTick);
        stats = new PlayerStats(this);

        animationModule = new PlayerAnimationModule(this);
        abilityExecutor.setStartCastCallback(animationModule::startCast);
        abilityExecutor.setCompleteAbilityCallback(this::completeAbility);
        abilityExecutor.setInterruptCastCallback(animationModule::interruptCast);

        equipment = new PlayerEquipment(this);
        editorModule = new PlayerEditorModule(this);
        effectHandler = new PlayerEffectHandler(this);
        pets = new EntityPetModule(this);
        attachUpdateEngine(updateEngine);
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

    public PlayerUpdateEngine getUpdateEngine() {
        return updateEngine;
    }

    public PersonaManager getPersonaManager() {
        return personaManager;
    }

    public PlayerTalentKnowledge getTalents() {
        return getPersona().getTalents();
    }

    public PlayerEntitlementKnowledge getEntitlements() {
        return getPersona().getEntitlements();
    }

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

    public PlayerAttributeMonitor getAttributes() {
        return attributes;
    }

    private void completeAbility(MKAbility ability) {
        animationModule.endCast(ability);
        if (isServerSide()) {
            getSkills().onCastAbility(ability);
        }
    }

    @Override
    public void onJoinWorld() {
        getPersonaManager().onJoinWorld();
        getPersona().onJoinWorld();
        getStats().onJoinWorld();
        getAbilityExecutor().onJoinWorld();
        getEffects().onJoinWorld();
        getCombatExtension().onJoinWorld();
        getAttributes().onJoinWorld();
        if (isServerSide()) {
            initialSync();
        }
    }

    private void onDeath() {
        getEffects().onDeath();
        getPets().onDeath(Entity.RemovalReason.KILLED);
    }

    private void enqueueTick(BooleanSupplier callback) {
        tickCallbacks.add(callback);
    }

    public void update() {
        getEffects().tick();
        getStats().tick();
        getAbilityExecutor().tick();
        getAnimationModule().tick();
        getCombatExtension().tick();

        if (!tickCallbacks.isEmpty()) {
            tickCallbacks.removeIf(BooleanSupplier::getAsBoolean);
        }

        if (isServerSide()) {
            syncState();
        }
    }

    public void clone(MKPlayerData previous, boolean death) {
        if (death) {
            previous.onDeath();
        }
        CompoundTag tag = previous.serializeNBT();
        deserializeNBT(tag);
    }

    private void syncState() {
        updateEngine.syncUpdates();
    }

    public void initialSync() {
        if (isServerSide()) {
            MKCore.LOGGER.debug("Sending initial sync for {}", player);
            updateEngine.sendAll((ServerPlayer) player);
        }
    }

    @Override
    public void onPlayerStartTracking(ServerPlayer otherPlayer) {
        updateEngine.sendAll(otherPlayer);
        getEffects().sendAllEffectsToPlayer(otherPlayer);
    }

    @Override
    public void attachUpdateEngine(UpdateEngine engine) {
        animationModule.getSyncComponent().attach(engine);
        combatExtensionModule.getSyncComponent().attach(engine);
        stats.getSyncComponent().attach(engine);
        editorModule.getSyncComponent().attach(engine);
        pets.getSyncComponent().attach(engine);
    }

    public void onPersonaActivated() {
        getEquipment().onPersonaActivated();
        getStats().onPersonaActivated();
    }

    public void onPersonaDeactivated() {
        getEquipment().onPersonaDeactivated();
        getStats().onPersonaDeactivated();
    }

    public <T extends IPersonaExtension> T getPersonaExtension(Class<T> clazz) {
        return getPersona().getExtension(clazz);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("persona", personaManager.serialize());
        tag.put("stats", getStats().serialize());
        tag.put("editor", getEditor().serialize());
        tag.put("effects", getEffects().serialize());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        personaManager.deserialize(tag.getCompound("persona"));
        getStats().deserialize(tag.getCompound("stats"));
        getEditor().deserialize(tag.getCompound("editor"));
        getEffects().deserialize(tag.getCompound("effects"));
    }
}
