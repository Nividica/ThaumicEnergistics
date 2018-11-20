package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEStack;

import thaumicenergistics.container.ActionType;
import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class PacketUIAction implements IMessage {

    public ActionType action;
    public IAEStack requestedStack;

    public PacketUIAction() {
    }

    public PacketUIAction(ActionType action) {
        this.action = action;
    }

    public PacketUIAction(ActionType action, IAEStack stack) {
        this(action);
        this.requestedStack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = ActionType.values()[buf.readByte()];
        if (buf.readableBytes() > 0) {
            String channelClass = ByteBufUtils.readUTF8String(buf);
            AEApi.instance().storage().storageChannels().forEach(channel -> {
                if (channel.getClass().getSimpleName().equalsIgnoreCase(channelClass)) {
                    try {
                        this.requestedStack = channel.readFromPacket(buf);
                    } catch (Throwable ignored) {
                        ThELog.error("Failed to read stack from packet, {}", channel.getClass().getSimpleName());
                    }
                }
            });
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.action.ordinal());
        try {
            if (this.requestedStack != null) {
                ByteBufUtils.writeUTF8String(buf, this.requestedStack.getChannel().getClass().getSimpleName());
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
