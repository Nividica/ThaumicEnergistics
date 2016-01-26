package thaumicenergistics.common.blocks;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.tiles.TileArcaneAssembler;
import thaumicenergistics.common.utils.EffectiveSide;
import appeng.api.implementations.items.IMemoryCard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * {@link TileArcaneAssembler} block.
 * 
 * @author Nividica
 * 
 */
public class BlockArcaneAssembler
	extends AbstractBlockAEWrenchable
{

	public static final int MAX_SPEED_UPGRADES = 4;

	public BlockArcaneAssembler()
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
	 * Called when the assembler is right-clicked
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
		// Get what the player is holding
		ItemStack playerHolding = player.inventory.getCurrentItem();

		// Get the tile
		TileEntity tileAssembler = world.getTileEntity( x, y, z );

		if( tileAssembler instanceof TileArcaneAssembler )
		{
			// Are they holding a memory card?
			if( ( playerHolding != null ) && ( playerHolding.getItem() instanceof IMemoryCard ) )
			{
				// Inform the tile of the event
				( (TileArcaneAssembler)tileAssembler ).onMemoryCardActivate( player, (IMemoryCard)playerHolding.getItem(), playerHolding );
			}
			else
			{
				// Can the player interact with the assembler?
				if( ( (TileArcaneAssembler)tileAssembler ).isUseableByPlayer( player ) )
				{
					// Launch the gui.
					ThEGuiHandler.launchGui( ThEGuiHandler.ARCANE_ASSEMBLER_ID, player, world, x, y, z );
				}
			}
		}

		return true;
	}

	/**
	 * Called when the assembler is dismantled via wrench.
	 */
	@Override
	protected ItemStack onDismantled( final World world, final int x, final int y, final int z )
	{
		// Create the itemstack
		ItemStack itemStack = new ItemStack( this );

		// Get the tile
		TileEntity tileAssembler = world.getTileEntity( x, y, z );

		if( tileAssembler instanceof TileArcaneAssembler )
		{
			// Create a compound tag
			NBTTagCompound data = new NBTTagCompound();

			// Save the vis levels
			( (TileArcaneAssembler)tileAssembler ).writeVisLevelsToNBT( data );

			// Set the itemstack tag
			if( !data.hasNoTags() )
			{
				itemStack.setTagCompound( data );
			}
		}

		return itemStack;
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
			TileEntity tileAssembler = world.getTileEntity( x, y, z );

			if( tileAssembler instanceof TileArcaneAssembler )
			{
				// Get the drops
				ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
				( (TileArcaneAssembler)tileAssembler ).getDrops( world, x, y, z, drops );

				for( ItemStack drop : drops )
				{
					world.spawnEntityInWorld( new EntityItem( world, 0.5 + x, 0.5 + y, 0.2 + z, drop ) );
				}

				// Inform the tile it is being broken
				( (TileArcaneAssembler)tileAssembler ).onBreak();
			}
		}

		// Call super
		super.breakBlock( world, x, y, z, block, metaData );
	}

	/**
	 * Creates a new Arcane Assembler tile
	 */
	@Override
	public TileEntity createNewTileEntity( final World world, final int metadata )
	{
		return new TileArcaneAssembler();
	}

	/**
	 * Gets the standard block icon.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		// Sides
		return BlockTextureManager.ARCANE_ASSEMBLER.getTexture();
	}

	/**
	 * Gets the unlocalized name of the block.
	 */
	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.ARCANE_ASSEMBLER.getUnlocalizedName();
	}

	@Override
	public final boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public final boolean isSideSolid( final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side )
	{
		return false;
	}

	@Override
	public void onBlockPlacedBy( final World world, final int x, final int y, final int z, final EntityLivingBase player, final ItemStack itemStack )
	{
		// Get the tile
		TileEntity tileAssembler = world.getTileEntity( x, y, z );

		if( tileAssembler instanceof TileArcaneAssembler )
		{
			if( itemStack.hasTagCompound() )
			{
				// Load the vis data
				( (TileArcaneAssembler)tileAssembler ).readVisLevelsFromNBT( itemStack.getTagCompound() );
			}

			if( player instanceof EntityPlayer )
			{
				// Set the owner
				( (TileArcaneAssembler)tileAssembler ).setOwner( (EntityPlayer)player );
			}
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
	public final boolean renderAsNormalBlock()
	{
		return false;
	}

	/**
	 * Prevents MC from using the default block renderer.
	 */
	@Override
	public boolean shouldSideBeRendered( final IBlockAccess iblockaccess, final int i, final int j, final int k, final int l )
	{
		return false;
	}

}
