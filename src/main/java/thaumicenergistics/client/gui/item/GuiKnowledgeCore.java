package thaumicenergistics.client.gui.item;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.item.ContainerKnowledgeCore;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketUIAction;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alex811
 */
public class GuiKnowledgeCore extends GuiContainer {
    private static final ResourceLocation STATES = new ResourceLocation(ModGlobals.MOD_ID_AE2, "textures/guis/states.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/knowledge_core.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 40;
    private final ContainerKnowledgeCore container;

    public GuiKnowledgeCore(ContainerKnowledgeCore container) {
        super(container);
        this.container = container;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        // Send to server for processing
        switch(container.getGUIAction()){
            case KNOWLEDGE_CORE_ADD:
                if(slotId > -1) container.playWriteSound(mc.player);
                PacketHandler.sendToServer(new PacketUIAction(ActionType.KNOWLEDGE_CORE_ADD, slotId));
                break;
            case KNOWLEDGE_CORE_DEL:
                if(slotId > -1) container.playWriteSound(mc.player);
                PacketHandler.sendToServer(new PacketUIAction(ActionType.KNOWLEDGE_CORE_DEL, slotId));
                break;
            case KNOWLEDGE_CORE_VIEW:
                PacketHandler.sendToServer(new PacketUIAction(ActionType.KNOWLEDGE_CORE_VIEW, slotId));
                break;
        }
    }

    @Override
    public void initGui() {
        this.xSize = WIDTH;
        this.ySize = HEIGHT;
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(STATES);
        switch(container.getGUIAction()){
            case KNOWLEDGE_CORE_ADD: this.drawTexturedModalRect(this.xSize - 22, 0, 0, 9 * 16, 16, 16); break;
            case KNOWLEDGE_CORE_DEL: this.drawTexturedModalRect(this.xSize - 22, 0, 0, 7 * 16, 16, 16); break;
            case KNOWLEDGE_CORE_VIEW: this.drawTexturedModalRect(this.xSize - 22, 0, 16, 9 * 16, 16, 16); break;
        }
        this.fontRenderer.drawString(ThEApi.instance().lang().itemKnowledgeCore().getLocalizedKey(), 8, 5, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY1) {
        this.mc.getTextureManager().bindTexture(TEXTURE);
        drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
    }
}
