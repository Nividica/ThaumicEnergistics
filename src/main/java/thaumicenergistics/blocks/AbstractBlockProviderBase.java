package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.EnumCache;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.tileentities.abstraction.TileProviderBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractBlockProviderBase
	extends AbstractBlockAEWrenchable
{

	protected AbstractBlockProviderBase( final Material material )
	{
		super( material );

		// Place in our creative tab
		this.setCreativeTab( ThaumicEnergistics.ThETab );
	}

	@Override
	public void breakBlock( final World world, final int x, final int y, final int z, final Block block, final int metadata )
	{
		// Get  tile entity
		TileEntity tileProvider = world.getTileEntity( x, y, z );

		// Is there still have a tile?
		if( tileProvider instanceof TileProviderBase )
		{
			// Inform it that its going away
			( (TileProviderBase)tileProvider ).onBreakBlock();
		}

		// Pass to super
		super.breakBlock( world, x, y, z, block, metadata );
	}

	@Override
	public final boolean canRenderInPass( final int pass )
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

	/* Colored Lights Core Test Code
	@Override
	public int getLightValue( final IBlockAccess world, final int x, final int y, final int z )
	{
		// Get  tile entity
		TileEntity tileProvider = world.getTileEntity( x, y, z );

		// Is there still have a tile?
		if( tileProvider instanceof TileProviderBase )
		{

			float r = 0;
			float g = 0;
			float b = 0;

			if( ( (TileProviderBase)tileProvider ).isActive() )
			{
				int mv = ( (TileProviderBase)tileProvider ).getColor().mediumVariant;

				r = ( ( mv >> 16 ) & 0xff ) / 255.0F;
				g = ( ( mv >> 8 ) & 0xff ) / 255.0F;
				b = ( ( mv ) & 0xff ) / 255.0F;
				return CLApi.makeRGBLightValue( r, g, b );
			}

			return 0;
		}

		return super.getLightValue( world, x, y, z );

	}
	*/

	@SideOnly(Side.CLIENT)
	@Override
	public final int getRenderBlockPass()
	{
		// Ensure the alpha pass is rendered
		return Renderers.PASS_ALPHA;
	}

	@SideOnly(Side.CLIENT)
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
	public final boolean isSideSolid( final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side )
	{
		// This is a solid cube
		return true;
	}

	// Sets the metadata for the block based on which side of the neighbor block they clicked on.
	@Override
	public final int onBlockPlaced( final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY,
									final float hitZ, final int metaData )
	{
		// Get the opposite face and return it
		return ForgeDirection.OPPOSITES[side];
	}

	@Override
	public final void onBlockPlacedBy( final World world, final int x, final int y, final int z, final EntityLivingBase entity,
										final ItemStack itemstack )
	{
		// Set the owner
		if( entity instanceof EntityPlayer )
		{
			TileEntity tileProvider = world.getTileEntity( x, y, z );
			if( tileProvider instanceof TileProviderBase )
			{
				( (TileProviderBase)tileProvider ).setOwner( (EntityPlayer)entity );
			}
		}
	}

	@Override
	public abstract void onNeighborBlockChange( World world, int x, int y, int z, Block neighbor );

	@Override
	public boolean recolourBlock( final World world, final int x, final int y, final int z, final ForgeDirection side, final int color )
	{
		return ( (TileProviderBase)world.getTileEntity( x, y, z ) ).recolourBlock( side, EnumCache.AE_COLOR[color], null );

	}

	@SideOnly(Side.CLIENT)
	@Override
	public final void registerBlockIcons( final IIconRegister register )
	{
		// Ignored

	}

	@Override
	public final boolean renderAsNormalBlock()
	{
		// We have a custom renderer for this block
		return false;
	}

}
