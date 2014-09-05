package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.texture.EnumAEStateIcons;

public class ButtonClearCraftingGrid
	extends AbstractAEButton
{
	/**
	 * Clear grid icon
	 */
	private static final EnumAEStateIcons CLEAR_ICON = EnumAEStateIcons.CLEAR_GRID;

	/**
	 * Creates the button
	 * 
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

	@Override
	public void getTooltip( List<String> tooltip )
	{
		// Add the tooltip text
		tooltip.add( StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.button.clear.crafting" ) );
	}

}
