package thaumicenergistics.client.gui;

import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.ITooltip;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.client.gui.helpers.GenericStackSizeRenderer;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.ISlotOptional;
import thaumicenergistics.container.slot.SlotGhostEssentia;
import thaumicenergistics.container.slot.SlotME;
import thaumicenergistics.container.slot.ThESlot;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class GuiBase extends GuiContainer {

    private static final GenericStackSizeRenderer stackSizeRenderer = new GenericStackSizeRenderer();

    public GuiBase(ContainerBase container) {
        super(container);
    }

    public void reload() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0f, 1.0F);
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
        } else if (slot instanceof SlotME) {
            SlotME slotME = (SlotME) slot;
            super.drawSlot(slot);
            stackSizeRenderer.renderStackSize(this.fontRenderer, slotME.getAEStack(), slot.xPos, slot.yPos);
            return;
        } else if (slot instanceof ThESlot) {
            if (((ThESlot) slot).hasBackgroundIcon()) {
                int index = ((ThESlot) slot).getBackgroundIconIndex();
                int uv_y = (int) Math.floor((double) index / 16);
                int uv_x = index - uv_y * 16;

                Minecraft.getMinecraft().getTextureManager().bindTexture(((ThESlot) slot).getBackgroundIcon());

                GlStateManager.enableBlend();
                GlStateManager.disableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                this.drawTexturedModelRectColor(slot.xPos, slot.yPos, uv_x * 16, uv_y * 16, 16, 16, new Color(1f, 1f, 1f, 0.4f));

                //GlStateManager.enableLighting();
            }
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
                this.drawHoveringText(stack.getAspect().getName(), mouseX, mouseY);
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
                if (Stream.of(Settings.ACTIONS, Settings.TERMINAL_STYLE, Settings.SEARCH_MODE).anyMatch(s -> s == b.getSetting()))
                    return;
                b.set(configManager.getSetting(b.getSetting()));
            });
        }
    }

    protected void addMESlot(SlotME slot) {
        slot.slotNumber = this.inventorySlots.inventorySlots.size();
        this.inventorySlots.inventorySlots.add(slot);
        this.inventorySlots.inventoryItemStacks.add(ItemStack.EMPTY);
    }

    protected abstract ResourceLocation getGuiBackground();

    protected void drawTexturedModelRectColor(int x, int y, int textureX, int textureY, int width, int height, Color color) {
        float offsetX = 0.00390625F;
        float offsetY = 0.00390625F;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buf.pos(x, y + height, this.zLevel).tex(textureX * offsetX, (textureY + height) * offsetY).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buf.pos(x + width, y + height, this.zLevel).tex((textureX + width) * offsetX, (textureY + height) * offsetY).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buf.pos(x + width, y, this.zLevel).tex((textureX + width) * offsetX, textureY * offsetY).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        buf.pos(x, y, this.zLevel).tex(textureX * offsetX, textureY * offsetY).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        tess.draw();
    }
}
