package thaumicenergistics.client.gui.part;

import net.minecraft.util.ResourceLocation;

import thaumicenergistics.container.part.ContainerEssentiaStorageBus;

/**
 * @author BrockWS
 */
public class GuiEssentiaStorageBus extends GuiSharedEssentiaBus {
    public GuiEssentiaStorageBus(ContainerEssentiaStorageBus container) {
        super(container);
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation("appliedenergistics2", "textures/guis/storagebus.png");
    }
}
