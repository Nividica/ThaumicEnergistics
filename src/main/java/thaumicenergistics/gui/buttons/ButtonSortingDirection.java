package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
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

	@Override
	public void getTooltip( List<String> tooltip )
	{
		tooltip.add( StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.SortOrder" ) );
		tooltip.add( EnumChatFormatting.GRAY + StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ToggleSortDirection" ) );
	}

}
