package thaumicenergistics.part;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;

import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.item.ItemPartBase;

/**
 * @author BrockWS
 */
public abstract class PartSharedTerminal extends PartBase implements ITerminalHost {

    protected final ModGUIs gui;    // the GUI that corresponds to this terminal, mainly used to know where to return to, from a different GUI

    public PartSharedTerminal(ItemPartBase item, ModGUIs gui) {
        super(item);
        this.gui = gui;
    }

    public ModGUIs getGui(){
        return this.gui;
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
    public int getLightLevel() {
        return this.blockLight(this.isPowered() ? 9 : 0);
    }
}
