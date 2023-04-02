//package com.chaosbuffalo.mkultra.abilities.ranger;
//
//import com.chaosbuffalo.mkultra.MKUltra;
//import com.chaosbuffalo.mkultra.core.IPlayerData;
//import com.chaosbuffalo.mkultra.core.PlayerAbility;
//import com.chaosbuffalo.mkultra.core.PlayerToggleAbility;
//import com.chaosbuffalo.mkultra.effects.spells.WildToxinPotion;
//import com.chaosbuffalo.mkultra.fx.ParticleEffects;
//import com.chaosbuffalo.mkultra.init.ModSounds;
//import com.chaosbuffalo.mkultra.network.packets.ParticleEffectSpawnPacket;
//import com.chaosbuffalo.mkultra.utils.AbilityUtils;
//import com.chaosbuffalo.targeting_api.Targeting;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.potion.Potion;
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
//public class WildToxin extends PlayerToggleAbility {
//    public static final WildToxin INSTANCE = new WildToxin();
//
//    @SubscribeEvent
//    public static void register(RegistryEvent.Register<PlayerAbility> event) {
//        event.getRegistry().register(INSTANCE.setRegistryName(INSTANCE.getAbilityId()));
//    }
//
//    public static int BASE_DURATION = 32767;
//    public static int DURATION_SCALE = 0;
//
//    private WildToxin() {
//        super(MKUltra.MODID, "ability.wild_toxin");
//    }
//
//    @Override
//    public int getCooldown(int currentRank) {
//        return 4 - currentRank;
//    }
//
//    @Override
//    public Targeting.TargetType getTargetType() {
//        return Targeting.TargetType.SELF;
//    }
//
//    @Override
//    public float getManaCost(int currentRank) {
//        return 3 - currentRank;
//    }
//
//    @Override
//    public float getDistance(int currentRank) {
//        return 1.0f;
//    }
//
//    @Override
//    public Potion getToggleEffect() {
//        return WildToxinPotion.INSTANCE;
//    }
//
//    @Override
//    public int getRequiredLevel(int currentRank) {
//        return 4 + currentRank * 2;
//    }
//
//    @Nullable
//    @Override
//    public SoundEvent getSpellCompleteSoundEvent() {
//        return null;
//    }
//
//    @Override
//    public void applyEffect(EntityPlayer entity, IPlayerData pData, World theWorld) {
//        super.applyEffect(entity, pData, theWorld);
//        int level = pData.getAbilityRank(getAbilityId());
//        AbilityUtils.playSoundAtServerEntity(entity, ModSounds.spell_buff_attack_2, SoundCategory.PLAYERS);
//        // What to do for each target hit
//        entity.addPotionEffect(WildToxinPotion.Create(entity).setTarget(entity).toPotionEffect(BASE_DURATION, level));
//        Vec3d lookVec = entity.getLookVec();
//        MKUltra.packetHandler.sendToAllAround(
//                new ParticleEffectSpawnPacket(
//                        EnumParticleTypes.SPELL_MOB.getParticleID(),
//                        ParticleEffects.CIRCLE_MOTION, 50, 0,
//                        entity.posX, entity.posY + 1.5,
//                        entity.posZ, 1.0, 1.0, 1.0, 1.0f,
//                        lookVec),
//                entity, 50.0f);
//
//    }
//}