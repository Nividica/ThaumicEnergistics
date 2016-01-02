package thaumicenergistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.BlockEnum;
import thaumicenergistics.common.tiles.TileEssentiaCellWorkbench;
import thaumicenergistics.common.utils.EffectiveSide;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEssentiaCellWorkbench
	extends AbstractBlockAEWrenchable
{
	/**
	 * Cached values of the top and bottom side indexes
	 */
	private static final int SIDE_TOP = ForgeDirection.UP.ordinal(), SIDE_BOTTOM = ForgeDirection.DOWN.ordinal();

	public BlockEssentiaCellWorkbench()
	{
		// Call super with material machine (iron) 
		super( Material.iron );

		// Basic hardness
		this.setHardness( 1.0f );

		// Sound of metal
		this.setStepSound( Block.soundTypeMetal );

		// Place in the ThE creative tab
		this.setCreativeTab( ThaumicEnergistics.ThETab );
	}

	/**
	 * Called when the workbench is right-clicked
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param player
	 * @return
	 */
	@Override
	protected boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		// Launch the gui.
		ThEGuiHandler.launchGui( ThEGuiHandler.CELL_WORKBENCH_ID, player, world, x, y, z );

		return true;
	}

	/**
	 * Called when the block is broken.
	 */
	@Override
	public void breakBlock( final World world, final int x, final int y, final int z, final Block block, final int metaData )
	{
		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Get the tile
			TileEntity tileWorkBench = world.getTileEntity( x, y, z );

			// Does the workbench have a cell?
			if( ( tileWorkBench instanceof TileEssentiaCellWorkbench ) && ( ( (TileEssentiaCellWorkbench)tileWorkBench ).hasEssentiaCell() ) )
			{
				// Spawn the cell as an item entity.
				world.spawnEntityInWorld( new EntityItem( world, 0.5 + x, 0.5 + y, 0.2 + z, ( (TileEssentiaCellWorkbench)tileWorkBench )
								.getStackInSlot( 0 ) ) );
			}
		}

		// Call super
		super.breakBlock( world, x, y, z, block, metaData );
	}

	@Override
	public boolean canPlayerInteract( final EntityPlayer player )
	{
		return !( player instanceof FakePlayer );
	}

	@Override
	public TileEntity createNewTileEntity( final World world, final int metaData )
	{
		return new TileEssentiaCellWorkbench();
	}

	/**
	 * Gets the blocks icons.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		// Is top?
		if( side == BlockEssentiaCellWorkbench.SIDE_TOP )
		{
			return BlockTextureManager.ESSENTIA_CELL_WORKBENCH.getTextures()[0];
		}
		// Is bottom?
		else if( side == BlockEssentiaCellWorkbench.SIDE_BOTTOM )
		{
			return BlockTextureManager.ESSENTIA_CELL_WORKBENCH.getTextures()[1];

		}

		// Sides
		return BlockTextureManager.ESSENTIA_CELL_WORKBENCH.getTextures()[2];
	}

	/**
	 * Gets the unlocalized name of the block.
	 */
	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.ESSENTIA_CELL_WORKBENCH.getUnlocalizedName();
	}

	/**
	 * Is opaque.
	 */
	@Override
	public final boolean isOpaqueCube()
	{
		// Occlude adjoining faces.
		return true;
	}

	/**
	 * Is solid.
	 */
	@Override
	public final boolean isSideSolid( final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side )
	{
		// This is a solid cube
		return true;
	}

	/**
	 * Taken care of by texture manager
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public final void registerBlockIcons( final IIconRegister register )
	{
		// Ignored
	}

	/**
	 * Normal renderer.
	 */
	@Override
	public final boolean renderAsNormalBlock()
	{
		return true;
	}

}
