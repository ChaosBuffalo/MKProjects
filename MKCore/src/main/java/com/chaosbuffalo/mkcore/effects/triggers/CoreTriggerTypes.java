package com.chaosbuffalo.mkcore.effects.triggers;

public final class CoreTriggerTypes {
    /**
     * Fired for the attacking entity during {@code LivingDamageEvent.Pre} after melee damage
     * classification and any melee crit/damage adjustments have been applied, but before final
     * damage is committed to the victim.
     */
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_MELEE =
            new EntityTriggerType<>("attacker_melee");
    /**
     * Fired for the attacking entity during {@code LivingDamageEvent.Pre} for non-melee MK damage
     * that is treated as spell or magic damage, after the damage amount has been scaled for that hit.
     */
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_MAGIC =
            new EntityTriggerType<>("attacker_magic");
    /**
     * Fired for the attacking entity during {@code LivingDamageEvent.Pre} after projectile-specific
     * damage processing, including ranged bonus, crit, and ranged resistance application.
     */
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_PROJECTILE =
            new EntityTriggerType<>("attacker_projectile");
    /**
     * Fired for the attacking entity during {@code LivingDamageEvent.Post} after damage has been
     * finalized and applied to the victim. This trigger is observational only and cannot modify
     * the damage sequence.
     */
    public static final EntityTriggerType<AttackerDamageTriggerContext> ATTACKER_POST =
            new EntityTriggerType<>("attacker_post");
    /**
     * Fired for the victim during {@code LivingDamageEvent.Pre} when the hit enters MKCore's
     * damage pipeline, before post-armor MK resistance adjustments are applied.
     */
    public static final EntityTriggerType<VictimDamageTriggerContext> VICTIM_INCOMING =
            new EntityTriggerType<>("victim_incoming");
    /**
     * Fired for the victim during {@code LivingDamageEvent.Post} after final damage has been
     * applied. Like {@link #ATTACKER_POST}, this is a reaction stage and cannot modify damage.
     */
    public static final EntityTriggerType<VictimDamageTriggerContext> VICTIM_POST =
            new EntityTriggerType<>("victim_post");
    /**
     * Fired when a living entity receives {@code LivingFallEvent}, before fall damage is resolved.
     */
    public static final EntityTriggerType<FallTriggerContext> FALL =
            new EntityTriggerType<>("fall");
    /**
     * Fired for the killer during {@code LivingDeathEvent} after the victim has died from the hit.
     */
    public static final EntityTriggerType<KillTriggerContext> KILL =
            new EntityTriggerType<>("kill");

    private CoreTriggerTypes() {
    }
}
