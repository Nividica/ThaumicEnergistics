package thaumicenergistics.network.packets;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.aspects.IAspectContainer;
import thaumicenergistics.part.PartBase;
import thaumicenergistics.part.PartSharedEssentiaBus;

/**
 * @author Alex811
 */
public class PacketEssentiaFilterAction implements IMessage {
    public enum ACTION {
        CLEAR,
        PARTITION
    }

    public ACTION action;
    public PartBase part;

    public PacketEssentiaFilterAction() {
        super();
    }

    public PacketEssentiaFilterAction(PartBase part, ACTION action) {
        this.part = part;
        this.action = action;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        TileEntity tile = this.part.getTile();
        BlockPos pos = tile.getPos();
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(tile.getWorld().provider.getDimension());
        buf.writeInt(this.part.side.ordinal());
        buf.writeInt(this.action.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        World world = DimensionManager.getWorld(buf.readInt());
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof IPartHost){
            IPart part = ((IPartHost) tile).getPart(AEPartLocation.values()[buf.readInt()]);
            if(part instanceof PartBase)
                this.part = (PartBase) part;
        }
        this.action = ACTION.values()[buf.readInt()];
    }

    public static class Handler implements IMessageHandler<PacketEssentiaFilterAction, IMessage> {

        @Override
        public IMessage onMessage(PacketEssentiaFilterAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if(message.part instanceof PartSharedEssentiaBus){
                    PartSharedEssentiaBus part = (PartSharedEssentiaBus) message.part;
                    if(message.action == ACTION.CLEAR)
                        part.getConfig().clear();
                    else if(message.action == ACTION.PARTITION){
                        TileEntity connectedTE = part.getConnectedTE();
                        if(connectedTE instanceof IAspectContainer)
                            part.getConfig().partition((IAspectContainer) connectedTE);
                    }
                }
            });
            return null;
        }
    }
}
