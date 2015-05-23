package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import thaumicenergistics.util.GuiHelper;

public abstract class AbstractGuiButtonBase
	extends GuiButton
{
	public AbstractGuiButtonBase( final int ID, final int xPosition, final int yPosition, final int width, final int height, final String text )
	{
		super( ID, xPosition, yPosition, width, height, text );
	}

	public AbstractGuiButtonBase( final int ID, final int xPosition, final int yPosition, final String text )
	{
		super( ID, xPosition, yPosition, text );
	}

	/**
	 * Called to get the tooltip for this button.
	 * 
	 * @param tooltip
	 * List to add tooltip string to.
	 */
	public abstract void getTooltip( final List<String> tooltip );

	/**
	 * Checks if the mouse is over this button.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public boolean isMouseOverButton( final int mouseX, final int mouseY )
	{
		return GuiHelper.INSTANCE.isPointInRegion( this.yPosition, this.xPosition, this.height, this.width, mouseX, mouseY );
	}

}
