package com.chaosbuffalo.mknpc.entity.ai.sensor;


import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKSensorTypes {

    private static final DeferredRegister<SensorType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.SENSOR_TYPES, MKNpc.MODID);

    public static final RegistryObject<SensorType<ThreatSensor>> THREAT_SENSOR = REGISTRY.register("sensor.threat",
            () -> new SensorType<>(ThreatSensor::new));

    public static final RegistryObject<SensorType<LivingEntitiesSensor>> ENTITIES_SENSOR = REGISTRY.register("sensor.entities",
            () -> new SensorType<>(LivingEntitiesSensor::new));

    public static final RegistryObject<SensorType<MovementStrategySensor>> DESTINATION_SENSOR = REGISTRY.register("sensor.destination",
            () -> new SensorType<>(MovementStrategySensor::new));

    public static final RegistryObject<SensorType<AbilityUseSensor>> ABILITY_SENSOR = REGISTRY.register("sensor.ability_use",
            () -> new SensorType<>(AbilityUseSensor::new));


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
