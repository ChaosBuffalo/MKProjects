package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.command.HotBarCommand;
import com.chaosbuffalo.mkcore.command.arguments.*;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CoreCommands {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES =
            DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MKCore.MOD_ID);

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> ABILITY_ID = ARGUMENT_TYPES.register("ability_id",
            () -> ArgumentTypeInfos.registerByClass(AbilityIdArgument.class,
                    SingletonArgumentInfo.contextFree(AbilityIdArgument::ability)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> ABILITY_GROUP = ARGUMENT_TYPES.register("ability_group",
            () -> ArgumentTypeInfos.registerByClass(HotBarCommand.AbilityGroupArgument.class,
                    SingletonArgumentInfo.contextFree(HotBarCommand.AbilityGroupArgument::abilityGroup)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> TALENT_ID = ARGUMENT_TYPES.register("talent_id",
            () -> ArgumentTypeInfos.registerByClass(TalentIdArgument.class,
                    SingletonArgumentInfo.contextFree(TalentIdArgument::talentId)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> TALENT_TREE = ARGUMENT_TYPES.register("talent_tree_id",
            () -> ArgumentTypeInfos.registerByClass(TalentTreeIdArgument.class,
                    SingletonArgumentInfo.contextFree(TalentTreeIdArgument::talentTreeId)));
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> TALENT_LINE = ARGUMENT_TYPES.register("talent_line_id",
            () -> ArgumentTypeInfos.registerByClass(TalentLineIdArgument.class,
                    SingletonArgumentInfo.contextFree(TalentLineIdArgument::talentLine)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> BONE_ID = ARGUMENT_TYPES.register("bone_id",
            () -> ArgumentTypeInfos.registerByClass(BipedBoneArgument.class,
                    SingletonArgumentInfo.contextFree(BipedBoneArgument::bipedBone)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> PARTICLE_ANIMATION_ID = ARGUMENT_TYPES.register("particle_animation_id",
            () -> ArgumentTypeInfos.registerByClass(ParticleAnimationArgument.class,
                    SingletonArgumentInfo.contextFree(ParticleAnimationArgument::particleAnimation)));

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> ABILITY_SOURCE_TYPE_ID = ARGUMENT_TYPES.register("ability_source_type_id",
            () -> ArgumentTypeInfos.registerByClass(AbilitySourceTypeArgument.class,
                    SingletonArgumentInfo.contextFree(AbilitySourceTypeArgument::abilitySourceType)));

    public static void register(IEventBus modBus) {
        ARGUMENT_TYPES.register(modBus);
    }
}
