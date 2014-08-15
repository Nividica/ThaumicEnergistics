package thaumicenergistics.gui.buttons;

import thaumicenergistics.texture.EnumAEStateIcons;

public class ButtonAETab
	extends AbstractAEButton
{
	/**
	 * Height of the button
	 */
	private static final int BUTTON_HEIGHT = EnumAEStateIcons.TAB_BUTTON.getHeight();

	/**
	 * Width of the button
	 */
	private static final int BUTTON_WIDTH = EnumAEStateIcons.TAB_BUTTON.getWidth();

	private static int ICON_X_POSITION = 3;

	private static int ICON_Y_POSITION = 3;

	/**
	 * Icon to draw on the button
	 */
	protected EnumAEStateIcons icon;

	public ButtonAETab( int ID, int xPosition, int yPosition, EnumAEStateIcons icon )
	{
		// Call super
		super( ID, xPosition, yPosition, ButtonAETab.BUTTON_WIDTH, ButtonAETab.BUTTON_HEIGHT, icon, ButtonAETab.ICON_X_POSITION,
				ButtonAETab.ICON_Y_POSITION, icon.getWidth(), icon.getHeight(), true );
	}

}
