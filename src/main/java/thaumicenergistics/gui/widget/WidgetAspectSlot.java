package thaumicenergistics.gui.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.PacketAspectSlot;

public class WidgetAspectSlot extends AbstractAspectWidget
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

	
	public WidgetAspectSlot(IWidgetHost hostGui, EntityPlayer player, IAspectSlotPart part, int posX, int posY)
	{
		this( hostGui, player, part, 0, posX, posY, null, (byte) 0 );
	}

	public WidgetAspectSlot(IWidgetHost hostGui, EntityPlayer player, IAspectSlotPart part, int id, int posX, int posY)
	{
		this( hostGui, player, part, id, posX, posY, null, (byte) 0 );
	}
	

	public WidgetAspectSlot(IWidgetHost hostGui, EntityPlayer player, IAspectSlotPart part, int id, int posX, int posY, IConfigurable configurable, byte configOption)
	{
		super( hostGui, null, posX, posY );
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
		if ( this.canRender() && ( this.aspect != null ) )
		{
			List<String> toolTips = new ArrayList<String>();

			toolTips.add( this.aspect.getName() );

			this.drawHoveringText( toolTips, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer );

		}
	}

	@Override
	public void drawWidget()
	{
		if ( ( this.aspect != null ) && this.canRender() )
		{
			GL11.glDisable( GL11.GL_LIGHTING );

			GL11.glEnable( GL11.GL_BLEND );
			
			GL11.glBlendFunc( 770, 771 );
			
			GL11.glColor3f( 1.0F, 1.0F, 1.0F );

			UtilsFX.drawTag( this.xPosition + 1, this.yPosition + 1, this.aspect, 0, 0, this.zLevel );
			
			GL11.glEnable( GL11.GL_LIGHTING );
			
			GL11.glDisable( GL11.GL_BLEND );
		}
	}

	public void mouseClicked( Aspect withAspect )
	{
		this.aspect = withAspect;

		new PacketAspectSlot( this.part, this.id, this.aspect, this.player ).sendPacketToServer();
	}

	@Override
	public void mouseClicked()
	{
		// Ignored
	}
}
