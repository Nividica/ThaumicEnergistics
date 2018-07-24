package thaumicenergistics.integration.appeng.grid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.networking.*;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;

import thaumicenergistics.part.PartBase;

/**
 * @author BrockWS
 */
public class ThEGridBlock implements IGridBlock {

    private PartBase part;

    public ThEGridBlock(PartBase partBase) {
        this.part = partBase;
    }


    @Override
    public double getIdlePowerUsage() {
        return this.part.getIdlePowerUsage();
    }

    @Nonnull
    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean isWorldAccessible() {
        return false;
    }

    @Nonnull
    @Override
    public DimensionalCoord getLocation() {
        return this.part.getLocation();
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
        return EnumSet.noneOf(EnumFacing.class);
    }

    @Nonnull
    @Override
    public IGridHost getMachine() {
        return this.part;
    }

    @Override
    public void gridChanged() {

    }

    @Nullable
    @Override
    public ItemStack getMachineRepresentation() {
        return this.part.getItemStack(PartItemStack.NETWORK);
    }
}
