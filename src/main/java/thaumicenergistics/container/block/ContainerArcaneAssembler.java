package thaumicenergistics.container.block;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.container.slot.SlotKnowledgeCore;
import thaumicenergistics.container.slot.SlotUpgrade;
import thaumicenergistics.item.ItemKnowledgeCore;
import thaumicenergistics.item.ItemMaterial;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketAssemblerGUIUpdate;
import thaumicenergistics.network.packets.PacketPlaySound;
import thaumicenergistics.tile.TileArcaneAssembler;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.ItemHandlerUtil;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alex811
 */
public class ContainerArcaneAssembler extends ContainerBase {
    protected TileArcaneAssembler TE;

    public ContainerArcaneAssembler(EntityPlayer player, TileArcaneAssembler TE) {
        super(player);
        this.TE = TE;
        this.addSlotToContainer(new SlotKnowledgeCore(this.getInventory("cores"), 0, 81, 66));
        for(int i = 0; i < this.getInventory("upgrades").getSlots(); i++)
            this.addSlotToContainer(new SlotUpgrade(this.getInventory("upgrades"), i, 186, 8 + i * 18));
        this.bindPlayerInventory(new PlayerMainInvWrapper(player.inventory), 0, 147);
        this.addListener(new KnowledgeCoreSlotListener());
        if(ForgeUtil.isServer())
            TE.subscribe(player);   // subscribe to aspect availability updates
    }

    public IItemHandler getInventory(String name) {
        return this.TE.getInventoryByName(name);
    }

    public TileArcaneAssembler getTE() {
        return TE;
    }

    public void playCoreSound(EntityPlayer player){ // plays the right sound, when the Knowledge Core gets removed or placed in the slot
        if(this.getInventory("cores").getStackInSlot(0).isEmpty()) {
            player.world.playSound(player, TE.getPos(), new SoundEvent(ThEApi.instance().sounds().knowledgeCorePowerDown()), SoundCategory.BLOCKS, 1, 1);
            PacketHandler.sendToPlayer((EntityPlayerMP) player, new PacketPlaySound(TE.getPos(), ThEApi.instance().sounds().knowledgeCorePowerDown(), SoundCategory.BLOCKS, 1, 1));
        }else {
            player.world.playSound(player, TE.getPos(), new SoundEvent(ThEApi.instance().sounds().knowledgeCorePowerUp()), SoundCategory.BLOCKS, 1, 1);
            PacketHandler.sendToPlayer((EntityPlayerMP) player, new PacketPlaySound(TE.getPos(), ThEApi.instance().sounds().knowledgeCorePowerUp(), SoundCategory.BLOCKS, 1, 1));
        }
    }

    private class KnowledgeCoreSlotListener implements IContainerListener{
        private boolean opened = false;

        @Override
        @ParametersAreNonnullByDefault
        public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
            if(slotInd == 0 && opened && ForgeUtil.isServer()) {
                IGridNode node = ContainerArcaneAssembler.this.TE.getActionableNode();
                ContainerArcaneAssembler.this.playCoreSound(ContainerArcaneAssembler.this.player);
                node.getGrid().postEvent(new MENetworkCraftingPatternChange(ContainerArcaneAssembler.this.TE, node)); // update ME system available patterns
            }
            opened = true;
        }

        // ignored //
        @Override
        @ParametersAreNonnullByDefault
        public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}
        @Override
        @ParametersAreNonnullByDefault
        public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}
        @Override
        @ParametersAreNonnullByDefault
        public void sendAllWindowProperties(Container containerIn, IInventory inventory) {}
    }

    @Override
    protected void handleQuickMove(Slot slot, ItemStack itemStack) {
        Item item = itemStack.getItem();
        if(item instanceof ItemKnowledgeCore)
            ItemHandlerUtil.quickMoveSlot(this.getInventory("cores"), slot);
        else if(item instanceof ItemMaterial || item instanceof appeng.items.materials.ItemMaterial)
            ItemHandlerUtil.quickMoveSlot(this.getInventory("upgrades"), slot);
    }
}
