package thaumicenergistics.client.gui.part;

import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.container.part.ContainerEssentiaImportBus;

/**
 * @author BrockWS
 * @author Alex811
 */
public class GuiEssentiaImportBus extends GuiSharedEssentiaBus {

    public GuiEssentiaImportBus(ContainerEssentiaImportBus container) {
        super(container);
    }

    @Override
    protected void upgradesChanged() {
        this.buttonList.clear();
        if(this.container.getPart().hasRedstoneCard())
            this.addButton(new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 8, Settings.REDSTONE_CONTROLLED, this.container.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(ThEApi.instance().lang().guiEssentiaImportBus().getLocalizedKey(), 8, 6, 4210752);
    }

    @Override
    protected int getSlotBackgroundX() {
        return 79;
    }

    @Override
    protected int getSlotBackgroundY() {
        return 39;
    }
}
