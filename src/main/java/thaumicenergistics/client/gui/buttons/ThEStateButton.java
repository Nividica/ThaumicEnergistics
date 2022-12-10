package thaumicenergistics.client.gui.buttons;

import com.google.common.base.Splitter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.client.textures.IStateIconTexture;

/**
 * Base class for ThE state buttons.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public abstract class ThEStateButton extends ThEGuiButtonBase {
    /**
     * Button background
     */
    private IStateIconTexture backgroundIcon;

    /**
     * Offset from the top-left corner of the button to draw the icon.
     */
    private int iconXOffset = 0;

    /**
     * Offset from the top-left corner of the button to draw the icon.
     */
    private int iconYOffset = 0;

    /**
     * Icon to draw on the button
     */
    protected IStateIconTexture stateIcon;

    /**
     *
     * @param ID
     * @param xPosition
     * @param yPosition
     * @param buttonWidth
     * @param buttonHeight
     * @param icon
     * @param iconXOffset
     * @param iconYOffset
     * @param backgroundIcon
     */
    public ThEStateButton(
            final int ID,
            final int xPosition,
            final int yPosition,
            final int buttonWidth,
            final int buttonHeight,
            final IStateIconTexture icon,
            final int iconXOffset,
            final int iconYOffset,
            final IStateIconTexture backgroundIcon) {
        // Call super
        super(ID, xPosition, yPosition, buttonWidth, buttonHeight, "");

        // Set the icon
        this.stateIcon = icon;

        // Set the offsets
        this.iconXOffset = iconXOffset;
        this.iconYOffset = iconYOffset;

        // Set background
        this.backgroundIcon = backgroundIcon;
    }

    /**
     * Draws a textured rectangle at the stored z-value, the texture will
     * be scaled to fit within the width and height
     */
    private void drawScaledTexturedModalRect(
            final int xPosition,
            final int yPosition,
            final int u,
            final int v,
            final int width,
            final int height,
            final int textureWidth,
            final int textureHeight) {
        // No idea what this is
        float magic_number = 0.00390625F;

        // Calculate the UV's
        float minU = u * magic_number;
        float maxU = (u + textureWidth) * magic_number;
        float minV = v * magic_number;
        float maxV = (v + textureHeight) * magic_number;
        double dHeight = height;
        double dWidth = width;

        // Get the tessellator
        Tessellator tessellator = Tessellator.instance;

        // Start drawing
        tessellator.startDrawingQuads();

        // Top left corner
        tessellator.addVertexWithUV(xPosition, yPosition + dHeight, this.zLevel, minU, maxV);

        // Top right corner
        tessellator.addVertexWithUV(xPosition + dWidth, yPosition + dHeight, this.zLevel, maxU, maxV);

        // Bottom right corner
        tessellator.addVertexWithUV(xPosition + dWidth, yPosition, this.zLevel, maxU, minV);

        // Bottom left corner
        tessellator.addVertexWithUV(xPosition, yPosition, this.zLevel, minU, minV);

        // Draw
        tessellator.draw();
    }

    /**
     * Adds info to the tooltip as a white header, and grey body.
     * The body is broken down into lines of length 30.
     *
     * @param tooltip
     * @param title
     * @param text
     */
    protected void addAboutToTooltip(final List<String> tooltip, final String title, final String text) {
        // Title
        tooltip.add(EnumChatFormatting.WHITE + title);

        // Body
        for (String line : Splitter.on("\n").split(WordUtils.wrap(text, 30, "\n", false))) {
            tooltip.add(EnumChatFormatting.GRAY + line.trim());
        }
    }

    /**
     * Draws an icon to the screen.
     *
     * @param minecraftInstance
     * @param icon
     * @param xPos
     * @param yPos
     * @param iconWidth
     * @param iconHeight
     */
    protected void drawIcon(
            final Minecraft minecraftInstance,
            final IStateIconTexture icon,
            final int xPos,
            final int yPos,
            final int iconWidth,
            final int iconHeight) {
        // Bind the sheet
        minecraftInstance.getTextureManager().bindTexture(icon.getTexture());

        // Draw the icon
        this.drawScaledTexturedModalRect(
                xPos + this.iconXOffset,
                yPos + this.iconYOffset,
                icon.getU(),
                icon.getV(),
                iconWidth,
                iconHeight,
                icon.getWidth(),
                icon.getHeight());
    }

    @Override
    public void drawButton(final Minecraft minecraftInstance, final int x, final int y) {
        // Full white
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.backgroundIcon != null) {
            // Draw the background
            this.drawIcon(
                    minecraftInstance, this.backgroundIcon, this.xPosition, this.yPosition, this.width, this.height);
        }

        if (this.stateIcon != null) {
            // Draw the overlay icon
            this.drawIcon(
                    minecraftInstance,
                    this.stateIcon,
                    this.xPosition + this.iconXOffset,
                    this.yPosition + this.iconYOffset,
                    this.width,
                    this.height);
        }
    }
}
