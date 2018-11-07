package thaumicenergistics.client.gui.part;

import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import thaumicenergistics.client.gui.GuiBase;
import thaumicenergistics.container.part.ContainerSharedEssentiaBus;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public abstract class GuiSharedEssentiaBus extends GuiBase {

    public GuiSharedEssentiaBus(ContainerSharedEssentiaBus container) {
        super(container);
        this.xSize = 176 + 35;
        this.ySize = 184;
    }

    @Override
    public void updateScreen() {
        ((ContainerSharedEssentiaBus) this.inventorySlots).recalculateSlots();
        super.updateScreen();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(this.getGuiBackground());
        // TODO: Check if user has network tool
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 184);
        this.drawTexturedModalRect(this.guiLeft + 176, this.guiTop, 176, 0, 35, 86);

        // TODO: Draw lighter background behind enabled slots
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void drawSlot(Slot slot) {
        super.drawSlot(slot);
    }

    @Override
    protected void handleMouseClick(Slot slot, int id, int button, ClickType type) {
        super.handleMouseClick(slot, id, button, type);
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/essentia_io_bus.png");
    }
}
