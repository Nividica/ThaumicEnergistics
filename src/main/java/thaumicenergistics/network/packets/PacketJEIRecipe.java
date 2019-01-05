package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.util.ThELog;

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
        this.tag = ByteBufUtils.readTag(buf);
        if (tag == null || tag.hasNoTags())
            ThELog.error("[FROM] TAG HAS NO INFO");
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (tag == null || tag.hasNoTags())
            ThELog.error("[TO] TAG HAS NO INFO");
        ByteBufUtils.writeTag(buf, this.tag);
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
