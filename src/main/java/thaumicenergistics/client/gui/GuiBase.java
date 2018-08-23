package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.ISlotOptional;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.container.slot.SlotMEEssentia;

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
    public void drawSlot(Slot slot) {
        mc.getTextureManager().bindTexture(this.getGuiBackground());
        if (slot instanceof ISlotOptional) {
            if (slot.isEnabled()) {
                // TODO: Draw slot background on enabled slots
            }
        }
        super.drawSlot(slot);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (this.hoveredSlot != null) {
            if (this.hoveredSlot instanceof SlotGhostEssentia && ((SlotGhostEssentia) this.hoveredSlot).getAspect() != null) {
                this.drawHoveringText(((SlotGhostEssentia) this.hoveredSlot).getAspect().getName(), mouseX, mouseY);
                return;
            }
            if (this.hoveredSlot instanceof SlotMEEssentia && this.hoveredSlot.getHasStack()) {
                IAEEssentiaStack stack = ((SlotMEEssentia) this.hoveredSlot).getAEStack();
                List<String> tooltip = new ArrayList<>();
                tooltip.add(stack.getAspect().getName());
                tooltip.add(Long.toString(stack.getStackSize()));
                this.drawHoveringText(tooltip, mouseX, mouseY);
                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    protected ResourceLocation getGuiBackground() {
        return null;
    }
}
