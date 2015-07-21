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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaVibrationChamber;
import thaumicenergistics.tileentities.abstraction.TileEVCBase;
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

	@Override
	protected boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		// Ignore fake players
		if( player instanceof FakePlayer )
		{
			return false;
		}

		// Launch the GUI
		ThEGuiHandler.launchGui( ThEGuiHandler.ESSENTIA_VIBRATION_CHAMBER, player, world, x, y, z );

		return true;
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
		TileEntity tileChamber = world.getTileEntity( x, y, z );

		// Ensure the chamber is present
		if( !( tileChamber instanceof TileEssentiaVibrationChamber ) )
		{
			return this.getIcon( side, 0 );
		}
		TileEssentiaVibrationChamber chamber = ( (TileEssentiaVibrationChamber)tileChamber );

		// Is this side the face?		
		if( ( chamber.getForward() != null ) && ( side == chamber.getForward().ordinal() ) )
		{
			// Get the aspect in the chamber
			Aspect aspect = chamber.processingAspect;

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
		// Get the chamber
		TileEntity tileChamber = world.getTileEntity( x, y, z );

		// Ensure the chamber is present
		if( tileChamber instanceof TileEVCBase )
		{
			if( itemStack.getTagCompound() != null )
			{
				//TODO: Load the saved data
				//chamber.onLoadNBT( itemStack.getTagCompound() );
			}

			if( player instanceof EntityPlayer )
			{
				// Set the owner
				( (TileEVCBase)tileChamber ).setOwner( (EntityPlayer)player );
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
				int sideIndex = MathHelper.floor_double( ( player.rotationYaw * 4.0F / 360.0F ) + 0.5D ) & 3;

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
			( (TileEVCBase)tileChamber ).setOrientation( face, ForgeDirection.UP );
		}
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
	public boolean rotateBlock( final World world, final int x, final int y, final int z, final ForgeDirection side )
	{
		// Get the chamber
		TileEntity tileChamber = world.getTileEntity( x, y, z );

		// Ensure the chamber is present
		if( !( tileChamber instanceof TileEVCBase ) )
		{
			return false;
		}

		// Increment the direction
		int sideIndex = ( (TileEVCBase)tileChamber ).getForward().ordinal() + 1;

		// Bounds check direction
		if( sideIndex >= ForgeDirection.VALID_DIRECTIONS.length )
		{
			sideIndex = 0;
		}

		// Apply rotation
		( (TileEVCBase)tileChamber ).setOrientation( ForgeDirection.getOrientation( sideIndex ), ForgeDirection.UP );

		return true;
	}

}
