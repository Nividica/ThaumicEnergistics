package thaumicenergistics.gui.widget;

import net.minecraft.client.gui.FontRenderer;

public interface IWidgetHost
{
	/**
	 * Gets the font renderer for the GUI.
	 * 
	 * @return
	 */
	public FontRenderer getFontRenderer();

	public int guiLeft();

	public int guiTop();
}
