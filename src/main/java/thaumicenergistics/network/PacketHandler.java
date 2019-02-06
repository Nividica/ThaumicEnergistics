package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.network.packets.*;

/**
 * @author BrockWS
 */
public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE = null;
    private static int PACKETID = 0;

    public static int nextID() {
        return PacketHandler.PACKETID++;
    }

    public static void register() {
        if (PacketHandler.INSTANCE != null)
            return;
        PacketHandler.INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModGlobals.MOD_ID);

        PacketHandler.INSTANCE.registerMessage(PacketEssentiaFilter.Handler.class, PacketEssentiaFilter.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketMEEssentiaUpdate.Handler.class, PacketMEEssentiaUpdate.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketMEItemUpdate.Handler.class, PacketMEItemUpdate.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketInvHeldUpdate.Handler.class, PacketInvHeldUpdate.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketVisUpdate.Handler.class, PacketVisUpdate.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketSettingChange.HandlerClient.class, PacketSettingChange.class, PacketHandler.nextID(), Side.CLIENT);

        PacketHandler.INSTANCE.registerMessage(PacketUIAction.Handler.class, PacketUIAction.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketSettingChange.HandlerServer.class, PacketSettingChange.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketJEIRecipe.Handler.class, PacketJEIRecipe.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketOpenGUI.Handler.class, PacketOpenGUI.class, PacketHandler.nextID(), Side.SERVER);
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
