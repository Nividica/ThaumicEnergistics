package thaumicenergistics.container;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import thaumicenergistics.integration.appeng.util.ThEConfigManager;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketSettingChange;
import thaumicenergistics.part.PartBase;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author Alex811
 */
public abstract class ContainerBaseConfigurable extends ContainerBase implements IConfigurableObject {
    protected IConfigManager serverConfigManager;
    protected IConfigManager clientConfigManager;

    public ContainerBaseConfigurable(EntityPlayer player, PartBase part) {
        super(player);
        this.clientConfigManager = new ThEConfigManager();
        if(ForgeUtil.isServer())
            this.serverConfigManager = part.getConfigManager();
    }

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
    public IConfigManager getConfigManager() {
        return ForgeUtil.isClient() ? this.clientConfigManager : this.serverConfigManager;
    }
}
