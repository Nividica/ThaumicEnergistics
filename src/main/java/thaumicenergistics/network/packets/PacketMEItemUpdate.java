package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

import thaumicenergistics.client.gui.part.GuiArcaneInscriber;
import thaumicenergistics.client.gui.part.GuiArcaneTerminal;
import thaumicenergistics.util.AEUtil;

/**
 * @author BrockWS
 */
public class PacketMEItemUpdate implements IMessage {

    public IItemList<IAEItemStack> list;

    public PacketMEItemUpdate() {
        this.list = AEUtil.getStorageChannel(IItemStorageChannel.class).createList();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        while (buf.isReadable()) {
            try {
                this.list.add(AEUtil.getStorageChannel(IItemStorageChannel.class).readFromPacket(buf));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (IAEItemStack stack : this.list) {
            try {
                stack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void appendStack(IAEItemStack stack) {
        this.list.add(stack);
    }

    public static class Handler implements IMessageHandler<PacketMEItemUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketMEItemUpdate message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiArcaneTerminal) {
                    GuiArcaneTerminal gui = (GuiArcaneTerminal) Minecraft.getMinecraft().currentScreen;
                    gui.onMEStorageUpdate(message.list);
                }
                if (Minecraft.getMinecraft().currentScreen instanceof GuiArcaneInscriber) {
                    GuiArcaneInscriber gui = (GuiArcaneInscriber) Minecraft.getMinecraft().currentScreen;
                    gui.onMEStorageUpdate(message.list);
                }
            });
            return null;
        }
    }
}
