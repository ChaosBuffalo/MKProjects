package com.chaosbuffalo.mkcore.core.player.events;

import net.minecraftforge.fml.LogicalSide;

public class EventType<T extends PlayerEvent<?>> {
//        public static final EventType<ActionEvent<LocalPlayerPatch>> ACTION_EVENT_CLIENT = new EventType<>(null);
//        public static final EventType<ActionEvent<ServerPlayerPatch>> ACTION_EVENT_SERVER = new EventType<>(null);
//        public static final EventType<AttackSpeedModifyEvent> MODIFY_ATTACK_SPEED_EVENT = new EventType<>(null);
//        public static final EventType<ModifyBaseDamageEvent<PlayerPatch<?>>> MODIFY_DAMAGE_EVENT = new EventType<>(null);
//        public static final EventType<DealtDamageEvent> DEALT_DAMAGE_EVENT_PRE = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<DealtDamageEvent> DEALT_DAMAGE_EVENT_POST = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<HurtEvent.Pre> HURT_EVENT_PRE = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<HurtEvent.Post> HURT_EVENT_POST = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<AttackEndEvent> ATTACK_ANIMATION_END_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<BasicAttackEvent> BASIC_ATTACK_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<MovementInputEvent> MOVEMENT_INPUT_EVENT = new EventType<>(LogicalSide.CLIENT);
//        public static final EventType<RightClickItemEvent<LocalPlayerPatch>> CLIENT_ITEM_USE_EVENT = new EventType<>(LogicalSide.CLIENT);
//        public static final EventType<RightClickItemEvent<ServerPlayerPatch>> SERVER_ITEM_USE_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<ItemUseEndEvent> SERVER_ITEM_STOP_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<ProjectileHitEvent> PROJECTILE_HIT_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<SkillExecuteEvent> SKILL_EXECUTE_EVENT = new EventType<>(null);
//        public static final EventType<SkillCancelEvent> SKILL_CANCEL_EVENT = new EventType<>(null);
//        public static final EventType<SkillConsumeEvent> SKILL_CONSUME_EVENT = new EventType<>(null);
//        public static final EventType<ComboCounterHandleEvent> COMBO_COUNTER_HANDLE_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<TargetIndicatorCheckEvent> TARGET_INDICATOR_ALERT_CHECK_EVENT = new EventType<>(LogicalSide.CLIENT);
//        public static final EventType<FallEvent> FALL_EVENT = new EventType<>(null);
//        public static final EventType<SetTargetEvent> SET_TARGET_EVENT = new EventType<>(LogicalSide.SERVER);
//        public static final EventType<DodgeSuccessEvent> DODGE_SUCCESS_EVENT = new EventType<>(LogicalSide.SERVER);

    LogicalSide side;

    EventType(LogicalSide side) {
        this.side = side;
    }

    public boolean canFire(boolean isRemote) {
        return this.side == null || this.side.isClient() == isRemote;
    }

    public static <TEvent extends PlayerEvent<?>> EventType<TEvent> serverSide() {
        return new EventType<>(LogicalSide.SERVER);
    }

    public static <TEvent extends PlayerEvent<?>> EventType<TEvent> clientSide() {
        return new EventType<>(LogicalSide.CLIENT);
    }

    public static <TEvent extends PlayerEvent<?>> EventType<TEvent> bothSides() {
        return new EventType<>(null);
    }
}
