package thaumicenergistics.common.network.packet.server;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.network.IAspectSlotPart;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.parts.ThEPartBase;
import io.netty.buffer.ByteBuf;

/**
 * Aspect slot server-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_AspectSlot extends ThEServerPacket {

    private static final byte MODE_SET_ASPECT = 0;

    private int index;

    private Aspect aspect;

    private IAspectSlotPart part;

    public static void sendAspectChange(final IAspectSlotPart part, final int index, final Aspect aspect,
            final EntityPlayer player) {
        Packet_S_AspectSlot packet = new Packet_S_AspectSlot();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = Packet_S_AspectSlot.MODE_SET_ASPECT;

        // Set the index
        packet.index = index;

        // Set the part
        packet.part = part;

        // Set the aspect
        packet.aspect = aspect;

        // Send it
        NetworkHandler.sendPacketToServer(packet);
    }

    @Override
    public void execute() {
        switch (this.mode) {
            case Packet_S_AspectSlot.MODE_SET_ASPECT:
                // Inform the part of the aspect change
                this.part.setAspect(this.index, this.aspect, this.player);
                break;
        }
    }

    @Override
    public void readData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_S_AspectSlot.MODE_SET_ASPECT:
                // Read the part
                this.part = ((IAspectSlotPart) ThEBasePacket.readPart(stream));

                // Read the index
                this.index = stream.readInt();

                // Read the aspect
                this.aspect = ThEBasePacket.readAspect(stream);
                break;
        }
    }

    @Override
    public void writeData(final ByteBuf stream) {
        switch (this.mode) {
            case Packet_S_AspectSlot.MODE_SET_ASPECT:
                // Write the part
                ThEBasePacket.writePart((ThEPartBase) this.part, stream);

                // Write the index
                stream.writeInt(this.index);

                // Write the aspect
                ThEBasePacket.writeAspect(this.aspect, stream);
                break;
        }
    }
}
