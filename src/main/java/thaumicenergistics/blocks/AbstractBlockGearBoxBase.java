package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.tileentities.TileGearBox;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractBlockGearBoxBase
	extends AbstractBlockAEWrenchable
{
	/**
	 * Determines if thaumcraft golems are allowed to interact with the gearbox.
	 */
	protected boolean allowGolemInteraction = false;

	/**
	 * Creates the block.
	 */
	public AbstractBlockGearBoxBase()
	{
		// Set material type
		super( Material.ground );

		// Set creative tab
		this.setCreativeTab( ThaumicEnergistics.TETab );

		// Set sound type
		this.setStepSound( Block.soundTypeStone );

		// Set hardness
		this.setHardness( 0.6F );
	}

	@Override
	public boolean canPlayerInteract( final EntityPlayer player )
	{
		// Fake player?
		if( player instanceof FakePlayer )
		{
			// Are golems allowed to interact?
			if( !this.allowGolemInteraction )
			{
				// Golem interaction not allowed
				return false;
			}

			// Is the fake player a golem?
			if( !player.getGameProfile().getName().equalsIgnoreCase( "FakeThaumcraftGolem" ) )
			{
				// Not a golem
				return false;
			}
		}

		return true;
	}

	/**
	 * Creates the gear box tile.
	 */
	@Override
	public TileEntity createNewTileEntity( final World w, final int meta )
	{
		return new TileGearBox();
	}

	/**
	 * Get the icon from subclass.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public abstract IIcon getIcon( final int side, final int meta );

	/**
	 * Get the name from subclass.
	 */
	@Override
	public abstract String getUnlocalizedName();

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
	 * Determine if the tile should be cranked.
	 */
	@Override
	public boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		// Get the tile
		TileGearBox gearBox = (TileGearBox)world.getTileEntity( x, y, z );

		// Crank it
		return gearBox.crank();
	}

	/**
	 * One of the adjacent blocks has changed.
	 */
	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block neighbor )
	{
		// Get the tile
		TileGearBox gearBox = (TileGearBox)w.getTileEntity( x, y, z );

		// Update it
		gearBox.updateCrankables();
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
