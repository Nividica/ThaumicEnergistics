package thaumicenergistics.client.gui.buttons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.util.StatCollector;
import thaumicenergistics.client.textures.AEStateIconsEnum;

@SideOnly(Side.CLIENT)
public class GuiButtonCraftingMode extends ThEStateButton {
    private static final String TOOLTIP_LOC_HEADER = "gui.tooltips.appliedenergistics2.";
    private boolean alwaysCraft = false;

    public GuiButtonCraftingMode(final int ID, final int xPos, final int yPos, final int width, final int height) {
        super(ID, xPos, yPos, width, height, AEStateIconsEnum.CRAFT_EITHER, 0, 0, AEStateIconsEnum.REGULAR_BUTTON);
    }

    @Override
    public void getTooltip(final List<String> tooltip) {
        this.addAboutToTooltip(
                tooltip,
                StatCollector.translateToLocal(TOOLTIP_LOC_HEADER + "Craft"),
                StatCollector.translateToLocal(TOOLTIP_LOC_HEADER + (alwaysCraft ? "CraftOnly" : "CraftEither")));
    }

    public void setAlwaysCraft(boolean c) {
        alwaysCraft = c;
        this.stateIcon = alwaysCraft ? AEStateIconsEnum.CRAFT_ONLY : AEStateIconsEnum.CRAFT_EITHER;
    }
}
