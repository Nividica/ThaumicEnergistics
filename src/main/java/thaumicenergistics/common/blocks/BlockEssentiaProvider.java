package thaumicenergistics.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.registries.Renderers;
import thaumicenergistics.common.tiles.TileEssentiaProvider;

/**
 * {@link TileEssentiaProvider} block.
 *
 * @author Nividica
 *
 */
public class BlockEssentiaProvider
	extends AbstractBlockProviderBase
{

	public BlockEssentiaProvider()
	{
		// Call super with material machine (iron)
		super( Material.iron );

		// Basic hardness
		this.setHardness( 1.0f );

		// Sound of metal
		this.setStepSound( Block.soundTypeMetal );
	}

	@Override
	public TileEntity createNewTileEntity( final World world, final int metaData )
	{
		// Create a new provider tile, passing the side to attach to
		TileEssentiaProvider tile = new TileEssentiaProvider();

		// Setup the essentia provider
		tile.setupProvider( metaData );

		// Return the tile
		return tile;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( final int side, final int metaData )
	{
		return BlockTextureManager.ESSENTIA_PROVIDER.getTexture();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		// Provide our custom ID
		return Renderers.EssentiaProviderRenderID;
	}

	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.ESSENTIA_PROVIDER.getUnlocalizedName();
	}

	@Override
	public void onNeighborBlockChange( final World world, final int x, final int y, final int z, final Block neighbor )
	{
		TileEntity te = world.getTileEntity( x, y, z );
		if( te instanceof TileEssentiaProvider )
		{
			// Inform our tile entity a neighbor has changed
			( (TileEssentiaProvider)te ).checkGridConnectionColor();
		}
	}

}
