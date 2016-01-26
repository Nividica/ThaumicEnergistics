package thaumicenergistics.client.gui.buttons;

import java.util.List;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import appeng.core.localization.ButtonToolTips;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Displays encode pattern icon.
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiButtonEncodePattern
	extends ThEStateButton
{

	public GuiButtonEncodePattern( final int ID, final int posX, final int posY, final int width, final int height )
	{
		// Call super
		super( ID, posX, posY, width, height, AEStateIconsEnum.ARROW_DOWN, 0, 0, AEStateIconsEnum.REGULAR_BUTTON );
	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		this.addAboutToTooltip( tooltip, ButtonToolTips.Encode.getLocal(), ButtonToolTips.EncodeDescription.getLocal() );
	}

}
