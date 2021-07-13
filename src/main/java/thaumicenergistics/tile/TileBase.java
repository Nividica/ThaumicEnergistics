package thaumicenergistics.tile;

import appeng.api.util.ICommonTile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class TileBase extends TileEntity implements ICommonTile {
    public TileBase() {

    }

    @Override
    public void getDrops(World world, BlockPos blockPos, List<ItemStack> list) {

    }
}
