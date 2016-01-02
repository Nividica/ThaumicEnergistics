package thaumicenergistics.client.gui.buttons;

import java.util.List;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.common.registries.ThEStrings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonClearCraftingGrid
	extends ThEStateButton
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
	public GuiButtonClearCraftingGrid( final int ID, final int xPosition, final int yPosition, final int width, final int height )
	{
		super( ID, xPosition, yPosition, width, height, GuiButtonClearCraftingGrid.CLEAR_ICON, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		// Add the tooltip text
		tooltip.add( ThEStrings.TooltipButton_DepositCraftingGrid.getLocalized() );
	}

}
