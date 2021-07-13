package thaumicenergistics.client.gui.part;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.component.GuiImageButton;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.part.ContainerArcaneInscriber;
import thaumicenergistics.container.slot.ThEGhostSlot;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.item.ItemKnowledgeCore;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.util.KnowledgeCoreUtil;

import java.util.ArrayList;

/**
 * @author Alex811
 */
public class GuiArcaneInscriber extends GuiArcaneTerminal {

    private GuiImageButton coreAddButton;
    private GuiImageButton coreDelButton;
    private GuiImageButton coreViewButton;
    private final ResourceLocation images = new ResourceLocation(ModGlobals.MOD_ID_AE2, "textures/guis/states.png");

    public GuiArcaneInscriber(ContainerArcaneInscriber container) {
        super(container);
    }

    @Override
    public void initGui() {
        super.initGui();
        ArrayList<GuiImageButton> coreButtons = new ArrayList<>();
        int coreBtnRowY = this.guiTop + 90;

        coreButtons.add(coreAddButton = new GuiImageButton(this.getGuiLeft() + 87, coreBtnRowY, 0, 12, images));
        coreButtons.add(coreDelButton = new GuiImageButton(this.getGuiLeft() + 104, coreBtnRowY, 0, 7, images));
        coreButtons.add(coreViewButton = new GuiImageButton(this.getGuiLeft() + 121, coreBtnRowY, 1, 12, images));

        coreButtons.forEach(btn -> {
            addButton(btn);
            btn.recalculateY(this.rows);
        });
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        ItemStack knowledgeCore = this.container.getInventory("upgrades").getStackInSlot(0);
        boolean currentIsBlank = knowledgeCore.getItem().getClass() != ItemKnowledgeCore.class;
        ItemStack result = this.container.getInventory("result").getStackInSlot(0);
        boolean hasRecipe = !result.isEmpty();
        boolean recipeExists = KnowledgeCoreUtil.hasRecipe(knowledgeCore, result.getItem());
        if(button == coreAddButton && !knowledgeCore.isEmpty() && hasRecipe && ((ContainerArcaneInscriber)this.container).recipeIsArcane && !recipeExists){
            PacketHandler.sendToServer(new PacketUIAction(ActionType.KNOWLEDGE_CORE_ADD));
        }else if(button == coreDelButton && !knowledgeCore.isEmpty() && !currentIsBlank){
            PacketHandler.sendToServer(new PacketUIAction(ActionType.KNOWLEDGE_CORE_DEL));
        }else if(button == coreViewButton && !knowledgeCore.isEmpty() && !currentIsBlank){
            PacketHandler.sendToServer(new PacketUIAction(ActionType.KNOWLEDGE_CORE_VIEW));
        }
    }

    public void setIsArcane(boolean isArcane){
        ((ContainerArcaneInscriber) this.container).recipeIsArcane = isArcane;
    }

    @Override
    protected void recalcSlotY(Slot slot){
        super.recalcSlotY(slot);
        if (slot instanceof ThEGhostSlot)
            ((ThEGhostSlot) slot).recalculateY(this.rows);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.setCurrMousePos(mouseX, mouseY);

        this.fontRenderer.drawString(ThEApi.instance().lang().guiVisRequired().getLocalizedKey(this.visRequired > -1 ? this.visRequired : 0), 60, this.getYSize() - 168, 4210752);
        this.fontRenderer.drawString(ThEApi.instance().lang().guiArcaneInscriber().getLocalizedKey(), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.getYSize() - 91, 4210752);

        if (this.scrollBar != null)
            this.scrollBar.draw(this);

        ItemStack knowledgeCore = this.container.getInventory("upgrades").getStackInSlot(0);
        boolean hasArcaneRecipe = ((ContainerArcaneInscriber) this.container).recipeIsArcane;
        ItemStack result = this.container.getInventory("result").getStackInSlot(0);
        boolean hasRecipe = !result.isEmpty();
        boolean recipeExists = KnowledgeCoreUtil.hasRecipe(knowledgeCore, result.getItem());
        boolean currentIsBlank = knowledgeCore.getItem().getClass() != ItemKnowledgeCore.class;
        if(!knowledgeCore.isEmpty()){
            renderButton(coreAddButton, hasRecipe && hasArcaneRecipe && !recipeExists);
            if(currentIsBlank) {
                renderButton(coreViewButton, false);
                renderButton(coreDelButton, false);
                if((coreViewButton.isHovered() || coreDelButton.isHovered()))
                    renderText(ThEApi.instance().lang().guiKnowledgeCoreBlank().getLocalizedKey(), mouseX, mouseY);
            }else{
                renderButton(coreViewButton, true);
                renderButton(coreDelButton, true);
            }
            if(coreAddButton.isHovered()){
                if(hasRecipe){
                    if(!hasArcaneRecipe) renderText(ThEApi.instance().lang().guiRecipeNotArcane().getLocalizedKey(), mouseX, mouseY);
                    else if(recipeExists) renderText(ThEApi.instance().lang().guiRecipeAlreadyStored().getLocalizedKey(), mouseX, mouseY);
                }else renderText(ThEApi.instance().lang().guiNoRecipe().getLocalizedKey(), mouseX, mouseY);
            }
        }else{
            renderButton(coreAddButton, false);
            renderButton(coreViewButton, false);
            renderButton(coreDelButton, false);
            if((coreViewButton.isHovered() || coreDelButton.isHovered() || coreAddButton.isHovered()))
                renderText(ThEApi.instance().lang().guiInsertKnowledgeCore().getLocalizedKey(), mouseX, mouseY);
        }
    }

    protected void renderButton(GuiImageButton button, boolean enabled){
        button.enabled = enabled;
        if(enabled){
            button.setButtonAlpha(1.0F, 1.0F);
            if(button == coreDelButton) coreDelButton.setImageAlpha(1.0F, 1.0F);
            else button.setAllImages(images, (button == coreViewButton) ? button.width : 0, button.height * 11);
        }else{
            coreDelButton.setImageAlpha(0.5F, 0.5F);
            if(button == coreDelButton) coreDelButton.setImageAlpha(0.5F, 0.5F);
            else button.setAllImages(images, (button == coreViewButton) ? button.width : 0, button.height * 12);
        }
    }

    protected void renderText(String text, int x, int y){
        this.drawCenteredString(mc.fontRenderer, text, x - this.getGuiLeft(), y - this.getGuiTop() - 5, 0x00e6ac);
    }
}
