package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.editor.PlayerEditorModule;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.PersonaManager;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.core.player.*;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.sync.PlayerUpdateEngine;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

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

    public MKPlayerData(Player playerEntity) {
        player = Objects.requireNonNull(playerEntity);
        updateEngine = new PlayerUpdateEngine(this);
        personaManager = PersonaManager.getPersonaManager(this);
        abilityExecutor = new PlayerAbilityExecutor(this);
        combatExtensionModule = new PlayerCombatExtensionModule(this);
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

    @Override
    public PlayerKnowledge getKnowledge() {
        return getPersonaManager().getActivePersona().getKnowledge();
    }

    public PlayerAbilityLoadout getLoadout() {
        return getKnowledge().getAbilityLoadout();
    }

    public PlayerAbilityKnowledge getAbilities() {
        return getKnowledge().getAbilityKnowledge();
    }

    public PlayerSkills getSkills() {
        return getKnowledge().getSkills();
    }

    public PlayerUpdateEngine getUpdateEngine() {
        return updateEngine;
    }

    public PersonaManager getPersonaManager() {
        return personaManager;
    }

    public PlayerTalentKnowledge getTalents() {
        return getKnowledge().getTalentKnowledge();
    }

    public PlayerEntitlementKnowledge getEntitlements() {
        return getKnowledge().getEntitlementsKnowledge();
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
    public PlayerEffectHandler getEffects() {
        return effectHandler;
    }

    @Override
    public boolean isServerSide() {
        return player instanceof ServerPlayer;
    }

    private void completeAbility(MKAbility ability) {
        animationModule.endCast(ability);
        if (isServerSide()) {
            getSkills().onCastAbility(ability);
        }
    }

    @Override
    public void onJoinWorld() {
        getPersonaManager().ensurePersonaLoaded();
        getKnowledge().onJoinWorld();
        getStats().onJoinWorld();
        getAbilityExecutor().onJoinWorld();
        getEffects().onJoinWorld();
        if (isServerSide()) {
            MKCore.LOGGER.info("server player joined world!");
            initialSync();
        } else {
            MKCore.LOGGER.info("client player joined world!");
        }
    }

    private void onDeath() {
        getEffects().onDeath();
        getPets().onDeath();
    }

    public void update() {
        getEntity().getCommandSenderWorld().getProfiler().push("MKPlayerData.update");

        getEntity().getCommandSenderWorld().getProfiler().push("PlayerEffects.tick");
        getEffects().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("PlayerStats.tick");
        getStats().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("AbilityExecutor.tick");
        getAbilityExecutor().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("Animation.tick");
        getAnimationModule().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("PlayerCombat.tick");
        getCombatExtension().tick();

        if (isServerSide()) {
            getEntity().getCommandSenderWorld().getProfiler().popPush("Updater.sync");
            syncState();
        }
        getEntity().getCommandSenderWorld().getProfiler().pop();

        getEntity().getCommandSenderWorld().getProfiler().pop();
    }

    public void clone(MKPlayerData previous, boolean death) {
        if (death) {
            previous.onDeath();
        }
        CompoundTag tag = previous.serialize();
        deserialize(tag);
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
        getAbilityExecutor().onPersonaActivated();
        getStats().onPersonaActivated();
    }

    public void onPersonaDeactivated() {
        getEquipment().onPersonaDeactivated();
        getAbilityExecutor().onPersonaDeactivated();
        getStats().onPersonaDeactivated();
    }

    public <T extends IPersonaExtension> T getPersonaExtension(Class<T> clazz) {
        return getPersonaManager().getActivePersona().getExtension(clazz);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("persona", personaManager.serialize());
        tag.put("stats", getStats().serialize());
        tag.put("editor", getEditor().serialize());
        tag.put("effects", getEffects().serialize());
        return tag;
    }

    @Override
    public EntityPetModule getPets() {
        return pets;
    }

    public PlayerEditorModule getEditor() {
        return editorModule;
    }


    @Override
    public void deserialize(CompoundTag tag) {
        personaManager.deserialize(tag.getCompound("persona"));
        getStats().deserialize(tag.getCompound("stats"));
        getEditor().deserialize(tag.getCompound("editor"));
        getEffects().deserialize(tag.getCompound("effects"));
    }
}
