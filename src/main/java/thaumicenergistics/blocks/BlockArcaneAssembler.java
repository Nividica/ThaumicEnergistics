package thaumicenergistics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.gui.TEGuiHandler;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockArcaneAssembler
	extends AbstractBlockAEWrenchable
{

	public BlockArcaneAssembler()
	{
		// Call super with material machine (iron) 
		super( Material.iron );

		// Basic hardness
		this.setHardness( 1.0f );

		// Sound of metal
		this.setStepSound( Block.soundTypeMetal );

		// Place in the TE creative tab
		this.setCreativeTab( ThaumicEnergistics.ThETab );
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
		// Launch the gui.
		TEGuiHandler.launchGui( TEGuiHandler.ARCANE_ASSEMBLER_ID, player, world, x, y, z );

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

	/**
	 * Normal renderer.
	 */
	@Override
	public final boolean renderAsNormalBlock()
	{
		return true;
	}

}
