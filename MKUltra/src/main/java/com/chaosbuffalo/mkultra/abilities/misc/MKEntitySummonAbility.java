package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.SummonPetCondition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.pets.MKPet;
import com.chaosbuffalo.mkcore.core.pets.PetNonCombatBehavior;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Function;

public class MKEntitySummonAbility extends MKAbility {
    protected final ResourceLocationAttribute npcDefintion = new ResourceLocationAttribute("npc", NpcDefinitionManager.INVALID_NPC_DEF);
    protected final Attribute summoningSkill;


    public MKEntitySummonAbility(ResourceLocation npcDef, Attribute skillAttribute) {
        super();
        npcDefintion.setDefaultValue(npcDef);
        addAttribute(npcDefintion);
        setCastTime(5 * GameConstants.TICKS_PER_SECOND);
        setUseCondition(new SummonPetCondition(this));
        addSkillAttribute(skillAttribute);
        summoningSkill = skillAttribute;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.POSITION_INCLUDE_ENTITIES;
    }

    @Override
    public int getCastTime(IMKEntityData casterData) {
        return casterData.getPets().isPetActive(getAbilityId()) ? 0 : super.getCastTime(casterData);
    }

    @Override
    public float getManaCost(IMKEntityData casterData) {
        return casterData.getPets().isPetActive(getAbilityId()) ? 0f : super.getManaCost(casterData);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 20.0f;
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        TargetUtil.LivingOrPosition target = context.getMemory(MKAbilityMemories.ABILITY_POSITION_TARGET).orElse(null);
        if (target == null) {
            return;
        }
        if (!casterData.getPets().isPetActive(getAbilityId())) {
            NpcDefinition def = NpcDefinitionManager.getDefinition(npcDefintion.getValue());
            if (def != null && target.getPosition().isPresent()) {
                UUID id = casterData instanceof MKPlayerData playerData ?
                        playerData.getPersonaManager().getActivePersona().getPersonaId() :
                        MKNpc.getNpcData(castingEntity).map(IEntityNpcData::getSpawnID).orElse(castingEntity.getUUID());
                Entity entity = def.createEntity(castingEntity.getCommandSenderWorld(), target.getPosition().get(), id, context.getSkill(summoningSkill));
                MKPet<MKEntity> pet = MKPet.makePetFromEntity(MKEntity.class, getAbilityId(), entity);
                if (pet.getEntity() != null) {
                    casterData.getPets().addPet(pet);
                    castingEntity.getCommandSenderWorld().addFreshEntity(pet.getEntity());
                    pet.getEntity().setNoncombatBehavior(new PetNonCombatBehavior(castingEntity));
                    pet.getEntity().setNonCombatMoveType(MKEntity.NonCombatMoveType.STATIONARY);
                    MKNpc.getNpcData(pet.getEntity()).ifPresent(x -> x.setMKSpawned(true));
                    pet.getEntity().getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY).ifPresent(x -> x.setFactionName(MKFaction.INVALID_FACTION));
                    Component newName = Component.translatable("mkultra.pet_name_format", castingEntity.getName(), pet.getEntity().getName());
                    pet.getEntity().setCustomName(newName);
                } else {
                    if (entity != null) {
                        EntityUtils.mkDiscard(entity);
                    }
                    MKUltra.LOGGER.error("Summon Ability {} failed to cast npc: {} to a MKEntity", getAbilityId(), npcDefintion.getValue());
                }
            } else {
                MKUltra.LOGGER.error("Summon Ability {} Failed to summon npc: {}, definition invalid.", getAbilityId(), npcDefintion.getValue());
            }
        } else {
            if (target.getEntity().isPresent()) {
                LivingEntity tar = target.getEntity().get();
                casterData.getPets().getPet(getAbilityId()).ifPresent(x -> {
                    if (tar.equals(x.getEntity())) {
                        if (castingEntity.isShiftKeyDown()) {
                            EntityUtils.mkDiscard(x.getEntity());
                            casterData.getPets().removePet(x);
                        } else {
                            x.getEntity().setNoncombatBehavior(new PetNonCombatBehavior(castingEntity));
                        }
                    } else {
                        if (x.getEntity() != null) {
                            if (Targeting.isValidEnemy(castingEntity, tar)) {
                                float newThreat = x.getEntity().getHighestThreat() + 500.0f;
                                x.getEntity().addThreat(tar, newThreat, true);
                                x.getEntity().getBrain().eraseMemory(MKMemoryModuleTypes.SPAWN_POINT.get());
                                if (x.getEntity() instanceof Mob) {
                                    ((Mob) x.getEntity()).getNavigation().stop();
                                }
                                x.getEntity().enterCombatMovementState(tar);

                            } else if (Targeting.isValidFriendly(castingEntity, tar)) {
                                x.getEntity().setNoncombatBehavior(new PetNonCombatBehavior(tar));
                            }
                        }
                    }
                });
            } else if (target.getPosition().isPresent()) {
                Vec3 pos = target.getPosition().get();
                casterData.getPets().getPet(getAbilityId()).ifPresent(
                        x -> {
                            if (x.getEntity() != null) {
                                x.getEntity().setNoncombatBehavior(new PetNonCombatBehavior(pos));
                                x.getEntity().clearThreat();
                            }
                        });
            }
        }
    }
}
