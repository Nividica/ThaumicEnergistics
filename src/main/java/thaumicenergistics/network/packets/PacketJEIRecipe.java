package thaumicenergistics.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.util.ThELog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

/**
 * @author BrockWS
 */
public class PacketJEIRecipe implements IMessage {

    private NBTTagCompound tag;

    public PacketJEIRecipe() {
    }

    public PacketJEIRecipe(NBTTagCompound tag) {
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            this.tag = CompressedStreamTools.readCompressed(new ByteBufInputStream(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (tag == null || tag.isEmpty())
            ThELog.error("[FROM] TAG HAS NO INFO");
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (tag == null || tag.isEmpty()) {
            ThELog.error("[TO] TAG HAS NO INFO");
            return;
        }

        try {
            CompressedStreamTools.writeCompressed(this.tag, new ByteBufOutputStream(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (buf.writerIndex() >= 32676) {
            ThELog.warn("PacketJEIRecipe is too large! {}", buf.writerIndex());
            ThELog.warn("{}", this.tag.toString());
        }
    }

    public static class Handler implements IMessageHandler<PacketJEIRecipe, IMessage> {

        @Override
        public IMessage onMessage(PacketJEIRecipe message, MessageContext ctx) {
            NetHandlerPlayServer handler = ctx.getServerHandler();
            EntityPlayerMP player = handler.player;
            IThreadListener thread = (IThreadListener) player.world;
            thread.addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerBase) {
                    ((ContainerBase) player.openContainer).handleJEITransfer(player, message.tag);
                }
            });
            return null;
        }
    }
}
