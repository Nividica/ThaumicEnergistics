package thaumicenergistics.gui.buttons;

import java.util.List;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.AEStateIconsEnum;

public class ButtonClearCraftingGrid
	extends AbstractAEButton
{
	/**
	 * Clear grid icon
	 */
	private static final AEStateIconsEnum CLEAR_ICON = AEStateIconsEnum.CLEAR_GRID;

	/**
	 * Creates the button
	 * 
	 * @param ID
	 * @param xPosition
	 * @param yPosition
	 * @param width
	 * @param height
	 */
	public ButtonClearCraftingGrid( final int ID, final int xPosition, final int yPosition, final int width, final int height )
	{
		super( ID, xPosition, yPosition, width, height, ButtonClearCraftingGrid.CLEAR_ICON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		// Add the tooltip text
		tooltip.add( ThEStrings.TooltipButton_DepositCraftingGrid.getLocalized() );
	}

}
