package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;

public class NpcDefinitionEntry extends MKLayout {
    private final NpcDefinitionClient npcDefinitionClient;
    private final Consumer<NpcDefinitionClient> callback;

    public NpcDefinitionEntry(NpcDefinitionClient definition, int width, Font font,
                              Consumer<NpcDefinitionClient> callback) {
        super(0, 0, width, 18);
        this.npcDefinitionClient = definition;
        this.callback = callback;
        MKFaction faction = MKFactionRegistry.getFaction(definition.getFaction());
        String text;
        if (faction != null){
            text = String.format("%s (%s) %s", definition.getName(),
                    faction.getTranslationKey() != null ? I18n.get(faction.getTranslationKey()) :
                            definition.getFaction().toString(), definition.getDefinitionName().toString());
        } else {
            text = definition.getName();
        }

        MKText nameText = new MKText(font, text);
        nameText.setMultiline(true);
        nameText.setWidth(width);
        setMarginTop(3);
        addWidget(nameText);
        addConstraintToWidget(MarginConstraint.TOP, nameText);
        addConstraintToWidget(MarginConstraint.LEFT, nameText);
//        MKText locText = new MKText(font, definition.getDefinitionName().toString());
//        locText.setWidth(font.getStringWidth(definition.getDefinitionName().toString()));
//        addWidget(locText);
//        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), locText);
//        addConstraintToWidget(new HorizontalStackConstraint(), locText);
//        MKText factionText = new MKText(font, definition.getFaction().toString());
//        factionText.setWidth(font.getStringWidth(definition.getFaction().toString()));
//        addWidget(factionText);
//        addConstraintToWidget(new MarginConstraint(MarginConstraint.MarginType.TOP), factionText);
//        addConstraintToWidget(new HorizontalStackConstraint(), factionText);
        MKRectangle divider = new MKRectangle(0, 0, width, 1, 0xaaffffff);
        addWidget(divider);
        addConstraintToWidget(MarginConstraint.BOTTOM, divider);
        addConstraintToWidget(MarginConstraint.LEFT, divider);
        setHeight(Math.max(nameText.getHeight() + 1 + 5, 18));
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        callback.accept(getNpcDefinitionClient());
        return true;
    }

    @Override
    public void postDraw(PoseStack stack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            mkFill(stack, x, y, x + width, y + height, 0x55ffffff);
        }
    }

    public NpcDefinitionClient getNpcDefinitionClient() {
        return npcDefinitionClient;
    }
}
