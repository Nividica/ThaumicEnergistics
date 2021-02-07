package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.util.IThESubscribable;

/**
 * Packet to subscribe/unsubscribe to changes that happen to a TileEntity
 * The TileEntity should implement {@link IThESubscribable}
 * @author Alex811
 */
public class PacketSubscribe<T extends TileEntity & IThESubscribable> implements IMessage {

    public BlockPos TEPos;
    public boolean sub;

    public PacketSubscribe() {
    }

    /**
     * @param subscribable the TileEntity to subscribe/unsubscribe to
     * @param sub true to subscribe, false to unsubscribe
     */
    public PacketSubscribe(T subscribable, boolean sub) {
        this.TEPos = subscribable.getPos();
        this.sub = sub;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.TEPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.sub = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.TEPos.getX());
        buf.writeInt(this.TEPos.getY());
        buf.writeInt(this.TEPos.getZ());
        buf.writeBoolean(this.sub);
    }

    public static class Handler implements IMessageHandler<PacketSubscribe, IMessage> {

        @Override
        public IMessage onMessage(PacketSubscribe message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            World world = player.world;
            ((IThreadListener) world).addScheduledTask(() -> {
                TileEntity TE = world.getTileEntity(message.TEPos);
                if(TE instanceof IThESubscribable){
                    IThESubscribable subscribable = ((IThESubscribable) TE);
                    if(message.sub) subscribable.subscribe(player);
                    else subscribable.unsubscribe(player);
                }
            });
            return null;
        }
    }
}