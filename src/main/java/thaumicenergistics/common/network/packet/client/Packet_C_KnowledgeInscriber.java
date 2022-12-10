package thaumicenergistics.common.network.packet.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.client.gui.GuiKnowledgeInscriber;
import thaumicenergistics.common.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.tiles.TileKnowledgeInscriber;

/**
 * {@link TileKnowledgeInscriber} client-bound packet.
 *
 * @author Nividica
 *
 */
public class Packet_C_KnowledgeInscriber extends ThEClientPacket {

    private static final byte MODE_SENDSAVE = 0;

    private static final CoreSaveState[] SAVE_STATES = CoreSaveState.values();

    private CoreSaveState saveState;

    private boolean justSaved;

    public static void sendSaveState(
            final EntityPlayer player, final CoreSaveState saveState, final boolean justSaved) {
        Packet_C_KnowledgeInscriber packet = new Packet_C_KnowledgeInscriber();

        // Set the player
        packet.player = player;

        // Set the mode
        packet.mode = Packet_C_KnowledgeInscriber.MODE_SENDSAVE;

        // Set the state
        packet.saveState = saveState;
        packet.justSaved = justSaved;

        // Send it
        NetworkHandler.sendPacketToClient(packet);
    }

    @Override
    protected void readData(final ByteBuf stream) {
        this.saveState = Packet_C_KnowledgeInscriber.SAVE_STATES[stream.readInt()];
        this.justSaved = stream.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void wrappedExecute() {
        // Get the gui
        Gui gui = Minecraft.getMinecraft().currentScreen;

        // Ensure it is the knowledge inscriber
        if (gui instanceof GuiKnowledgeInscriber) {
            ((GuiKnowledgeInscriber) gui).onReceiveSaveState(this.saveState, this.justSaved);
        }
    }

    @Override
    protected void writeData(final ByteBuf stream) {
        stream.writeInt(this.saveState.ordinal());
        stream.writeBoolean(this.justSaved);
    }
}
