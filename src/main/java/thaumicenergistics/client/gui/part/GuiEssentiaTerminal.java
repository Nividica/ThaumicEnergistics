package thaumicenergistics.client.gui.part;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.TerminalStyle;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.api.storage.IEssentiaStorageChannel;
import thaumicenergistics.client.gui.helpers.GuiScrollBar;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.config.ThEConfig;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.part.ContainerEssentiaTerminal;
import thaumicenergistics.container.slot.SlotME;
import thaumicenergistics.container.slot.ThESlot;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketSettingChange;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.util.ThEUtil;

import org.lwjgl.input.Mouse;

/**
 * @author BrockWS
 */
public class GuiEssentiaTerminal extends GuiAbstractTerminal<IAEEssentiaStack, IEssentiaStorageChannel> {

    private GuiScrollBar scrollBar;
    private GuiImgButton sortByButton;
    private GuiImgButton sortDirButton;
    private GuiImgButton terminalSizeButton;
    private int rows = 6;

    public GuiEssentiaTerminal(ContainerEssentiaTerminal container) {
        super(container);
        this.repo = new MERepo<>(IEssentiaStorageChannel.class);
    }

    @Override
    public void initGui() {
        this.xSize = 197;
        this.ySize = 114;

        double remainingY = this.height - this.ySize;
        int maxRows = (int) Math.floor(remainingY / 18);
        if (ThEApi.instance().config().terminalStyle() != TerminalStyle.TALL) {
            this.rows = Math.min(maxRows, 6);
        } else {
            this.rows = maxRows;
        }

        this.ySize += 18 * this.rows;
        this.scrollBar = new GuiScrollBar(175, 18, 18 * this.rows - 2);
        this.repo.setScrollBar(this.scrollBar);
        this.updateScroll();

        this.inventorySlots.inventorySlots.removeIf(slot -> slot instanceof SlotME);
        this.addTerminalSlots(9, 18);

        this.buttonList.clear();
        super.initGui();

        IConfigManager cm = ((IConfigurableObject) this.inventorySlots).getConfigManager();

        this.sortByButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 8, Settings.SORT_BY, cm.getSetting(Settings.SORT_BY));
        this.sortDirButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 28, Settings.SORT_DIRECTION, cm.getSetting(Settings.SORT_DIRECTION));
        this.terminalSizeButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 48, Settings.TERMINAL_STYLE, ThEApi.instance().config().terminalStyle());

        this.addButton(this.sortByButton);
        this.addButton(this.sortDirButton);
        this.addButton(this.terminalSizeButton);

        this.inventorySlots.inventorySlots.forEach(slot -> {
            if (slot instanceof ThESlot)
                ((ThESlot) slot).recalculateY(this.rows);
        });
    }

    @Override
    public void reload() {
        this.initGui();
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
        // Hide Search
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
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.getYSize() - 93, 4210752);

        if (this.scrollBar != null)
            this.scrollBar.draw(this);
    }

    @Override
    public void updateSetting(Settings setting, Enum value) {
        super.updateSetting(setting, value);
        this.repo.setSortOrder((SortOrder) ((IConfigurableObject) this.inventorySlots).getConfigManager().getSetting(Settings.SORT_BY));
        this.repo.setSortDir((SortDir) ((IConfigurableObject) this.inventorySlots).getConfigManager().getSetting(Settings.SORT_DIRECTION));
        this.repo.updateView();
        this.updateScroll();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (this.scrollBar != null) {
            if (mouseWithin(scrollBar))
                this.scrollBar.click(mouseY - this.getGuiTop());
            this.repo.updateView();
            this.updateScroll();
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.scrollBar != null) {
            if (mouseWithin(scrollBar)) {
                this.scrollBar.click(mouseY - this.getGuiTop());
                this.repo.updateView();
                this.updateScroll();
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot instanceof SlotME) {
            // Send to server for processing
            if (slot.getHasStack())
                PacketHandler.sendToServer(new PacketUIAction(ActionType.FILL_ESSENTIA_ITEM, ((SlotME) slot).getAEStack()));
            else
                PacketHandler.sendToServer(new PacketUIAction(ActionType.EMPTY_ESSENTIA_ITEM));
            return;
        }
        super.handleMouseClick(slot, slotId, mouseButton, type);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int delta = Mouse.getEventDWheel();

        if (delta != 0) {
            if (this.scrollBar != null)
                this.scrollBar.wheel(delta);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof GuiImgButton) {
            GuiImgButton btn = (GuiImgButton) button;
            Enum currentValue = btn.getCurrentValue();
            Enum next = ThEUtil.rotateEnum(currentValue, btn.getSetting().getPossibleValues(), Mouse.isButtonDown(1));
            if (next.equals(SortOrder.MOD)) // Can't sort aspects by mod id
                next = ThEUtil.rotateEnum(next, btn.getSetting().getPossibleValues(), Mouse.isButtonDown(1));
            btn.set(next);
            if (btn.getSetting() == Settings.TERMINAL_STYLE) {
                ThEConfig.client.terminalStyle = (TerminalStyle) next;
                ThEConfig.save();
                this.reload();
                return;
            }
            PacketHandler.sendToServer(new PacketSettingChange(btn.getSetting(), next));
        }
    }

    public void onMEStorageUpdate(IItemList<IAEEssentiaStack> list) {
        this.repo.clear();
        for (IAEEssentiaStack stack : list)
            this.repo.postUpdate(stack);
        this.repo.updateView();
        this.updateScroll();
    }

    public void updateScroll() {
        this.scrollBar.setRows(this.rows);
        this.scrollBar.setRange(0, (this.repo.size() + 8) / 9 - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation(ModGlobals.MOD_ID_AE2, "textures/guis/terminal.png");
    }
}
