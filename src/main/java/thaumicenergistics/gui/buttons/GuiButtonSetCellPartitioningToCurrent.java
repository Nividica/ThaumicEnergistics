package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;

public class GuiButtonSetCellPartitioningToCurrent
	extends AbstractStateGuiButton
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
	public GuiButtonSetCellPartitioningToCurrent( final int ID, final int xPosition, final int yPosition )
	{
		super( ID, xPosition, yPosition, 16, 16, GuiButtonSetCellPartitioningToCurrent.WRENCH_ICON, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		// Add the info
		this.addAboutToTooltip( tooltip, StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.PartitionStorage" ),
			StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.PartitionStorageHint" ) );
	}
}
