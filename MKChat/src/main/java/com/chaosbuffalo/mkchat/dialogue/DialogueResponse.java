package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogueResponse {
    public static final String INVALID_RESPONSE_ID = "dialogue_response.invalid";
    private final List<DialogueCondition> conditions;
    private String responseNodeId;

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

    public boolean isValid() {
        return !responseNodeId.equals(INVALID_RESPONSE_ID);
    }

    public DialogueResponse addCondition(DialogueCondition condition) {
        conditions.add(condition);
        return this;
    }

    public static <D> DataResult<DialogueResponse> fromDynamic(Dynamic<D> dynamic) {
        Optional<String> name = decodeKey(dynamic);
        if (name.isEmpty()) {
            return DataResult.error(() -> "Failed to decode dialogue response id: " + dynamic);
        }

        DialogueResponse resp = new DialogueResponse(name.get());
        resp.deserialize(dynamic);
        if (resp.isValid()) {
            return DataResult.success(resp);
        }
        return DataResult.error(() -> "Unable to decode dialogue response: " + name.get());
    }

    private static <D> Optional<String> decodeKey(Dynamic<D> dynamic) {
        return dynamic.get("responseNodeId").asString().resultOrPartial(DialogueUtils::throwParseException);
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        responseNodeId = decodeKey(dynamic)
                .orElseThrow(IllegalStateException::new);
        conditions.clear();
        dynamic.get("conditions")
                .asList(DialogueCondition::fromDynamic)
                .forEach(dr -> dr.resultOrPartial(DialogueUtils::throwParseException).ifPresent(conditions::add));
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("responseNodeId"), ops.createString(responseNodeId));
        if (!conditions.isEmpty()) {
            builder.put(ops.createString("conditions"),
                    ops.createList(conditions.stream().map(x -> x.serialize(ops))));
        }
        return ops.createMap(builder.build());
    }
}
