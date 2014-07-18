package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.PacketAspectSlot;
import thaumicenergistics.render.GuiTextureManager;

public class WidgetAspectSlot extends Gui
{
	public static interface IConfigurable
	{
		public byte getConfigState();
	}
	
	private int id;
	private int posX;
	private int posY;
	private Aspect aspect;
	private IAspectSlotPart part;
	private EntityPlayer player;
	private IConfigurable configurable;

	private byte configOption;

	public WidgetAspectSlot(EntityPlayer player, IAspectSlotPart part, int posX, int posY)
	{
		this( player, part, 0, posX, posY, null, (byte) 0 );
	}

	public WidgetAspectSlot(EntityPlayer player, IAspectSlotPart part, int id, int posX, int posY)
	{
		this( player, part, id, posX, posY, null, (byte) 0 );
	}

	public WidgetAspectSlot(EntityPlayer player, IAspectSlotPart part, int id, int posX, int posY, IConfigurable configurable, byte configOption)
	{
		this.player = player;
		this.part = part;
		this.id = id;
		this.posX = posX;
		this.posY = posY;
		this.configurable = configurable;
		this.configOption = configOption;
	}

	protected void drawHoveringText( List<String> list, int x, int y, FontRenderer fontrenderer )
	{
		boolean lighting_enabled = GL11.glIsEnabled( 2896 );

		if ( !list.isEmpty() )
		{
			GL11.glDisable( 32826 );
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable( 2896 );
			GL11.glDisable( 2929 );
			int k = 0;
			for( Object string : list )
			{
				String s = (String) string;
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
				String s1 = list.get( k2 );
				fontrenderer.drawStringWithShadow( s1, i1, j1, -1 );
				if ( k2 == 0 )
				{
					j1 += 2;
				}
				j1 += 10;
			}

			this.zLevel = 0.0F;
			if ( lighting_enabled )
			{
				GL11.glEnable( 2896 );
			}
			GL11.glEnable( 2929 );
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable( 32826 );
		}
	}

	public boolean canRender()
	{
		return ( this.configurable == null ) || ( this.configurable.getConfigState() >= this.configOption );
	}

	public void drawTooltip( int mouseX, int mouseY )
	{
		if ( this.canRender() && ( this.aspect != null ) )
		{
			List<String> toolTips = new ArrayList<String>();

			toolTips.add( this.aspect.getName() );

			this.drawHoveringText( toolTips, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer );

		}
	}

	public void drawWidget()
	{
		if ( this.canRender() )
		{
			GL11.glDisable( 2896 );
			GL11.glEnable( 3042 );
			GL11.glBlendFunc( 770, 771 );
			GL11.glColor3f( 1.0F, 1.0F, 1.0F );
			GL11.glDisable( 2896 );
			GL11.glColor3f( 1.0F, 1.0F, 1.0F );
			Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_IO_BUS.getTexture() );
			this.drawTexturedModalRect( this.posX, this.posY, 79, 39, 18, 18 );
			GL11.glEnable( 2896 );

			if ( this.aspect != null )
			{
				Minecraft.getMinecraft().renderEngine.bindTexture( TextureMap.locationBlocksTexture );
				GL11.glDisable( 2896 );
				GL11.glColor3f( 1.0F, 1.0F, 1.0F );

				UtilsFX.drawTag( this.posX + 1, this.posY + 1, this.aspect, 0, 0, this.zLevel );

				GL11.glEnable( 2896 );
				GL11.glDisable( 3042 );
			}
		}
	}

	public Aspect getAspect()
	{
		return this.aspect;
	}

	public int getPosX()
	{
		return this.posX;
	}

	public int getPosY()
	{
		return this.posY;
	}

	public void mouseClicked( Aspect withAspect )
	{
		this.aspect = withAspect;

		new PacketAspectSlot( this.part, this.id, this.aspect, this.player ).sendPacketToServer();
	}

	public void setAspect( Aspect aspect )
	{
		this.aspect = aspect;
	}
}
