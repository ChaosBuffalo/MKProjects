package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.init.CoreEntitlementTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class SimpleEntitlement extends MKEntitlement {
    public static final MapCodec<SimpleEntitlement> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(MKEntitlement::getName),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(MKEntitlement::getDescription)
    ).apply(builder, SimpleEntitlement::new));

    public SimpleEntitlement(Component displayName, Component description) {
        super(displayName, description, 1);
    }

    @Override
    public EntitlementType<SimpleEntitlement> getEntitlementType() {
        return CoreEntitlementTypes.PLAYER_FLAG.get();
    }

}
