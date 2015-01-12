package thaumicenergistics.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileAsItemRenderer
	implements IItemRenderer
{

	/**
	 * Tile entity renderer.
	 */
	private TileEntitySpecialRenderer tileRenderer;

	/**
	 * Fake tile entity used to render.
	 */
	private TileEntity fakeEntity;

	public TileAsItemRenderer( final TileEntitySpecialRenderer tileRenderer, final TileEntity fakeEntity )
	{
		// Set the renderer and entity
		this.tileRenderer = tileRenderer;
		this.fakeEntity = fakeEntity;
	}

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		return true;
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack item, final Object ... data )
	{
		// Push the matrix
		GL11.glPushMatrix();

		// Adjust position based on render type
		switch ( type )
		{
			case ENTITY:
				GL11.glTranslatef( -0.5F, -0.5F, -0.5F );
				break;

			case EQUIPPED_FIRST_PERSON:
				GL11.glTranslatef( 0.5F, 0.2F, 0.0F );
				break;

			case INVENTORY:
				GL11.glTranslatef( -0.5F, -0.5F, -0.5F );
				break;

			case FIRST_PERSON_MAP:
			case EQUIPPED:
				break;
		}

		// Render the entity
		this.tileRenderer.renderTileEntityAt( this.fakeEntity, 0, 0, 0, 0 );

		// Pop the matrix
		GL11.glPopMatrix();

	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		return true;
	}

}
