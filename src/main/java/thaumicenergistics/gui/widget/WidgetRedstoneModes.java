package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.ThaumicEnergistics;
import appeng.api.config.RedstoneMode;
import com.google.common.base.Splitter;

public class WidgetRedstoneModes extends GuiButton
{
	private static final String GUI_TEXTURE_PATH = "textures/gui/redstonemodes.png";
	private static final ResourceLocation GUI_TEXTURE_RESOURCE = new ResourceLocation( ThaumicEnergistics.MODID, GUI_TEXTURE_PATH );

	private RedstoneMode redstoneMode;
	private boolean emitter = false;

	public WidgetRedstoneModes(int ID, int xPos, int yPos, int width, int height, RedstoneMode mode)
	{
		this( ID, xPos, yPos, width, height, mode, false );
	}

	public WidgetRedstoneModes(int ID, int xPos, int yPos, int width, int height, RedstoneMode mode, boolean emitter)
	{
		super( ID, xPos, yPos, width, height, "Display String" );

		this.emitter = emitter;

		this.redstoneMode = mode;
	}

	protected void drawHoveringText( List list, int x, int y, FontRenderer fontrenderer )
	{
		if ( !list.isEmpty() )
		{
			GL11.glDisable( 32826 );
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable( 2896 );
			GL11.glDisable( 2929 );
			int k = 0;
			Iterator iterator = list.iterator();

			while ( iterator.hasNext() )
			{
				String s = (String) iterator.next();
				int l = fontrenderer.getStringWidth( s );
				if ( l > k )
				{
					k = l;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if ( list.size() > 1 )
			{
				k1 += 2 + ( ( list.size() - 1 ) * 10 );
			}

			this.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect( i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1 );
			this.drawGradientRect( i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1 );
			this.drawGradientRect( i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1 );
			this.drawGradientRect( i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1 );
			this.drawGradientRect( i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1 );

			int i2 = 1347420415;
			int j2 = ( ( i2 & 0xFEFEFE ) >> 1 ) | ( i2 & 0xFF000000 );
			this.drawGradientRect( i1 - 3, ( j1 - 3 ) + 1, ( i1 - 3 ) + 1, ( j1 + k1 + 3 ) - 1, i2, j2 );
			this.drawGradientRect( i1 + k + 2, ( j1 - 3 ) + 1, i1 + k + 3, ( j1 + k1 + 3 ) - 1, i2, j2 );
			this.drawGradientRect( i1 - 3, j1 - 3, i1 + k + 3, ( j1 - 3 ) + 1, i2, i2 );
			this.drawGradientRect( i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2 );

			for( int k2 = 0; k2 < list.size(); k2++ )
			{
				String s1 = (String) list.get( k2 );
				fontrenderer.drawStringWithShadow( s1, i1, j1, -1 );

				if ( k2 == 0 )
				{
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			GL11.glEnable( 2896 );
			GL11.glEnable( 2929 );
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable( 32826 );
		}
	}

	@Override
	public void drawButton( Minecraft minecraftInstance, int x, int y )
	{
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		this.mouseDragged( minecraftInstance, x, y );

		minecraftInstance.getTextureManager().bindTexture( GUI_TEXTURE_RESOURCE );

		this.drawTexturedModalRect( this.xPosition, this.yPosition, 0, 16, 16, 16 );

		switch ( this.redstoneMode )
		{
			case HIGH_SIGNAL:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 16, 0, 16, 16 );
				break;

			case IGNORE:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 48, 0, 16, 16 );
				break;

			case LOW_SIGNAL:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 0, 0, 16, 16 );
				break;

			case SIGNAL_PULSE:
				this.drawTexturedModalRect( this.xPosition, this.yPosition, 32, 0, 16, 16 );
				break;
		}
	}

	public void drawTooltip( int mouseX, int mouseY )
	{
		List<String> description = new ArrayList<String>();

		description.add( StatCollector.translateToLocal( "AppEng.GuiTooltip.RedstoneMode" ) );

		String explanation = "";

		switch ( this.redstoneMode )
		{
			case HIGH_SIGNAL:
				explanation = StatCollector.translateToLocal( this.emitter	? "AppEng.GuiITooltip.EmitLevelAbove"
																			: "AppEng.GuiITooltip.ActiveWithSignal" );
				break;

			case IGNORE:
				explanation = StatCollector.translateToLocal( "AppEng.GuiITooltip.AlwaysActive" );
				break;

			case LOW_SIGNAL:
				explanation = StatCollector.translateToLocal( this.emitter	? "AppEng.GuiITooltip.EmitLevelsBelow"
																			: "AppEng.GuiITooltip.ActiveWithoutSignal" );
				break;

			case SIGNAL_PULSE:
				explanation = StatCollector.translateToLocal( "AppEng.GuiITooltip.ActiveOnPulse" );
				break;

		}

		for( String current : Splitter.fixedLength( 30 ).split( explanation ) )
		{
			description.add( EnumChatFormatting.GRAY + current );
		}

		Minecraft mc = Minecraft.getMinecraft();

		if ( ( mouseX >= this.xPosition ) && ( mouseX <= ( this.xPosition + this.width ) ) && ( mouseY >= this.yPosition ) &&
						( mouseY <= ( this.yPosition + this.height ) ) )
		{
			this.drawHoveringText( description, mouseX, mouseY, mc.fontRenderer );
		}
	}

	public void setRedstoneMode( RedstoneMode mode )
	{
		this.redstoneMode = mode;
	}
}
