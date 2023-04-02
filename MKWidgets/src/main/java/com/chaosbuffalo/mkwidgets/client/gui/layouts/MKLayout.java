package com.chaosbuffalo.mkwidgets.client.gui.layouts;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.IConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MKLayout extends MKWidget implements IMKLayout {
    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBot;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBot;
    private boolean needsRecompute;
    private HashMap<UUID, ArrayList<IConstraint>> constraints;

    public MKLayout(int x, int y, int width, int height){
        super(x, y, width, height);
        constraints = new HashMap<>();
        needsRecompute = false;
    }

    public void flagNeedsRecompute(){
        needsRecompute = true;
    }

    public void computeChildLayouts(){
        for (IMKWidget child : getChildren()){
            if (child instanceof MKLayout){
                if (((MKLayout) child).needsRecompute){
                    ((MKLayout) child).manualRecompute();
                }
            }
        }
    }

    public void recomputeChildren() {
        computeChildLayouts();
        preLayout();
        int i = 0;
        for (IMKWidget child : getChildren()) {
            layoutWidget(child, i);
            i++;
        }
        postLayout();
    }

    @Override
    public void addConstraintToWidget(IConstraint constraint, IMKWidget widget) {
        ArrayList<IConstraint> widgetConstraints =  constraints.computeIfAbsent(widget.getId(),
                (id) -> new ArrayList<>());
        widgetConstraints.add(constraint);
        flagNeedsRecompute();
    }

    @Override
    public void removeConstraintFromWidget(IConstraint constraint, IMKWidget widget) {
        if (constraints.containsKey(widget.getId())){
            ArrayList<IConstraint> widgetConstraints = constraints.get(widget.getId());
            widgetConstraints.removeIf((con) -> con.getConstraintID().equals(constraint.getConstraintID()));
            flagNeedsRecompute();
        }
    }

    public void applyConstraints(IMKWidget widget, int widgetIndex){
        if (constraints.containsKey(widget.getId())){
            ArrayList<IConstraint> widgetConstraints = constraints.get(widget.getId());
            for (IConstraint constraint : widgetConstraints){
                constraint.applyConstraint(this, widget, widgetIndex);
            }
        }

    }

    @Override
    public boolean addWidget(IMKWidget widget) {
        super.addWidget(widget);
        flagNeedsRecompute();
        return true;
    }


    @Override
    public void clearWidgets() {
        super.clearWidgets();
        flagNeedsRecompute();
    }


    @Override
    public IMKWidget setWidth(int newWidth) {
        super.setWidth(newWidth);
        flagNeedsRecompute();
        return this;
    }

    @Override
    public IMKWidget setHeight(int newHeight) {
        super.setHeight(newHeight);
        flagNeedsRecompute();
        return this;
    }

    @Override
    public IMKWidget setX(int newX) {
        super.setX(newX);
        flagNeedsRecompute();
        return this;
    }

    @Override
    public IMKWidget setY(int newY) {
        super.setY(newY);
        flagNeedsRecompute();
        return this;
    }


    @Override
    public void removeWidget(IMKWidget widget) {
        super.removeWidget(widget);
        clearWidgetConstraints(widget);
        flagNeedsRecompute();
    }

    @Override
    public void clearWidgetConstraints(IMKWidget widget) {
        if (constraints.containsKey(widget.getId())){
            constraints.remove(widget.getId());
            flagNeedsRecompute();
        }
    }

    public void manualRecompute(){
        recomputeChildren();
        needsRecompute = false;
    }

    @Override
    public void drawWidget(PoseStack matrixStack, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (needsRecompute){
            recomputeChildren();
            needsRecompute = false;
        }
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (doDrawDebugBounds()){
            drawDebugBounds(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        }
        preDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        draw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        int i = 0;
        for (IMKWidget child : getChildren()) {
            if (child.isVisible()) {
                child.drawWidget(matrixStack, mc, mouseX, mouseY, partialTicks);
            }
            i++;
        }
        postDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        handleLongHoverDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
    }


    @Override
    public IMKLayout setMarginTop(int value) {
        marginTop = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getMarginTop() {
        return marginTop;
    }

    @Override
    public IMKLayout setMarginBot(int value) {
        marginBot = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public void layoutWidget(IMKWidget widget, int index) {
        applyConstraints(widget, index);
        postLayoutWidget(widget, index);
    }

    public void postLayoutWidget(IMKWidget widget, int index){

    }

    protected void skipComputeSetWidth(int newWidth){
        super.setWidth(newWidth);
    }

    protected void skipComputeSetHeight(int newHeight){
        super.setHeight(newHeight);
    }

    @Override
    public int getMarginBot() {
        return marginBot;
    }

    @Override
    public IMKLayout setMarginLeft(int value) {
        marginLeft = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getMarginLeft() {
        return marginLeft;
    }

    @Override
    public IMKLayout setMarginRight(int value) {
        marginRight = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getMarginRight() {
        return marginRight;
    }

    @Override
    public IMKLayout setPaddingTop(int value) {
        paddingTop = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

    @Override
    public IMKLayout setPaddingBot(int value) {
        paddingBot = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingBot() {
        return paddingBot;
    }

    @Override
    public IMKLayout setPaddingLeft(int value) {
        paddingLeft = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingLeft() {
        return paddingLeft;
    }

    @Override
    public IMKLayout setPaddingRight(int value) {
        paddingRight = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }
}
