package thaumicenergistics.render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.items.ItemCraftingAspect;

public class RendererItemCraftingAspect
	implements IItemRenderer
{

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		// Handle only inventory
		return( type == ItemRenderType.INVENTORY );
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack stack, final Object ... data )
	{
		boolean isKnown = false;
		Aspect aspect = null;

		// Is the stack setup?
		if( ( stack.getItemDamage() == 0 ) && ( stack.hasTagCompound() ) )
		{
			// Get the player
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;

			// Get the aspect
			aspect = ItemCraftingAspect.getAspect( stack );

			// Can the knowledge be checked?
			if( ( player != null ) && ( aspect != null ) )
			{
				isKnown = ItemCraftingAspect.canPlayerSeeAspect( player, aspect );
			}
		}

		// Is the aspect known?
		if( isKnown )
		{
			// Render aspect
			UtilsFX.drawTag( 0, 0, aspect, 0.0F, 0, 0.0D );
		}
		else
		{
			// Render unknown

			// Disable standard lighting
			GL11.glDisable( GL11.GL_LIGHTING );

			// Set the alpha function
			GL11.glAlphaFunc( GL11.GL_GREATER, 0.003921569F );

			// Enable alpha blending
			GL11.glEnable( GL11.GL_BLEND );
			GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE );

			// Render
			UtilsFX.bindTexture( "textures/aspects/_unknown.png" );
			GL11.glColor4f( 0.5F, 0.5F, 0.5F, 0.5F );
			UtilsFX.drawTexturedQuadFull( 0, 0, 0.0D );

			// Restore
			GL11.glEnable( GL11.GL_LIGHTING );
			GL11.glDisable( GL11.GL_BLEND );
		}
	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		// Do not use the helper
		return false;
	}
}
