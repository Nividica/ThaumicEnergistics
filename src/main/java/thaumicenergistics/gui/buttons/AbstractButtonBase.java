package thaumicenergistics.gui.buttons;

import java.util.List;
import thaumicenergistics.util.GuiHelper;
import net.minecraft.client.gui.GuiButton;

public abstract class AbstractButtonBase
	extends GuiButton
{

	public AbstractButtonBase( int ID, int xPosition, int yPosition, String text )
	{
		super( ID, xPosition, yPosition, text );
	}

	public AbstractButtonBase( int ID, int xPosition, int yPosition, int width, int height, String text )
	{
		super( ID, xPosition, yPosition, width, height, text );
	}

	/**
	 * Called to get the tooltip for this button.
	 * @param tooltip List to add tooltip string to.
	 */
	public abstract void getTooltip( List<String> tooltip );

	/**
	 * Checks if the mouse is over this button.
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public boolean isMouseOverButton( int mouseX, int mouseY )
	{
		return GuiHelper.instance.isPointInRegion( this.yPosition, this.xPosition, this.height, this.width, mouseX, mouseY );
	}

}
