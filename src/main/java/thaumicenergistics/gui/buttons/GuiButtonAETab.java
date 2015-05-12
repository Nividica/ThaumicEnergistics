package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;

public class GuiButtonAETab
	extends AbstractStateGuiButton
{
	/**
	 * Height of the button
	 */
	private static final int BUTTON_HEIGHT = AEStateIconsEnum.TAB_BUTTON.getHeight();

	/**
	 * Width of the button
	 */
	private static final int BUTTON_WIDTH = AEStateIconsEnum.TAB_BUTTON.getWidth();

	private static int ICON_X_POSITION = 3;

	private static int ICON_Y_POSITION = 3;

	private String tooltipMessageUnlocalized;

	/**
	 * Icon to draw on the button
	 */
	protected AEStateIconsEnum icon;

	public GuiButtonAETab( final int ID, final int xPosition, final int yPosition, final AEStateIconsEnum icon, final String unlocalizedTooltip )
	{
		// Call super
		super( ID, xPosition, yPosition, GuiButtonAETab.BUTTON_WIDTH, GuiButtonAETab.BUTTON_HEIGHT, icon, GuiButtonAETab.ICON_X_POSITION,
				GuiButtonAETab.ICON_Y_POSITION, AEStateIconsEnum.TAB_BUTTON );

		// Set the tooltip
		this.tooltipMessageUnlocalized = unlocalizedTooltip;
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		if( !this.tooltipMessageUnlocalized.equals( "" ) )
		{
			tooltip.add( StatCollector.translateToLocal( this.tooltipMessageUnlocalized ) );
		}
	}

}
