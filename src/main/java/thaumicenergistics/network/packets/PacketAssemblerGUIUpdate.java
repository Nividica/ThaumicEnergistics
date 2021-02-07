package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.tile.TileArcaneAssembler;

import java.util.HashMap;

/**
 * @author Alex811
 */
public class PacketAssemblerGUIUpdate implements IMessage {

    public HashMap<String, Boolean> aspectExists = new HashMap<>();
    public boolean hasEnoughVis;
    public BlockPos TEPos;

    public PacketAssemblerGUIUpdate() {
    }

    public PacketAssemblerGUIUpdate(TileArcaneAssembler TE) {
        this.aspectExists = TE.getAspectExists();
        this.hasEnoughVis = TE.getHasEnoughVis();
        this.TEPos = TE.getPos();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.TEPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.hasEnoughVis = buf.readBoolean();
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
            this.aspectExists.put(ByteBufUtils.readUTF8String(buf), buf.readBoolean());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.TEPos.getX());
        buf.writeInt(this.TEPos.getY());
        buf.writeInt(this.TEPos.getZ());
        buf.writeBoolean(this.hasEnoughVis);
        buf.writeInt(this.aspectExists.size());
        this.aspectExists.forEach((k, v) -> {
            ByteBufUtils.writeUTF8String(buf, k);
            buf.writeBoolean(v);
        });
    }

    public static class Handler implements IMessageHandler<PacketAssemblerGUIUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketAssemblerGUIUpdate message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                TileEntity TE = Minecraft.getMinecraft().world.getTileEntity(message.TEPos);
                if(TE instanceof TileArcaneAssembler){
                    TileArcaneAssembler AATE = ((TileArcaneAssembler) TE);
                    AATE.setAspectExists(message.aspectExists);
                    AATE.setHasEnoughVis(message.hasEnoughVis);
                }
            });
            return null;
        }
    }
}