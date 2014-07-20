package thaumicenergistics.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.registries.Renderers;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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

	@Override
	public TileEntity createNewTileEntity( World world, int metaData )
	{
		return new TileEssentiaProvider();
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

	@Override
	public void onNeighborChange( IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ )
	{
		// Inform the super
		super.onNeighborChange( world, x, y, z, tileX, tileY, tileZ );
		
		// Inform our tile entity
		( (TileEssentiaProvider)world.getTileEntity( x, y, z ) ).onNeighborChange( world, x, y, z );
		
	}

}
