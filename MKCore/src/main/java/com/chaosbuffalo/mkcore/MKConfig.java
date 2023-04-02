package com.chaosbuffalo.mkcore;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber
public class MKConfig {

    public static final Client CLIENT;
    private static final ForgeConfigSpec CLIENT_SPEC;
    public static final Server SERVER;
    private static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    public static class Client {
        public ForgeConfigSpec.BooleanValue showMyCrits;
        public ForgeConfigSpec.BooleanValue showOthersCrits;
        public ForgeConfigSpec.BooleanValue enablePlayerCastAnimations;
        public ForgeConfigSpec.BooleanValue showArmorClassOnTooltip;
        public ForgeConfigSpec.BooleanValue showArmorClassEffectsOnTooltip;

        public Client(ForgeConfigSpec.Builder builder) {
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
            builder.pop();
        }
    }

    public static class Server {
        public final ForgeConfigSpec.BooleanValue healsDamageUndead;
        public final ForgeConfigSpec.DoubleValue undeadHealDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue enablePartyXpShare;
        public final ForgeConfigSpec.IntValue partyXpShareDistance;
        public final ForgeConfigSpec.BooleanValue enablePartyXpShareMending;

        public Server(ForgeConfigSpec.Builder builder) {
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

            builder.pop();
        }
    }
}
