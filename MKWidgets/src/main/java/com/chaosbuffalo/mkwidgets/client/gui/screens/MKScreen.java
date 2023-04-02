package com.chaosbuffalo.mkwidgets.client.gui.screens;

import com.chaosbuffalo.mkwidgets.MKWidgets;
import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.IInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class MKScreen extends Screen implements IMKScreen {
    public final ArrayDeque<IMKWidget> children;
    public static String NO_STATE = "NO_STATE";
    public boolean firstRender;
    private final Stack<String> stateStack;
    public HashMap<String, Supplier<IMKWidget>> states;
    private final HashMap<String, IMKWidget> stateCache;
    private final ArrayList<Runnable> postSetupCallbacks;
    private final ArrayList<Runnable> preDrawRunnables;
    private final ArrayList<IInstruction> postRenderInstructions;
    private final ArrayDeque<IMKModal> modals;
    private final ArrayDeque<Runnable> delayedTasks;
    private IDragState dragState;
    private IMKWidget dragSource;
    private IMKWidget focus;

    public MKScreen(Component title) {
        super(title);
        firstRender = true;
        children = new ArrayDeque<>();
        states = new HashMap<>();
        stateStack = new Stack<>();
        postSetupCallbacks = new ArrayList<>();
        preDrawRunnables = new ArrayList<>();
        stateCache = new HashMap<>();
        postRenderInstructions = new ArrayList<>();
        modals = new ArrayDeque<>();
        delayedTasks = new ArrayDeque<>();
        focus = null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        Iterator<IMKModal> modalIt = modals.descendingIterator();
        while (modalIt.hasNext()) {
            IMKModal child = modalIt.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mouseScrollWheel(this.minecraft, mouseX, mouseY, scrollAmount)) {
                return true;
            }
        }
        Iterator<IMKWidget> it = children.descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mouseScrollWheel(this.minecraft, mouseX, mouseY, scrollAmount)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void addPostRenderInstruction(IInstruction instruction) {
        postRenderInstructions.add(instruction);
    }

    @Override
    public void addModal(IMKModal modal) {
        modal.inheritScreen(this);
        modal.setWidth(width);
        modal.setHeight(height);
        this.modals.add(modal);
        clearHovers();
        setFocus(null);
    }

    @Override
    public void closeModal(IMKModal modal) {
        if (this.modals.removeIf((x) -> x.getId().equals(modal.getId()))) {
            if (modal.getOnCloseCallback() != null) {
                modal.getOnCloseCallback().run();
            }
            modal.inheritScreen(null);
            setFocus(null);
        }
    }

    @Override
    public IMKWidget getFocus() {
        return focus;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        MKWidgets.LOGGER.info("Resize event called?");
        for (IMKModal modal : modals){
            closeModal(modal);
        }
        flagNeedSetup();
    }

    @Override
    public void addPreDrawRunnable(Runnable runnable) {
        preDrawRunnables.add(runnable);
    }

    @Override
    public void removePreDrawRunnable(Runnable runnable) {
        preDrawRunnables.remove(runnable);
    }

    @Override
    public void clearPreDrawRunnables() {
        preDrawRunnables.clear();
    }

    public void clearHovers(){
        for (IMKModal modal : modals){
            modal.clearHovered();
        }
        for (IMKWidget child : children){
            child.clearHovered();
        }
    }

    @Override
    public void addRestoreStateCallbacks() {
        String state = popState();
        addPostSetupCallback(() -> pushState(state));
    }

    @Override
    public void addPostSetupCallback(Runnable callback) {
        postSetupCallbacks.add(callback);
    }

    @Override
    public void addState(String name, Supplier<IMKWidget> root) {
        this.states.put(name, root);
    }

    @Override
    public void removeState(String name) {
        this.states.remove(name);
    }

    @Override
    public void setDragState(IDragState dragState, IMKWidget source) {
        this.dragState = dragState;
        this.dragSource = source;
    }

    @Override
    public IMKWidget getDragSource() {
        return dragSource;
    }

    @Override
    public void clearDragState() {
        if (dragSource != null){
            this.dragSource.onDragEnd(dragState);
        }
        this.dragSource = null;
        this.dragState = null;
    }

    @Override
    public Optional<IDragState> getDragState() {
        return Optional.ofNullable(dragState);
    }

    @Override
    public void pushState(String newState) {
        if (newState.equals(getState())) {
            return;
        }
        if (handleStateTransition(newState, getState())){
            stateStack.push(newState);
        }
    }

    private IMKWidget getStateFromCache(String newState){
        return stateCache.computeIfAbsent(newState, (key) -> states.get(key).get());
    }

    private boolean handleStateTransition(String newState, String oldState){
        if (newState.equals(NO_STATE) || states.containsKey(newState)) {
            if (!oldState.equals(NO_STATE)) {
                this.removeWidget(getStateFromCache(oldState));
            }
            if (!newState.equals(NO_STATE)) {
                this.addWidget(getStateFromCache(newState));
            }
            return true;
        } else {
            MKWidgets.LOGGER.warn("Tried to set screen state to: {}, but state doesn't exist.", newState);
            return false;
        }
    }

    @Override
    public String popState() {
        String oldState;
        if (!stateStack.empty()){
            oldState = stateStack.pop();
            String newState = getState();
            handleStateTransition(newState, oldState);
            return oldState;
        } else {
            return NO_STATE;
        }
    }

    @Override
    public String getState() {
        if (stateStack.empty()){
            return NO_STATE;
        }
        return stateStack.peek();
    }

    @Override
    public void setFocus(@Nullable IMKWidget widget) {
        if (focus != null){
            focus.onFocusLost();
        }
        if (widget != null){
            widget.onFocus();
        }
        focus = widget;
    }

    private void runSetup() {
        setupScreen();
        for (Runnable cb : postSetupCallbacks) {
            cb.run();
        }
        postSetupCallbacks.clear();
    }

    public void setupScreen() {
        clearWidgets();
        clearPreDrawRunnables();
        this.states.clear();
        this.stateCache.clear();
    }

    public void flagNeedSetup() {
        firstRender = true;
    }

    @Override
    public void addWidget(IMKWidget widget) {
        widget.inheritScreen(this);
        this.children.add(widget);
    }

    @Override
    public void removeWidget(IMKWidget widget) {
        if (containsWidget(widget)) {
            if (children.removeIf((x) -> x.getId().equals(widget.getId()))) {
                widget.inheritScreen(null);
            }
        }
    }

    @Override
    public boolean containsWidget(IMKWidget widget) {
        for (IMKWidget child : children) {
            if (widget.getId().equals(child.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean onKeyPress(int keyCode, int scanCode, int modifiers){

        if (keyCode == GLFW.GLFW_KEY_TAB){
            List<IMKWidget> widgets = findFocusable();
            if (widgets.isEmpty()){
                return false;
            }
            int total = widgets.size();
            if (focus == null){
                setFocus(widgets.get(0));
            } else {
                int i = widgets.indexOf(focus);
                int next;
                if (hasShiftDown()){
                    next = i - 1;
                    if (next < 0){
                        next = total - 1;
                    }
                } else {
                    next = i + 1;
                    if (next >= total){
                        next = 0;
                    }
                }
                setFocus(widgets.get(next));
            }
            return true;
        }
        return false;
    }

    public List<IMKWidget> findFocusable(){
        List<IMKWidget> focusable = new ArrayList<>();
        for (IMKWidget child : children){
            child.findFocusable(focusable);
        }
        return focusable;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focus != null){
            boolean focusHandled = focus.keyPressed(this.minecraft, keyCode, scanCode, modifiers);
            if (!focusHandled && !onKeyPress(keyCode, scanCode, modifiers)){
                return super.keyPressed(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
        } else {
            return onKeyPress(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (firstRender) {
            if (!getState().equals(NO_STATE)){
                addRestoreStateCallbacks();
            }
            runSetup();
            firstRender = false;
        }
        for (Runnable runnable : preDrawRunnables) {
            runnable.run();
        }
        if (!modals.isEmpty()){
            for (IMKModal modal : modals){
                modal.mouseHover(this.minecraft, mouseX, mouseY, partialTicks);
            }
            for (IMKWidget child : children){
                child.clearHovered();
            }
        } else {
            for (IMKWidget child : children){
                child.mouseHover(this.minecraft, mouseX, mouseY, partialTicks);
            }
        }
        for (IMKWidget child : children) {
            if (child.isVisible()) {
                child.drawWidget(matrixStack, this.minecraft, mouseX, mouseY, partialTicks);
            }
        }
        for (IMKModal modal : modals) {
            if (modal.isVisible()) {
                modal.drawWidget(matrixStack, this.minecraft, mouseX, mouseY, partialTicks);
            }
        }
        if (dragState != null){
            dragState.updateDragState(minecraft, mouseX, mouseY, this);
        }
        for (IInstruction instruction : postRenderInstructions) {
            instruction.draw(matrixStack, getMinecraft().font, this.width, this.height, partialTicks, this);
        }
        postRenderInstructions.clear();
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton,
                                double dX, double dY) {
        Iterator<IMKModal> modalIt = modals.descendingIterator();
        while (modalIt.hasNext()) {
            IMKModal child = modalIt.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mouseDragged(this.minecraft, mouseX, mouseY, mouseButton, dX, dY)) {
                return true;
            }
        }
        Iterator<IMKWidget> it = children.descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mouseDragged(this.minecraft, mouseX, mouseY, mouseButton, dX, dY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dX, dY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        Iterator<IMKModal> modalIt = modals.descendingIterator();
        while (modalIt.hasNext()) {
            IMKModal child = modalIt.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mousePressed(this.minecraft, mouseX, mouseY, mouseButton)){
                return true;
            }
        }
        Iterator<IMKWidget> it = children.descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mousePressed(this.minecraft, mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        setFocus(null);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void clearWidgets() {
        for (IMKWidget widget : children) {
            widget.inheritScreen(null);
        }
        children.clear();
    }

    @Override
    public void clearModals() {
        for (IMKModal modal : modals) {
            modal.inheritScreen(null);
        }
        modals.clear();
    }

    @Override
    public void clear() {
        clearWidgets();
        clearModals();
        postRenderInstructions.clear();
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        Iterator<IMKModal> modalIt = modals.descendingIterator();
        while (modalIt.hasNext()) {
            IMKModal child = modalIt.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mouseReleased(mouseX, mouseY, mouseButton)){
                return true;
            }
        }
        Iterator<IMKWidget> it = children.descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (!child.isVisible()) {
                continue;
            }
            if (child.mouseReleased(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (focus != null){
            return focus.keyReleased(this.minecraft, keyCode, scanCode, modifiers);
        } else {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (focus != null){
            return focus.charTyped(this.minecraft, codePoint, modifiers);
        } else {
            return super.charTyped(codePoint, modifiers);
        }
    }

    public void scheduleNextTick(Runnable runnable){
        delayedTasks.add(runnable);
    }

    @Override
    public void tick() {
        super.tick();
        while (!delayedTasks.isEmpty()){
            delayedTasks.pop().run();
        }
    }
}
