package com.chaosbuffalo.mknpc.entity.ai.sensor;


import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MKSensorTypes {

    private static final DeferredRegister<SensorType<?>> REGISTRY =
            DeferredRegister.create(Registries.SENSOR_TYPE, MKNpc.MODID);

    public static final DeferredHolder<SensorType<?>, SensorType<ThreatSensor>> THREAT_SENSOR = REGISTRY.register("sensor.threat",
            () -> new SensorType<>(ThreatSensor::new));

    public static final DeferredHolder<SensorType<?>, SensorType<LivingEntitiesSensor>> ENTITIES_SENSOR = REGISTRY.register("sensor.entities",
            () -> new SensorType<>(LivingEntitiesSensor::new));

    public static final DeferredHolder<SensorType<?>, SensorType<MovementStrategySensor>> DESTINATION_SENSOR = REGISTRY.register("sensor.destination",
            () -> new SensorType<>(MovementStrategySensor::new));

    public static final DeferredHolder<SensorType<?>, SensorType<AbilityUseSensor>> ABILITY_SENSOR = REGISTRY.register("sensor.ability_use",
            () -> new SensorType<>(AbilityUseSensor::new));


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
