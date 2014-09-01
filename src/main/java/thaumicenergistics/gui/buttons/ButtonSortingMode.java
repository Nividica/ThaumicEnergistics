package thaumicenergistics.gui.buttons;

import appeng.api.config.SortOrder;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.texture.EnumAEStateIcons;

public class ButtonSortingMode
	extends AbstractAEButton
{
	public ButtonSortingMode( int ID, int xPosition, int yPosition, int width, int height )
	{
		super( ID, xPosition, yPosition, width, height, EnumAEStateIcons.SORT_MODE_ALPHABETIC );
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
				this.icon = EnumAEStateIcons.SORT_MODE_ALPHABETIC;
				break;

			case MODE_AMOUNT:
				this.icon = EnumAEStateIcons.SORT_MODE_AMOUNT;
				break;

		}
	}
	
	public void setSortMode( SortOrder order )
	{
		switch( order )
		{
			case AMOUNT:
				this.icon = EnumAEStateIcons.SORT_MODE_AMOUNT;
				break;
				
			case INVTWEAKS:
				this.icon = EnumAEStateIcons.SORT_MODE_INVTWEAK;
				break;
				
			case MOD:
				this.icon = EnumAEStateIcons.SORT_MODE_MOD;
				break;
				
			case NAME:
				this.icon = EnumAEStateIcons.SORT_MODE_ALPHABETIC;
				break;
			
		}
	}

}
