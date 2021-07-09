package thaumicenergistics.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.tile.TileBase;
import thaumicenergistics.util.ForgeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author BrockWS
 * @author Alex811
 */
public abstract class BlockBase extends Block {
    private static final Random rand = new Random();

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

    private static double randCoordOffset(int coord){
        return (rand.nextInt() % 32 - 16) / 82.0 + 0.5 + coord;
    }

    public static void spawnDrops(World world, BlockPos pos, List<ItemStack> drops){
        if(ForgeUtil.isClient()) return;
        drops.parallelStream()
                .filter(is -> !is.isEmpty())
                .forEach(is -> world.spawnEntity(new EntityItem(world, randCoordOffset(pos.getX()), randCoordOffset(pos.getY()), randCoordOffset(pos.getZ()), is)));
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof TileBase)) return;
        TileBase tb = (TileBase) te;
        ArrayList<ItemStack> drops = new ArrayList<>();
        tb.getDrops(worldIn, pos, drops);
        spawnDrops(worldIn, pos, drops);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }
}
