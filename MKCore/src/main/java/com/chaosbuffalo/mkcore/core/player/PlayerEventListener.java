package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;
import java.util.function.Consumer;

// Inspired by epicfightmod
public class PlayerEventListener {
    private final MKPlayerData playerData;
    private final Map<EventType<?>, TreeMultimap<Integer, EventTrigger<?>>> events;


    public PlayerEventListener(MKPlayerData playerData) {
        this.playerData = playerData;
        this.events = new IdentityHashMap<>();
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function) {
        this.subscribe(eventType, uuid, function, -1);
    }

    public <T extends PlayerEvent<?>> void subscribe(EventType<T> eventType, UUID uuid, Consumer<T> function, int priority) {
        if (!eventType.canFire(playerData.isClientSide())) {
            return;
        }

        if (!events.containsKey(eventType)) {
            events.put(eventType, TreeMultimap.create());
        }

        var triggerRecord = new EventTrigger<>(uuid, function, priority);

        unsubscribe(eventType, uuid, priority);
        TreeMultimap<Integer, EventTrigger<? extends PlayerEvent<?>>> map = events.get(eventType);
        map.put(priority, triggerRecord);
    }

    public <T extends PlayerEvent<?>> void unsubscribe(EventType<T> eventType, UUID uuid) {
        unsubscribe(eventType, uuid, -1);
    }

    public <T extends PlayerEvent<?>> void unsubscribe(EventType<T> eventType, UUID uuid, int priority) {
        Multimap<Integer, EventTrigger<? extends PlayerEvent<?>>> map = events.get(eventType);

        if (map != null) {
            map.get(priority).removeIf(trigger -> trigger.is(uuid));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PlayerEvent<?>> void trigger(EventType<T> eventType, T event) {
        if (eventType.canFire(playerData.isClientSide())) {
            TreeMultimap<Integer, EventTrigger<? extends PlayerEvent<?>>> map = events.get(eventType);

            if (map != null) {
                for (int i : map.keySet().descendingSet()) {
                    for (EventTrigger<?> eventTrigger : map.get(i)) {
                        EventTrigger<T> castedTrigger = ((EventTrigger<T>) eventTrigger);
                        castedTrigger.trigger(event);
                    }
                }
            }
        }
    }


    public static class EventTrigger<T extends PlayerEvent<?>> implements Comparable<EventTrigger<?>> {
        private final UUID uuid;
        private final Consumer<T> function;
        private final int priority;

        public EventTrigger(UUID uuid, Consumer<T> function, int priority) {
            this.uuid = uuid;
            this.function = function;
            this.priority = priority;
        }

        public boolean is(UUID uuid) {
            return this.uuid.equals(uuid);
        }

        public void trigger(T args) {
            function.accept(args);
        }

        @Override
        public int compareTo(EventTrigger<?> o) {
            if (uuid == o.uuid) {
                return 0;
            } else {
                return priority > o.priority ? 1 : -1;
            }
        }
    }

    public static class PlayerEvent<T extends MKPlayerData> {
        private final T playerData;

        public PlayerEvent(T playerData) {
            this.playerData = playerData;
        }

        public T getPlayerData() {
            return playerData;
        }
    }

    public static class EventType<T extends PlayerEvent<?>> {
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
    }
}
