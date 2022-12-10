package thaumicenergistics.client.gui.widget;

import appeng.core.AEConfig;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IAspectSelectorGui;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.client.gui.ThEGuiHelper;
import thaumicenergistics.common.utils.ThEUtils;

/**
 * Widget displaying an aspect and if it is selected.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class WidgetAspectSelector extends AspectWidgetBase {
    /**
     * Thickness of the selector outline.
     */
    private static final int borderThickness = 1;

    /**
     * The number of iterations in the gradient
     */
    private static final int GRADIENT_COUNT = 15;

    /**
     * Color of the border while aspect is selected
     */
    private static final int selectorBorderColor = 0xFF00FFFF;

    /**
     * Array of colors that pulse behind the aspect
     */
    private int[] backgroundPulseGradient;

    /**
     * If true the amount will not be drawn, also if the stack is craftable
     * it will show the crafting text.
     */
    protected boolean hideAmount = false;

    public WidgetAspectSelector(
            final IAspectSelectorGui selectorGui,
            final IAspectStack stack,
            final int xPos,
            final int yPos,
            final EntityPlayer player) {
        // Call super
        super(selectorGui, stack, xPos, yPos, player);
    }

    /**
     * Draws the selector outline.
     *
     * @param posX
     * @param posY
     * @param width
     * @param height
     * @param color
     * @param thickness
     */
    private void drawHollowRectWithCorners(
            final int posX, final int posY, final int width, final int height, final int color, final int thickness) {
        // Calculate points

        // Ending X point of the right line
        int rightXEnd = posX + width;

        // Beginning X point of the right line
        int rightXBegin = rightXEnd - thickness;

        // Ending X point of the left line
        int leftXEnd = posX + thickness;

        // Ending Y point of the top line
        int topYEnd = posY + thickness;

        // Ending Y point of the bottom line
        int bottomYEnd = posY + height;

        // Beginning Y point of the bottom line
        int bottomYBegin = bottomYEnd - thickness;

        // Draw background gradient
        Gui.drawRect(posX, posY, rightXEnd, bottomYEnd, color);

        // Draw notches

        // Top-left notch
        Gui.drawRect(posX, posY, leftXEnd + 1, topYEnd + 1, selectorBorderColor);

        // Top-right notch
        Gui.drawRect(rightXEnd, posY, rightXBegin - 1, topYEnd + 1, selectorBorderColor);

        // Bottom-right notch
        Gui.drawRect(rightXEnd, bottomYEnd, rightXBegin - 1, bottomYBegin - 1, selectorBorderColor);

        // Bottom-left notch
        Gui.drawRect(posX, bottomYEnd, leftXEnd + 1, bottomYBegin - 1, selectorBorderColor);

        // Draw lines

        // Top side
        Gui.drawRect(posX, posY, rightXEnd, topYEnd, selectorBorderColor);

        // Bottom side
        Gui.drawRect(posX, bottomYBegin, rightXEnd, bottomYEnd, selectorBorderColor);

        // Left side
        Gui.drawRect(posX, posY, leftXEnd, bottomYEnd, selectorBorderColor);

        // Right side
        Gui.drawRect(rightXBegin, posY, rightXEnd, bottomYEnd, selectorBorderColor);
    }

    /**
     * Gets the background gradient color based on the current time.
     *
     * @return
     */
    private int getBackgroundColor() {
        // Get the current time, slowed down.
        int time = (int) (System.currentTimeMillis() / 45L);

        // Lerp the index
        int index = Math.abs(Math.abs(time % (GRADIENT_COUNT * 2)) - GRADIENT_COUNT);

        // Return the index
        return this.backgroundPulseGradient[index];
    }

    @Override
    protected void onStackChanged() {
        // Call super
        super.onStackChanged();

        if (!this.hasAspect()) {
            return;
        }

        // Get the aspect color
        int aspectColor = this.getStack().getAspect().getColor();

        // Create the gradient using the aspect color, varying between opacities
        this.backgroundPulseGradient = ThEGuiHelper.INSTANCE.createColorGradient(
                0x70000000 | aspectColor, 0x20000000 | aspectColor, GRADIENT_COUNT + 1);
    }

    /**
     * Draws the aspect icon and selector border if it is selected.
     */
    @Override
    public void drawWidget() {
        // Is the widget empty?
        if (!this.hasAspect()) {
            return;
        }
        // Disable lighting
        GL11.glDisable(GL11.GL_LIGHTING);

        // Enable blending
        GL11.glEnable(GL11.GL_BLEND);

        // Set the blending mode to blend alpha
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // No tint
        GL11.glColor3f(1.0F, 1.0F, 1.0F);

        // Get the selected aspect
        Aspect selectedAspect = ((IAspectSelectorGui) this.hostGUI).getSelectedAspect();

        // Does the selected aspect match the widgets?
        if (selectedAspect == this.getAspect()) {
            // Draw the selection box
            this.drawHollowRectWithCorners(
                    this.xPosition,
                    this.yPosition,
                    ThEWidget.WIDGET_SIZE,
                    ThEWidget.WIDGET_SIZE,
                    this.getBackgroundColor(),
                    WidgetAspectSelector.borderThickness);
        }

        // Draw the aspect
        this.drawAspect();

        // Get the amount and crafting
        long stackSize = this.getAmount();

        // Is there anything to draw?
        if (((stackSize > 0) && !this.hideAmount) || this.getCraftable()) {
            // Text to draw
            String text;

            // Get the font renderer
            final FontRenderer fontRenderer = this.hostGUI.getFontRenderer();

            // Disable unicode if enabled
            final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
            fontRenderer.setUnicodeFlag(false);

            // Is the terminal using a large font?
            boolean largeFont = AEConfig.instance.useTerminalUseLargeFont();

            // Set the scale
            float scale = largeFont ? 0.85f : 0.5f;

            // Set the position offset
            float positionOffset = largeFont ? 0 : -1.0f;

            // Calculate position
            float posX = this.xPosition + positionOffset + ThEWidget.WIDGET_SIZE;
            float posY = (this.yPosition + positionOffset + ThEWidget.WIDGET_SIZE) - 1;

            // Has amount to draw?
            if (((stackSize > 0) && !this.hideAmount)) {
                if (largeFont) {
                    text = ReadableNumberConverter.INSTANCE.toWideReadableForm(stackSize);
                } else {
                    text = ReadableNumberConverter.INSTANCE.toSlimReadableForm(stackSize);
                }
            }
            // Crafting
            else {
                // Get the "craftable" text from AE2
                if (largeFont) {
                    text = appeng.core.localization.GuiText.LargeFontCraft.getLocal();
                } else {
                    text = appeng.core.localization.GuiText.SmallFontCraft.getLocal();
                }
            }

            // Draw it
            ThEGuiHelper.drawScaledText(fontRenderer, text, scale, posX, posY);

            // Reset unicode status
            fontRenderer.setUnicodeFlag(unicodeFlag);
        }

        // Enable lighting
        GL11.glEnable(GL11.GL_LIGHTING);

        // Disable blending
        GL11.glDisable(GL11.GL_BLEND);
    }

    /**
     * Called when the widget is clicked.
     */
    @Override
    public void onMouseClicked() {
        // Get this aspect
        Aspect widgetAspect = this.getAspect();

        // Get the selected aspect
        Aspect selectedAspect = ((IAspectSelectorGui) this.hostGUI).getSelectedAspect();

        // Did the selected aspect change?
        boolean changed = false;

        // Are both aspects the same?
        if (widgetAspect == selectedAspect) {
            // Is something selected?
            if (selectedAspect != null) {
                // Unselect
                selectedAspect = null;
                changed = true;
            }
        } else {
            // Set to the widget aspect
            selectedAspect = widgetAspect;
            changed = true;
        }

        // Was the selected aspect changed?
        if (changed) {
            // Play clicky sound
            ThEUtils.playClientSound(null, "gui.button.press");

            // Set the selected aspect
            ((IAspectSelectorGui) this.hostGUI).getContainer().setSelectedAspect(selectedAspect);
        }
    }

    /**
     * Sets whether or not to show the amount.
     *
     * @param hide
     * @return
     */
    public void setHideAmount(final boolean hide) {
        this.hideAmount = hide;
    }
}
