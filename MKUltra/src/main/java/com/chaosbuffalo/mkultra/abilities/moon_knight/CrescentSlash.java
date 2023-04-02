//package com.chaosbuffalo.mkultra.abilities.moon_knight;
//
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.core.IPlayerData;
//import com.chaosbuffalo.mkultra.core.PlayerAbility;
//import com.chaosbuffalo.mkultra.effects.AreaEffectBuilder;
//import com.chaosbuffalo.mkultra.effects.SpellCast;
//import com.chaosbuffalo.mkultra.effects.spells.AbilityMeleeDamage;
//import com.chaosbuffalo.mkultra.effects.spells.ParticlePotion;
//import com.chaosbuffalo.mkultra.effects.spells.SoundPotion;
//import com.chaosbuffalo.mkultra.fx.ParticleEffects;
//import com.chaosbuffalo.mkultra.init.ModSounds;
//import com.chaosbuffalo.mkultra.network.packets.ParticleEffectSpawnPacket;
//import com.chaosbuffalo.targeting_api.Targeting;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.util.EnumParticleTypes;
//import net.minecraft.util.SoundCategory;
//import net.minecraft.util.SoundEvent;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.world.World;
//import net.minecraftforge.event.RegistryEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
//import javax.annotation.Nullable;
//
//@Mod.EventBusSubscriber(modid = MKUltra.MODID)
//public class CrescentSlash extends PlayerAbility {
//    public static final CrescentSlash INSTANCE = new CrescentSlash();
//
//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<PlayerAbility> event) {
//        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
//    }
//
//    public static float BASE_DAMAGE = 4.0f;
//    public static float DAMAGE_SCALE = 4.0f;
//
//    private CrescentSlash() {
//        super(MKUltra.MODID, "ability.crescent_slash");
//    }
//
//    @Override
//    public int getCooldown(int currentRank) {
//        return 7 - currentRank;
//    }
//
//    @Override
//    public Targeting.TargetType getTargetType() {
//        return Targeting.TargetType.ENEMY;
//    }
//
//    @Override
//    public float getManaCost(int currentRank) {
//        return 8 - 2 * currentRank;
//    }
//
//    @Override
//    public float getDistance(int currentRank) {
//        return 7.0f + currentRank * 2.0f;
//    }
//
//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_shadow_5;
//    }
//
//    @Override
//    public int getRequiredLevel(int currentRank) {
//        return currentRank * 2;
//    }
//
//    @Override
//    public void execute(EntityPlayer entity, IPlayerData pData, World theWorld) {
//        int level = pData.getAbilityRank(getAbilityId());
//
//        EntityLivingBase targetEntity = getSingleLivingTarget(entity, getDistance(level));
//        if (targetEntity != null) {
//            pData.startAbility(this);
//
//            // What to do for each target hit
//            SpellCast damage = AbilityMeleeDamage.Create(entity, BASE_DAMAGE, DAMAGE_SCALE);
//            SpellCast particlePotion = ParticlePotion.Create(entity,
//                    EnumParticleTypes.SWEEP_ATTACK.getParticleID(),
//                    ParticleEffects.CIRCLE_MOTION, false, new Vec3d(1.0, 1.0, 1.0),
//                    new Vec3d(0.0, 1.0, 0.0), 4, 0, 1.0);
//
//            AreaEffectBuilder.Create(entity, targetEntity)
//                    .spellCast(damage, level, getTargetType())
//                    .spellCast(particlePotion, level, getTargetType())
//                    .spellCast(SoundPotion.Create(entity, ModSounds.spell_shadow_3, SoundCategory.PLAYERS),
//                            1, getTargetType())
//                    .instant()
//                    .color(16409620).radius(5.0f, true)
//                    .particle(EnumParticleTypes.CRIT)
//                    .spawn();
//
//            Vec3d lookVec = entity.getLookVec();
//            MKUltra.packetHandler.sendToAllAround(
//                    new ParticleEffectSpawnPacket(
//                            EnumParticleTypes.SWEEP_ATTACK.getParticleID(),
//                            ParticleEffects.SPHERE_MOTION, 5, 5,
//                            targetEntity.posX, targetEntity.posY + 1.0,
//                            targetEntity.posZ, 1.0, 1.0, 1.0, 1.5,
//                            lookVec),
//                    targetEntity, 50.0f);
//        }
//    }
//}