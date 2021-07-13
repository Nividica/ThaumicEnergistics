package thaumicenergistics.container;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import thaumicenergistics.config.AESettings;
import thaumicenergistics.integration.appeng.util.ThEConfigManager;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketSettingChange;
import thaumicenergistics.util.ForgeUtil;

/**
 * Container that implements {@link IConfigurableObject} and syncs the client with the server.
 * If you're looking to register new settings, see {@link AESettings}.
 *
 * @author Alex811
 */
public abstract class ContainerBaseConfigurable extends ContainerBase implements IConfigurableObject {
    protected ThEConfigManager serverConfigManager;
    protected ThEConfigManager clientConfigManager;

    public ContainerBaseConfigurable(EntityPlayer player, ThEConfigManager serverConfigManager) {
        super(player);
        this.clientConfigManager = new ThEConfigManager();
        this.clientConfigManager.registerSettings(this.getAESettingSubject());
        if(ForgeUtil.isServer())
            this.serverConfigManager = serverConfigManager;
    }

    protected abstract AESettings.SUBJECT getAESettingSubject();

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if(ForgeUtil.isServer()){
            for (Settings setting : this.serverConfigManager.getSettings()) {
                Enum server = this.serverConfigManager.getSetting(setting);
                Enum client = this.clientConfigManager.getSetting(setting);
                if (client != server) {
                    for (IContainerListener player : this.listeners)
                        if (player instanceof EntityPlayerMP) {
                            // Only update the local cache when we actually were able to send it
                            this.clientConfigManager.putSetting(setting, server);
                            PacketHandler.sendToPlayer((EntityPlayerMP) player, new PacketSettingChange(setting, server));
                        }
                }
            }
        }
    }

    @Override
    public ThEConfigManager getConfigManager() {
        return ForgeUtil.isClient() ? this.clientConfigManager : this.serverConfigManager;
    }
}
