package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.talents.*;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.TalentPointActionPacket;
import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

public class TalentTreeWidget extends MKLayout {
    private final Font fontRenderer;
    private final int originalWidth;
    private final int originalHeight;
    private final Supplier<TalentTreeRecord> recordSupplier;
    private final IMKEntityData entityData;

    public TalentTreeWidget(IMKEntityData entityData, int x, int y, int width, int height,
                            Font fontRenderer, Supplier<TalentTreeRecord> recordSupplier) {
        super(x, y, width, height);
        this.fontRenderer = fontRenderer;
        this.originalWidth = width;
        this.originalHeight = height;
        this.recordSupplier = recordSupplier;
        setMargins(6, 6, 6, 6);
        this.entityData = entityData;
        setup();
    }

    public void setup() {
        TalentTreeRecord current = getCurrent();
        if (current == null) {
            MKText noSelectPrompt = new MKText(fontRenderer,
                    Component.translatable("mkcore.gui.select_talent_tree"));
            noSelectPrompt.setColor(0xffffffff);
            addWidget(noSelectPrompt, MarginConstraint.TOP, MarginConstraint.LEFT);
            setWidth(originalWidth);
            setHeight(originalHeight);
        } else {
            TalentTreeDefinition currentTree = current.getTreeDefinition();
            ResourceKey<TalentTreeDefinition> treeId = current.getTreeId();

            int treeRenderingMarginX = getMarginRight() + getMarginLeft();
            int treeRenderingPaddingX = 5;
            int talentButtonHeight = TalentButton.HEIGHT;
            int talentButtonWidth = TalentButton.WIDTH;
            int talentButtonYMargin = getMarginTop();
            Map<String, TalentLineDefinition> lineDefs = currentTree.getTalentLines();
            int count = lineDefs.size();
            int talentWidth = talentButtonWidth * count + treeRenderingMarginX + (count - 1) * treeRenderingPaddingX;
            int spacePerColumn = talentWidth / count;
            int columnOffset = (spacePerColumn - talentButtonWidth) / 2;
            int talentXOffset = getWidth() > talentWidth ? (getWidth() - talentWidth) / 2 : 0;
            int i = 0;
            String[] keys = lineDefs.keySet().toArray(new String[0]);
            Arrays.sort(keys);
            int largestIndex = 0;
            int columnOffsetTotal = 0;
            for (String name : keys) {
                TalentLineDefinition lineDef = lineDefs.get(name);
                for (int talentIndex = 0; talentIndex < lineDef.getLength(); talentIndex++) {
                    TalentRecord record = current.getNodeRecord(name, talentIndex);
                    if (record == null) {
                        continue;
                    }
                    TalentRecord nextRecord = current.getNodeRecord(name, talentIndex + 1);
                    if (nextRecord != null) {
                        int lineColor = nextRecord.isKnown() ? 0x99ffffff : 0xff555555;
                        MKRectangle rect = new MKRectangle(
                                getX() + talentXOffset + spacePerColumn * i + columnOffsetTotal + TalentButton.SLOT_X_OFFSET
                                        + TalentButton.SLOT_WIDTH / 2 - 1,
                                getY() + talentIndex * talentButtonHeight + talentButtonYMargin
                                        + TalentButton.SLOT_Y_OFFSET + TalentButton.SLOT_HEIGHT / 2,
                                2, talentButtonHeight + talentButtonYMargin,
                                lineColor
                        );
                        addWidget(rect);
                    }
                    TalentButton button = new TalentButton(entityData, talentIndex, name, record,
                            getX() + talentXOffset + spacePerColumn * i + columnOffsetTotal,
                            getY() + talentIndex * talentButtonHeight + talentButtonYMargin
                    );
                    int cbIndex = talentIndex;
                    button.setPressedCallback((b, mouseButton) -> {
                        TalentPointActionPacket.Action action = mouseButton == UIConstants.MOUSE_BUTTON_RIGHT ?
                                TalentPointActionPacket.Action.REFUND :
                                TalentPointActionPacket.Action.SPEND;
                        PacketHandler.sendMessageToServer(new TalentPointActionPacket(treeId, name, cbIndex, action));
                        return true;
                    });
                    addWidget(button);
                    if (talentIndex > largestIndex) {
                        largestIndex = talentIndex;
                    }
                }
                i++;
                columnOffsetTotal += columnOffset;
            }
            setWidth(talentWidth);
            setWidth(Math.max(talentWidth, originalWidth));
            setHeight(Math.max((largestIndex + 1) * talentButtonHeight + talentButtonYMargin, originalHeight));
        }
    }

    private TalentTreeRecord getCurrent() {
        return recordSupplier.get();
    }

    public void refresh() {
        clearWidgets();
        setup();
    }
}
