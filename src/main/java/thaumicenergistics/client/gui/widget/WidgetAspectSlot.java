package thaumicenergistics.client.gui.widget;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IWidgetHost;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.network.IAspectSlotPart;
import thaumicenergistics.common.network.packet.server.Packet_S_AspectSlot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WidgetAspectSlot
	extends AspectWidgetBase
{
	public static interface IConfigurable
	{
		public byte getConfigState();
	}

	private int id;
	private IAspectSlotPart part;
	private EntityPlayer player;
	private IConfigurable configurable;

	private byte configOption;

	public WidgetAspectSlot( final IWidgetHost hostGui, final EntityPlayer player, final IAspectSlotPart part, final int posX, final int posY )
	{
		this( hostGui, player, part, 0, posX, posY, null, (byte)0 );
	}

	public WidgetAspectSlot( final IWidgetHost hostGui, final EntityPlayer player, final IAspectSlotPart part, final int id, final int posX,
								final int posY )
	{
		this( hostGui, player, part, id, posX, posY, null, (byte)0 );
	}

	public WidgetAspectSlot( final IWidgetHost hostGui, final EntityPlayer player, final IAspectSlotPart part, final int id, final int posX,
								final int posY, final IConfigurable configurable, final byte configOption )
	{
		super( hostGui, null, posX, posY, player );
		this.player = player;
		this.part = part;
		this.id = id;
		this.configurable = configurable;
		this.configOption = configOption;
	}

	public boolean canRender()
	{
		return ( this.configurable == null ) || ( this.configurable.getConfigState() >= this.configOption );
	}

	@Override
	public void drawWidget()
	{
		// Is this slot open?
		if( this.canRender() )
		{
			// Disable lighting
			GL11.glDisable( GL11.GL_LIGHTING );

			// Enable blending
			GL11.glEnable( GL11.GL_BLEND );

			// Set the blend mode
			GL11.glBlendFunc( 770, 771 );

			// Full white
			GL11.glColor3f( 1.0F, 1.0F, 1.0F );

			// Bind to the gui texture
			Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_IO_BUS.getTexture() );

			// Draw this slot just like the center slot of the gui
			this.drawTexturedModalRect( this.xPosition, this.yPosition, 79, 39, ThEWidget.WIDGET_SIZE, ThEWidget.WIDGET_SIZE );

			// Do we have an aspect?
			if( this.getAspect() != null )
			{
				// Draw the aspect
				this.drawAspect();
			}

			// Re-enable lighting
			GL11.glEnable( GL11.GL_LIGHTING );

			// Re-disable blending
			GL11.glDisable( GL11.GL_BLEND );

		}

	}

	@Override
	public void getTooltip( final List<String> tooltip )
	{
		if( this.canRender() && ( this.getAspect() != null ) )
		{
			tooltip.add( this.aspectName );
		}
	}

	public void mouseClicked( final Aspect withAspect )
	{
		this.setAspect( withAspect, 1, false );

		Packet_S_AspectSlot.sendAspectChange( this.part, this.id, this.getAspect(), this.player );
	}

	@Override
	public void onMouseClicked()
	{
		// Ignored
	}
}
