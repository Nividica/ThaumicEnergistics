package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block definition of the essentia provider.
 * @author Nividica
 *
 */
public class BlockEssentiaProvider
	extends BlockContainer
{
	
	public BlockEssentiaProvider()
	{
		// Call super with material machine (iron) 
		super( Material.iron );

		// Basic hardness
		this.setHardness( 1.0f );

		// Sound of metal
		this.setStepSound( Block.soundTypeMetal );

		// Place in our creative tab
		this.setCreativeTab( ThaumicEnergistics.ModTab );
	}
	
	// Sets the metadata for the block based on which side of the neighbor block they clicked on.
    @Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metaData )
    {
    	// Get the opposite face and return it
    	return ForgeDirection.OPPOSITES[side];
    }

	@Override
	public TileEntity createNewTileEntity( World world, int metaData )
	{
		// Create a new provider tile, passing the side to attach to
		return new TileEssentiaProvider( metaData );
	}
	
	@Override
	public void onNeighborBlockChange( World world, int x, int y, int z, Block neighbor )
	{
		// Inform our tile entity a neighbor has changed
		( (TileEssentiaProvider)world.getTileEntity( x, y, z ) ).checkGridConnectionColor();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		// We have a custom renderer for this block
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		// Occlude adjoining faces.
		return true;
	}

	@Override
	public int getRenderType()
	{
		// Provide our custom ID
		return Renderers.EssentiaProviderRenderID;
	}

	@Override
	public boolean canRenderInPass( int pass )
	{
		// Mark the current pass
		Renderers.currentRenderPass = pass;

		// We render during both passes
		return true;
	}

	@Override
	public int getRenderBlockPass()
	{
		// Ensure the alpha pass is rendered
		return Renderers.PASS_ALPHA;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon( int side, int metaData )
	{
		return BlockTextureManager.ESSENTIA_PROVIDER.getTexture();
	}

	@Override
	public String getUnlocalizedName()
	{
		return BlockEnum.ESSENTIA_PROVIDER.getUnlocalizedName();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons( IIconRegister register )
	{
		// Ignored
		
	}


}
