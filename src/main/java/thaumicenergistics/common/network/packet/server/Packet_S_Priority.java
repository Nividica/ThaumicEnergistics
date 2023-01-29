package thaumicenergistics.common.network.packet.server;

import net.minecraft.entity.player.EntityPlayer;

import thaumicenergistics.common.container.ContainerPriority;
import thaumicenergistics.common.network.NetworkHandler;
import io.netty.buffer.ByteBuf;

/**
 * Priority GUI client-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_Priority extends ThEServerPacket {

    private static final byte MODE_SET = 0;
    private static final byte MODE_ADJUST = 1;
    private static final byte MODE_REQUEST = 2;

    private int priority;

    /**
     * Creates the packet
     *
     * @param player
     * @param mode
     * @return
     */
    private static Packet_S_Priority newPacket(final EntityPlayer player, final byte mode) {
        // Create the packet
        Packet_S_Priority packet = new Packet_S_Priority();

        // Set the player & mode
        packet.player = player;
        packet.mode = mode;

        return packet;
    }

    /**
     * Asks the server to set the priority to the specified value.
     *
     * @param priority
     * @param player
     * @return
     */
    public static void sendPriority(final int priority, final EntityPlayer player) {
        Packet_S_Priority packet = newPacket(player, MODE_SET);

        // Set the priority
        packet.priority = priority;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    /**
     * Asks the server to adjust the priority by the specified value.
     *
     * @param priority
     * @param player
     * @return
     */
    public static void sendPriorityDelta(final int priority, final EntityPlayer player) {
        Packet_S_Priority packet = newPacket(player, MODE_ADJUST);

        // Set the priority delta
        packet.priority = priority;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    public static void sendPriorityRequest(final EntityPlayer player) {
        Packet_S_Priority packet = newPacket(player, MODE_REQUEST);

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    @Override
    protected void readData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_S_Priority.MODE_SET:
            case Packet_S_Priority.MODE_ADJUST:
                // Read the priority
                this.priority = stream.readInt();
                break;
        }
    }

    @Override
    protected void writeData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_S_Priority.MODE_SET:
            case Packet_S_Priority.MODE_ADJUST:
                // Write the priority
                stream.writeInt(this.priority);
                break;
        }
    }

    @Override
    public void execute() {
        // Ensure we have a player
        if (this.player == null) {
            return;
        }

        // Ensure the player has opened the priority container
        if (!(this.player.openContainer instanceof ContainerPriority)) {
            return;
        }

        switch (this.mode) {
            case Packet_S_Priority.MODE_SET:
                // Set the priority
                ((ContainerPriority) this.player.openContainer).onClientRequestSetPriority(this.priority);
                break;

            case Packet_S_Priority.MODE_ADJUST:
                // Adjust the priority
                ((ContainerPriority) this.player.openContainer).onClientRequestAdjustPriority(this.priority);
                break;

            case Packet_S_Priority.MODE_REQUEST:
                // Request the priority
                ((ContainerPriority) this.player.openContainer).onClientRequestPriority();
                break;
        }
    }
}
