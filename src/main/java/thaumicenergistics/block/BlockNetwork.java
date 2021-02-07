package thaumicenergistics.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;

import thaumicenergistics.tile.TileNetwork;

/**
 * @author BrockWS
 */
public abstract class BlockNetwork extends BlockBase implements ITileEntityProvider {

    public BlockNetwork(String id) {
        this(id, Material.IRON);
    }

    public BlockNetwork(String id, Material material) {
        super(id, material);

    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (world.isRemote)
            return;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileNetwork && placer instanceof EntityPlayer) {
            IGridNode node = ((TileNetwork) te).getActionableNode();
            node.setPlayerID(AEApi.instance().registries().players().getID((EntityPlayer) placer));
        }
    }
}
