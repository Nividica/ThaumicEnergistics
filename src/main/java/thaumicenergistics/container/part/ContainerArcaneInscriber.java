package thaumicenergistics.container.part;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigurableObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.ICraftingContainer;
import thaumicenergistics.container.slot.SlotArcaneGhostMatrix;
import thaumicenergistics.container.slot.SlotArcaneResult;
import thaumicenergistics.container.slot.SlotKnowledgeCore;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.integration.thaumcraft.TCCraftingManager;
import thaumicenergistics.item.ItemKnowledgeCore;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketIsArcaneUpdate;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.part.PartArcaneInscriber;
import thaumicenergistics.util.*;
import thaumicenergistics.util.inventory.ThEInternalInventory;

/**
 * @author Alex811
 */
public class ContainerArcaneInscriber extends ContainerArcaneTerminal implements IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingContainer, IConfigurableObject {

    public boolean recipeIsArcane = false;

    public ContainerArcaneInscriber(EntityPlayer player, PartArcaneInscriber part) {
        super(player, part);
    }

    @Override
    public void onAction(EntityPlayerMP player, PacketUIAction packet) {
        super.onAction(player, packet);
        if(ForgeUtil.isServer()){
            ItemStack knowledgeCore = this.getInventory("upgrades").getStackInSlot(0);
            if(knowledgeCore.isEmpty()) return;
            boolean currentIsBlank = ((ItemKnowledgeCore) knowledgeCore.getItem()).isBlank();
            ItemStack result = this.getInventory("result").getStackInSlot(0);
            if(packet.action == ActionType.KNOWLEDGE_CORE_ADD && !result.isEmpty() && this.recipeIsArcane) {
               if(currentIsBlank)
                    ThEApi.instance().items().knowledgeCore().maybeStack(1).ifPresent(newCore -> {
                        ((InvWrapper) this.getInventory("upgrades")).getInv().setInventorySlotContents(0, newCore);
                    });
               else if(KnowledgeCoreUtil.hasRecipe(knowledgeCore, result.getItem())) return;
               GuiHandler.openGUI(ModGUIs.KNOWLEDGE_CORE_ADD, player, this.part.getLocation().getPos(), this.part.side);
            }else if(packet.action == ActionType.KNOWLEDGE_CORE_DEL && !currentIsBlank)
                GuiHandler.openGUI(ModGUIs.KNOWLEDGE_CORE_DEL, player, this.part.getLocation().getPos(), this.part.side);
            else if(packet.action == ActionType.KNOWLEDGE_CORE_VIEW && !currentIsBlank)
                GuiHandler.openGUI(ModGUIs.KNOWLEDGE_CORE_VIEW, player, this.part.getLocation().getPos(), this.part.side);
        }
    }

    public void refreshIsArcane(){
        if (ForgeUtil.isClient()) return;
        boolean recipeIsArcane;
        InvWrapper crafting = (InvWrapper) this.getInventory("crafting");
        if(this.recipe != null && !crafting.getInv().isEmpty())
            recipeIsArcane = (TCCraftingManager.findArcaneRecipe(crafting, this.player) != null);
        else recipeIsArcane = false;
        if(this.recipeIsArcane != recipeIsArcane) {
            this.recipeIsArcane = recipeIsArcane;
            PacketHandler.sendToPlayer((EntityPlayerMP) this.player, new PacketIsArcaneUpdate(recipeIsArcane));
        }
    }

    @Override
    public void onMatrixChanged() {
        super.onMatrixChanged();
        refreshIsArcane();
    }

    @Override
    protected void clearCrafting() {
        IItemHandler crafting = this.getInventory("crafting");
        for (int slot = 0; slot < crafting.getSlots(); slot++)
            crafting.extractItem(slot, crafting.getStackInSlot(slot).getCount(), false);
    }

