package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.server.PacketServerAspectSlot;
import thaumicenergistics.texture.GuiTextureManager;

public class WidgetAspectSlot
	extends AbstractAspectWidget
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

	public WidgetAspectSlot( IWidgetHost hostGui, EntityPlayer player, IAspectSlotPart part, int posX, int posY )
	{
		this( hostGui, player, part, 0, posX, posY, null, (byte)0 );
	}

	public WidgetAspectSlot( IWidgetHost hostGui, EntityPlayer player, IAspectSlotPart part, int id, int posX, int posY )
	{
		this( hostGui, player, part, id, posX, posY, null, (byte)0 );
	}

	public WidgetAspectSlot( IWidgetHost hostGui, EntityPlayer player, IAspectSlotPart part, int id, int posX, int posY, IConfigurable configurable,
								byte configOption )
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
	public void drawTooltip( int mouseX, int mouseY )
	{
		if( this.canRender() && ( this.aspect != null ) )
		{
			List<String> toolTips = new ArrayList<String>();

			toolTips.add( this.aspectName );

			this.drawTooltip( toolTips, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer );

		}
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
			this.drawTexturedModalRect( this.xPosition, this.yPosition, 79, 39, AbstractWidget.WIDGET_SIZE, AbstractWidget.WIDGET_SIZE );

			// Do we have an aspect?
			if( this.aspect != null )
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

	public void mouseClicked( Aspect withAspect )
	{
		this.aspect = withAspect;

		new PacketServerAspectSlot().createUpdatePartAspect( this.part, this.id, this.aspect, this.player ).sendPacketToServer();
	}

	@Override
	public void mouseClicked()
	{
		// Ignored
	}
}
