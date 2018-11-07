package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import appeng.api.config.Settings;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.render.StackSizeRenderer;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.ISlotOptional;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.container.slot.SlotME;

/**
 * @author BrockWS
 */
public abstract class GuiBase extends GuiContainer {

    private static StackSizeRenderer stackSizeRenderer = new StackSizeRenderer();

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
        } else if (slot instanceof SlotME && ((SlotME) slot).getAEStack() instanceof IAEItemStack) {
            SlotME slotME = (SlotME) slot;
            super.drawSlot(slot);
            stackSizeRenderer.renderStackSize(this.fontRenderer, (IAEItemStack) slotME.getAEStack(), slot.xPos, slot.yPos);
            return;
        }
        super.drawSlot(slot);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (this.hoveredSlot != null) {
            if (this.hoveredSlot instanceof SlotGhostEssentia && ((SlotGhostEssentia) this.hoveredSlot).getAspect() != null) {
                this.drawHoveringText(((SlotGhostEssentia) this.hoveredSlot).getAspect().getName(), mouseX, mouseY);
                return;
            }
            if (this.hoveredSlot instanceof SlotME && this.hoveredSlot.getHasStack() && ((SlotME) this.hoveredSlot).getAEStack() instanceof IAEEssentiaStack) {
                IAEEssentiaStack stack = (IAEEssentiaStack) ((SlotME) this.hoveredSlot).getAEStack();
                List<String> tooltip = new ArrayList<>();
                tooltip.add(stack.getAspect().getName());
                tooltip.add(Long.toString(stack.getStackSize()));
                this.drawHoveringText(tooltip, mouseX, mouseY);
                return;
            }
        }

        // TODO: Don't use AE2 Core classes
        for (GuiButton c : this.buttonList) {
            if (!(c instanceof ITooltip) || !((ITooltip) c).isVisible())
                continue;
            ITooltip t = (ITooltip) c;
            int x = t.xPos();
            int y = t.yPos();
            if (mouseX >= x && mouseX <= x + t.getWidth() && mouseY >= y && mouseY <= y + t.getHeight()) {
                if (t.getMessage() == null || t.getMessage().isEmpty())
                    continue;

                if (y < 15)
                    y = 15;

                List<String> lines = Arrays.asList(t.getMessage().split("\n"));
                // AE2 Has the first line as WHITE and the rest as GRAY
                lines.set(0, TextFormatting.WHITE + lines.get(0));
                for (int i = 1; i < lines.size(); i++)
                    lines.set(i, TextFormatting.GRAY + lines.get(i));

                this.drawHoveringText(lines, x + 11, y + 4, this.fontRenderer);
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    /**
     * Called when a PacketSettingChange is received (Client and Server)
     *
     * @param setting Setting changed
     * @param value   New Value
     */
    public void updateSetting(Settings setting, Enum value) {
        if (this.inventorySlots instanceof IConfigurableObject) {
            IConfigManager configManager = ((IConfigurableObject) this.inventorySlots).getConfigManager();
            configManager.putSetting(setting, value);
            this.buttonList.forEach(btn -> {
                if (!(btn instanceof GuiImgButton))
                    return;
                GuiImgButton b = (GuiImgButton) btn;
                b.set(configManager.getSetting(b.getSetting()));
            });
        }
    }

    protected void addMESlot(SlotME slot) {
        slot.slotNumber = this.inventorySlots.inventorySlots.size();
        this.inventorySlots.inventorySlots.add(slot);
        this.inventorySlots.inventoryItemStacks.add(ItemStack.EMPTY);
    }

    protected ResourceLocation getGuiBackground() {
        return null;
    }
}
