package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.tile.TileArcaneAssembler;

/**
 * @author Alex811
 */
public class PacketAssemblerGUIUpdateRequest implements IMessage {

    public TileArcaneAssembler TE;

    public PacketAssemblerGUIUpdateRequest() {
    }

    public PacketAssemblerGUIUpdateRequest(TileArcaneAssembler TE) {
        this.TE = TE;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        World world = DimensionManager.getWorld(buf.readInt());
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileArcaneAssembler)
            this.TE = (TileArcaneAssembler) tile;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BlockPos pos = TE.getPos();
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(TE.getWorld().provider.getDimension());
    }

    public static class Handler implements IMessageHandler<PacketAssemblerGUIUpdateRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketAssemblerGUIUpdateRequest message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if(message.TE != null)
                    PacketHandler.sendToPlayer(ctx.getServerHandler().player, new PacketAssemblerGUIUpdate(message.TE));
            });
            return null;
        }
    }
}
