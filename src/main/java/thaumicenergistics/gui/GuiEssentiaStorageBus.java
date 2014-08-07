package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.gui.buttons.ButtonRedstoneModes;
import thaumicenergistics.gui.widget.AbstractWidget;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaStorageBus;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.GuiHelper;
import appeng.api.AEApi;

/**
 * Gui for the storage bus.
 * 
 * @author Nividica
 * 
 */
public class GuiEssentiaStorageBus
	extends GuiWidgetHost
	implements IAspectSlotGui
{
	/**
	 * The number of columns in the gui.
	 */
	private static final int COLUMNS = 9;

	/**
	 * The number of rows in the gui.
	 */
	private static final int ROWS = 6;

	/**
	 * The starting X position of the widgets.
	 */
	private static final int WIDGET_X_POS = 7;

	/**
	 * The starting Y position of the widgets.
	 */
	private static final int WIDGET_Y_POS = 17;

	/**
	 * The width of the gui
	 */
	public static final int GUI_WIDTH = 246;

	/**
	 * The height of the gui
	 */
	public static final int GUI_HEIGHT = 222;

	/**
	 * Player viewing this gui.
	 */
	private EntityPlayer player;

	/**
	 * Filter widget list
	 */
	private List<WidgetAspectSlot> aspectWidgetList = new ArrayList<WidgetAspectSlot>();

	/**
	 * Filter aspect list
	 */
	private List<Aspect> filteredAspects = new ArrayList<Aspect>();

	/**
	 * Does the player have a network tool?
	 */
	private boolean hasNetworkTool;

	/**
	 * Creates the GUI.
	 * 
	 * @param part
	 * The part associated with the gui.
	 * @param player
	 * The player viewing the gui.
	 * @param container
	 * The inventory container.
	 */
	public GuiEssentiaStorageBus( AEPartEssentiaStorageBus part, EntityPlayer player )
	{
		// Call super
		super( new ContainerPartEssentiaStorageBus( part, player ) );

		// Set the player
		this.player = player;

		// Set the network tool
		this.hasNetworkTool = ( (ContainerPartEssentiaStorageBus)this.inventorySlots ).hasNetworkTool();

		// Create the widgets
		for( int row = 0; row < GuiEssentiaStorageBus.ROWS; row++ )
		{
			for( int column = 0; column < GuiEssentiaStorageBus.COLUMNS; column++ )
			{
				this.aspectWidgetList.add( new WidgetAspectSlot( this, this.player, part, ( row * GuiEssentiaStorageBus.COLUMNS ) + column,
								GuiEssentiaStorageBus.WIDGET_X_POS + ( AbstractWidget.WIDGET_SIZE * column ),
								GuiEssentiaStorageBus.WIDGET_Y_POS + ( AbstractWidget.WIDGET_SIZE * row ) ) );
			}
		}

		// Set the width and height
		this.xSize = GuiEssentiaStorageBus.GUI_WIDTH;
		this.ySize = GuiEssentiaStorageBus.GUI_HEIGHT;

		// Request an update
		new PacketServerEssentiaStorageBus().createRequestFullUpdate( player, part ).sendPacketToServer();
	}

	private boolean isMouseOverSlot( Slot slot, int x, int y )
	{
		return GuiHelper.isPointInGuiRegion( slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y, this.guiLeft, this.guiTop );
	}

	/**
	 * Draws the gui background
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_STORAGE_BUS.getTexture() );

		
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, 176, 222 );

		this.drawTexturedModalRect( this.guiLeft + 179, this.guiTop, 179, 0, 32, 86 );

		if ( this.hasNetworkTool )
		{
			this.drawTexturedModalRect( this.guiLeft + 179, this.guiTop + 93, 178, 93, 68, 68 );
		}
		
		// Call super
		super.drawGuiContainerBackgroundLayer( alpha, mouseX, mouseY );

	}

	@Override
	protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		boolean hoverUnderlayRendered = false;

		for( int i = 0; i < 54; i++ )
		{
			WidgetAspectSlot currentWidget = this.aspectWidgetList.get( i );

			if ( ( !hoverUnderlayRendered ) && ( currentWidget.canRender() ) && ( currentWidget.isMouseOverWidget( mouseX, mouseY ) ) )
			{
				currentWidget.drawMouseHoverUnderlay();
				
				hoverUnderlayRendered = true;
			}
			
			currentWidget.drawWidget();
		}

		for( Object button : this.buttonList )
		{
			if ( ( button instanceof ButtonRedstoneModes ) )
			{
				( (ButtonRedstoneModes)button ).drawTooltip( this.guiLeft, this.guiTop );
			}
		}
	}

	protected Slot getSlotAtPosition( int x, int y )
	{
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			if ( this.isMouseOverSlot( slot, x, y ) )
			{
				return slot;
			}
		}

		return null;
	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
	{
		Slot slot = this.getSlotAtPosition( mouseX, mouseY );

		if ( ( slot != null ) && ( slot.getStack() != null ) && ( slot.getStack().isItemEqual( AEApi.instance().items().itemNetworkTool.stack( 1 ) ) ) )
		{
			return;
		}

		super.mouseClicked( mouseX, mouseY, mouseButton );

		for( WidgetAspectSlot aspectSlot : this.aspectWidgetList )
		{
			if ( aspectSlot.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the aspect of the currently held item
				Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( this.player.inventory.getItemStack() );

				// Is there an aspect?
				if ( itemAspect != null )
				{
					// Are we already filtering for this aspect?
					if ( this.filteredAspects.contains( itemAspect ) )
					{
						// Ignore
						return;
					}

				}

				aspectSlot.mouseClicked( itemAspect );

				break;
			}
		}
	}

	@Override
	public void updateAspects( List<Aspect> aspectList )
	{
		int count = Math.min( this.aspectWidgetList.size(), aspectList.size() );

		for( int i = 0; i < count; i++ )
		{
			this.aspectWidgetList.get( i ).setAspect( aspectList.get( i ) );
		}

		this.filteredAspects = aspectList;

	}

}
