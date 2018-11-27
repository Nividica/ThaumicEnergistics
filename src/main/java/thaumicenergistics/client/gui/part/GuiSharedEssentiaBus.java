package thaumicenergistics.client.gui.part;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import thaumicenergistics.client.gui.GuiBase;
import thaumicenergistics.container.part.ContainerSharedEssentiaBus;
import thaumicenergistics.container.slot.ISlotOptional;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public abstract class GuiSharedEssentiaBus extends GuiBase {

    protected int mainBackgroundWidth = 176;
    protected int mainBackgroundHeight = 184;
    protected int upgradeOffset = 3;
    protected int upgradeBackgroundWidth = 32;
    protected int upgradeBackgroundHeight = 86;

    public GuiSharedEssentiaBus(ContainerSharedEssentiaBus container) {
        super(container);
        this.xSize = 211;
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
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.mainBackgroundWidth, this.mainBackgroundHeight);
        this.drawTexturedModalRect(this.guiLeft + this.mainBackgroundWidth + this.upgradeOffset, this.guiTop, this.mainBackgroundWidth + this.upgradeOffset, 0, this.upgradeBackgroundWidth, this.upgradeBackgroundHeight);

        this.inventorySlots.inventorySlots.forEach(this::drawSlotBackground);
    }

    protected void drawSlotBackground(Slot slot) {
        if (!(slot instanceof ISlotOptional))
            return;
        if (!((ISlotOptional) slot).isSlotEnabled()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F);
            GlStateManager.enableBlend();
        }
        this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, this.getSlotBackgroundX(), this.getSlotBackgroundY(), 18, 18);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1);
    }

    // Slot border X
    protected int getSlotBackgroundX() {
        return 0;
    }

    // Slot border Y
    protected int getSlotBackgroundY() {
        return 0;
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation("appliedenergistics2", "textures/guis/bus.png");
    }
}
