package thaumicenergistics.client.gui.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import thaumicenergistics.api.ThEApi;

import java.awt.Color;

/**
 * @author Alex811
 */
public class GuiSearchField extends GuiTextField {
    private static final String ASPECT_SEARCH_PREFIX = ThEApi.instance().config().aspectSearchPrefix();
    private static final String MOD_SEARCH_PREFIX = ThEApi.instance().config().modSearchPrefix();
    protected int enabledTextColor = 0xFFE0E0E0;
    protected int disabledTextColor = 0xFF707070;
    protected int aspectSearchTextColor = 0xFFE6B3FF;
    protected int modSearchTextColor = 0xFFFFD699;
    protected int focusedBGColor = 0xFF606060;
    protected int normalBGColor = 0xFFA8A8A8;
    protected int cursorColor = 0xAA009933;
    protected int selectedTextColor = 0xFF000080;
    protected Color selectionColor;
    protected final FontRenderer fontRenderer;
    protected int lineScrollOffset;
    protected boolean doBGDrawing = true;
    protected boolean isEnabled = true;
    protected int cursorCounter = 0;

    public GuiSearchField(FontRenderer fontRenderer, int x, int y, int w, int h) {
        super(0, fontRenderer, x, y, w, h);
        super.setEnableBackgroundDrawing(false);
        this.fontRenderer = fontRenderer;
        this.setSelectionColor(0xFF9f9f9f);
    }

    @Override
    public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn) {
        this.doBGDrawing = enableBackgroundDrawingIn;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.isEnabled = enabled;
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        this.enabledTextColor = color;
    }

    @Override
    public void setDisabledTextColour(int color) {
        super.setDisabledTextColour(color);
        this.disabledTextColor = color;
    }

    public void setAspectSearchTextColor(int aspectSearchTextColor) {
        this.aspectSearchTextColor = aspectSearchTextColor;
    }

    public void setModSearchTextColor(int modSearchTextColor) {
        this.modSearchTextColor = modSearchTextColor;
    }

    public void setFocusedBGColor(int focusedBGColor) {
        this.focusedBGColor = focusedBGColor;
    }

    public void setNormalBGColor(int normalBGColor) {
        this.normalBGColor = normalBGColor;
    }

    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
    }

    public void setSelectionColor(int selectionColor) {
        this.selectionColor = new Color(selectionColor, true);
    }

    public void selectAll() {
        this.setCursorPosition(0);
        this.setSelectionPos(this.getMaxStringLength());
    }

    public void selectNone() {
        this.setSelectionPos(this.getCursorPosition());
    }

    @Override
    public void setSelectionPos(int position) { // Just keeps our lineScrollOffset updated, to avoid using reflection instead
        super.setSelectionPos(position);
        if(fontRenderer == null) return;
        int len = this.getText().length();
        position = MathHelper.clamp(position, 0, len);
        this.lineScrollOffset = Math.min(this.lineScrollOffset, len);
        int trimmedStrLen = this.fontRenderer.trimStringToWidth(this.getText().substring(this.lineScrollOffset), this.width).length() + this.lineScrollOffset;
        if(position == this.lineScrollOffset)
            this.lineScrollOffset -= this.fontRenderer.trimStringToWidth(this.getText(), this.width, true).length();
        if(position > trimmedStrLen)
            this.lineScrollOffset += position - trimmedStrLen;
        else if(position <= this.lineScrollOffset)
            this.lineScrollOffset -= this.lineScrollOffset - position;
        this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, len);
    }

    @Override
    public void setFocused(boolean isFocusedIn) {
        super.setFocused(isFocusedIn);
        if(isFocusedIn && !this.isFocused())
            this.cursorCounter = 0;
    }

    @Override
    public void updateCursorCounter() {
        super.updateCursorCounter();
        this.cursorCounter++;
    }

    @Override
    public void drawTextBox() {
        if(this.getVisible()){
            if(doBGDrawing) {
                if(this.isFocused())
                    drawRect(this.x - 1, this.y - 1, this.x + this.width, this.y + this.height - 1, focusedBGColor);
                else
                    drawRect(this.x - 1, this.y - 1, this.x + this.width, this.y + this.height - 1, normalBGColor);
            }

            String visibleText = this.fontRenderer.trimStringToWidth(this.getText().substring(this.lineScrollOffset), this.width);
            drawText(visibleText);
            if(this.isFocused()) {
                this.drawSelectionBox(visibleText);
                this.drawCursor(visibleText);
            }
        }
    }

    protected int getTextColor(){
        if(!this.isEnabled) return this.disabledTextColor;
        if(this.getText().startsWith(ASPECT_SEARCH_PREFIX)) return this.aspectSearchTextColor;
        if(this.getText().startsWith(MOD_SEARCH_PREFIX)) return this.modSearchTextColor;
        return this.enabledTextColor;
    }

    protected void drawText(String visibleText){
        if (!visibleText.isEmpty())
            this.fontRenderer.drawStringWithShadow(visibleText, (float) this.x, (float) this.y, getTextColor());
    }

    protected void drawCursor(String visibleText){
        int curPos = this.getCursorPosition() - this.lineScrollOffset;
        boolean curInVisBounds = curPos >= 0 && curPos <= visibleText.length();
        boolean drawCursor = this.cursorCounter / 6 % 2 == 0 && curInVisBounds;
        boolean isAtEnd = this.getCursorPosition() >= this.getText().length() && this.getText().length() < this.getMaxStringLength();
        if (drawCursor){
            int curX = this.x + this.fontRenderer.getStringWidth(visibleText.substring(0, curPos));
            if (isAtEnd)
                this.fontRenderer.drawStringWithShadow("_", (float) curX, (float) this.y, cursorColor);
            else
                Gui.drawRect(curX, this.y - 1, curX + 1, this.y + this.fontRenderer.FONT_HEIGHT, cursorColor);
        }
    }

    protected void drawSelectionBox(String visibleText){
        if(this.getCursorPosition() == this.getSelectionEnd()) return;
        int selStartIndex = Math.min(this.getCursorPosition(), this.getSelectionEnd()) - this.lineScrollOffset;
        int selEndIndex = Math.max(this.getCursorPosition(), this.getSelectionEnd()) - this.lineScrollOffset;

        if(selStartIndex < 0)
            selStartIndex = 0;
        if(selEndIndex > visibleText.length())
            selEndIndex = visibleText.length();  // note: used for substring, so exclusive

        String unselectedStart = visibleText.substring(0, selStartIndex);
        String selectedText = visibleText.substring(unselectedStart.length(), selEndIndex);
        int startX = this.x + this.fontRenderer.getStringWidth(unselectedStart);
        int startY = this.y - 1;
        int endX = startX + this.fontRenderer.getStringWidth(selectedText);
        int endY = startY + this.fontRenderer.FONT_HEIGHT;

        this.drawSelectionBox(startX, startY, endX, endY);
        this.fontRenderer.drawString(selectedText, startX, this.y, selectedTextColor);
    }

    protected void drawSelectionBox(int startX, int startY, int endX, int endY) {
        if(startX == endX || startY == endY) return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(selectionColor.getRed() / 255.0F, selectionColor.getGreen() / 255.0F, selectionColor.getBlue() / 255.0F, selectionColor.getAlpha() / 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.AND_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }
}
