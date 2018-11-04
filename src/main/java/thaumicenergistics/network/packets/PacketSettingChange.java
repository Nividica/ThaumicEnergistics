package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;

import thaumicenergistics.client.gui.GuiBase;

/**
 * @author BrockWS
 */
public class PacketSettingChange implements IMessage {

    private String setting;
    private String value;

    public PacketSettingChange() {
    }

    public PacketSettingChange(Settings setting, Enum value) {
        this(setting.name(), value.name());
    }

    public PacketSettingChange(String s, String v) {
        this.setting = s;
        this.value = v;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.setting = ByteBufUtils.readUTF8String(buf);
        this.value = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.setting);
        ByteBufUtils.writeUTF8String(buf, this.value);
    }

    public Settings getSetting() {
        return Settings.valueOf(this.setting);
    }

    public Enum getValue() {
        for (Enum e : this.getSetting().getPossibleValues())
            if (e.name().equalsIgnoreCase(this.value))
                return e;
        return null;
    }

    public static class HandlerServer implements IMessageHandler<PacketSettingChange, IMessage> {

        @Override
        public IMessage onMessage(PacketSettingChange message, MessageContext ctx) {
            NetHandlerPlayServer handler = ctx.getServerHandler();
            EntityPlayerMP player = handler.player;
            IThreadListener thread = (IThreadListener) player.world;
            thread.addScheduledTask(() -> {
                if (player.openContainer instanceof IConfigurableObject) {
                    IConfigManager cm = ((IConfigurableObject) player.openContainer).getConfigManager();
                    if (cm != null)
                        cm.putSetting(message.getSetting(), message.getValue());
                }
            });
            return null;
        }
    }

    public static class HandlerClient implements IMessageHandler<PacketSettingChange, IMessage> {

        @Override
        public IMessage onMessage(PacketSettingChange message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                if (gui instanceof GuiBase)
                    ((GuiBase) gui).updateSetting(message.getSetting(), message.getValue());
            });
            return null;
        }
    }
}
