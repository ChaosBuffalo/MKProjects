package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.content.databases.ILevelOptionDatabase;
import com.chaosbuffalo.mknpc.content.databases.IQuestDatabase;
import com.chaosbuffalo.mknpc.content.databases.IStructureDatabase;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IWorldNpcData extends IStructureDatabase, IQuestDatabase, ILevelOptionDatabase,
        INBTSerializable<CompoundTag> {

}
