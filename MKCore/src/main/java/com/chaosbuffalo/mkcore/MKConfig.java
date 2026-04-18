package com.chaosbuffalo.mkcore;



import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MKConfig {

    public static final Client CLIENT;
    private static final ModConfigSpec CLIENT_SPEC;
    public static final Server SERVER;
    private static final ModConfigSpec SERVER_SPEC;

    static {
        final Pair<Client, ModConfigSpec> clientSpecPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final Pair<Server, ModConfigSpec> serverSpecPair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    public static void init(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    public static class Client {
        public ModConfigSpec.BooleanValue showMyCrits;
        public ModConfigSpec.BooleanValue showOthersCrits;
        public ModConfigSpec.BooleanValue enablePlayerCastAnimations;
        public ModConfigSpec.BooleanValue showArmorClassOnTooltip;
        public ModConfigSpec.BooleanValue showArmorClassEffectsOnTooltip;
        public ModConfigSpec.BooleanValue disableAutoattackForFriend;
        public ModConfigSpec.IntValue particleEmitterUpdatesPerSecond;

        public Client(ModConfigSpec.Builder builder) {
            builder.comment("General settings").push("general");
            showMyCrits = builder
                    .comment("Show your own crit messages")
                    .define("showMyCrits", true);
            showOthersCrits = builder
                    .comment("Show other's crit messages")
                    .define("showOthersCrits", true);
            enablePlayerCastAnimations = builder
                    .comment("Enable player cast animations. Requires client restart to take effect")
                    .worldRestart()
                    .define("enablePlayerCastAnimations", true);
            showArmorClassOnTooltip = builder
                    .comment("Show armor class on the item tooltip")
                    .define("showArmorClassOnTooltip", true);
            showArmorClassEffectsOnTooltip = builder
                    .comment("Show armor class effects on the item tooltip")
                    .define("showArmorClassEffectsOnTooltip", true);
            disableAutoattackForFriend = builder
                    .comment("Disables auto-attacking on friendly targets")
                    .define("disableAttackForFriend", false);
            particleEmitterUpdatesPerSecond = builder
                    .comment("How many times per second render-driven particle emitters may update on the client")
                    .defineInRange("particleEmitterUpdatesPerSecond", 60, 1, 240);
            builder.pop();
        }
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue healsDamageUndead;
        public final ModConfigSpec.DoubleValue undeadHealDamageMultiplier;
        public final ModConfigSpec.BooleanValue enablePartyXpShare;
        public final ModConfigSpec.IntValue partyXpShareDistance;
        public final ModConfigSpec.BooleanValue enablePartyXpShareMending;
        public final ModConfigSpec.DoubleValue skillScalingMultiplier;
        public final ModConfigSpec.IntValue worldDifficultyBandSize;
        public final ModConfigSpec.DoubleValue difficultyBandIncrease;
        public final ModConfigSpec.IntValue maxTalentPoints;
        public final ModConfigSpec.IntValue talentPointsPerSkill;
        public final ModConfigSpec.IntValue baseXpPerTalentPoint;
        public final ModConfigSpec.DoubleValue totalTalentXpMultiplier;
        public final ModConfigSpec.IntValue scalingXpPerTalentPoint;

        public Server(ModConfigSpec.Builder builder) {
            builder.comment("Gameplay settings").push("gameplay");
            healsDamageUndead = builder
                    .comment("Should healing spells damage undead entities")
                    .define("healsDamageUndead", true);
            undeadHealDamageMultiplier = builder
                    .comment("Damage multiplier to use when healing spells damage undead entities (if healsDamageUndead is set)")
                    .defineInRange("undeadHealDamageMultiplier", 2.0, 0.0, 20);
            enablePartyXpShare = builder
                    .comment("Whether to split XP picked up with the player's party")
                    .define("enablePartyXpShare", true);
            partyXpShareDistance = builder
                    .comment("Distance to share XP with party members")
                    .defineInRange("partyXpShareDistance", 100, 0, Integer.MAX_VALUE);
            enablePartyXpShareMending = builder
                    .comment("Whether shared XP triggers the Mending enchantment")
                    .define("enablePartyXpShareMending", true);
            skillScalingMultiplier = builder
                    .comment("The amount of the skill scaling multiplier that will apply")
                    .defineInRange("skillScalingMultiplier", 0.75, 0.0, 1.0);
            worldDifficultyBandSize = builder
                    .comment("The size of the difficulty band")
                    .defineInRange("worldDifficultyBandSize", 2000, 100, 10000);
            difficultyBandIncrease = builder
                    .comment("The amount difficulty goes up each band")
                    .defineInRange("difficultyBandIncrease", 10.0, 0.0, 100.0);
            maxTalentPoints = builder
                    .comment("The maximum number of talent points you can collect")
                    .defineInRange("maxTalentPoints", 0, 0, Integer.MAX_VALUE);
            talentPointsPerSkill = builder
                    .comment("The number of talent points it takes to increase your skill cap by 10 (set to 0 to disable).")
                    .defineInRange("talentPointsPerSkill", 10, 0, Integer.MAX_VALUE);
            baseXpPerTalentPoint = builder
                    .comment("The amount of xp it takes to get your first talent point.")
                    .defineInRange("baseXpPerTalentPoint", 100, 1, Integer.MAX_VALUE);
            totalTalentXpMultiplier = builder
                    .comment("Your current total talent points are scaled by this value before applying the cost multiplier.")
                    .defineInRange("totalTalentXpMultiplier", 0.5, 0.01, Double.MAX_VALUE);
            scalingXpPerTalentPoint = builder
                    .comment("The cost multiplier for additional talent points after your first.")
                    .defineInRange("scalingXpPerTalentPoint", 100, 0, Integer.MAX_VALUE);
            builder.pop();
        }
    }
}
