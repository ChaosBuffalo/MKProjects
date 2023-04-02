//package com.chaosbuffalo.mkultra.abilities.ranger;
//
//import com.chaosbuffalo.mkcore.abilities.MKAbility;
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.abilities.cast_states.CastState;
//import com.chaosbuffalo.mkultra.effects.spells.ShieldingPotion;
//import com.chaosbuffalo.mkultra.init.ModSounds;
//import com.chaosbuffalo.targeting_api.Targeting;
//import net.minecraft.init.MobEffects;
//import net.minecraft.util.EnumParticleTypes;
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
//public class DesperateSurge extends MKAbility {
//    public static final DesperateSurge INSTANCE = new DesperateSurge();
//
//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<PlayerAbility> event) {
//        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
//    }
//
//    public static int BASE_DURATION = 6;
//    public static int DURATION_SCALE = 2;
//    public static int BASE_SHIELDING = 0;
//    public static int SHIELDING_SCALE = 2;
//    public static int FOOD_COST = 2;
//    public static int FOOD_SCALE = 2;
//
//    private DesperateSurge() {
//        super(MKUltra.MODID, "ability.desperate_surge");
//    }
//
//    @Override
//    public int getCooldown(int currentRank) {
//        return 16 - 2 * currentRank;
//    }
//
//    @Override
//    public Targeting.TargetType getTargetType() {
//        return Targeting.TargetType.SELF;
//    }
//
//    @Override
//    public float getManaCost(int currentRank) {
//        return 4 + 2 * currentRank;
//    }
//
//    @Override
//    public float getDistance(int currentRank) {
//        return 1.0f;
//    }
//
//    @Override
//    public int getRequiredLevel(int currentRank) {
//        return 2 + currentRank * 2;
//    }
//
//    @Override
//    public int getCastTime(int currentRank) {
//        return GameConstants.TICKS_PER_SECOND / 2;
//    }
//
//    @Override
//    public SoundEvent getCastingSoundEvent() {
//        return ModSounds.casting_shadow;
//    }
//
//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return ModSounds.spell_dark_11;
//    }
//
//    @Override
//    public void endCast(EntityPlayer entity, IPlayerData data, World theWorld, CastState state) {
//        super.endCast(entity, data, theWorld, state);
//        int level = data.getAbilityRank(getAbilityId());
//        entity.getFoodStats().setFoodLevel(entity.getFoodStats().getFoodLevel() - (FOOD_COST + level * FOOD_SCALE));
//        int duration = (BASE_DURATION + DURATION_SCALE * level) * GameConstants.TICKS_PER_SECOND;
//        duration = PlayerFormulas.applyBuffDurationBonus(data, duration);
//        entity.addPotionEffect(ShieldingPotion.Create(entity).setTarget(entity).toPotionEffect(
//                duration, BASE_SHIELDING + level * SHIELDING_SCALE));
//        entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, duration * 2, 2 + level));
//        float healAmount = PlayerFormulas.applyHealBonus(data, 2.0f * level);
//        entity.heal(healAmount);
//        Vec3d lookVec = entity.getLookVec();
//        MKUltra.packetHandler.sendToAllAround(
//                new ParticleEffectSpawnPacket(
//                        EnumParticleTypes.SMOKE_NORMAL.getParticleID(),
//                        ParticleEffects.CIRCLE_MOTION, 25, 0,
//                        entity.posX, entity.posY + 1.5,
//                        entity.posZ, 1.0, 1.0, 1.0, 1.0f,
//                        lookVec),
//                entity, 50.0f);
//    }
//
//    @Override
//    public void execute(EntityPlayer entity, IPlayerData pData, World theWorld) {
//        int level = pData.getAbilityRank(getAbilityId());
//        if (entity.getFoodStats().getFoodLevel() >= FOOD_COST + level * FOOD_SCALE) {
//            pData.startAbility(this);
//        }
//    }
//}
//
