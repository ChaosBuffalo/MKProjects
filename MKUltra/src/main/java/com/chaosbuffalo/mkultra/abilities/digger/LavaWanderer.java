//package com.chaosbuffalo.mkultra.abilities.digger;
//
//import com.chaosbuffalo.mkultra.GameConstants;
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.abilities.cast_states.CastState;
//import com.chaosbuffalo.mkultra.core.IPlayerData;
//import com.chaosbuffalo.mkultra.core.PlayerAbility;
//import com.chaosbuffalo.mkultra.core.PlayerFormulas;
//import com.chaosbuffalo.mkultra.effects.AreaEffectBuilder;
//import com.chaosbuffalo.mkultra.effects.SpellCast;
//import com.chaosbuffalo.mkultra.effects.spells.ParticlePotion;
//import com.chaosbuffalo.mkultra.effects.spells.SoundPotion;
//import com.chaosbuffalo.mkultra.fx.ParticleEffects;
//import com.chaosbuffalo.mkultra.init.ModSounds;
//import com.chaosbuffalo.mkultra.network.packets.ParticleEffectSpawnPacket;
//import com.chaosbuffalo.targeting_api.Targeting;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.MobEffects;
//import net.minecraft.potion.PotionEffect;
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
//public class LavaWanderer extends PlayerAbility {
//    public static LavaWanderer INSTANCE = new LavaWanderer();
//
//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<PlayerAbility> event) {
//        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
//    }
//
//    private static final int DURATION_PER_LEVEL = 20;
//
//    private LavaWanderer() {
//        super(MKUltra.MODID, "ability.lava_wanderer");
//    }
//
//    @Override
//    public int getCooldown(int currentRank) {
//        return 30;
//    }
//
//    @Override
//    public Targeting.TargetType getTargetType() {
//        return Targeting.TargetType.FRIENDLY;
//    }
//
//    @Override
//    public float getManaCost(int currentRank) {
//        return 5 + 5 * currentRank;
//    }
//
//    @Override
//    public float getDistance(int currentRank) {
//        return 5.0f + 5.0f * currentRank;
//    }
//
//    @Override
//    public int getRequiredLevel(int currentRank) {
//        return 6 + currentRank * 2;
//    }
//
//    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.casting_fire;
//    }
//
//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_fire_1;
//    }
//
//    @Override
//    public int getCastTime(int currentRank) {
//        return GameConstants.TICKS_PER_SECOND * (4 - currentRank);
//    }
//
//    @Override
//    public void endCast(EntityPlayer entity, IPlayerData data, World theWorld, CastState state) {
//        super.endCast(entity, data, theWorld, state);
//        int level = data.getAbilityRank(getAbilityId());
//
//        int duration = GameConstants.TICKS_PER_SECOND * DURATION_PER_LEVEL * level;
//        duration = PlayerFormulas.applyBuffDurationBonus(data, duration);
//        PotionEffect fireResist = new PotionEffect(MobEffects.FIRE_RESISTANCE,
//                duration,
//                level, false, true);
//        PotionEffect speed = new PotionEffect(MobEffects.HASTE,
//                duration,
//                level, false, true);
//
//        SpellCast particle = ParticlePotion.Create(entity,
//                EnumParticleTypes.DRIP_LAVA.getParticleID(),
//                ParticleEffects.CIRCLE_MOTION, false, new Vec3d(1.0, 1.0, 1.0),
//                new Vec3d(0.0, 1.0, 0.0), 40, 5, 1.0);
//
//        AreaEffectBuilder.Create(entity, entity)
//                .effect(speed, getTargetType())
//                .effect(fireResist, getTargetType())
//                .spellCast(SoundPotion.Create(entity, ModSounds.spell_fire_7, SoundCategory.PLAYERS),
//                        1, getTargetType())
//                .spellCast(particle, level, getTargetType())
//                .instant()
//                .particle(EnumParticleTypes.DRIP_LAVA)
//                .color(16762880).radius(getDistance(level), true)
//                .spawn();
//
//        Vec3d lookVec = entity.getLookVec();
//        MKUltra.packetHandler.sendToAllAround(
//                new ParticleEffectSpawnPacket(
//                        EnumParticleTypes.FLAME.getParticleID(),
//                        ParticleEffects.SPHERE_MOTION, 40, 10,
//                        entity.posX, entity.posY + 1.0,
//                        entity.posZ, 1.0, 1.0, 1.0, 1.0,
//                        lookVec),
//                entity, 50.0f);
//    }
//
//    @Override
//    public void execute(EntityPlayer entity, IPlayerData pData, World theWorld) {
//        pData.startAbility(this);
//    }
//}
