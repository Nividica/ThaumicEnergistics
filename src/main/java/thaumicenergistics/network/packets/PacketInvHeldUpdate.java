package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author BrockWS
 */
public class PacketInvHeldUpdate implements IMessage {

    public ItemStack stack;

    public PacketInvHeldUpdate() {
    }

    public PacketInvHeldUpdate(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.stack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.stack);
    }

    public static class Handler implements IMessageHandler<PacketInvHeldUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketInvHeldUpdate message, MessageContext ctx) {
            Minecraft.getMinecraft().player.inventory.setItemStack(message.stack != null ? message.stack : ItemStack.EMPTY);
            return null;
        }
    }
}
