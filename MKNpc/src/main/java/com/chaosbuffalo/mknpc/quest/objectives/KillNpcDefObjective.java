package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.EmptyInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.*;

public class KillNpcDefObjective extends QuestObjective<EmptyInstanceData> implements IKillObjectiveHandler{
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.kill_npc_def");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute("npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);
    protected IntAttribute count = new IntAttribute("count", 1);

    public KillNpcDefObjective(String name, ResourceLocation definition, int count) {
        super(NAME, name, defaultDescription);
        npcDefinition.setValue(definition);
        this.count.setValue(count);
        addAttributes(npcDefinition, this.count);
    }

    public KillNpcDefObjective() {
        super(NAME, "invalid", defaultDescription);
        addAttributes(npcDefinition, this.count);
    }

    @Override
    public EmptyInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        return new EmptyInstanceData();
    }

    @Override
    public EmptyInstanceData instanceDataFactory() {
        return new EmptyInstanceData();
    }

    @Override
    public PlayerQuestObjectiveData playerDataFactory() {
        return new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
    }

    @Override
    public List<MutableComponent> getDescription() {
        return Collections.singletonList(getDescriptionWithKillCount(0));
    }

    private MutableComponent getDescriptionWithKillCount(int count){
        NpcDefinition def = NpcDefinitionManager.getDefinition(npcDefinition.getValue());
        return new TranslatableComponent("mknpc.objective.kill_npc_def.desc", def.getDisplayName(),
                count, this.count.value());
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        PlayerQuestObjectiveData newObj = playerDataFactory();
        newObj.putInt("killCount", 0);
        return newObj;
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                         LivingDeathEvent event, QuestData questData, PlayerQuestChainInstance playerChain) {
        if (def.getDefinitionName().equals(npcDefinition.getValue()) && !isComplete(objectiveData)){
            int currentCount = objectiveData.getInt("killCount");
            currentCount++;
            objectiveData.putInt("killCount", currentCount);
            objectiveData.setDescription(getDescriptionWithKillCount(currentCount));
            player.sendMessage(getDescriptionWithKillCount(currentCount).withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
            if (currentCount == count.value()){
                signalCompleted(objectiveData);
            }
            playerChain.notifyDirty();
            return true;

        }
        return false;
    }
}
