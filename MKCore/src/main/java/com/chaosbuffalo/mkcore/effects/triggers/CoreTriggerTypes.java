package com.chaosbuffalo.mkcore.effects.triggers;

public final class CoreTriggerTypes {
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_MELEE =
            new EntityTriggerType<>("attacker_melee");
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_MAGIC =
            new EntityTriggerType<>("attacker_magic");
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_PROJECTILE =
            new EntityTriggerType<>("attacker_projectile");
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_POST =
            new EntityTriggerType<>("attacker_post");
    public static final EntityTriggerType<VictimDamageTriggerContext> VICTIM_PRE_SCALE =
            new EntityTriggerType<>("victim_pre_scale");
    public static final EntityTriggerType<VictimDamageTriggerContext> VICTIM_POST_SCALE =
            new EntityTriggerType<>("victim_post_scale");

    private CoreTriggerTypes() {
    }
}
