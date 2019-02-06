package thaumicenergistics.network.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.api.util.AEPartLocation;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;

import io.netty.buffer.ByteBuf;

/**
 * @author BrockWS
 */
public class PacketOpenGUI implements IMessage {

    public int gui;
    public BlockPos pos;
    public AEPartLocation side;

    public PacketOpenGUI() {
    }

    public PacketOpenGUI(ModGUIs gui, BlockPos pos, AEPartLocation side) {
        this.gui = gui.ordinal();
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.gui = buf.readByte();
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.side = AEPartLocation.fromOrdinal(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.gui);
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
        buf.writeByte(this.side.ordinal());
    }

    public static class Handler implements IMessageHandler<PacketOpenGUI, IMessage> {

        @Override
        public IMessage onMessage(PacketOpenGUI message, MessageContext ctx) {
            NetHandlerPlayServer handler = ctx.getServerHandler();
            EntityPlayerMP player = handler.player;
            IThreadListener thread = (IThreadListener) player.world;
            thread.addScheduledTask(() -> GuiHandler.openGUI(ModGUIs.values()[message.gui], ctx.getServerHandler().player, message.pos, message.side));
            return null;
        }
    }
}
