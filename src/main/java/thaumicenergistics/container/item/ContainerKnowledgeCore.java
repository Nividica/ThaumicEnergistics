package thaumicenergistics.container.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.part.ContainerArcaneInscriber;
import thaumicenergistics.container.slot.SlotGhost;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.network.packets.PacketUIAction;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.KnowledgeCoreUtil;
import thaumicenergistics.util.inventory.ThEInternalInventory;

/**
 * @author Alex811
 */
public class ContainerKnowledgeCore extends ContainerBase {
    private static final int SLOT_NUM = 9;
    private final ModGUIs GUIAction;
    private ItemStack knowledgeCoreStack;
    private ContainerArcaneInscriber inscriber;
    private ThEInternalInventory inventory;

    public ContainerKnowledgeCore(EntityPlayer player, ModGUIs GUIAction, Container inscriber) {
        super(player);
        this.GUIAction = GUIAction;
        try{
            this.inscriber = (ContainerArcaneInscriber) inscriber;
            this.knowledgeCoreStack = this.inscriber.getInventory("upgrades").getStackInSlot(0);
            initInv();
            addSlots(8, 15);
        }catch(ClassCastException ex){
            ex.printStackTrace();
        }
    }

    private void initInv(){
        inventory = new ThEInternalInventory("KCore", 9, 1);
    }

    private void addSlots(int offsetX, int offsetY){
        for (int i = 0; i < SLOT_NUM; i++) {
            SlotGhost slotGhost = new SlotGhost(inventory, i, offsetX + (i * 18), offsetY);
            if(KnowledgeCoreUtil.hasRecipe(knowledgeCoreStack, i)){
                KnowledgeCoreUtil.Recipe recipe = KnowledgeCoreUtil.getRecipe(knowledgeCoreStack, i);
                if(recipe != null) slotGhost.putStack(recipe.getResult());
            }
            this.addSlotToContainer(slotGhost);
        }
    }

    @Override
    public void onAction(EntityPlayerMP player, PacketUIAction packet) {
        if(ForgeUtil.isServer()) {
            if(packet.index > -1)
                switch (packet.action) {
                    case KNOWLEDGE_CORE_ADD:
                        playWriteSound(player);
                        ThEInternalInventory ingredients = (ThEInternalInventory) ((InvWrapper) inscriber.getInventory("crafting")).getInv();
                        ItemStack result = inscriber.getInventory("result").getStackInSlot(0);
                        KnowledgeCoreUtil.setRecipe(knowledgeCoreStack, packet.index, new KnowledgeCoreUtil.Recipe(ingredients, result, inscriber.getCurrentRequiredVis()));
                        break;
                    case KNOWLEDGE_CORE_DEL:
                        playWriteSound(player);
                        KnowledgeCoreUtil.setRecipe(knowledgeCoreStack, packet.index, null);
                        break;
                }
            if(KnowledgeCoreUtil.isEmpty(knowledgeCoreStack))
                ThEApi.instance().items().blankKnowledgeCore().maybeStack(1).ifPresent(blank -> ((InvWrapper) inscriber.getInventory("upgrades")).getInv().setInventorySlotContents(0, blank));
            GuiHandler.openGUI(ModGUIs.ARCANE_INSCRIBER, player, inscriber.getPartPos(), inscriber.getPartSide());
        }
    }

    public void playWriteSound(EntityPlayer player){
        player.world.playSound(player, inscriber.getPartPos(), new SoundEvent(ThEApi.instance().sounds().knowledgeCoreWrite()), SoundCategory.BLOCKS, 1, 1);
    }

    public ModGUIs getGUIAction() {
        return GUIAction;
    }

    public ItemStack getKnowledgeCoreStack() {
        return knowledgeCoreStack;
    }

    public ContainerArcaneInscriber getInscriber() {
        return inscriber;
    }
}
