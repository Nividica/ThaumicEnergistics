package thaumicenergistics.client.gui;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.SlotGhostEssentia;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

/**
 * @author BrockWS
 */
public abstract class GuiBase extends GuiContainer {

    public GuiBase(ContainerBase container) {
        super(container);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (this.hoveredSlot != null) {
            if (this.hoveredSlot instanceof SlotGhostEssentia && ((SlotGhostEssentia) this.hoveredSlot).getAspect() != null) {
                this.drawHoveringText(((SlotGhostEssentia) this.hoveredSlot).getAspect().getName(), mouseX, mouseY);
                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    protected ResourceLocation getGuiBackground() {
        return null;
    }
}
