package de.melanx.skyblockbuilder.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class BaseScreen extends Screen {

    private final int xSize;
    private final int ySize;
    private int relX;
    private int relY;

    protected BaseScreen(int xSize, int ySize, Component title) {
        super(title);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void init() {
        this.relX = (this.width - this.xSize) / 2;
        this.relY = (this.height - this.ySize) / 2;
    }

    protected final int getXSize() {
        return this.xSize;
    }

    protected final int getYSize() {
        return this.ySize;
    }

    protected final int x(int posX) {
        return this.relX + posX;
    }

    protected final int y(int posY) {
        return this.relY + posY;
    }

    protected final int centeredX(int widgetWidth) {
        return this.x(this.xSize / 2) - widgetWidth / 2;
    }

    protected final int centeredY(int widgetHeight) {
        return this.y(this.ySize / 2) - widgetHeight / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