    @Override
    protected float getRequiredVis(IRecipe recipe, EntityPlayer player) {
        if (!(recipe instanceof IArcaneRecipe))
            return -1;
        return ((IArcaneRecipe) recipe).getVis();
    }

    @Override
    public void handleJEITransfer(EntityPlayer player, NBTTagCompound tag) {
        NBTBase normal = tag.getTag("normal");
        NBTBase crystals = tag.getTag("crystal");

        clearCrafting();
        this.onMatrixChanged();

        handleJEITag(0, normal, true);
        handleJEITag(9, crystals, false);

        this.onMatrixChanged();
    }

    private void handleJEITag(int startAtSlot, NBTBase ingredientGroup, boolean mustBeSingle){
        IItemHandler crafting = this.getInventory("crafting");
        IItemHandler playerInv = this.getInventory("player");

        if (ingredientGroup == null || ingredientGroup.isEmpty()) {
            // TODO: Probably check if its already in the slot
            return;
        }
        NBTTagList subs = (NBTTagList) ingredientGroup;
        for (int i = 0; i < subs.tagCount(); i++) {
            int slot = startAtSlot + i;
            NBTTagCompound ingredient = ((NBTTagList) subs.get(i)).getCompoundTagAt(0);
            ItemStack stack = new ItemStack(ingredient);
            if (stack.isEmpty()) continue;

            ThELog.debug("Adding {} for {}", stack.getDisplayName(), slot);
            IAEItemStack aeStack = this.channel.createStack(stack);
            if (aeStack == null) {
                ThELog.warn("Failed to create IAEItemStack for {}, report to developer!", stack.toString());
                continue;
            }
            IAEItemStack aeExtract = AEUtil.inventoryExtract(aeStack, this.monitor, this.part.source, null, Actionable.SIMULATE);
            if (aeExtract != null && aeExtract.getStackSize() > 0) {
                ItemStack AEExtractStack = aeExtract.createItemStack();
                if(mustBeSingle) AEExtractStack.setCount(1);
                crafting.insertItem(slot, AEExtractStack, false);
            }

            if (!crafting.getStackInSlot(slot).isEmpty()) // We managed to pull everything from the system
                continue;

            // Try pull from player
            ThELog.debug("Failed to pull item from ae inv, trying player inventory");

            ItemStack invExtract = ItemHandlerUtil.extract(playerInv, stack, true);
            if (!invExtract.isEmpty()) {
                if(mustBeSingle) invExtract.setCount(1);
                crafting.insertItem(slot, invExtract, false);
            }
        }
        ThELog.debug("Failed to find valid item");
    }

    @Override
    public ItemStack onCraft(ItemStack toCraft) {
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("SameParameterValue")
    @Override
    protected void addMatrixSlots(int offsetX, int offsetY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.addSlotToContainer(new SlotArcaneGhostMatrix(this, i * 3 + j, offsetX + (j * 18), offsetY + (i * 18)));
            }
        }
        offsetX += 104;
        for (int i = 0; i < 3; i++) { // Y
            for (int j = 0; j < 2; j++) { // X
                this.addSlotToContainer(new SlotArcaneGhostMatrix(this, 9 + (i * 2 + j), offsetX + (j * 18), offsetY + (i * 18)));
            }
        }
        offsetX -= 104;
        this.craftingResult = new ThEInternalInventory("Result", 1, 64);
        this.addSlotToContainer(this.resultSlot = new SlotArcaneResult(this, this.player, 0, offsetX + 84, offsetY + 18));
        this.onMatrixChanged();
    }

    @Override
    protected void addUpgradeSlots(int offsetX, int offsetY) {
        this.addSlotToContainer(new SlotKnowledgeCore(this.getInventory("upgrades"), 0, offsetX, offsetY));
    }

    @Override
    protected float getWorldVis(){
        return Float.MAX_VALUE;
    }

    @Override
    protected float getDiscount(EntityPlayer player){
        return 0;
    }
}
