package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.texture.AEStateIconsEnum;
import appeng.api.config.SortOrder;

public class ButtonSortingMode
	extends AbstractAEButton
{
	private String tooltipSortBy = "";

	public ButtonSortingMode( final int ID, final int xPosition, final int yPosition, final int width, final int height )
	{
		super( ID, xPosition, yPosition, width, height, AEStateIconsEnum.SORT_MODE_ALPHABETIC );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.SortBy" ), EnumChatFormatting.GRAY +
						this.tooltipSortBy );
	}

	/**
	 * Sets the buttons icon based on the specified comparator mode.
	 * 
	 * @param mode
	 */
	public void setSortMode( final ComparatorMode mode )
	{
		switch ( mode )
		{
			case MODE_ALPHABETIC:
				this.icon = AEStateIconsEnum.SORT_MODE_ALPHABETIC;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ItemName" );
				break;

			case MODE_AMOUNT:
				this.icon = AEStateIconsEnum.SORT_MODE_AMOUNT;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.NumberOfItems" );
				break;

		}
	}

	public void setSortMode( final SortOrder order )
	{
		switch ( order )
		{
			case AMOUNT:
				this.icon = AEStateIconsEnum.SORT_MODE_AMOUNT;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.NumberOfItems" );
				break;

			case INVTWEAKS:
				this.icon = AEStateIconsEnum.SORT_MODE_INVTWEAK;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.InventoryTweaks" );
				break;

			case MOD:
				this.icon = AEStateIconsEnum.SORT_MODE_MOD;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.Mod" );
				break;

			case NAME:
				this.icon = AEStateIconsEnum.SORT_MODE_ALPHABETIC;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ItemName" );
				break;

		}
	}

}
