package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;

public class ButtonAETab
	extends AbstractAEButton
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

	public ButtonAETab( final int ID, final int xPosition, final int yPosition, final AEStateIconsEnum icon, final String unlocalizedTooltip )
	{
		// Call super
		super( ID, xPosition, yPosition, ButtonAETab.BUTTON_WIDTH, ButtonAETab.BUTTON_HEIGHT, icon, ButtonAETab.ICON_X_POSITION,
				ButtonAETab.ICON_Y_POSITION, icon.getWidth(), icon.getHeight(), true );

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
