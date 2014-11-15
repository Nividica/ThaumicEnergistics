package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;
import appeng.api.config.SortDir;

public class ButtonSortingDirection
	extends AbstractAEButton
{

	public ButtonSortingDirection( final int ID, final int xPosition, final int yPosition, final int width, final int height )
	{
		super( ID, xPosition, yPosition, width, height, AEStateIconsEnum.SORT_DIR_ASC );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.SortOrder" ),
			StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ToggleSortDirection" ) );
	}

	public void setSortingDirection( final SortDir direction )
	{
		switch ( direction )
		{
			case ASCENDING:
				this.icon = AEStateIconsEnum.SORT_DIR_ASC;
				break;

			case DESCENDING:
				this.icon = AEStateIconsEnum.SORT_DIR_DEC;
				break;
		}
	}
}
