package com.chaosbuffalo.mknpc.client.gui.screens;

import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageBase;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.client.gui.widgets.QuestListEntry;
import com.chaosbuffalo.mknpc.client.gui.widgets.QuestPanel;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.InterModComms;


public class QuestPage extends PlayerPageBase {
    public static final ResourceLocation PAGE_ID = new ResourceLocation(MKNpc.MODID, "quests");
    private QuestPanel questPanel;
    private ScrollingListPanelLayout currentScrollingPanel;
    private PlayerQuestChainInstance currentQuest;

    public QuestPage(MKPlayerData playerData) {
        super(playerData, new TextComponent("Quest Log"));
    }

    @Override
    public ResourceLocation getPageId() {
        return PAGE_ID;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createQuestsPage());
    }

    private MKWidget createQuestsPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(
                GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, true);
        minecraft.player.getCapability(NpcCapabilities.PLAYER_QUEST_DATA_CAPABILITY).ifPresent((pData) -> {
            int contentX = xPos + xOffset;
            int contentY = yPos + DATA_BOX_OFFSET;
            int contentWidth = dataBoxRegion.width;
            int contentHeight = dataBoxRegion.height;
            ScrollingListPanelLayout panel = new ScrollingListPanelLayout(
                    contentX, contentY, contentWidth, contentHeight);
            currentScrollingPanel = panel;
            QuestPanel questPanel = new QuestPanel(0, 0, panel.getContentScrollView().getWidth(),
                    panel.getContentScrollView().getWidth(), pData, font);
            this.questPanel = questPanel;
            panel.setContent(questPanel);
            MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0,
                    panel.getListScrollView().getWidth());
            stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                    .setMarginRight(4).setPaddingBot(2).setPaddingRight(2);
            stackLayout.doSetChildWidth(true);
            pData.getQuestChains().forEach(questChain -> {
                if (!questChain.isQuestComplete()){
                    QuestListEntry questEntry = new QuestListEntry(0, 0, 16, font, questChain, this);
                    stackLayout.addWidget(questEntry);
                    MKRectangle div = new MKRectangle(0, 0,
                            panel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                    stackLayout.addWidget(div);
                }
            });
            panel.setList(stackLayout);
            root.addWidget(panel);
        });
        return root;
    }

    public PlayerQuestChainInstance getCurrentQuest() {
        return currentQuest;
    }

    public void setCurrentQuest(PlayerQuestChainInstance currentQuest) {
        this.currentQuest = currentQuest;
        if (questPanel != null){
            questPanel.setCurrentChain(currentQuest);
        }
    }

    @Override
    protected void persistState(boolean wasResized) {
        final PlayerQuestChainInstance currentQuest = getCurrentQuest();
        addPostSetupCallback(() -> {
            if (questPanel != null) {
                questPanel.setCurrentChain(currentQuest);
            }
        });
        persistScrollingListPanelState(() -> currentScrollingPanel, wasResized);
    }

    static class PageFactory implements PlayerPageRegistry.Extension {

        @Override
        public ResourceLocation getId() {
            return PAGE_ID;
        }

        @Override
        public Component getDisplayName() {
            return new TranslatableComponent("mknpc.gui.page.quests.name");
        }

        @Override
        public MKScreen createPage(MKPlayerData playerData) {
            return new QuestPage(playerData);
        }
    }

    public static void registerPlayerPage() {
        PlayerPageRegistry.ExtensionProvider provider = PageFactory::new;
        InterModComms.sendTo("mkcore", "register_player_page", () -> {
            MKFactionMod.LOGGER.info("Faction register player page");
            return provider;
        });
    }
}
