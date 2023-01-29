package thaumicenergistics.common.network.packet.server;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.container.ContainerEssentiaCellTerminalBase;
import thaumicenergistics.common.items.ItemEssentiaCell;
import thaumicenergistics.common.items.ItemWirelessEssentiaTerminal;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.PartEssentiaTerminal;
import io.netty.buffer.ByteBuf;

/**
 * {@link PartEssentiaTerminal}, {@link ItemWirelessEssentiaTerminal}, and {@link ItemEssentiaCell} server-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_EssentiaCellTerminal extends ThEServerPacket {

    /**
     * Packet modes
     */
    private static final byte MODE_SELECTED_ASPECT = 0, MODE_FULL_UPDATE = 1, MODE_SORT_CHANGE = 2, MODE_AUTO_CRAFT = 3,
            MODE_VIEW_CHANGE = 4, MODE_HELD_ITEM = 5;

    /**
     * The aspect.
     */
    private Aspect selectedAspect;

    /**
     * Boolean flag. What more needs to be said :P
     */
    private Boolean flag;

    /**
     * Creates the packet
     *
     * @param player
     * @param mode
     * @return
     */
    private static Packet_S_EssentiaCellTerminal newPacket(final EntityPlayer player, final byte mode) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = new Packet_S_EssentiaCellTerminal();

        // Set the player & mode
        packet.player = player;
        packet.mode = mode;

        return packet;
    }

    /**
     * Asks the server to begin a crafting job for the specified aspect.
     *
     * @param player
     * @param aspect
     */
    public static void sendAutoCraft(final EntityPlayer player, final Aspect aspect) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = newPacket(player, MODE_AUTO_CRAFT);

        // Set the aspect
        packet.selectedAspect = aspect;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Asks the server to change the sorting mode.
     *
     * @param player
     */
    public static void sendChangeSorting(final EntityPlayer player, final boolean wasRightClick) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = newPacket(player, MODE_SORT_CHANGE);

        // Set the flag
        packet.flag = wasRightClick;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Asks the server to change the view mode.
     *
     * @param player
     */
    public static void sendChangeView(final EntityPlayer player, final boolean wasRightClick) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = newPacket(player, MODE_VIEW_CHANGE);

        // Set the flag
        packet.flag = wasRightClick;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Requests a full update from the server.
     *
     * @param player
     */
    public static void sendFullUpdateRequest(final EntityPlayer player) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = newPacket(player, MODE_FULL_UPDATE);

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Informs the server when an aspect has been clicked on and the player is holding an item.
     *
     * @param player
     * @param aspect
     */
    public static void sendInteractWithHeldItem(final EntityPlayer player, final Aspect aspect) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = newPacket(player, MODE_HELD_ITEM);

        // Set the aspect
        packet.selectedAspect = aspect;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Informs the server the player has (un)selected an aspect.
     *
     * @param player
     * @param currentAspect
     */
    public static void sendSelectedAspect(final EntityPlayer player, final Aspect currentAspect) {
        // Create the packet
        Packet_S_EssentiaCellTerminal packet = newPacket(player, MODE_SELECTED_ASPECT);

        // Set the current aspect
        packet.selectedAspect = currentAspect;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    @Override
    public void execute() {
        // Sanity checks
        if ((this.player == null) || !(this.player.openContainer instanceof ContainerEssentiaCellTerminalBase)) {
            return;
        }

        switch (this.mode) {
            case MODE_SELECTED_ASPECT:
                ((ContainerEssentiaCellTerminalBase) this.player.openContainer)
                        .onReceivedSelectedAspect(this.selectedAspect);
                break;

            case MODE_FULL_UPDATE:
                ((ContainerEssentiaCellTerminalBase) this.player.openContainer).onClientRequestFullUpdate();
                break;

            case MODE_SORT_CHANGE:
                ((ContainerEssentiaCellTerminalBase) this.player.openContainer)
                        .onClientRequestSortModeChange(this.player, this.flag);
                break;

            case MODE_AUTO_CRAFT:
                ((ContainerEssentiaCellTerminalBase) this.player.openContainer)
                        .onClientRequestAutoCraft(this.player, this.selectedAspect);
                break;

            case MODE_VIEW_CHANGE:
                ((ContainerEssentiaCellTerminalBase) this.player.openContainer)
                        .onClientRequestViewModeChange(this.player, this.flag);
                break;

            case MODE_HELD_ITEM:
                ((ContainerEssentiaCellTerminalBase) this.player.openContainer)
                        .onInteractWithHeldItem(this.player, this.selectedAspect);
                break;
        }
    }

    @Override
    public void readData(final ByteBuf stream) {
        switch (this.mode) {
            case MODE_SELECTED_ASPECT:
            case MODE_AUTO_CRAFT:
            case MODE_HELD_ITEM:
                // Read the aspect
                this.selectedAspect = ThEBasePacket.readAspect(stream);
                break;

            case MODE_SORT_CHANGE:
            case MODE_VIEW_CHANGE:
                // Read the flag
                this.flag = stream.readBoolean();
                break;
        }
    }

    @Override
    public void writeData(final ByteBuf stream) {
        switch (this.mode) {
            case MODE_SELECTED_ASPECT:
            case MODE_AUTO_CRAFT:
            case MODE_HELD_ITEM:
                // Write the aspect
                ThEBasePacket.writeAspect(this.selectedAspect, stream);
                break;

            case MODE_SORT_CHANGE:
            case MODE_VIEW_CHANGE:
                // Write the flag
                stream.writeBoolean(this.flag);
                break;
        }
    }
}
