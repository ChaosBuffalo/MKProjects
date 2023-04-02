//package com.chaosbuffalo.mkultra.abilities.skald;
//
//import com.chaosbuffalo.mkultra.GameConstants;
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.abilities.cast_states.CastState;
//import com.chaosbuffalo.mkultra.core.IPlayerData;
//import com.chaosbuffalo.mkultra.core.PlayerAbility;
//import com.chaosbuffalo.mkultra.effects.AreaEffectBuilder;
//import com.chaosbuffalo.mkultra.effects.SpellCast;
//import com.chaosbuffalo.mkultra.effects.spells.AbilityMagicDamage;
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
//public class KPDarkWail extends PlayerAbility {
//    public static final KPDarkWail INSTANCE = new KPDarkWail();
//
//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<PlayerAbility> event) {
//        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
//    }
//
//    public static float BASE_DAMAGE = 8.0f;
//    public static float DAMAGE_SCALE = 2.0f;
//    public static int DURATION_BASE = 4;
//    public static int DURATION_SCALE = 2;
//
//    private KPDarkWail() {
//        super(MKUltra.MODID, "ability.kp_dark_wail");
//    }
//
//    @Override
//    public int getCooldown(int currentRank) {
//        return 14 - 2 * currentRank;
//    }
//
//    @Override
//    public Targeting.TargetType getTargetType() {
//        return Targeting.TargetType.ENEMY;
//    }
//
//    @Override
//    public float getManaCost(int currentRank) {
//        return 2 + currentRank * 2;
//    }
//
//    @Override
//    public float getDistance(int currentRank) {
//        return 1.0f + currentRank * 2.0f;
//    }
//
//    @Override
//    public int getRequiredLevel(int currentRank) {
//        return 4 + currentRank * 2;
//    }
//
//    @Override
//    public int getCastTime(int currentRank) {
//        return GameConstants.TICKS_PER_SECOND / currentRank;
//    }
//
//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_scream_1;
//    }
//
//    @Override
//    public void endCast(EntityPlayer entity, IPlayerData data, World theWorld, CastState state) {
//        super.endCast(entity, data, theWorld, state);
//        int level = data.getAbilityRank(getAbilityId());
//
//        // What to do for each target hit
//        SpellCast damage = AbilityMagicDamage.Create(entity, BASE_DAMAGE, DAMAGE_SCALE);
//        SpellCast particle = ParticlePotion.Create(entity,
//                EnumParticleTypes.CRIT_MAGIC.getParticleID(),
//                ParticleEffects.CIRCLE_PILLAR_MOTION, false, new Vec3d(1.0, 1.0, 1.0),
//                new Vec3d(0.0, 1.0, 0.0), 20, 5, 1.0);
//
//        PotionEffect slow = new PotionEffect(MobEffects.SLOWNESS,
//                DURATION_BASE + DURATION_SCALE * level * GameConstants.TICKS_PER_SECOND, 100);
//
//        AreaEffectBuilder.Create(entity, entity)
//                .spellCast(damage, level, getTargetType())
//                .spellCast(particle, level, getTargetType())
//                .spellCast(SoundPotion.Create(entity, ModSounds.spell_negative_effect_1, SoundCategory.PLAYERS),
//                        1, getTargetType())
//                .effect(slow, getTargetType())
//                .instant()
//                .color(16711935).radius(getDistance(level), true)
//                .particle(EnumParticleTypes.NOTE)
//                .spawn();
//
//        Vec3d lookVec = entity.getLookVec();
//        MKUltra.packetHandler.sendToAllAround(
//                new ParticleEffectSpawnPacket(
//                        EnumParticleTypes.CRIT_MAGIC.getParticleID(),
//                        ParticleEffects.CIRCLE_MOTION, 20, 0,
//                        entity.posX, entity.posY + 1.0,
//                        entity.posZ, 1.0, 1.0, 1.0, 2.0,
//                        lookVec),
//                entity, 50.0f);
//    }
//
//    @Override
//    public void execute(EntityPlayer entity, IPlayerData pData, World theWorld) {
//        pData.startAbility(this);
//
//    }
//}
