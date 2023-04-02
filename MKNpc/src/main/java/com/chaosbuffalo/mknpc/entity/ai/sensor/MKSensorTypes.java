package com.chaosbuffalo.mknpc.entity.ai.sensor;


import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKSensorTypes {


    public static SensorType<ThreatSensor> THREAT_SENSOR;

    public static SensorType<LivingEntitiesSensor> ENTITIES_SENSOR;

    public static SensorType<MovementStrategySensor> DESTINATION_SENSOR;

    public static SensorType<AbilityUseSensor> ABILITY_SENSOR;

    @SubscribeEvent
    public static void registerModuleTypes(RegistryEvent.Register<SensorType<?>> evt) {
        ENTITIES_SENSOR = new SensorType<>(LivingEntitiesSensor::new);
        ENTITIES_SENSOR.setRegistryName(MKNpc.MODID, "sensor.entities");
        evt.getRegistry().register(ENTITIES_SENSOR);
        THREAT_SENSOR = new SensorType<>(ThreatSensor::new);
        THREAT_SENSOR.setRegistryName(MKNpc.MODID, "sensor.threat");
        evt.getRegistry().register(THREAT_SENSOR);
        DESTINATION_SENSOR = new SensorType<>(MovementStrategySensor::new);
        DESTINATION_SENSOR.setRegistryName(MKNpc.MODID, "sensor.destination");
        evt.getRegistry().register(DESTINATION_SENSOR);
        ABILITY_SENSOR = new SensorType<>(AbilityUseSensor::new);
        ABILITY_SENSOR.setRegistryName(MKNpc.MODID, "sensor.ability_use");
        evt.getRegistry().register(ABILITY_SENSOR);
    }
}
