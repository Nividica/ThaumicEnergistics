package thaumicenergistics.client.gui.buttons;

import java.util.List;
import appeng.api.config.ViewItems;
import appeng.core.localization.ButtonToolTips;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.EnumChatFormatting;
import thaumicenergistics.client.textures.AEStateIconsEnum;

/**
 * Displays view types icons.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiButtonViewType
	extends ThEStateButton
{
	/**
	 * Cached tooltip for the buttons view mode.
	 */
	private String tooltipViewType = "";

	/**
	 * Creates the view button.
	 *
	 * @param ID
	 * @param xPosition
	 * @param yPosition
	 * @param width
	 * @param height
	 */
	public GuiButtonViewType( final int ID, final int xPosition, final int yPosition, final int width, final int height )
	{
		super( ID, xPosition, yPosition, width, height, AEStateIconsEnum.VIEW_TYPE_ALL, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );
		this.setViewMode( ViewItems.ALL );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, ButtonToolTips.View.getLocal(), EnumChatFormatting.GRAY + this.tooltipViewType );
	}

	/**
	 * Sets the icon based on the view mode.
	 *
	 * @param mode
	 */
	public void setViewMode( final ViewItems mode )
	{
		switch ( mode )
		{
		case ALL:
			this.tooltipViewType = ButtonToolTips.StoredCraftable.getLocal();
			this.stateIcon = AEStateIconsEnum.VIEW_TYPE_ALL;
			break;

		case CRAFTABLE:
			this.tooltipViewType = ButtonToolTips.Craftable.getLocal();
			this.stateIcon = AEStateIconsEnum.VIEW_TYPE_CRAFT;
			break;

		case STORED:
			this.tooltipViewType = ButtonToolTips.StoredItems.getLocal();
			this.stateIcon = AEStateIconsEnum.VIEW_TYPE_STORED;
			break;
		}
	}

}
