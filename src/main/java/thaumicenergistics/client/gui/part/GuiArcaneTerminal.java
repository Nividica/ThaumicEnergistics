package thaumicenergistics.client.gui.part;

import java.awt.*;
import java.io.IOException;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.api.config.*;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.component.GuiSearchField;
import thaumicenergistics.client.gui.helpers.GuiScrollBar;
import thaumicenergistics.client.gui.helpers.MERepo;
import thaumicenergistics.config.ThEConfig;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.part.ContainerArcaneTerminal;
import thaumicenergistics.container.slot.SlotME;
import thaumicenergistics.container.slot.ThESlot;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.jei.ThEJEI;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketOpenGUI;
import thaumicenergistics.network.packets.PacketSettingChange;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.util.AEUtil;
import thaumicenergistics.util.ThEUtil;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * @author BrockWS
 * @author Alex811
 */
public class GuiArcaneTerminal extends GuiAbstractTerminal<IAEItemStack, IItemStorageChannel> {

    protected ContainerArcaneTerminal container;

    private GuiSearchField searchField;
    protected GuiScrollBar scrollBar;
    private GuiImgButton sortByButton;
    private GuiImgButton viewItemsButton;
    private GuiImgButton sortDirButton;
    private GuiImgButton searchModeButton;
    private GuiImgButton terminalSizeButton;
    private GuiImgButton clearButton;
    private GuiTabButton craftingStatusBtn;
    private boolean isAutoFocus = false;
    private static String memoryText = "";

    protected int rows = 6;
    private float visAvailable = -1;
    protected float visRequired = -1;
    private float discount = 0f;

    public GuiArcaneTerminal(ContainerArcaneTerminal container) {
        super(container);
        this.container = container;
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.repo = new MERepo<>(IItemStorageChannel.class);
    }

    public void initSearchField(){
        SearchBoxMode searchModeSetting = ThEApi.instance().config().searchBoxMode();
        this.isAutoFocus = Stream.of(SearchBoxMode.AUTOSEARCH, SearchBoxMode.JEI_AUTOSEARCH, SearchBoxMode.AUTOSEARCH_KEEP, SearchBoxMode.JEI_AUTOSEARCH_KEEP).anyMatch(m -> m == searchModeSetting);
        boolean isKeepFilter = Stream.of(SearchBoxMode.AUTOSEARCH_KEEP, SearchBoxMode.JEI_AUTOSEARCH_KEEP, SearchBoxMode.MANUAL_SEARCH_KEEP, SearchBoxMode.JEI_MANUAL_SEARCH_KEEP).anyMatch(m -> m == searchModeSetting);
        boolean isJEIEnabled = Stream.of(SearchBoxMode.JEI_AUTOSEARCH, SearchBoxMode.JEI_MANUAL_SEARCH).anyMatch(m -> m == searchModeSetting);
        this.searchField.setText("");
        this.searchField.setFocused(this.isAutoFocus);
        if(isJEIEnabled) memoryText = ThEJEI.getSearchText();
        if(isKeepFilter && !memoryText.isEmpty()){
            this.searchField.setText(memoryText);
            this.searchField.selectAll();
            this.repo.setSearchString(memoryText);
            this.repo.updateView();
            this.updateScroll();
        }
    }

    @Override
    public void initGui() {
        this.xSize = 201;
        this.ySize = 190;

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
        this.addTerminalSlots(8, 18);

        this.buttonList.clear();
        super.initGui();

        this.searchField = new GuiSearchField(this.fontRenderer, this.getGuiLeft() + 98, this.getGuiTop() + 6, 70, 10);
        this.searchField.setVisible(true);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);

        this.initSearchField();

        IConfigManager cm = ((IConfigurableObject) this.inventorySlots).getConfigManager();

