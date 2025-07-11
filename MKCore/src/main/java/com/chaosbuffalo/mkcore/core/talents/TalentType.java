package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public abstract class TalentType<T extends TalentNode> implements IRecordType<TalentRecord> {

    public abstract MapCodec<T> codec();

    public abstract TalentTypeHandler createTypeHandler(Persona persona);

    public abstract MutableComponent getTalentNodeName(TalentRecord record);

    public abstract MutableComponent getTypeDisplayName(TalentRecord record);

    public abstract MutableComponent getTalentDescription(TalentRecord record);

    /* Tooltip
    [NodeName] - default getTalentNameKey, uses talentId
    [TypeDisplayName]
    [Description]
    <extra additions by describeTalent>
     */
    public void buildTooltip(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        consumer.accept(getTalentNodeName(record));
        consumer.accept(getTypeDisplayName(record).withStyle(ChatFormatting.GOLD));
        describeTalent(entityData, record, consumer);
    }

    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        consumer.accept(getTalentDescription(record).withStyle(ChatFormatting.GRAY));
    }
}
