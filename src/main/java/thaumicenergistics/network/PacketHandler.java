package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.network.packets.*;
import thaumicenergistics.util.ThELog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
        PacketHandler.INSTANCE.registerMessage(PacketIsArcaneUpdate.Handler.class, PacketIsArcaneUpdate.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketPlaySound.Handler.class, PacketPlaySound.class, PacketHandler.nextID(), Side.CLIENT);
        PacketHandler.INSTANCE.registerMessage(PacketAssemblerGUIUpdate.Handler.class, PacketAssemblerGUIUpdate.class, PacketHandler.nextID(), Side.CLIENT);

        PacketHandler.INSTANCE.registerMessage(PacketUIAction.Handler.class, PacketUIAction.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketSettingChange.HandlerServer.class, PacketSettingChange.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketJEIRecipe.Handler.class, PacketJEIRecipe.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketOpenGUI.Handler.class, PacketOpenGUI.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketCraftRequest.Handler.class, PacketCraftRequest.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketSubscribe.Handler.class, PacketSubscribe.class, PacketHandler.nextID(), Side.SERVER);
        PacketHandler.INSTANCE.registerMessage(PacketEssentiaFilterAction.Handler.class, PacketEssentiaFilterAction.class, PacketHandler.nextID(), Side.SERVER);
    }

    public static void sendToPlayer(EntityPlayerMP player, IMessage message) {
        if (!(message instanceof PacketVisUpdate)) {
            ByteBuf buf = Unpooled.buffer();
            message.toBytes(buf);
            ThELog.trace("sendToPlayer readableBytes {} | read {} | write {} | message {}", buf.readableBytes(), buf.readerIndex(), buf.writerIndex(), message.getClass().getSimpleName());
        }
        PacketHandler.INSTANCE.sendTo(message, player);
    }

    public static void sendToServer(IMessage message) {
        if (!(message instanceof PacketVisUpdate)) {
            ByteBuf buf = Unpooled.buffer();
            message.toBytes(buf);
            ThELog.trace("sendToServer readableBytes {} | read {} | write {} | message {}", buf.readableBytes(), buf.readerIndex(), buf.writerIndex(), message.getClass().getSimpleName());
        }
        PacketHandler.INSTANCE.sendToServer(message);
    }

    public static void sendToAll(IMessage message) {
        if (!(message instanceof PacketVisUpdate)) {
            ByteBuf buf = Unpooled.buffer();
            message.toBytes(buf);
            ThELog.trace("sendToAll readableBytes {} | read {} | write {} | message {}", buf.readableBytes(), buf.readerIndex(), buf.writerIndex(), message.getClass().getSimpleName());
        }
        PacketHandler.INSTANCE.sendToAll(message);
    }
}
