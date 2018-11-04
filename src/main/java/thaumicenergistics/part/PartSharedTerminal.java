package thaumicenergistics.part;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.me.GridAccessException;

import thaumicenergistics.container.part.ContainerArcaneTerminal;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.integration.appeng.util.ThEConfigManager;
import thaumicenergistics.item.ItemPartBase;

/**
 * @author BrockWS
 */
public abstract class PartSharedTerminal extends PartBase implements ITerminalHost {

    private IConfigManager cm = new ThEConfigManager();

    public PartSharedTerminal(ItemPartBase item) {
        super(item);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.getConfigManager().readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.getConfigManager().writeToNBT(nbt);
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        try {
            return GridUtil.getStorageGrid(this).getInventory(channel);
        } catch (GridAccessException e) {
            // Ignored
        }
        return null;
    }

    @Override
    public double getIdlePowerUsage() {
        return 0.5d;
    }

    @Override
    public final void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

}
