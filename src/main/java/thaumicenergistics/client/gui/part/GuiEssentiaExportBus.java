package thaumicenergistics.client.gui.part;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.container.part.ContainerEssentiaExportBus;

/**
 * @author BrockWS
 */
public class GuiEssentiaExportBus extends GuiSharedEssentiaBus {

    public GuiEssentiaExportBus(ContainerEssentiaExportBus container) {
        super(container);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(ThEApi.instance().lang().guiEssentiaExportBus().getLocalizedKey(), 8, 6, 4210752);
    }
}
