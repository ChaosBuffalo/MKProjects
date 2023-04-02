package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.widgets.AbilitySlotWidget;
import com.chaosbuffalo.mkcore.client.gui.widgets.CycleButton;
import com.chaosbuffalo.mkcore.client.gui.widgets.IconText;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class PersonalAbilityPage extends AbilityPageBase implements IAbilityScreen {

    public static class AbilitySlotKey {
        public AbilityGroupId group;
        public int slot;

        public AbilitySlotKey(AbilityGroupId group, int index) {
            this.group = group;
            this.slot = index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, group);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AbilitySlotKey) {
                AbilitySlotKey otherKey = (AbilitySlotKey) other;
                return slot == otherKey.slot && group.equals(otherKey.group);
            }
            return false;
        }
    }

    public enum AbilityFilter {
        All(new TextComponent("All"), EnumSet.allOf(AbilityType.class)),
        Basic(new TranslatableComponent("mkcore.gui.actives"), EnumSet.of(AbilityType.Basic)),
        Passive(new TranslatableComponent("mkcore.gui.passives"), EnumSet.of(AbilityType.Passive)),
        Ultimate(new TranslatableComponent("mkcore.gui.ultimates"), EnumSet.of(AbilityType.Ultimate));

        private final Component name;
        private final EnumSet<AbilityType> accepting;

        AbilityFilter(Component name, EnumSet<AbilityType> accepting) {
            this.name = name;
            this.accepting = accepting;
        }

        public boolean accepts(AbilityType t) {
            return accepting.contains(t);
        }

        public Component getName() {
            return name;
        }
    }

    private final Map<AbilitySlotKey, AbilitySlotWidget> abilitySlots = new HashMap<>();
    private final List<AbilityFilter> availableFilters = new ArrayList<>();
    private AbilityFilter currentFilter = AbilityFilter.All;

    public PersonalAbilityPage(MKPlayerData playerData) {
        super(playerData, new TranslatableComponent("mk_character_screen.title"));
    }

    @Override
    public ResourceLocation getPageId() {
        return MKCore.makeRL("abilities");
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createAbilitiesPage());
    }

    private MKWidget createAbilitiesPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(getDataBoxTexture());
        if (dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(getDataBoxTexture(), GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, true);

        // Stat Panel
        addSlotGroupWidgets(xPos, yPos, xOffset, root);

        int contentX = xPos + xOffset;
        int contentY = yPos + DATA_BOX_OFFSET;
        int contentWidth = dataBoxRegion.width;
        int contentHeight = dataBoxRegion.height;
        abilitiesScrollPanel = getAbilityScrollPanel(contentX, contentY, contentWidth, contentHeight);
        root.addWidget(abilitiesScrollPanel);

        MKLayout footer = createPoolManagementFooter();
        root.addWidget(footer);
        return root;
    }

    private void addSlotGroupWidgets(int xPos, int yPos, int xOffset, MKLayout root) {
        int slotsY = yPos + DATA_BOX_OFFSET - 28;
        int slotsX = xPos + xOffset + 4;
        MKText activesLabel = new MKText(font, new TranslatableComponent("mkcore.gui.actives"));
        activesLabel.setX(slotsX);
        activesLabel.setY(slotsY - 12);
        root.addWidget(activesLabel);
        MKLayout regularSlots = createAbilityGroupLayout(slotsX, slotsY, AbilityGroupId.Basic);
        root.addWidget(regularSlots);
        regularSlots.manualRecompute();

        int ultSlotsX = regularSlots.getX() + regularSlots.getWidth() + 30;
        MKLayout ultSlots = createAbilityGroupLayout(ultSlotsX, slotsY, AbilityGroupId.Ultimate);
        root.addWidget(ultSlots);
        ultSlots.manualRecompute();
        MKText ultLabel = new MKText(font, new TranslatableComponent("mkcore.gui.ultimates"));
        ultLabel.setX(ultSlotsX);
        ultLabel.setY(slotsY - 12);
        root.addWidget(ultLabel);

        int passiveSlotX = ultSlots.getX() + ultSlots.getWidth() + 30;
        MKLayout passiveSlots = createAbilityGroupLayout(passiveSlotX, slotsY, AbilityGroupId.Passive);
        MKText passivesLabel = new MKText(font, new TranslatableComponent("mkcore.gui.passives"));
        passivesLabel.setX(passiveSlotX);
        passivesLabel.setY(slotsY - 12);
        root.addWidget(passivesLabel);
        root.addWidget(passiveSlots);
    }

    @Override
    protected Collection<MKAbility> getSortedAbilityList() {
        return currentAbilityList().stream()
                .sorted(Comparator.comparing(a -> a.getAbilityName().getString()))
                .collect(Collectors.toList());
    }

    private List<MKAbility> currentAbilityList() {
        availableFilters.clear();
        availableFilters.add(AbilityFilter.All);

        Set<AbilityType> knownTypes = new HashSet<>();
        List<MKAbility> knownAbilities = playerData.getAbilities()
                .getKnownStream()
                .map(info -> {
                    knownTypes.add(info.getAbility().getType());
                    return info.getAbility();
                }).collect(Collectors.toList());
        MKAbilityInfo itemAbility = playerData.getLoadout().getAbilityGroup(AbilityGroupId.Item).getAbilityInfo(0);
        if (itemAbility != null) {
            knownAbilities.add(itemAbility.getAbility());
            knownTypes.add(itemAbility.getAbility().getType());
        }
        if (knownTypes.contains(AbilityType.Basic))
            availableFilters.add(AbilityFilter.Basic);
        if (knownTypes.contains(AbilityType.Passive))
            availableFilters.add(AbilityFilter.Passive);
        if (knownTypes.contains(AbilityType.Ultimate))
            availableFilters.add(AbilityFilter.Ultimate);

        if (!availableFilters.contains(currentFilter)) {
            currentFilter = AbilityFilter.All;
        }

        return knownAbilities.stream()
                .filter(ability -> currentFilter.accepts(ability.getType()))
                .collect(Collectors.toList());
    }

    private CycleButton<AbilityFilter> createFilterButton() {
        CycleButton<AbilityFilter> button = new CycleButton<>(
                availableFilters,
                f -> new TextComponent("Filter: ").append(f.getName()),
                f -> {
                    currentFilter = f;
                    if (getSelectedAbility() != null && !currentFilter.accepts(getSelectedAbility().getType())) {
                        setSelectedAbility(null);
                    }
                    abilitiesScrollPanel.getListScrollView().resetView();
                    flagNeedSetup();
                });
        button.setCurrent(currentFilter);
        return button;
    }

    private MKLayout createPoolManagementFooter() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(getDataBoxTexture(), GuiTextures.BACKGROUND_320_240);
        int yStart = yPos + DATA_BOX_OFFSET + 136;
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(xPos + xOffset, yStart, 20);
        layout.setPaddingLeft(16);
        layout.setPaddingRight(16);
        layout.setMarginLeft(0);

        MKButton filterButton = createFilterButton();
        filterButton.setWidth(84);
        layout.addWidget(filterButton);

        IconText poolText = createPoolUsageText();
        layout.addWidget(poolText, new OffsetConstraint(0, 2, false, true));

        MKButton manage = createManageButton();
        layout.addWidget(manage);
        return layout;

    }

    private MKLayout createAbilityGroupLayout(int x, int y, AbilityGroupId group) {
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(x, y, 24);
        layout.setPaddings(2, 2, 0, 0);
        layout.setMargins(2, 2, 2, 2);
        for (int i = 0; i < group.getMaxSlots(); i++) {
            AbilitySlotWidget slot = new AbilitySlotWidget(0, 0, group, i, this);
            abilitySlots.put(new AbilitySlotKey(slot.getSlotGroup(), slot.getSlotIndex()), slot);
            layout.addWidget(slot);
        }
        return layout;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean handled = super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isDraggingAbility()) {
            stopDraggingAbility();
            clearDragState();
            return true;
        }
        return handled;
    }

    @Override
    public boolean allowsDraggingAbilities() {
        return true;
    }

    @Override
    public void stopDraggingAbility() {
        for (AbilitySlotWidget widget : abilitySlots.values()) {
            widget.setBackgroundColor(0xffffffff);
            widget.setIconColor(0xffffffff);
        }
        super.stopDraggingAbility();
    }

    @Override
    public void startDraggingAbility(MKAbility dragging) {
        super.startDraggingAbility(dragging);
        abilitySlots.forEach((key, widget) -> {
            if (!key.group.fitsAbilityType(dragging.getType())) {
                widget.setBackgroundColor(0xff555555);
                widget.setIconColor(0xff555555);
            }
        });
    }
}
