package thaumicenergistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.tiles.TileKnowledgeInscriber;
import thaumicenergistics.common.utils.EffectiveSide;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * {@link TileKnowledgeInscriber} block.
 * 
 * @author Nividica
 * 
 */
public class BlockKnowledgeInscriber
	extends AbstractBlockAEWrenchable
{
	public BlockKnowledgeInscriber()
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
	protected final boolean onBlockActivated( final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		// Launch the gui.
		ThEGuiHandler.launchGui( ThEGuiHandler.KNOWLEDGE_INSCRIBER, player, world, x, y, z );

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
			TileEntity tile = world.getTileEntity( x, y, z );

			if( tile instanceof TileKnowledgeInscriber )
			{
				TileKnowledgeInscriber tileKI = (TileKnowledgeInscriber)tile;
				// Does the inscriber have a cell?
				if( tileKI.hasKCore() )
				{
					// Spawn the core as an item entity.
					world.spawnEntityInWorld( new EntityItem( world, 0.5 + x, 0.5 + y, 0.2 + z, tileKI.getStackInSlot(
									TileKnowledgeInscriber.KCORE_SLOT ) ) );
				}
			}
		}

		// Call super
		super.breakBlock( world, x, y, z, block, metaData );
	}

	@Override
	public TileEntity createNewTileEntity( final World world, final int metaData )
	{
		return new TileKnowledgeInscriber();
	}

	/**
	 * Gets the standard block icon.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( final int side, final int meta )
	{
		// Face?
		if( side == ForgeDirection.OPPOSITES[meta] )
		{
			// Face texture
			return BlockTextureManager.KNOWLEDGE_INSCRIBER.getTextures()[1];
		}

		// Top or bottom?
		if( ( ForgeDirection.VALID_DIRECTIONS[side] == ForgeDirection.UP )
						|| ( ForgeDirection.VALID_DIRECTIONS[side] == ForgeDirection.DOWN ) )
		{
			// Bottom texture
			return BlockTextureManager.KNOWLEDGE_INSCRIBER.getTextures()[2];

		}

		// Sides
		return BlockTextureManager.KNOWLEDGE_INSCRIBER.getTextures()[0];
	}

	/**
	 * Gets the unlocalized name of the block.
	 */
	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.KNOWLEDGE_INSCRIBER.getUnlocalizedName();
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

	@Override
	public void onBlockPlacedBy( final World world, final int x, final int y, final int z, final EntityLivingBase player, final ItemStack itemStack )
	{
		// Set the metadata to up
		world.setBlockMetadataWithNotify( x, y, z, 0, 2 );
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

	@Override
	public boolean rotateBlock( final World world, final int x, final int y, final int z, final ForgeDirection side )
	{
		// Get and increment the meta data
		int metaData = world.getBlockMetadata( x, y, z ) + 1;

		// Bounds check
		if( metaData >= ForgeDirection.VALID_DIRECTIONS.length )
		{
			metaData = 0;
		}

		// Set the meta data
		world.setBlockMetadataWithNotify( x, y, z, metaData, 3 );

		return true;
	}

}
