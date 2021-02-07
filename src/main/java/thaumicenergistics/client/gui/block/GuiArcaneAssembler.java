package thaumicenergistics.client.gui.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;
import thaumcraft.codechicken.lib.math.MathHelper;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.GuiBase;
import thaumicenergistics.container.block.ContainerArcaneAssembler;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketSubscribe;

import java.awt.*;
import java.util.HashMap;

/**
 * @author Alex811
 */
public class GuiArcaneAssembler extends GuiBase {
    private static final String[] aspects = {"aer", "terra", "ignis", "aqua", "ordo", "perditio"};
    private static final int[][] aspectGUILoc = {{69, 2}, {21, 82}, {21, 25}, {117, 25}, {117, 82}, {69, 106}};
    private static final ResourceLocation BACKGROUND_INACTIVE = new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/arcane_assembler/inactive.png");
    private static final ResourceLocation BACKGROUND_ACTIVE = new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/arcane_assembler/active.png");
    private static final ResourceLocation ASPECTS = new ResourceLocation(ModGlobals.MOD_ID, "textures/gui/arcane_assembler/aspects.png");
    private static final int WIDTH = 210;
    private static final int HEIGHT = 231;
    private final ContainerArcaneAssembler container;
    private final IItemHandler inv;
    private float enAlpha;

    public GuiArcaneAssembler(ContainerArcaneAssembler container){
        super(container);
        this.container = container;
        this.inv = container.getInventory("cores");
        this.enAlpha = inv.getStackInSlot(0).isEmpty() ? 0.0F : 1.0F;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiBackground() {
        return BACKGROUND_INACTIVE;
    }

    @Override
    public void initGui() {
        this.xSize = WIDTH;
        this.ySize = HEIGHT;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(ThEApi.instance().lang().tileArcaneAssembler().getLocalizedKey(), 8, 3, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.getYSize() - 92, 4210752);
        if(!this.inv.getStackInSlot(0).isEmpty()){
            if(this.container.getTE().getAspectExists().containsValue(false))
                this.fontRenderer.drawString(ThEApi.instance().lang().guiOutOfAspect().getLocalizedKey(), 100, this.getYSize() - 92, Color.RED.getRGB());
            if(!this.container.getTE().getHasEnoughVis())
                this.fontRenderer.drawString(ThEApi.instance().lang().guiOutOfVis().getLocalizedKey(), 115, 3, Color.RED.getRGB());
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (this.inv.getStackInSlot(0).isEmpty()) {
            if (this.enAlpha > 0.0F) this.enAlpha -= 0.05F * partialTicks;
        } else {
            if (this.enAlpha < 1.0F) this.enAlpha += 0.05F * partialTicks;
        }

        if(this.enAlpha < 1.0F){
            this.mc.getTextureManager().bindTexture(BACKGROUND_INACTIVE);
            drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
        }
        if(this.enAlpha > 0.0F){
            GL11.glColor4f(1.0F, 1.0F, 1.0F, this.enAlpha);
            this.mc.getTextureManager().bindTexture(BACKGROUND_ACTIVE);
            drawModalRectWithCustomSizedTexture(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        this.mc.getTextureManager().bindTexture(ASPECTS);
        HashMap<String, Boolean> aspectExists = this.container.getTE().getAspectExists();
        for(int i = 0; i < 6; i++){
            Boolean haveAspect = aspectExists.get(aspects[i]);
            int x = aspectGUILoc[i][0];
            int y = aspectGUILoc[i][1];
            if(haveAspect != null && !this.inv.getStackInSlot(0).isEmpty()){   // recipe needs this aspect & we have a KCore
                if(!haveAspect){       // we don't have enough of this aspect
                    float alpha = (float) ((MathHelper.sin((Minecraft.getSystemTime() / 200.0) % (2 * MathHelper.pi)) + 1.0) / 2.0);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha * this.enAlpha);
                }else{
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.2F + this.enAlpha * 0.8F);
                }
            }else{
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.2F + this.enAlpha * 0.5F);
            }
            drawModalRectWithCustomSizedTexture(this.guiLeft + x, this.guiTop + y, x, y, 40, 40, this.xSize, this.ySize);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        PacketHandler.sendToServer(new PacketSubscribe<>(container.getTE(), false)); // unsubscribe from aspect availability updates
    }
}
