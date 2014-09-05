package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import appeng.api.config.SortOrder;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.texture.EnumAEStateIcons;

public class ButtonSortingMode
	extends AbstractAEButton
{
	private String tooltipSortBy = "";
	
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
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ItemName" );
				break;

			case MODE_AMOUNT:
				this.icon = EnumAEStateIcons.SORT_MODE_AMOUNT;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.NumberOfItems" );
				break;

		}
	}
	
	public void setSortMode( SortOrder order )
	{
		switch( order )
		{
			case AMOUNT:
				this.icon = EnumAEStateIcons.SORT_MODE_AMOUNT;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.NumberOfItems" );
				break;
				
			case INVTWEAKS:
				this.icon = EnumAEStateIcons.SORT_MODE_INVTWEAK;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.InventoryTweaks" );
				break;
				
			case MOD:
				this.icon = EnumAEStateIcons.SORT_MODE_MOD;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.Mod" );
				break;
				
			case NAME:
				this.icon = EnumAEStateIcons.SORT_MODE_ALPHABETIC;
				this.tooltipSortBy = StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.ItemName" );
				break;
			
		}
	}

	@Override
	public void getTooltip( List<String> tooltip )
	{
		tooltip.add( StatCollector.translateToLocal( "gui.tooltips.appliedenergistics2.SortBy" ) );
		tooltip.add( EnumChatFormatting.GRAY + this.tooltipSortBy );
	}

}
