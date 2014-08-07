package thaumicenergistics.gui.buttons;

import thaumicenergistics.texture.AEStateIcons;

public class ButtonClearCraftingGrid
	extends AbstractAEButton
{
	/**
	 * Clear grid icon
	 */
	private static final AEStateIcons CLEAR_ICON = AEStateIcons.CLEAR_GRID;

	/**
	 * Creates the button
	 * @param ID
	 * @param xPosition
	 * @param yPosition
	 * @param width
	 * @param height
	 */
	public ButtonClearCraftingGrid( int ID, int xPosition, int yPosition, int width, int height )
	{
		super( ID, xPosition, yPosition, width, height, ButtonClearCraftingGrid.CLEAR_ICON );
	}

}
