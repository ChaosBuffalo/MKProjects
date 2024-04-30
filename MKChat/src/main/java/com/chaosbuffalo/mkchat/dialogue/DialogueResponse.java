package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DialogueResponse {
    public static final Codec<DialogueResponse> CODEC = RecordCodecBuilder.<DialogueResponse>mapCodec(builder ->
            builder.group(
                    Codec.STRING.fieldOf("responseNodeId").forGetter(i -> i.responseNodeId),
                    Codec.list(DialogueCondition.CODEC).optionalFieldOf("conditions", Collections.emptyList()).forGetter(i -> i.conditions)
            ).apply(builder, DialogueResponse::new)).codec();


    private final List<DialogueCondition> conditions;
    private final String responseNodeId;

    private DialogueResponse(String nodeId, List<DialogueCondition> conditions) {
        responseNodeId = nodeId;
        this.conditions = conditions;
    }

    public DialogueResponse(String nodeId) {
        this.responseNodeId = nodeId;
        this.conditions = new ArrayList<>();
    }

    public DialogueResponse(DialogueNode node) {
        this(node.getId());
    }

    public String getResponseNodeId() {
        return responseNodeId;
    }

    public DialogueResponse copy() {
        DialogueResponse newResponse = new DialogueResponse(getResponseNodeId());
        conditions.forEach(c -> newResponse.addCondition(c.copy()));
        return newResponse;
    }

    public boolean doesMatchConditions(ServerPlayer player, LivingEntity source) {
        return conditions.stream().allMatch(x -> x.checkCondition(player, source));
    }

    public List<DialogueCondition> getConditions() {
        return conditions;
    }

    public DialogueResponse addCondition(DialogueCondition condition) {
        conditions.add(condition);
        return this;
    }
}
