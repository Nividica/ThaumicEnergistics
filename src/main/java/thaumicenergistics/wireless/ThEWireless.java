package thaumicenergistics.wireless;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import thaumicenergistics.api.wireless.IThEWireless;
import thaumicenergistics.api.wireless.IThEWirelessHandler;

/**
 * Contains wireless utilities
 *
 * @author BrockWS
 */
public class ThEWireless implements IThEWireless {

    private ClassToInstanceMap<IThEWirelessHandler> handlers;

    public ThEWireless() {
        this.handlers = MutableClassToInstanceMap.create();
    }

    @Override
    public <C extends IThEWirelessHandler> void registerWirelessHandler(Class<C> clazz, C handler) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(handler);
        Preconditions.checkArgument(clazz.isInstance(handler));
        Preconditions.checkArgument(!this.handlers.containsKey(clazz));

        this.handlers.putInstance(clazz, handler);
    }

    @Override
    public IThEWirelessHandler getWirelessHandler(Class<? extends IThEWirelessHandler> clazz) {
        Preconditions.checkNotNull(clazz);

        return this.handlers.getInstance(clazz);
    }

    @Override
    public IThEWirelessHandler getWirelessHandler(Object obj, EntityPlayer player) {
        Preconditions.checkNotNull(obj);
        Preconditions.checkNotNull(player);

        for (IThEWirelessHandler handler : this.handlers.values())
            if (handler.canHandle(obj, player))
                return handler;
        return null;
    }

    @Override
    public boolean isHandled(Object obj, EntityPlayer player) {
        Preconditions.checkNotNull(obj);
        Preconditions.checkNotNull(player);

        return this.getWirelessHandler(obj, player) != null;
    }

    @Override
    public void openGUI(Object obj, EntityPlayer player) {
        Preconditions.checkNotNull(obj);
        Preconditions.checkNotNull(player);

        if (!this.isHandled(obj, player)) {
            ITextComponent error = new TextComponentTranslation("chat.thaumicenergistics.missing_wireless_handler", obj.getClass().getName());
            error.getStyle().setColor(TextFormatting.RED);
            player.sendMessage(error);
            return;
        }
        this.getWirelessHandler(obj, player).openGUI(obj, player);
    }
}
