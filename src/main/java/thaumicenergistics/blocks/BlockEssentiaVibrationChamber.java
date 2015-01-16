package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaVibrationChamber;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEssentiaVibrationChamber
	extends AbstractBlockAEWrenchable
{
	public static final int[] FACE_DIRS = new int[] { 2, 5, 3, 4 };

	private static final int FLAG_ON = 8, BITS_SIDE = 7;

	public BlockEssentiaVibrationChamber()
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
	 * Gets the side that represents the face of the block.
	 * 
	 * @param metaData
	 * @return
	 */
	public static final ForgeDirection getFaceSideFromMeta( final int metaData )
	{
		int index = ( metaData & BlockEssentiaVibrationChamber.BITS_SIDE ) - 1;

		return ForgeDirection.VALID_DIRECTIONS[BlockEssentiaVibrationChamber.FACE_DIRS[index]];

	}

	/**
	 * Sets the index of the side that should be the face.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param index
	 */
	public static final void setBlockFaceIndex( final World world, final int x, final int y, final int z, int index )
	{
		// Ensure index bits are clean
		index = index % BlockEssentiaVibrationChamber.FACE_DIRS.length;

		// Get the block meta data
		int metaData = world.getBlockMetadata( x, y, z );

		// Extract the on/off flag
		int onFlag = metaData & BlockEssentiaVibrationChamber.FLAG_ON;

		// Combine
		metaData = ( index + 1 ) | onFlag;

		// Set
		world.setBlockMetadataWithNotify( x, y, z, metaData, 3 );
	}

	/**
	 * Sets the face texture to on or off.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param isOn
	 */
	public static final void setBlockOnState( final World world, final int x, final int y, final int z, final boolean isOn )
	{
		// Get the block meta data, stripping the ON flags
		int metaData = world.getBlockMetadata( x, y, z ) & BlockEssentiaVibrationChamber.BITS_SIDE;

		// Combine
		if( isOn )
		{
			metaData |= BlockEssentiaVibrationChamber.FLAG_ON;
		}

		// Set
		world.setBlockMetadataWithNotify( x, y, z, metaData, 2 );
	}

	@Override
	protected boolean onWrenched( final World world, final int x, final int y, final int z, final int side )
	{
		// Get and increment the meta data, striping ON flag
		int index = ( world.getBlockMetadata( x, y, z ) & BlockEssentiaVibrationChamber.BITS_SIDE );

		// Bounds check
		if( index >= BlockEssentiaVibrationChamber.FACE_DIRS.length )
		{
			index = 0;
		}

		// Set
		BlockEssentiaVibrationChamber.setBlockFaceIndex( world, x, y, z, index );

		return true;
	}

	@Override
	public TileEntity createNewTileEntity( final World world, final int metaData )
	{
		return new TileEssentiaVibrationChamber().setupChamberTile();
	}

	/**
	 * Gets the standard block icon.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		// Inventory icons
		if( meta == 0 )
		{
			if( side == 4 )
			{
				// Face off
				return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[1];
			}

			// Sides
			return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[0];
		}

		int index = ( meta & BlockEssentiaVibrationChamber.BITS_SIDE ) - 1;

		// World icons
		// Is face?
		if( side == BlockEssentiaVibrationChamber.FACE_DIRS[index] )
		{
			if( ( meta & BlockEssentiaVibrationChamber.FLAG_ON ) > 0 )
			{
				// Face on
				return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[2];
			}

			// Face off
			return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[1];
		}

		// Sides
		return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[0];

	}

	/**
	 * Gets the unlocalized name of the block.
	 */
	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.ESSENTIA_VIBRATION_CHAMBER.getUnlocalizedName();
	}

	@Override
	public void onBlockPlacedBy( final World world, final int x, final int y, final int z, final EntityLivingBase player, final ItemStack itemStack )
	{
		// Get the tile
		TileEssentiaVibrationChamber chamber = (TileEssentiaVibrationChamber)world.getTileEntity( x, y, z );

		if( chamber != null )
		{
			if( itemStack.getTagCompound() != null )
			{
				//TODO: Load the saved data
				//chamber.onLoadNBT( itemStack.getTagCompound() );
			}

			if( player instanceof EntityPlayer )
			{
				// Set the owner
				chamber.setOwner( (EntityPlayer)player );
			}
		}

		// Get the index based on which direction the player is turned
		int index = MathHelper.floor_double( ( player.rotationYaw * 4.0F / 360.0F ) + 0.5D ) & 3;

		// Set the meta data
		world.setBlockMetadataWithNotify( x, y, z, ( index + 1 ), 2 );
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

}
