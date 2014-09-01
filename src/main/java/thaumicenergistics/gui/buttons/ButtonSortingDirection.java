package thaumicenergistics.gui.buttons;

import appeng.api.config.SortDir;
import thaumicenergistics.texture.EnumAEStateIcons;

public class ButtonSortingDirection
	extends AbstractAEButton
{

	public ButtonSortingDirection( int ID, int xPosition, int yPosition, int width, int height )
	{
		super( ID, xPosition, yPosition, width, height, EnumAEStateIcons.SORT_DIR_ASC );
	}
	
	public void setSortingDirection( SortDir direction )
	{
		switch ( direction )
		{
			case ASCENDING:
				this.icon = EnumAEStateIcons.SORT_DIR_ASC;
				break;
				
			case DESCENDING:
				this.icon = EnumAEStateIcons.SORT_DIR_DEC;
				break;
		}
	}

}
