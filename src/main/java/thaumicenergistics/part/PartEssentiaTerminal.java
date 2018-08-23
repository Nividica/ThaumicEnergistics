package thaumicenergistics.part;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.me.GridAccessException;

import thaumicenergistics.client.gui.GuiHandler;
import thaumicenergistics.init.ModGUIs;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.appeng.ThEPartModel;
import thaumicenergistics.integration.appeng.grid.GridUtil;
import thaumicenergistics.integration.appeng.util.ThEConfigManager;
import thaumicenergistics.item.part.ItemEssentiaTerminal;

/**
 * @author BrockWS
 */
public class PartEssentiaTerminal extends PartBase implements ITerminalHost {

    public static ResourceLocation[] MODELS = new ResourceLocation[]{
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_terminal/base"), // 0
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_terminal/on"), // 1
            new ResourceLocation(ModGlobals.MOD_ID, "part/essentia_terminal/off"), // 2
            new ResourceLocation("appliedenergistics2", "part/display_status_has_channel"), // 3
            new ResourceLocation("appliedenergistics2", "part/display_status_on"), // 4
            new ResourceLocation("appliedenergistics2", "part/display_status_off") // 5

    };

    private static IPartModel MODEL_ON = new ThEPartModel(MODELS[0], MODELS[1], MODELS[4]);
    private static IPartModel MODEL_OFF = new ThEPartModel(MODELS[0], MODELS[2], MODELS[5]);
    private static IPartModel MODEL_HAS_CHANNEL = new ThEPartModel(MODELS[0], MODELS[1], MODELS[3]);

    private IConfigManager cm = new ThEConfigManager();

    public PartEssentiaTerminal(ItemEssentiaTerminal itemEssentiaTerminal) {
        super(itemEssentiaTerminal);
        //this.cm.registerSetting();
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        GuiHandler.openGUI(ModGUIs.ESSENTIA_TERMINAL, player, this.hostTile.getPos(), this.side);
        return true;
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

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isPowered())
            if (this.isActive())
                return MODEL_HAS_CHANNEL;
            else
                return MODEL_ON;
        return MODEL_OFF;
    }
}
