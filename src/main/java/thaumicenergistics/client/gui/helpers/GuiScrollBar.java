package thaumicenergistics.client.gui.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import thaumicenergistics.client.gui.GuiBase;

/**
 * @author BrockWS
 */
public class GuiScrollBar {

    private int x;
    private int y;
    private int height;
    private int rows = 6;

    private int minScroll = 0;
    private int maxScroll = 0;
    private int currentPosition = 0;

    public GuiScrollBar(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
    }

    public void draw(GuiBase gui) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png"));
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        if (this.getRange() == 0) { // We don't need to scroll
            gui.drawTexturedModalRect(this.x, this.y, 244, 0, 12, 15);
        } else {
            int offset = (this.currentPosition - this.minScroll) * (this.height - 15) / this.getRange();
            gui.drawTexturedModalRect(this.x, this.y + offset, 232, 0, 12, 15);
        }
    }

    public void wheel(float delta) {
        delta = Math.max(Math.min(-delta, 1), -1);
        this.currentPosition += delta * this.rows;
        this.lockRange();
    }

    public void click(int mouseY) {
        if (this.getRange() == 0)
            return;
        this.currentPosition = mouseY - this.getY();
        this.currentPosition = this.minScroll + ((this.currentPosition * 2 * this.getRange() / this.height));
        this.currentPosition = (this.currentPosition + 1) >> 1;
        this.lockRange();
    }

    private void lockRange() {
        this.currentPosition = Math.max(Math.min(this.currentPosition, this.maxScroll), this.minScroll);
    }

    public void setRange(int min, int max, int rows) {
        this.minScroll = min;
        this.maxScroll = max;
        this.rows = rows;

        if (this.minScroll > this.maxScroll)
            this.maxScroll = this.minScroll;

        this.lockRange();
    }

    public int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getHeight() {
        return this.height;
    }

    public float getCurrentPosition() {
        return this.currentPosition;
    }

    public int getRows() {
        return this.rows;
    }
}
