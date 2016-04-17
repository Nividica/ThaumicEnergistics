package thaumicenergistics.client.gui.buttons;

import java.util.List;
import appeng.api.config.SearchBoxMode;
import appeng.core.localization.ButtonToolTips;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.EnumChatFormatting;
import thaumicenergistics.client.textures.AEStateIconsEnum;

/**
 * Displays search mode icons.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiButtonSearchMode
	extends ThEStateButton
{
	private String tooltipMode = "";

	public GuiButtonSearchMode(	final int ID, final int xPosition, final int yPosition, final int buttonWidth, final int buttonHeight,
								final SearchBoxMode currentMode )
	{
		super( ID, xPosition, yPosition, buttonWidth, buttonHeight, null, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );

		this.setSearchMode( currentMode );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, ButtonToolTips.SearchMode.getLocal(), EnumChatFormatting.GRAY + this.tooltipMode );
	}

	/**
	 * Sets the icon and tooltip based on the specified mode.
	 *
	 * @param mode
	 */
	public void setSearchMode( final SearchBoxMode mode )
	{
		switch ( mode )
		{
		case AUTOSEARCH:
			this.tooltipMode = ButtonToolTips.SearchMode_Auto.getLocal();
			this.stateIcon = AEStateIconsEnum.SEARCH_MODE_AUTO;
			break;

		case MANUAL_SEARCH:
			this.tooltipMode = ButtonToolTips.SearchMode_Standard.getLocal();
			this.stateIcon = AEStateIconsEnum.SEARCH_MODE_MANUAL;
			break;

		case NEI_AUTOSEARCH:
			this.tooltipMode = ButtonToolTips.SearchMode_NEIAuto.getLocal();
			this.stateIcon = AEStateIconsEnum.SEARCH_MODE_NEI_AUTO;
			break;

		case NEI_MANUAL_SEARCH:
			this.tooltipMode = ButtonToolTips.SearchMode_NEIStandard.getLocal();
			this.stateIcon = AEStateIconsEnum.SEARCH_MODE_NEI_MANUAL;
			break;

		}
	}

}
