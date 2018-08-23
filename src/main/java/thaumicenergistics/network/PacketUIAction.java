package thaumicenergistics.network;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.integration.appeng.AEEssentiaStack;

/**
 * @author BrockWS
 */
public class PacketUIAction implements IMessage {

    public ActionType action;
    public IAEEssentiaStack requestedStack;

    public PacketUIAction() {
    }

    public PacketUIAction(ActionType action) {
        this.action = action;
    }

    public PacketUIAction(ActionType action, IAEEssentiaStack stack) {
        this(action);
        this.requestedStack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = ActionType.values()[buf.readByte()];
        if (buf.readableBytes() > 0)
            this.requestedStack = AEEssentiaStack.fromPacket(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.action.ordinal());
        try {
            if (this.requestedStack != null) {
                this.requestedStack.writeToPacket(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<PacketUIAction, IMessage> {

        @Override
        public IMessage onMessage(PacketUIAction message, MessageContext ctx) {
            NetHandlerPlayServer handler = ctx.getServerHandler();
            EntityPlayerMP player = handler.player;
            IThreadListener thread = (IThreadListener) player.world;
            thread.addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerBase) {
                    ((ContainerBase) player.openContainer).onAction(player, message);
                }
            });
            return null;
        }
    }
}
