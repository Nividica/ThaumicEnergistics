package thaumicenergistics.gui.buttons;

import java.util.List;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.AEStateIconsEnum;
import thaumicenergistics.texture.ThEStateIconsEnum;

public class GuiButtonSwapArmor
	extends AbstractStateGuiButton
{

	public GuiButtonSwapArmor( final int ID, final int xPosition, final int yPosition, final int buttonWidth, final int buttonHeight )
	{
		super( ID, xPosition, yPosition, buttonWidth, buttonHeight, ThEStateIconsEnum.SWAP, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, ThEStrings.TooltipButton_SwapArmor_Title.getLocalized(), ThEStrings.TooltipButton_SwapArmor.getLocalized() );
	}

}
