package thaumicenergistics.client.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import thaumicenergistics.client.gui.ThEGuiHelper;

/**
 * Base class for ThE buttons.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public abstract class ThEGuiButtonBase extends GuiButton {
    public ThEGuiButtonBase(
            final int ID,
            final int xPosition,
            final int yPosition,
            final int width,
            final int height,
            final String text) {
        super(ID, xPosition, yPosition, width, height, text);
    }

    public ThEGuiButtonBase(final int ID, final int xPosition, final int yPosition, final String text) {
        super(ID, xPosition, yPosition, text);
    }

    /**
     * Called to get the tooltip for this button.
     *
     * @param tooltip
     * List to add tooltip string to.
     */
    public abstract void getTooltip(final List<String> tooltip);

    /**
     * Checks if the mouse is over this button.
     *
     * @param mouseX
     * @param mouseY
     * @return
     */
    public boolean isMouseOverButton(final int mouseX, final int mouseY) {
        return ThEGuiHelper.INSTANCE.isPointInRegion(
                this.yPosition, this.xPosition, this.height, this.width, mouseX, mouseY);
    }
}
