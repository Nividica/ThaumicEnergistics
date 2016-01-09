package thaumicenergistics.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IAspectSlotGui;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.client.gui.buttons.GuiButtonAETab;
import thaumicenergistics.client.gui.buttons.GuiButtonAllowVoid;
import thaumicenergistics.client.gui.widget.ThEWidget;
import thaumicenergistics.client.gui.widget.WidgetAspectSlot;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.common.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.common.network.packet.server.Packet_S_EssentiaStorageBus;
import thaumicenergistics.common.parts.PartEssentiaStorageBus;
import thaumicenergistics.common.registries.AEPartsEnum;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the storage bus.
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaStorageBus
	extends ThEBaseGui
	implements IAspectSlotGui
{
	/**
	 * The number of columns in the gui.
	 */
	private static final int WIDGET_COLUMNS = 3;

	/**
	 * The number of rows in the gui.
	 */
	private static final int WIDGET_ROWS = 3;

	/**
	 * The starting X position of the widgets.
	 */
	private static final int WIDGET_X_POS = 61;

	/**
	 * The starting Y position of the widgets.
	 */
	private static final int WIDGET_Y_POS = 21;

	/**
	 * The width of the gui with a network tool.
	 */
	private static final int GUI_WIDTH_NETWORK_TOOL = 246;

	/**
	 * The width of the gui without a network too
	 */
	private static final int GUI_WIDTH_NO_TOOL = 210;

	/**
	 * The height of the gui
	 */
	private static final int GUI_HEIGHT = 184;

	/**
	 * X position of the title string.
	 */
	private static final int TITLE_X_POS = 6;

	/**
	 * Y position of the title string.
	 */
	private static final int TITLE_Y_POS = 5;

	/**
	 * ID of the priority button
	 */
	private static final int BUTTON_PRIORITY_ID = 0;

	/**
	 * X offset position of the priority button
	 */
	private static final int BUTTON_PRIORITY_X_POSITION = 154;

	private static final int BUTTON_ALLOW_VOID_ID = 1;

	private static final int BUTTON_ALLOW_VOID_X_POS = -18;

	private static final int BUTTON_ALLOW_VOID_Y_POS = 8;

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
	 * Storage bus associated with this gui
	 */
	private PartEssentiaStorageBus storageBus;

	/**
	 * Title of the gui
	 */
	private final String guiTitle = AEPartsEnum.EssentiaStorageBus.getLocalizedName();

	/**
	 * Is the storage buss allowed to void excess essentia?
	 */
	private boolean isVoidAllowed = false;

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
	public GuiEssentiaStorageBus( final PartEssentiaStorageBus storageBus, final EntityPlayer player )
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
		this.xSize = ( this.hasNetworkTool ? GUI_WIDTH_NETWORK_TOOL : GUI_WIDTH_NO_TOOL );
		this.ySize = GUI_HEIGHT;
	}

	/**
	 * Draws the gui background
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
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
		super.drawAEToolAndUpgradeSlots( alpha, mouseX, mouseY );

	}

	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Call super
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		// Draw the title
		this.fontRendererObj.drawString( this.guiTitle, TITLE_X_POS, TITLE_Y_POS, 0x000000 );

		WidgetAspectSlot slotUnderMouse = null;

		// Draw widgets
		for( WidgetAspectSlot currentWidget : this.aspectWidgetList )
		{
			if( ( slotUnderMouse == null ) && ( currentWidget.canRender() ) && ( currentWidget.isMouseOverWidget( mouseX, mouseY ) ) )
			{
				// Draw the underlay
				currentWidget.drawMouseHoverUnderlay();

				// Set the slot
				slotUnderMouse = currentWidget;
			}

			// Draw the widget
			currentWidget.drawWidget();
		}

		// Should we get the tooltip from the slot?
		if( slotUnderMouse != null )
		{
			// Add the tooltip from the widget
			slotUnderMouse.getTooltip( this.tooltip );
		}
	}

	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );

		for( WidgetAspectSlot aspectSlot : this.aspectWidgetList )
		{
			if( aspectSlot.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the aspect of the currently held item
				Aspect itemAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( this.player.inventory.getItemStack() );

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

	/**
	 * Called when a button is clicked.
	 */
	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Was the priority button clicked?
		if( button.id == BUTTON_PRIORITY_ID )
		{
			// Get the storage buses host 
			TileEntity host = this.storageBus.getHostTile();

			// Get the side the storage bus is attached to
			ForgeDirection side = this.storageBus.getSide();

			// Ask the server to change to the priority gui
			Packet_S_ChangeGui.sendGuiChange( ThEGuiHandler.generateSidedID( ThEGuiHandler.PRIORITY_ID, side ), this.player,
				host.getWorldObj(), host.xCoord, host.yCoord, host.zCoord );

		}
		else if( button.id == BUTTON_ALLOW_VOID_ID )
		{
			// Toggle the mode
			this.isVoidAllowed = !this.isVoidAllowed;

			// Update the button
			( (GuiButtonAllowVoid)this.buttonList.get( BUTTON_ALLOW_VOID_ID ) ).isVoidAllowed = this.isVoidAllowed;

			// Update the server
			Packet_S_EssentiaStorageBus.setVoidAllowed( this.player, this.storageBus, this.isVoidAllowed );
		}

	}

	@Override
	public void initGui()
	{
		super.initGui();

		// Create the widgets
		for( int row = 0; row < WIDGET_ROWS; row++ )
		{
			for( int column = 0; column < WIDGET_COLUMNS; column++ )
			{
				this.aspectWidgetList.add( new WidgetAspectSlot( this, this.player, this.storageBus,
								( row * WIDGET_COLUMNS ) + column,
								WIDGET_X_POS + ( ThEWidget.WIDGET_SIZE * column ),
								WIDGET_Y_POS + ( ThEWidget.WIDGET_SIZE * row ) ) );
			}
		}

		// Create the priority tab button
		this.buttonList.add( new GuiButtonAETab( BUTTON_PRIORITY_ID, this.guiLeft +
						BUTTON_PRIORITY_X_POSITION, this.guiTop, AEStateIconsEnum.WRENCH, "gui.appliedenergistics2.Priority" ) );

		// Create the allow void button
		this.buttonList.add( new GuiButtonAllowVoid( BUTTON_ALLOW_VOID_ID, this.guiLeft +
						BUTTON_ALLOW_VOID_X_POS, this.guiTop + BUTTON_ALLOW_VOID_Y_POS ) );
	}

	/**
	 * Called when the server has sent a change to void mode.
	 * 
	 * @param isVoidAllowed
	 */
	public void onServerSentVoidMode( final boolean isVoidAllowed )
	{
		// Set the mode
		this.isVoidAllowed = isVoidAllowed;

		// Update the button
		( (GuiButtonAllowVoid)this.buttonList.get( BUTTON_ALLOW_VOID_ID ) ).isVoidAllowed = this.isVoidAllowed;
	}

	@Override
	public void updateAspects( final List<Aspect> aspectList )
	{
		int count = Math.min( this.aspectWidgetList.size(), aspectList.size() );

		for( int i = 0; i < count; i++ )
		{
			this.aspectWidgetList.get( i ).setAspect( aspectList.get( i ), 1, false );
		}

		this.filteredAspects = aspectList;

	}

}