        // FIXME: Don't use AE Core classes
        this.sortByButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 8, Settings.SORT_BY, cm.getSetting(Settings.SORT_BY));
        this.viewItemsButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 28, Settings.VIEW_MODE, cm.getSetting(Settings.VIEW_MODE));
        this.sortDirButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 48, Settings.SORT_DIRECTION, cm.getSetting(Settings.SORT_DIRECTION));
        this.searchModeButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 68, Settings.SEARCH_MODE, ThEApi.instance().config().searchBoxMode());
        this.terminalSizeButton = new GuiImgButton(this.getGuiLeft() - 18, this.getGuiTop() + 88, Settings.TERMINAL_STYLE, ThEApi.instance().config().terminalStyle());
        this.clearButton = new GuiImgButton(this.getGuiLeft() + 87, this.getGuiTop() + this.getYSize() - 156, Settings.ACTIONS, ActionItems.STASH);
        this.clearButton.setHalfSize(true);
        this.craftingStatusBtn = new GuiTabButton(this.guiLeft + 170, this.guiTop - 4, 2 + 11 * 16, GuiText.CraftingStatus.getLocal(), this.itemRender);
        this.craftingStatusBtn.setHideEdge(13);

        this.addButton(this.sortByButton);
        this.addButton(this.viewItemsButton);
        this.addButton(this.sortDirButton);
        this.addButton(this.searchModeButton);
        this.addButton(this.terminalSizeButton);
        this.addButton(this.clearButton);
        this.addButton(this.craftingStatusBtn);

        this.inventorySlots.inventorySlots.forEach(this::recalcSlotY);
    }

    protected void recalcSlotY(Slot slot){
        if (slot instanceof ThESlot)
            ((ThESlot) slot).recalculateY(this.rows);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        this.fontRenderer.drawString(ThEApi.instance().lang().guiVisRequiredOutOf().getLocalizedKey(this.visRequired > -1 ? this.visRequired : 0, (int) (this.visAvailable > -1 ? this.visAvailable : 0)), 35, this.getYSize() - 168, this.visRequired > this.visAvailable ? Color.RED.getRGB() : 4210752);

        if (this.discount > 0f)
            this.fontRenderer.drawString(ThEApi.instance().lang().guiVisDiscount().getLocalizedKey((int) (this.discount * 100)), 90, this.getYSize() - 94, 4210752);

        this.fontRenderer.drawString(ThEApi.instance().lang().guiArcaneTerminal().getLocalizedKey(), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.getYSize() - 91, 4210752);

        if (this.scrollBar != null)
            this.scrollBar.draw(this);
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
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 1 && mouseWithin(searchField)) { // Right click should reset the search field
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
        }
        this.searchField.mouseClicked(mouseX, mouseY, button);
        if (this.scrollBar != null) {
            if (mouseWithin(scrollBar))
                this.scrollBar.click(mouseY - this.getGuiTop());
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
                case CLONE:
                    action = ActionType.AUTO_CRAFT;
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
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.checkHotbarKeys(keyCode) && container.inventorySlots.stream()
                .filter(slot -> !(slot instanceof SlotME))
                .anyMatch(this::mouseWithin))
            return; // abort if it was a hotbar key and the mouse is over a normal slot

        if (typedChar == 'B' && this.scrollBar != null)
            this.scrollBar.wheel(1f);

        // Same keybindings as GuiMEMonitorable to keep consistency across all terminals
        if (AEUtil.getFocusKeyBinding().isActiveAndMatches(keyCode))
            this.searchField.setFocused(!this.searchField.isFocused());

        if (this.searchField.isFocused() && keyCode == Keyboard.KEY_RETURN) {
            this.searchField.setFocused(false);
            return;
        }

        if (this.isAutoFocus && !this.searchField.isFocused() && mouseWithin())
            this.searchField.setFocused(true);

        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            this.repo.setSearchString(this.searchField.getText());
            this.repo.updateView();
            this.updateScroll();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void reload() {
        this.initGui();
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        memoryText = this.searchField.getText();
    }

    @Override
    public void updateSetting(Settings setting, Enum value) {
        super.updateSetting(setting, value);

        IConfigManager cm = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        this.repo.setSortOrder((SortOrder) cm.getSetting(Settings.SORT_BY));
        this.repo.setViewMode((ViewItems) cm.getSetting(Settings.VIEW_MODE));
        this.repo.setSortDir((SortDir) cm.getSetting(Settings.SORT_DIRECTION));
        this.repo.updateView();
        this.updateScroll();
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/arcane_crafting_no_side.png");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == this.clearButton) {
            PacketHandler.sendToServer(new PacketUIAction(ActionType.CLEAR_GRID));
            return;
        }
        if (button == this.craftingStatusBtn) {
            PacketHandler.sendToServer(new PacketOpenGUI(ModGUIs.AE2_CRAFT_STATUS, this.container.getPartPos(), this.container.getPartSide()));
            return;
        }
        if (button instanceof GuiImgButton) {
            GuiImgButton btn = (GuiImgButton) button;
            Enum currentValue = btn.getCurrentValue();
            Enum next = ThEUtil.rotateEnum(currentValue, btn.getSetting().getPossibleValues(), Mouse.isButtonDown(1));
            btn.set(next);
            if (btn.getSetting() == Settings.TERMINAL_STYLE) {
                ThEConfig.client.terminalStyle = (TerminalStyle) next;
                ThEConfig.save();
                this.reload();
                return;
            }else if (btn.getSetting() == Settings.SEARCH_MODE){
                ThEConfig.client.searchBoxMode = (SearchBoxMode) next;
                ThEConfig.save();
                this.initSearchField();
                return;
            }
            PacketHandler.sendToServer(new PacketSettingChange(btn.getSetting(), next));
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int delta = Mouse.getEventDWheel();

        if (delta != 0) {
            if (isShiftKeyDown()) {
                Slot slot = this.getSlotUnderMouse();
                if (slot instanceof SlotME && slot.getHasStack()) {
                    delta = delta / Math.abs(delta);
                    for (int k = 0; k < Math.abs(delta); k++) {
                        PacketHandler.sendToServer(new PacketUIAction(delta > 0 ? ActionType.SCROLL_DOWN : ActionType.SCROLL_UP, ((SlotME) slot).getAEStack()));
                    }
                }
            } else if (this.scrollBar != null)
                this.scrollBar.wheel(delta);
        }
    }

    private void addTerminalSlots(int offsetX, int offsetY) {
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < 9; c++) {
                this.addMESlot(new SlotME<>(this.repo, c + r * 9, offsetX + c * 18, offsetY + r * 18));
            }
        }
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
}
