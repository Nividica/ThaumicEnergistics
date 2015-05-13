package thaumicenergistics.gui.buttons;

import java.util.List;
import net.minecraft.util.EnumChatFormatting;
import thaumicenergistics.texture.AEStateIconsEnum;
import appeng.api.config.TerminalStyle;
import appeng.core.localization.ButtonToolTips;

public class GuiButtonTerminalStyle
	extends AbstractStateGuiButton
{
	private String tooltipStyle = "";

	public GuiButtonTerminalStyle( final int ID, final int xPosition, final int yPosition, final int buttonWidth, final int buttonHeight,
									final TerminalStyle currentStyle )
	{
		super( ID, xPosition, yPosition, buttonWidth, buttonHeight, null, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );

		// Set the style
		this.setTerminalStyle( currentStyle );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, ButtonToolTips.TerminalStyle.getLocal(), EnumChatFormatting.GRAY + this.tooltipStyle );
	}

	public void setTerminalStyle( final TerminalStyle style )
	{
		switch ( style )
		{
			case SMALL:
				this.stateIcon = AEStateIconsEnum.TERM_STYLE_SMALL;
				this.tooltipStyle = ButtonToolTips.TerminalStyle_Small.getLocal();
				break;

			case TALL:
				this.stateIcon = AEStateIconsEnum.TERM_STYLE_TALL;
				this.tooltipStyle = ButtonToolTips.TerminalStyle_Tall.getLocal();
				break;

			default:
				break;

		}
	}

}
