package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.gui.buttons.ButtonAETab;
import thaumicenergistics.gui.buttons.ButtonRedstoneModes;
import thaumicenergistics.gui.widget.AbstractWidget;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.server.PacketServerChangeGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaStorageBus;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import thaumicenergistics.texture.EnumAEStateIcons;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;

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
	private static final int COLUMNS = 3;

	/**
	 * The number of rows in the gui.
	 */
	private static final int ROWS = 3;

	/**
	 * The starting X position of the widgets.
	 */
	private static final int WIDGET_X_POS = 61;

	/**
	 * The starting Y position of the widgets.
	 */
	private static final int WIDGET_Y_POS = 21;

	/**
	 * The width of the gui
	 */
	private static final int GUI_WIDTH = 246;

	/**
	 * The height of the gui
	 */
	private static final int GUI_HEIGHT = 184;

	/**
	 * ID of the priority button
	 */
	private static final int PRIORITY_BUTTON_ID = 0;

	/**
	 * X offset position of the priority button
	 */
	private static final int PRIORITY_BUTTON_X_POSITION = 154;

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

	private AEPartEssentiaStorageBus storageBus;

	/**
	 * Creates the GUI.
	 * 
	 * @param storageBus
	 * The part associated with the gui.
	 * @param player
	 * The player viewing the gui.
	 * @param container
	 * The inventory container.
	 */
	public GuiEssentiaStorageBus( AEPartEssentiaStorageBus storageBus, EntityPlayer player )
	{
		// Call super
		super( new ContainerPartEssentiaStorageBus( storageBus, player ) );

		// Set the player
		this.player = player;

		// Set the storage bus
		this.storageBus = storageBus;

		// Set the network tool
		this.hasNetworkTool = ( (ContainerPartEssentiaStorageBus)this.inventorySlots ).hasNetworkTool();

		// Set the width and height
		this.xSize = GuiEssentiaStorageBus.GUI_WIDTH;
		this.ySize = GuiEssentiaStorageBus.GUI_HEIGHT;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		// Create the widgets
		for( int row = 0; row < GuiEssentiaStorageBus.ROWS; row++ )
		{
			for( int column = 0; column < GuiEssentiaStorageBus.COLUMNS; column++ )
			{
				this.aspectWidgetList.add( new WidgetAspectSlot( this, this.player, this.storageBus,
								( row * GuiEssentiaStorageBus.COLUMNS ) + column, GuiEssentiaStorageBus.WIDGET_X_POS +
												( AbstractWidget.WIDGET_SIZE * column ), GuiEssentiaStorageBus.WIDGET_Y_POS +
												( AbstractWidget.WIDGET_SIZE * row ) ) );
			}
		}

		// Create the priority tab button
		this.buttonList.add( new ButtonAETab( GuiEssentiaStorageBus.PRIORITY_BUTTON_ID, this.guiLeft +
						GuiEssentiaStorageBus.PRIORITY_BUTTON_X_POSITION, this.guiTop, EnumAEStateIcons.WRENCH ) );

		// Request an update
		new PacketServerEssentiaStorageBus().createRequestFullUpdate( this.player, this.storageBus ).sendPacketToServer();
	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	public void actionPerformed( GuiButton button )
	{
		// Was the priority button clicked?
		if( button.id == GuiEssentiaStorageBus.PRIORITY_BUTTON_ID )
		{
			// Get the storage buses host 
			TileEntity host = this.storageBus.getHostTile();

			// Get the side the storage bus is attached to
			ForgeDirection side = this.storageBus.getSide();

			// Ask the server to change to the priority gui
			new PacketServerChangeGui().createChangeGuiRequest( GuiHandler.generatePriorityID( side ), this.player, host.getWorldObj(), host.xCoord,
				host.yCoord, host.zCoord ).sendPacketToServer();

		}

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

		// Draw main gui
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, 176, 222 );

		// Draw upgrade slot
		this.drawTexturedModalRect( this.guiLeft + 179, this.guiTop, 179, 0, 32, 32 );

		if( this.hasNetworkTool )
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

		for( int i = 0; i < AEPartEssentiaStorageBus.FILTER_SIZE; i++ )
		{
			WidgetAspectSlot currentWidget = this.aspectWidgetList.get( i );

			if( ( !hoverUnderlayRendered ) && ( currentWidget.canRender() ) && ( currentWidget.isMouseOverWidget( mouseX, mouseY ) ) )
			{
				currentWidget.drawMouseHoverUnderlay();

				hoverUnderlayRendered = true;
			}

			currentWidget.drawWidget();
		}

		for( Object button : this.buttonList )
		{
			if( ( button instanceof ButtonRedstoneModes ) )
			{
				( (ButtonRedstoneModes)button ).drawTooltip( this.guiLeft, this.guiTop );
			}
		}
	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
	{
		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );

		for( WidgetAspectSlot aspectSlot : this.aspectWidgetList )
		{
			if( aspectSlot.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the aspect of the currently held item
				Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( this.player.inventory.getItemStack() );

				// Is there an aspect?
				if( itemAspect != null )
				{
					// Are we already filtering for this aspect?
					if( this.filteredAspects.contains( itemAspect ) )
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
