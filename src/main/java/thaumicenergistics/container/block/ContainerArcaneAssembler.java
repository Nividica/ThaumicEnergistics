package thaumicenergistics.container.block;

import appeng.api.networking.events.MENetworkCraftingPatternChange;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
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
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketAssemblerGUIUpdate;
import thaumicenergistics.network.packets.PacketPlaySound;
import thaumicenergistics.tile.TileArcaneAssembler;
import thaumicenergistics.util.ForgeUtil;

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
        addListener(new knowledgeCoreSlotListener(TE, this));
        if(ForgeUtil.isServer()) {
            PacketHandler.sendToPlayer((EntityPlayerMP) player, new PacketAssemblerGUIUpdate(this.TE)); // send current aspect availability
            TE.subscribe(player);   // subscribe to aspect availability updates
        }
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

    private static class knowledgeCoreSlotListener implements IContainerListener{
        private final TileArcaneAssembler te;
        private final ContainerArcaneAssembler container;
        private boolean opened = false;

        public knowledgeCoreSlotListener(TileArcaneAssembler te, ContainerArcaneAssembler container) {
            this.te = te;
            this.container = container;
        }

        @Override
        @ParametersAreNonnullByDefault
        public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
            if(slotInd == 0 && opened && ForgeUtil.isServer()) {
                container.playCoreSound(container.player);
                te.getActionableNode().getGrid().postEvent(new MENetworkCraftingPatternChange(this.te, this.te.getActionableNode())); // update ME system available patterns
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
}
