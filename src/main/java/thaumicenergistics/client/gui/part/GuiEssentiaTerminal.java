package thaumicenergistics.client.gui.part;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.api.storage.data.IItemList;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.client.gui.helpers.GuiScrollBar;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.part.ContainerEssentiaTerminal;
import thaumicenergistics.container.slot.SlotME;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketUIAction;

/**
 * @author BrockWS
 */
public class GuiEssentiaTerminal extends GuiAbstractTerminal<IAEEssentiaStack, IEssentiaStorageChannel> {

    private GuiScrollBar scrollBar;
    private int rows = 6;

    public GuiEssentiaTerminal(ContainerEssentiaTerminal container) {
        super(container);
        this.xSize = 197;
        this.ySize = 102;
        this.ySize += 20 * this.rows;
        this.repo = new MERepo<>(IEssentiaStorageChannel.class);
        this.scrollBar = new GuiScrollBar(175, 18, 106);
        this.repo.setScrollBar(this.scrollBar);
        this.addTerminalSlots(9, 18);
    }

    private void addTerminalSlots(int offsetX, int offsetY) {
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < 9; c++) {
                this.addMESlot(new SlotME<>(this.repo, c + r * 9, offsetX + c * 18, offsetY + r * 18));
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(this.getGuiBackground());
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.getXSize(), 18);
        // FIXME: Hide search box for now
        this.drawTexturedModalRect(this.guiLeft + 100, this.guiTop, 5, 0, 70, 16);
        this.drawTexturedModalRect(this.guiLeft + 50, this.guiTop, 5, 0, 70, 16);

        for (int i = 0; i < this.rows; i++)
            this.drawTexturedModalRect(this.guiLeft, this.guiTop + 18 + i * 18, 0, 18, this.getXSize(), 18);

        this.drawTexturedModalRect(this.guiLeft, this.guiTop + 16 + this.rows * 18, 0, 106 - 18 - 18, this.getXSize(), 99 + 77);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(ThEApi.instance().lang().guiEssentiaTerminal().getLocalizedKey(), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot instanceof SlotME) {
            // Send to server for processing
            if (slot.getHasStack())
                PacketHandler.sendToServer(new PacketUIAction(ActionType.FILL_ESSENTIA_ITEM, (IAEEssentiaStack) ((SlotME) slot).getAEStack()));
            else
                PacketHandler.sendToServer(new PacketUIAction(ActionType.EMPTY_ESSENTIA_ITEM));
            return;
        }
        super.handleMouseClick(slot, slotId, mouseButton, type);
    }

    public void onMEStorageUpdate(IItemList<IAEEssentiaStack> list) {
        this.repo.clear();
        for (IAEEssentiaStack stack : list)
            this.repo.postUpdate(stack);
        this.repo.updateView();
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation("appliedenergistics2", "textures/guis/terminal.png");
    }
}
