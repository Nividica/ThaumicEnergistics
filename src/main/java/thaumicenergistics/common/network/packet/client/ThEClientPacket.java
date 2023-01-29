package thaumicenergistics.common.network.packet.client;

import net.minecraft.client.Minecraft;

import thaumicenergistics.common.network.ThEBasePacket;
import thaumicenergistics.common.utils.EffectiveSide;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Packet to be sent to the client.
 *
 * @author Nividica
 *
 */
public abstract class ThEClientPacket extends ThEBasePacket {

    @SideOnly(Side.CLIENT)
    private final void preWrap() {
        // Set the player
        this.player = Minecraft.getMinecraft().thePlayer;

        // Execute the packet
        this.wrappedExecute();
    }

    @Override
    protected final boolean includePlayerInStream() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected abstract void wrappedExecute();

    @Override
    public final void execute() {
        // Ensure this is client side
        if (EffectiveSide.isClientSide()) {
            this.preWrap();
        }
    }
}
