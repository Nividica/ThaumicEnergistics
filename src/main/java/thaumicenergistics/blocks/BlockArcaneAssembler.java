package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.implementations.items.IMemoryCard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	 * Called when the block is broken.
	 */
	@Override
	public void breakBlock( final World world, final int x, final int y, final int z, final Block block, final int metaData )
	{
		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Get the tile
			TileArcaneAssembler assembler = (TileArcaneAssembler)world.getTileEntity( x, y, z );

			// Does the inscriber have a cell?
			if( assembler != null )
			{
				if( assembler.hasKCore() )
				{
					// Spawn the core as an item entity.
					world.spawnEntityInWorld( new EntityItem( world, 0.5 + x, 0.5 + y, 0.2 + z, assembler.getInternalInventory().getStackInSlot(
						TileArcaneAssembler.KCORE_SLOT_INDEX ) ) );
				}

				// Inform the tile it is being broken
				assembler.onBreak();
			}
		}

		// Call super
		super.breakBlock( world, x, y, z, block, metaData );
	}

	/**
	 * Creates a new Arcane Assembler tile
	 */
	@Override
	public TileEntity createNewTileEntity( final World p_149915_1_, final int p_149915_2_ )
	{
		TileArcaneAssembler assembler = new TileArcaneAssembler();

		assembler.setupAssemblerTile();

		return assembler;
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
	public boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		// Get what the player is holding
		ItemStack playerHolding = player.inventory.getCurrentItem();

		// Are they holding a memory card?
		if( ( playerHolding != null ) && ( playerHolding.getItem() instanceof IMemoryCard ) )
		{
			// Get the tile
			TileArcaneAssembler assembler = (TileArcaneAssembler)world.getTileEntity( x, y, z );
			if( assembler != null )
			{
				// Inform the tile of the event
				assembler.onMemoryCardActivate( player, (IMemoryCard)playerHolding.getItem(), playerHolding );
			}
		}
		else
		{
			// Launch the gui.
			ThEGuiHandler.launchGui( ThEGuiHandler.ARCANE_ASSEMBLER_ID, player, world, x, y, z );
		}

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
