package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.texture.AEStateIconsEnum;
import appeng.api.config.SortDir;

public class GuiButtonSortingDirection
	extends AbstractStateGuiButton
{

	public GuiButtonSortingDirection( final int ID, final int xPosition, final int yPosition, final int width, final int height )
	{
		super( ID, xPosition, yPosition, width, height, AEStateIconsEnum.SORT_DIR_ASC, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );
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
			this.stateIcon = AEStateIconsEnum.SORT_DIR_ASC;
			break;

		case DESCENDING:
			this.stateIcon = AEStateIconsEnum.SORT_DIR_DEC;
			break;
		}
	}
}
