package thaumicenergistics.client.gui.buttons;

import java.util.List;

import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.ThEStateIconsEnum;
import thaumicenergistics.common.registries.ThEStrings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Displays swap armor icon.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiButtonSwapArmor extends ThEStateButton {

    public GuiButtonSwapArmor(final int ID, final int xPosition, final int yPosition, final int buttonWidth,
            final int buttonHeight) {
        super(
                ID,
                xPosition,
                yPosition,
                buttonWidth,
                buttonHeight,
                ThEStateIconsEnum.SWAP,
                0,
                0,
                AEStateIconsEnum.REGULAR_BUTTON);
    }

    @Override
    public void getTooltip(final List<String> tooltip) {
        this.addAboutToTooltip(
                tooltip,
                ThEStrings.TooltipButton_SwapArmor_Title.getLocalized(),
                ThEStrings.TooltipButton_SwapArmor.getLocalized());
    }
}
