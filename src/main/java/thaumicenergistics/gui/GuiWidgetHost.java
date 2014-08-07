package thaumicenergistics.gui;

import net.minecraft.inventory.Container;
import thaumicenergistics.gui.widget.IWidgetHost;

public abstract class GuiWidgetHost
	extends GuiWithUpgradeSlots
	implements IWidgetHost
{

	public GuiWidgetHost( Container container )
	{
		super( container );
	}

	/**
	 * Gets the starting X position for the Gui.
	 */
	@Override
	public final int guiLeft()
	{
		return this.guiLeft;
	}

	/**
	 * Gets the starting Y position for the Gui.
	 */
	@Override
	public final int guiTop()
	{
		return this.guiTop;
	}

}
