package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileInfusionProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockInfusionProvider
	extends BlockBaseProvider
{

	public BlockInfusionProvider()
	{
		// Call super with material machine (iron) 
		super( Material.iron );

		// Basic hardness
		this.setHardness( 1.0f );

		// Sound of metal
		this.setStepSound( Block.soundTypeMetal );
	}

	@Override
	public TileEntity createNewTileEntity( World world, int metaData )
	{
		// Create a new provider tile, passing the side to attach to
		TileInfusionProvider tile = new TileInfusionProvider();
		
		// Setup the infusion provider
		tile.setupProvider( metaData );
		
		// Return the tile
		return tile;
	}
	
	@Override
	public void onNeighborBlockChange( World world, int x, int y, int z, Block neighbor )
	{
		// Inform our tile entity a neighbor has changed
		( (TileInfusionProvider)world.getTileEntity( x, y, z ) ).checkGridConnectionColor();
	}
	
	@Override
	public void breakBlock( World world, int x, int y, int z, Block block, int metadata )
	{
		// Get our tile entity
		TileInfusionProvider tile = (TileInfusionProvider)world.getTileEntity( x, y, z );
		
		// Do we still have a tile?
		if( tile!= null )
		{
			// Inform it that its going away
			tile.onBreakBlock();
		}
		
		// Pass to super
		super.breakBlock( world, x, y, z, block, metadata );
	}

	@Override
	public int getRenderType()
	{
		// Provide our custom ID
		return Renderers.InfusionProviderRenderID;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( int side, int metaData )
	{
		return BlockTextureManager.INFUSION_PROVIDER.getTextures()[1];
	}

	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.INFUSION_PROVIDER.getUnlocalizedName();
	}

}
