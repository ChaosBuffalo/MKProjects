package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;


// TODO: Might be a good candidate for a record in J16
public class AbilityRequirementEvaluation {
    private final Component requirementDescription;
    private final boolean isMet;

    public AbilityRequirementEvaluation(Component description, boolean isMet) {
        this.requirementDescription = description;
        this.isMet = isMet;
    }

    public boolean isMet() {
        return isMet;
    }

    public Component description() {
        return requirementDescription;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        ComponentSerialization.STREAM_CODEC.encode(buffer, requirementDescription);
        buffer.writeBoolean(isMet);
    }

    public static AbilityRequirementEvaluation read(RegistryFriendlyByteBuf buffer) {
        return new AbilityRequirementEvaluation(ComponentSerialization.STREAM_CODEC.decode(buffer), buffer.readBoolean());
    }
}
