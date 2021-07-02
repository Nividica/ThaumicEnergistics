package thaumicenergistics.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.tile.TileBase;
import thaumicenergistics.util.ItemHandlerUtil;

import java.util.ArrayList;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class BlockBase extends Block {

    public BlockBase(String id) {
        this(id, Material.IRON);
    }

    public BlockBase(String id, Material material) {
        super(material);
        this.setRegistryName(id);
        this.setTranslationKey(ModGlobals.MOD_ID + "." + id);
        this.setCreativeTab(ModGlobals.CREATIVE_TAB);
        this.setHardness(1f);
    }

    public void registerTileEntity() {
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof TileBase)) return;
        TileBase tb = (TileBase) te;
        ArrayList<ItemStack> drops = new ArrayList<>();
        tb.getDrops(worldIn, pos, drops);
        ItemHandlerUtil.spawnDrops(worldIn, pos, drops);
        super.breakBlock(worldIn, pos, state);
    }
}
