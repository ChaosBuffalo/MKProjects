package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KillNotableNpcObjective extends StructureInstanceObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "objective.kill_notable");
    protected ResourceLocationAttribute npcDefinition = new ResourceLocationAttribute("npcDefinition", NpcDefinitionManager.INVALID_NPC_DEF);


    public KillNotableNpcObjective(String name, ResourceLocation structure, int index, ResourceLocation npcDefinition,
                                   MutableComponent... description) {
        super(NAME, name,  structure, index, description);
        this.npcDefinition.setValue(npcDefinition);
        addAttributes(this.npcDefinition);
    }

    public KillNotableNpcObjective() {
        super(NAME, "invalid", defaultDescription);
        addAttributes(npcDefinition);
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                         LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)){
            UUIDInstanceData objData = getInstanceData(quest);
            boolean applies = event.getEntityLiving().getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY).map(
                    x -> x.getNotableUUID().equals(objData.getUuid())).orElse(false);
            if (applies){
                objectiveData.putBool("hasKilled", true);
                objectiveData.removeBlockPos("npcPos");
                player.sendMessage(new TranslatableComponent("mknpc.objective.kill_notable.complete",
                        event.getEntityLiving().getDisplayName()).withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
                signalCompleted(objectiveData);
                playerChain.notifyDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures) {
        MKStructureEntry entry = questStructures.get(getStructureName()).get(structureIndex.value());
        Optional<NotableNpcEntry> npcOpt = entry.getFirstNotableOfType(npcDefinition.getValue());
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }


    @Override
    public PlayerQuestObjectiveData playerDataFactory() {
        return new PlayerQuestObjectiveData(getObjectiveName(), getDescription());
    }

    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return structureName.getValue().equals(entry.getStructureName()) && entry.hasNotableOfType(npcDefinition.getValue());
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = playerDataFactory();
        NotableNpcEntry notable = worldData.getNotableNpc(objData.getUuid());
        if (notable != null) {
            newObj.setDescription((new TranslatableComponent("mknpc.objective.kill_notable.desc", notable.getName())));
            newObj.putBlockPos("npcPos", notable.getLocation());
        }
        newObj.putBool("hasKilled", false);
        return newObj;
    }
}
