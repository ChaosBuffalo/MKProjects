package com.chaosbuffalo.mkcore.abilities.training;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;


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

    public void write(FriendlyByteBuf buffer) {
        buffer.writeComponent(requirementDescription);
        buffer.writeBoolean(isMet);
    }

    public static AbilityRequirementEvaluation read(FriendlyByteBuf buffer) {
        return new AbilityRequirementEvaluation(buffer.readComponent(), buffer.readBoolean());
    }
}
