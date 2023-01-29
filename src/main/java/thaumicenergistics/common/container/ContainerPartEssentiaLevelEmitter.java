package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.network.packet.client.Packet_C_AspectSlot;
import thaumicenergistics.common.network.packet.client.Packet_C_EssentiaEmitter;
import thaumicenergistics.common.parts.PartEssentiaLevelEmitter;
import appeng.api.config.RedstoneMode;

/**
 * Inventory container for essentia emitter.
 *
 * @author Nividica
 *
 */
public class ContainerPartEssentiaLevelEmitter extends ContainerWithPlayerInventory {

    /**
     * X position for the player inventory
     */
    private static int PLAYER_INV_POSITION_Y = 102;

    /**
     * Y position for the hotbar inventory
     */
    private static int HOTBAR_INV_POSITION_Y = 160;

    /**
     * The AE part associated with this container.
     */
    private PartEssentiaLevelEmitter emitter;

    /**
     * The redstone mode the emitter is in.
     */
    private RedstoneMode redstoneMode = null;

    /**
     * The threshold value.
     */
    private long thresholdLevel = -1;

    /**
     * The tracked aspect.
     */
    private Aspect trackedAspect = null;

    /**
     * Create the container.
     *
     * @param player The owner of this container.
     */
    public ContainerPartEssentiaLevelEmitter(final PartEssentiaLevelEmitter part, final EntityPlayer player) {
        // Call super
        super(player);

        // Set the part
        this.emitter = part;

        // Get the players inventory and bind it to the container.
        this.bindPlayerInventory(
                player.inventory,
                ContainerPartEssentiaLevelEmitter.PLAYER_INV_POSITION_Y,
                ContainerPartEssentiaLevelEmitter.HOTBAR_INV_POSITION_Y);
    }

    @Override
    protected boolean detectAndSendChangesMP(@Nonnull final EntityPlayerMP playerMP) {
        // Threshold
        if (this.thresholdLevel != this.emitter.getThresholdLevel()) {
            this.thresholdLevel = this.emitter.getThresholdLevel();
            Packet_C_EssentiaEmitter.sendThresholdValue(this.thresholdLevel, this.player);
        }

        // Redstone mode
        if (this.redstoneMode != this.emitter.getRedstoneMode()) {
            this.redstoneMode = this.emitter.getRedstoneMode();
            Packet_C_EssentiaEmitter.sendRedstoneMode(this.redstoneMode, this.player);
        }

        // Tracked aspect
        if (this.trackedAspect != this.emitter.getTrackedAspect()) {
            this.trackedAspect = this.emitter.getTrackedAspect();

            List<Aspect> filter = new ArrayList<Aspect>();
            filter.add(this.trackedAspect);
            Packet_C_AspectSlot.setFilterList(filter, this.player);
        }

        return false;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        if (this.emitter != null) {
            return this.emitter.isPartUseableByPlayer(player);
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int slotNumber) {
        // Get the slot
        Slot slot = this.getSlotOrNull(slotNumber);

        if ((slot != null) && (slot.getHasStack())) {
            // Can we set the filter based on this item?
            if ((this.emitter != null) && this.emitter.setFilteredAspectFromItemstack(player, slot.getStack())) {
                return null;
            }

            // Attempt to swap
            if (this.swapSlotInventoryHotbar(slotNumber, slot.getStack())) {
                // Did the merger drain the stack?
                if (slot.getStack().stackSize == 0) {
                    // Set the slot to have no item
                    slot.putStack(null);
                }

                // Inform the slot its stack changed;
                slot.onSlotChanged();
            }
        }

        return null;
    }
}
