package thaumicenergistics.client.gui.part;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;

import thaumicenergistics.client.gui.GuiBase;
import thaumicenergistics.client.gui.helpers.GuiScrollBar;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.part.ContainerArcaneTerminal;
import thaumicenergistics.container.slot.SlotME;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketSettingChange;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ThELog;
import thaumicenergistics.util.ThEUtil;

/**
 * @author BrockWS
 */
public class GuiArcaneTerminal extends GuiBase {

    private MERepo<IAEItemStack> repo;
    private GuiTextField searchField;
    private GuiScrollBar scrollBar;
    private GuiImgButton sortByButton;
    private GuiImgButton sortDirButton;
    private GuiImgButton viewItemsButton;

    private int rows = 6;
    private float visAvailable = -1;
    private float visRequired = -1;
    private float discount = 0f;

    public GuiArcaneTerminal(ContainerArcaneTerminal container) {
        super(container);
        this.xSize = 197;
        this.ySize = 183;
        this.ySize += 20 * this.rows;
        this.scrollBar = new GuiScrollBar(175, 18, 106);
        this.repo = new MERepo<>(IItemStorageChannel.class, this.scrollBar);
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;

        this.updateScroll();

        this.addTerminalSlots(8, 18);
    }

    private void addTerminalSlots(int offsetX, int offsetY) {
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < 9; c++) {
                this.addMESlot(new SlotME<>(this.repo, c + r * 9, offsetX + c * 18, offsetY + r * 18));
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.searchField = new GuiTextField(0, this.fontRenderer, this.getGuiLeft() + 98, this.getGuiTop() + 6, 70, 10);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);

        IConfigManager cm = ((IConfigurableObject) this.inventorySlots).getConfigManager();

