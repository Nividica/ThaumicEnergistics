package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.tileentities.TileProviderBase;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockProviderBase
	extends BlockContainer
{

	protected BlockProviderBase( Material material )
	{
		super( material );

		// Place in our creative tab
		this.setCreativeTab( ThaumicEnergistics.ModTab );
	}

	@Override
	public final boolean canRenderInPass( int pass )
	{
		// Mark the current pass
		Renderers.currentRenderPass = pass;

		// We render during both passes
		return true;
	}

	@Override
	public abstract TileEntity createNewTileEntity( World world, int metaData );

	@SideOnly(Side.CLIENT)
	@Override
	public abstract IIcon getIcon( int side, int metaData );

	@Override
	public final int getRenderBlockPass()
	{
		// Ensure the alpha pass is rendered
		return Renderers.PASS_ALPHA;
	}

	@Override
	public abstract int getRenderType();

	@Override
	public abstract String getUnlocalizedName();

	@Override
	public final boolean isOpaqueCube()
	{
		// Occlude adjoining faces.
		return true;
	}

	@Override
	public boolean isSideSolid( IBlockAccess world, int x, int y, int z, ForgeDirection side )
	{
		// This is a solid cube
		return true;
	}

	// Sets the metadata for the block based on which side of the neighbor block they clicked on.
	@Override
	public final int onBlockPlaced( World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metaData )
	{
		// Get the opposite face and return it
		return ForgeDirection.OPPOSITES[side];
	}

	@Override
	public abstract void onNeighborBlockChange( World world, int x, int y, int z, Block neighbor );

	@SideOnly(Side.CLIENT)
	@Override
	public final void registerBlockIcons( IIconRegister register )
	{
		// Ignored

	}

	@Override
	public final boolean renderAsNormalBlock()
	{
		// We have a custom renderer for this block
		return false;
	}

	@Override
	public boolean recolourBlock( World world, int x, int y, int z, ForgeDirection side, int color )
	{
		return ( (TileProviderBase)world.getTileEntity( x, y, z ) ).recolourBlock( side, AEColor.values()[color], null );

	}

}
