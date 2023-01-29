package thaumicenergistics.common.network.packet.client;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;

import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartArcaneCraftingTerminal;
import thaumicenergistics.common.registries.EnumCache;
import appeng.api.AEApi;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

/**
 * {@link PartArcaneCraftingTerminal} client-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_C_ArcaneCraftingTerminal extends ThEClientPacket {

    /**
     * Packet modes
     */
    private static final byte MODE_RECEIVE_CHANGE = 0, MODE_RECEIVE_FULL_LIST = 1, MODE_RECEIVE_SORTS = 3,
            MODE_UPDATE_COSTS = 4;

    private IAEItemStack changedStack;
    private IItemList<IAEItemStack> fullList;
    private SortOrder sortingOrder;
    private SortDir sortingDirection;
    private ViewItems viewMode;

    /**
     * Creates the packet
     *
     * @param player
     * @param mode
     * @return
     */
    private static Packet_C_ArcaneCraftingTerminal newPacket(final EntityPlayer player, final byte mode) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = new Packet_C_ArcaneCraftingTerminal();

        // Set the player & mode
        packet.player = player;
        packet.mode = mode;

        return packet;
    }

    /**
     * Creates a packet with the full list of items in the AE network. Only send in response to a request.
     *
     * @param player
     * @param fullList
     */
    public static void sendAllNetworkItems(final EntityPlayer player, final IItemList<IAEItemStack> fullList) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = newPacket(
                player,
                Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST);

        // Enable compression
        packet.useCompression = true;

        // Set the full list
        packet.fullList = fullList;

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    /**
     * Creates a packet with an update to the sorting order and direction.
     *
     * @param player
     * @param order
     * @param direction
     * @return
     */
    public static void sendModeChange(final EntityPlayer player, final SortOrder order, final SortDir direction,
            final ViewItems viewMode) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = newPacket(player, Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_SORTS);

        // Set the sorts
        packet.sortingDirection = direction;
        packet.sortingOrder = order;
        packet.viewMode = viewMode;

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    /**
     * Creates a packet with a changed network stack amount
     *
     * @param player
     * @param change
     */
    public static void stackAmountChanged(final EntityPlayer player, final IAEItemStack change) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = newPacket(player, Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_CHANGE);

        // Set the change
        packet.changedStack = change;

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    /**
     * Forces the client to re-calculate the displayed aspect costs
     *
     * @return
     */
    public static void updateAspectCost(final EntityPlayer player) {
        // Create the packet
        Packet_C_ArcaneCraftingTerminal packet = newPacket(player, Packet_C_ArcaneCraftingTerminal.MODE_UPDATE_COSTS);

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void wrappedExecute() {
        // Get the current screen being displayed to the user
        Gui gui = Minecraft.getMinecraft().currentScreen;

        // Is that screen the gui for the ACT?
        if (gui instanceof GuiArcaneCraftingTerminal) {
            switch (this.mode) {
                case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
                    // Set the item list
                    ((GuiArcaneCraftingTerminal) gui).onReceiveFullList(this.fullList);
                    break;

                case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
                    // Update the item list
                    ((GuiArcaneCraftingTerminal) gui).onReceiveChange(this.changedStack);
                    break;

                case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_SORTS:
                    ((GuiArcaneCraftingTerminal) gui)
                            .onReceiveSorting(this.sortingOrder, this.sortingDirection, this.viewMode);
                    break;

                case Packet_C_ArcaneCraftingTerminal.MODE_UPDATE_COSTS:
                    ((GuiArcaneCraftingTerminal) gui).onServerSendForceUpdateCost();
                    break;
            }
        }
    }

    @Override
    public void readData(final ByteBuf stream) {

        switch (this.mode) {
            case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
                // Create a new list
                this.fullList = AEApi.instance().storage().createItemList();

                // Read how many items there are
                int count = stream.readInt();

                for (int i = 0; i < count; i++) {
                    // Also ensure there are bytes to read
                    if (stream.readableBytes() <= 0) {
                        break;
                    }

                    // Read the itemstack
                    IAEItemStack itemStack = ThEBasePacket.readAEItemStack(stream);

                    // Ensure it is not null
                    if (itemStack != null) {
                        // Add to the list
                        this.fullList.add(itemStack);
                    }
                }
                break;

            case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
                // Read the change amount
                int changeAmount = stream.readInt();

                // Read the item
                this.changedStack = ThEBasePacket.readAEItemStack(stream);

                // Adjust it's size
                this.changedStack.setStackSize(changeAmount);

                break;

            case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_SORTS:
                // Read sorts
                this.sortingDirection = EnumCache.AE_SORT_DIRECTIONS[stream.readInt()];
                this.sortingOrder = EnumCache.AE_SORT_ORDERS[stream.readInt()];
                this.viewMode = EnumCache.AE_VIEW_ITEMS[stream.readInt()];
                break;
        }
    }

    @Override
    public void writeData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_FULL_LIST:
                // Is the list null?
                if (this.fullList == null) {
                    // No items
                    stream.writeInt(0);
                    return;
                }

                // Write how many items there are
                stream.writeInt(this.fullList.size());

                // Get the iterator
                Iterator<IAEItemStack> listIterator = this.fullList.iterator();

                // Write each item
                while (listIterator.hasNext()) {
                    ThEBasePacket.writeAEItemStack(listIterator.next(), stream);
                }
                break;

            case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_CHANGE:
                // Write the change amount
                stream.writeInt((int) this.changedStack.getStackSize());

                // Write the change
                ThEBasePacket.writeAEItemStack(this.changedStack, stream);
                break;

            case Packet_C_ArcaneCraftingTerminal.MODE_RECEIVE_SORTS:
                // Write the sorts
                stream.writeInt(this.sortingDirection.ordinal());
                stream.writeInt(this.sortingOrder.ordinal());
                stream.writeInt(this.viewMode.ordinal());
                break;
        }
    }
}
