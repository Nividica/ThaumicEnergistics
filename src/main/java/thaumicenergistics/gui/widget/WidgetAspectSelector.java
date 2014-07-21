package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureMap;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.aspect.AspectStack;

public class WidgetAspectSelector
	extends AbstractAspectWidget
{
	public static int WIDGET_WIDTH = 18;
	public static int WIDGET_HEIGHT = 18;

	private long amount = 0L;
	private int color;
	private int borderThickness;

	public WidgetAspectSelector(IAspectSelectorGui selectorGui, AspectStack stack)
	{
		super( selectorGui, WIDGET_HEIGHT, WIDGET_WIDTH, stack.aspect );

		this.amount = stack.amount;

		this.color = -16711681;

		this.borderThickness = 1;
	}

	private void drawHollowRectWithCorners( int posX, int posY, int heigth, int width, int color, int thickness )
	{
		Gui.drawRect( posX, posY, posX + heigth, posY + thickness, color );
		Gui.drawRect( posX, ( posY + width ) - thickness, posX + heigth, posY + width, color );
		Gui.drawRect( posX, posY, posX + thickness, posY + width, color );
		Gui.drawRect( ( posX + heigth ) - thickness, posY, posX + heigth, posY + width, color );

		Gui.drawRect( posX, posY, posX + thickness + 1, posY + thickness + 1, color );
		Gui.drawRect( posX + heigth, posY + width, ( posX + heigth ) - thickness - 1, ( posY + width ) - thickness - 1, color );
		Gui.drawRect( posX + heigth, posY, ( posX + heigth ) - thickness - 1, posY + thickness + 1, color );
		Gui.drawRect( posX, posY + width, posX + thickness + 1, ( posY + width ) - thickness - 1, color );
	}

	@Override
	public boolean drawTooltip( int posX, int posY, int mouseX, int mouseY )
	{
		if ( ( this.aspect == null ) || ( this.amount <= 0L ) )
		{
			return false;
		}

		String amountToText = Long.toString( this.amount );

		List<String> description = new ArrayList<String>();

		description.add( this.aspect.getName() );

		description.add( amountToText );

		this.drawHoveringText( description, mouseX - this.selectorGui.guiLeft(), mouseY - this.selectorGui.guiTop(),
			Minecraft.getMinecraft().fontRenderer );

		return true;
	}

	@Override
	public void drawWidget( int posX, int posY )
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( TextureMap.locationBlocksTexture );

		GL11.glDisable( 2896 );
		GL11.glEnable( 3042 );
		GL11.glBlendFunc( 770, 771 );
		GL11.glColor3f( 1.0F, 1.0F, 1.0F );

		AspectStack terminalFluid = ( (IAspectSelectorGui) this.selectorGui ).getSelectedAspect();

		Aspect currentAspect = terminalFluid != null ? terminalFluid.aspect : null;

		if ( this.aspect != null )
		{
			UtilsFX.drawTag( posX + 1, posY + 1, this.aspect, 0, 0, this.zLevel );
		}
		if ( this.aspect == currentAspect )
		{
			this.drawHollowRectWithCorners( posX, posY, this.height, this.width, this.color, this.borderThickness );
		}

		GL11.glEnable( 2896 );
		GL11.glDisable( 3042 );
	}

	public long getAmount()
	{
		return this.amount;
	}

	@Override
	public void mouseClicked( int posX, int posY, int mouseX, int mouseY )
	{
		if ( this.aspect != null )
		{
			( (IAspectSelectorGui) this.selectorGui ).getContainer().setSelectedAspect( this.aspect );
		}
	}

	public void setAmount( long amount )
	{
		this.amount = amount;
	}

}
