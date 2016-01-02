package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaVibrationChamber;
import thaumicenergistics.util.EffectiveSide;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEssentiaVibrationChamber
	extends AbstractBlockAEWrenchable
{

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
	 * Returns the EVC tile entity, if found and is valid.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private TileEssentiaVibrationChamber getEVCTile( final IBlockAccess world, final int x, final int y, final int z )
	{
		// Ensure the world is not null
		if( world != null )
		{
			// Get the tile entity
			TileEntity te = world.getTileEntity( x, y, z );

			// Ensure it is an EVC
			if( te instanceof TileEssentiaVibrationChamber )
			{
				return (TileEssentiaVibrationChamber)te;
			}
		}

		return null;
	}

	@Override
	protected boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		// Ignore if player is sneaking
		if( player.isSneaking() )
		{
			return false;
		}

		// Server side?
		if( EffectiveSide.isServerSide() )
		{
			// Is the tile valid?
			if( this.getEVCTile( world, x, y, z ) != null )
			{
				// Launch the GUI
				ThEGuiHandler.launchGui( ThEGuiHandler.ESSENTIA_VIBRATION_CHAMBER, player, world, x, y, z );
			}
		}

		return true;
	}

	@Override
	protected ItemStack onDismantled( final World world, final int x, final int y, final int z )
	{
		// Ignore client side.
		if( EffectiveSide.isClientSide() )
		{
			return null;
		}

		// Get the chamber
		TileEssentiaVibrationChamber chamber = this.getEVCTile( world, x, y, z );

		// Validate
		if( chamber == null )
		{
			// Invalid EVC tile
			return null;
		}

		// Anything to save?
		if( !chamber.hasSaveDataForDismanle() )
		{
			// Nothing unique to save
			return null;
		}

		// Reset processing speed
		chamber.resetForDismantle();

		// Create the tag
		NBTTagCompound tag = new NBTTagCompound();

		// Save into the tag
		chamber.onNBTSave( tag );

		// Create the stack
		ItemStack representive = new ItemStack( this );

		// Set the tag
		representive.setTagCompound( tag );

		return representive;
	}

	@Override
	public TileEntity createNewTileEntity( final World world, final int metaData )
	{
		return new TileEssentiaVibrationChamber();
	}

	/**
	 * Gets the world icons
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param side
	 * @return
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon( final IBlockAccess world, final int x, final int y, final int z, final int side )
	{
		// Get the chamber
		TileEssentiaVibrationChamber chamber = this.getEVCTile( world, x, y, z );

		// Is the tile valid?
		if( chamber == null )
		{
			return this.getIcon( side, 0 );
		}

		// Is this side the face?		
		if( ( chamber.getForward() != null ) && ( side == chamber.getForward().ordinal() ) )
		{
			// Get the aspect in the chamber
			Aspect aspect = chamber.getProcessingAspect();

			if( aspect == Aspect.ENERGY )
			{
				// On: Potentia
				return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[3];
			}
			else if( aspect == Aspect.FIRE )
			{
				// On: Ignis
				return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[2];
			}

			// Off
			return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[1];
		}

		// Input
		return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[0];
	}

	/**
	 * Gets the inventory icons.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		// Inventory icons
		if( side == 4 )
		{
			// Face off
			return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[1];
		}

		// Sides
		return BlockTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTextures()[0];

	}

	/**
	 * Bright when active, dark when not.
	 */
	@Override
	public int getLightValue( final IBlockAccess world, final int x, final int y, final int z )
	{
		// Get the chamber
		TileEssentiaVibrationChamber chamber = this.getEVCTile( world, x, y, z );
		if( chamber != null )
		{
			// Return bright if chamber
			return( chamber.getProcessingAspect() != null ? 12 : 0 );
		}

		return 0;
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
		// Get the chamber tile
		TileEssentiaVibrationChamber chamber = this.getEVCTile( world, x, y, z );

		// Is the tile valid?
		if( chamber == null )
		{
			// Invalid tile
			return;
		}

		if( ( itemStack.getTagCompound() != null ) && ( EffectiveSide.isServerSide() ) )
		{
			// Load the saved data
			chamber.onNBTLoad( itemStack.getTagCompound() );
		}

		if( player instanceof EntityPlayer )
		{
			// Set the owner
			chamber.setOwner( (EntityPlayer)player );
		}

		ForgeDirection face = ForgeDirection.NORTH;

		// Is the player looking down?
		if( player.rotationPitch > 50 )
		{
			face = ForgeDirection.UP;
		}
		// Is the player looking up?
		else if( player.rotationPitch < -50 )
		{
			face = ForgeDirection.DOWN;
		}

		else
		{
			// Get the side index based on which direction the player is turned
			int sideIndex = MathHelper.floor_double( ( ( player.rotationYaw * 4.0F ) / 360.0F ) + 0.5D ) & 3;

			switch ( sideIndex )
			{
			case 0:
				face = ForgeDirection.NORTH;
				break;
			case 1:
				face = ForgeDirection.EAST;
				break;
			case 2:
				face = ForgeDirection.SOUTH;
				break;
			case 3:
				face = ForgeDirection.WEST;
				break;
			}
		}

		// Set the orientation
		chamber.setOrientation( face, ForgeDirection.UP );

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

	@Override
	public boolean rotateBlock( final World world, final int x, final int y, final int z, final ForgeDirection axis )
	{
		// Cast
		TileEssentiaVibrationChamber chamber = this.getEVCTile( world, x, y, z );

		// Validate
		if( chamber == null )
		{
			return false;
		}

		// Get the current facing
		ForgeDirection forward = chamber.getForward();
		ForgeDirection up = chamber.getUp();

		// Rotate
		forward = Platform.rotateAround( forward, axis );
		up = Platform.rotateAround( up, axis );

		// Apply rotation
		chamber.setOrientation( forward, up );

		return true;
	}

}
