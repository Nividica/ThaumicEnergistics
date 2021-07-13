package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
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

    public TileEntity TE;
    public boolean sub;

    public PacketSubscribe() {
    }

    /**
     * @param subscribable the TileEntity to subscribe/unsubscribe to
     * @param sub true to subscribe, false to unsubscribe
     */
    public PacketSubscribe(T subscribable, boolean sub) {
        TE = subscribable;
        this.sub = sub;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        BlockPos TEPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        World world = DimensionManager.getWorld(buf.readInt());
        this.TE = world.getTileEntity(TEPos);
        this.sub = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BlockPos TEPos = this.TE.getPos();
        buf.writeInt(TEPos.getX());
        buf.writeInt(TEPos.getY());
        buf.writeInt(TEPos.getZ());
        buf.writeInt(this.TE.getWorld().provider.getDimension());
        buf.writeBoolean(this.sub);
    }

    public static class Handler implements IMessageHandler<PacketSubscribe, IMessage> {

        @Override
        public IMessage onMessage(PacketSubscribe message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            World world = player.world;
            ((IThreadListener) world).addScheduledTask(() -> {
                if(message.TE instanceof IThESubscribable){
                    IThESubscribable subscribable = ((IThESubscribable) message.TE);
                    if(message.sub) subscribable.subscribe(player);
                    else subscribable.unsubscribe(player);
                }
            });
            return null;
        }
    }
}