        // FIXME: Don't use AE Core classes
        this.sortByButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 8, Settings.SORT_BY, cm.getSetting(Settings.SORT_BY));
        this.viewItemsButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 48, Settings.VIEW_MODE, cm.getSetting(Settings.VIEW_MODE));
        this.sortDirButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 28, Settings.SORT_DIRECTION, cm.getSetting(Settings.SORT_DIRECTION));

        this.addButton(this.sortByButton);
        this.addButton(this.viewItemsButton);
        this.addButton(this.sortDirButton);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(this.getGuiBackground());
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.getXSize(), 18);

        for (int i = 0; i < this.rows; i++)
            this.drawTexturedModalRect(this.guiLeft, this.guiTop + 18 + i * 18, 0, 18, this.getXSize(), 18);

        this.drawTexturedModalRect(this.guiLeft, this.guiTop + 16 + this.rows * 18, 0, 106 - 18 - 18, this.getXSize(), 99 + 77);

        this.searchField.drawTextBox();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // TODO: Use lang name
        if (this.visAvailable > -1)
            this.fontRenderer.drawString((int) this.visAvailable + " Available", 90, 130, 4210752);
        if (this.visRequired > -1)
            if (this.visRequired > this.visAvailable)
                this.fontRenderer.drawString("Vis: " + this.visRequired, 93, 145, Color.RED.getRGB());
            else
                this.fontRenderer.drawString("Vis: " + this.visRequired, 93, 145, 4210752);
        if (this.discount > 0f)
            this.fontRenderer.drawString((int) (this.discount * 100) + "% Discount", 90, 204, 4210752);
        this.fontRenderer.drawString("Arcane Terminal", 8, 6, 4210752);
        this.fontRenderer.drawString("Inventory", 8, this.ySize - 96, 4210752);

        if (this.scrollBar != null)
            this.scrollBar.draw(this);
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        EntityPlayer player = this.mc.player;
        if (slot instanceof SlotME) {
            // Send to server for processing
            ActionType action = null;
            IAEStack stack = null;

            switch (type) {
                case PICKUP:
                    action = (mouseButton == 1) ? ActionType.SPLIT_OR_PLACE_SINGLE : ActionType.PICKUP_OR_SETDOWN;
                    stack = ((SlotME) slot).getAEStack();

                    if (stack != null &&
                            action == ActionType.PICKUP_OR_SETDOWN &&
                            stack.getStackSize() == 0 &&
                            player.inventory.getItemStack().isEmpty())
                        action = ActionType.AUTO_CRAFT;
                    break;
                case QUICK_MOVE:
                    action = (mouseButton == 1) ? ActionType.PICKUP_SINGLE : ActionType.SHIFT_MOVE;
                    stack = ((SlotME) slot).getAEStack();
                    break;
                default:
            }
            if (action != null) {
                PacketHandler.sendToServer(new PacketUIAction(action, stack));
            }
            return;
        }
        super.handleMouseClick(slot, slotId, mouseButton, type);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        boolean flag = mouseX >= this.searchField.x &&
                mouseX < this.searchField.x + this.searchField.width &&
                mouseY >= this.searchField.y &&
                mouseY < this.searchField.y + this.searchField.height;

        if (button == 1 && flag) { // Right click should reset the search field
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
        }
        this.searchField.mouseClicked(mouseX, mouseY, button);
        if (this.scrollBar != null) {
            int x = mouseX - this.getGuiLeft();
            int y = mouseY - this.getGuiTop();
            flag = x >= this.scrollBar.getX() &&
                    x <= this.scrollBar.getX() + 15 &&
                    y >= this.scrollBar.getY() &&
                    y <= this.scrollBar.getY() + this.scrollBar.getHeight();
            if (flag)
                this.scrollBar.click(y);
            this.repo.updateView();
            this.updateScroll();
        }


        if (button == 1) {
            for (final GuiButton btn : this.buttonList) {
                if (!btn.mousePressed(this.mc, mouseX, mouseY))
                    continue;
                super.mouseClicked(mouseX, mouseY, 0); // Make the code think we lmb the button
                return;

            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.scrollBar != null) {
            mouseX -= this.getGuiLeft();
            mouseY -= this.getGuiTop();
            boolean flag = mouseX >= this.scrollBar.getX() &&
                    mouseX <= this.scrollBar.getX() + 15 &&
                    mouseY >= this.scrollBar.getY() &&
                    mouseY <= this.scrollBar.getY() + this.scrollBar.getHeight();
            if (flag)
                this.scrollBar.click(mouseY);
            this.repo.updateView();
            this.updateScroll();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int delta = Mouse.getEventDWheel();

        if (delta != 0 && this.scrollBar != null)
            this.scrollBar.wheel(delta);

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.checkHotbarKeys(keyCode))
            return;

        if (typedChar == 'B' && this.scrollBar != null)
            this.scrollBar.wheel(1f);

        // Same keybindings as GuiMEMonitorable to keep consistency across all terminals
        if (AEUtil.getFocusKeyBinding().isActiveAndMatches(keyCode))
            this.searchField.setFocused(!this.searchField.isFocused());

        if (this.searchField.isFocused() && keyCode == Keyboard.KEY_RETURN) {
            this.searchField.setFocused(false);
            return;
        }

        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            this.repo.setSearchString(this.searchField.getText());
            this.repo.updateView();
            this.updateScroll();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof GuiImgButton) {
            GuiImgButton btn = (GuiImgButton) button;
            Enum currentValue = btn.getCurrentValue();
            Enum next = ThEUtil.rotateEnum(currentValue, btn.getSetting().getPossibleValues(), Mouse.isButtonDown(1));
            btn.set(next);
            // TODO: If large terminal/search mode setting call AEConfig.instance().getConfigManager() instead
            PacketHandler.sendToServer(new PacketSettingChange(btn.getSetting(), next));
        }
    }

    @Override
    public void updateSetting(Settings setting, Enum value) {
        super.updateSetting(setting, value);
        this.repo.setSortOrder((SortOrder) ((IConfigurableObject) this.inventorySlots).getConfigManager().getSetting(Settings.SORT_BY));
        this.repo.setSortDir((SortDir) ((IConfigurableObject) this.inventorySlots).getConfigManager().getSetting(Settings.SORT_DIRECTION));
        this.repo.setViewMode((ViewItems) ((IConfigurableObject) this.inventorySlots).getConfigManager().getSetting(Settings.VIEW_MODE));
        this.repo.updateView();
        this.updateScroll();
    }

    public void onMEStorageUpdate(IItemList<IAEItemStack> list) {
        this.repo.clear();
        for (IAEItemStack stack : list)
            this.repo.postUpdate(stack);
        this.repo.updateView();
        this.updateScroll();
    }

    public void setVisInfo(float chunkVis, float visRequired, float discount) {
        this.visAvailable = chunkVis;
        this.visRequired = visRequired;
        this.discount = discount;
    }

    public void updateScroll() {
        this.scrollBar.setRows(this.rows);
        this.scrollBar.setRange(0, (this.repo.size() + 8) / 9 - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/arcane_crafting.png");
    }
}
