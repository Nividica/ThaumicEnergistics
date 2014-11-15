package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;

public class ButtonClearCellPartitioning
	extends AbstractAEButton
{

	/**
	 * Clear icon
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
	public ButtonClearCellPartitioning( final int ID, final int xPosition, final int yPosition )
	{
		super( ID, xPosition, yPosition, 16, 16, ButtonClearCellPartitioning.CLEAR_ICON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		// Add the info
		this.addAboutToTooltip( tooltip, StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.Clear" ),
			StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ClearSettings" ) );
	}
}
