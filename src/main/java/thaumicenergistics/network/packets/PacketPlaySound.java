package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.ThaumicEnergistics;

/**
 * @author Alex811
 */
public class PacketPlaySound implements IMessage {

    public ResourceLocation sound;
    public BlockPos pos;
    public SoundCategory category;
    public float vol;
    public float pitch;

    public PacketPlaySound() {
    }

    public PacketPlaySound(BlockPos pos, ResourceLocation sound, SoundCategory category, float vol, float pitch){
        this.sound = sound;
        this.pos = pos;
        this.category = category;
        this.vol = vol;
        this.pitch = pitch;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.sound = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.category = SoundCategory.values()[buf.readByte()];
        this.vol = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.sound.toString());
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
        buf.writeByte(this.category.ordinal());
        buf.writeFloat(this.vol);
        buf.writeFloat(this.pitch);
    }

    public static class Handler implements IMessageHandler<PacketPlaySound, IMessage> {

        @Override
        public IMessage onMessage(PacketPlaySound message, MessageContext ctx) {
            Minecraft.getMinecraft().world.playSound(ThaumicEnergistics.proxy.getPlayerEntFromCtx(ctx), message.pos, new SoundEvent(message.sound), message.category, message.vol, message.pitch);
            return null;
        }
    }
}
