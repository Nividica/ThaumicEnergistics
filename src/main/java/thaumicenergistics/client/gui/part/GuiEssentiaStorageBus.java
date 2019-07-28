package thaumicenergistics.client.gui.part;

import net.minecraft.util.ResourceLocation;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.container.part.ContainerEssentiaStorageBus;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class GuiEssentiaStorageBus extends GuiSharedEssentiaBus {
    public GuiEssentiaStorageBus(ContainerEssentiaStorageBus container) {
        super(container);
        this.ySize = 251;
        this.mainBackgroundHeight = 251;
        this.upgradeBackgroundHeight += 18;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(ThEApi.instance().lang().guiEssentiaStorageBus().getLocalizedKey(), 8, 6, 4210752);
    }

    @Override
    protected int getSlotBackgroundX() {
        return 7;
    }

    @Override
    protected int getSlotBackgroundY() {
        return 28;
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation(ModGlobals.MOD_ID_AE2, "textures/guis/storagebus.png");
    }
}
