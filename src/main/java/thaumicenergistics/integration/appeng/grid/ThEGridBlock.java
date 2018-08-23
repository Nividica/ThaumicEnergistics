package thaumicenergistics.integration.appeng.grid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.networking.*;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;

import thaumicenergistics.part.PartBase;

/**
 * @author BrockWS
 */
public class ThEGridBlock implements IGridBlock {

    private IThEGridHost host;
    private ItemStack rep;
    private TileEntity repTE;
    private boolean inWorld;
    private double idlePower = 1;

    public ThEGridBlock(IThEGridHost host, TileEntity rep, boolean inWorld) {
        this.host = host;
        this.repTE = rep;
        this.inWorld = inWorld;
    }

    public ThEGridBlock(IThEGridHost host, ItemStack rep, boolean inWorld) {
        this.host = host;
        this.rep = rep;
        this.inWorld = inWorld;
    }

    public ThEGridBlock(PartBase part) {
        this.host = part;
        this.rep = part.getItemStack(PartItemStack.NETWORK);
        this.inWorld = false;
        this.idlePower = part.getIdlePowerUsage();
    }

    @Override
    public double getIdlePowerUsage() {
        return this.idlePower;
    }

    @Nonnull
    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean isWorldAccessible() {
        return this.inWorld;
    }

    @Nonnull
    @Override
    public DimensionalCoord getLocation() {
        return this.host.getLocation();
    }

    @Nonnull
    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(@Nonnull GridNotification gridNotification) {

    }

    @Override
    @Deprecated
    public void setNetworkStatus(IGrid grid, int usedChannels) {
        // TODO: DEPRECATED: rv7
    }

    @Nonnull
    @Override
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Nonnull
    @Override
    public IGridHost getMachine() {
        return this.host;
    }

    @Override
    public void gridChanged() {
        this.host.gridChanged();
    }

    @Nullable
    @Override
    public ItemStack getMachineRepresentation() {
        if (this.repTE != null) {
            World world = this.repTE.getWorld();
            BlockPos pos = this.repTE.getPos();
            IBlockState state = world.getBlockState(pos);
            return new ItemStack(state.getBlock());
        }
        if (this.rep == null)
            return ItemStack.EMPTY;
        return this.rep;
    }
}
