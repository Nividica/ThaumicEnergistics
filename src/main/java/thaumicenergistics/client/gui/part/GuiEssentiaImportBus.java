package thaumicenergistics.client.gui.part;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.container.part.ContainerEssentiaImportBus;

/**
 * @author BrockWS
 */
public class GuiEssentiaImportBus extends GuiSharedEssentiaBus {

    public GuiEssentiaImportBus(ContainerEssentiaImportBus container) {
        super(container);
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
