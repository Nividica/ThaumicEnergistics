package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class PacketHandler {

    private static int PACKETID = 0;

    public static SimpleNetworkWrapper INSTANCE = null;

    public static int nextID() {
        return PacketHandler.PACKETID++;
    }

    public static void register() {
        if (PacketHandler.INSTANCE != null)
            return;
        PacketHandler.INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModGlobals.MOD_ID);

        PacketHandler.INSTANCE.registerMessage(PacketEssentiaFilter.Handler.class, PacketEssentiaFilter.class, PacketHandler.nextID(), Side.CLIENT);
    }

    public static void sendToPlayer(EntityPlayerMP player, IMessage message) {
        PacketHandler.INSTANCE.sendTo(message, player);
    }

    public static void sendToServer(IMessage message) {
        PacketHandler.INSTANCE.sendToServer(message);
    }

    public static void sendToAll(IMessage message) {
        PacketHandler.INSTANCE.sendToAll(message);
    }
}
