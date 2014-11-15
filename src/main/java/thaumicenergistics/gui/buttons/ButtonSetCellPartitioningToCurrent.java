package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;

public class ButtonSetCellPartitioningToCurrent
	extends AbstractAEButton
{
	/**
	 * Wrench icon
	 */
	private static final AEStateIconsEnum WRENCH_ICON = AEStateIconsEnum.WRENCH;

	/**
	 * Creates the button
	 * 
	 * @param ID
	 * @param xPosition
	 * @param yPosition
	 * @param width
	 * @param height
	 */
	public ButtonSetCellPartitioningToCurrent( final int ID, final int xPosition, final int yPosition )
	{
		super( ID, xPosition, yPosition, 16, 16, ButtonSetCellPartitioningToCurrent.WRENCH_ICON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		// Add the info
		this.addAboutToTooltip( tooltip, StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.PartitionStorage" ),
			StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.PartitionStorageHint" ) );
	}
}
