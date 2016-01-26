package thaumicenergistics.api.gui;

import javax.annotation.Nonnull;
import net.minecraft.client.gui.FontRenderer;

/**
 * GUI that displays widgets.
 * 
 * @author Nividica
 * 
 */
public interface IWidgetHost
{
	/**
	 * Gets the font renderer for the GUI.
	 * 
	 * @return
	 */
	@Nonnull
	FontRenderer getFontRenderer();

	/**
	 * Return the left of the GUI.
	 * 
	 * @return
	 */
	int guiLeft();

	/**
	 * Return the top of the GUI.
	 * 
	 * @return
	 */
	int guiTop();
}
