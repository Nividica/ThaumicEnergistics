package thaumicenergistics.gui.buttons;

import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.texture.EnumAEStateIcons;

public class ButtonSortingMode
	extends AbstractAEButton
{

	public ButtonSortingMode( int ID, int xPosition, int yPosition, int width, int height )
	{
		super( ID, xPosition, yPosition, width, height, EnumAEStateIcons.SORT_ALPHABETIC );
	}

	/**
	 * Sets the buttons icon based on the specified comparator mode.
	 * 
	 * @param mode
	 */
	public void setSortMode( ComparatorMode mode )
	{
		switch ( mode )
		{
			case MODE_ALPHABETIC:
				this.icon = EnumAEStateIcons.SORT_ALPHABETIC;
				break;

			case MODE_AMOUNT:
				this.icon = EnumAEStateIcons.SORT_AMOUNT;
				break;

		}
	}

}
