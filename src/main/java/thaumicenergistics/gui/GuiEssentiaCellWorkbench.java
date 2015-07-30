package thaumicenergistics.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerEssentiaCellWorkbench;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.gui.buttons.GuiButtonClearCellPartitioning;
import thaumicenergistics.gui.buttons.GuiButtonSetCellPartitioningToCurrent;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCellWorkbench;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.AEStateIconsEnum;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.tileentities.TileEssentiaCellWorkbench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the cell workbench
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaCellWorkbench
	extends AbstractGuiBase
	implements IAspectSlotGui
{
	private class SimpleAspectWidget
		extends WidgetAspectSlot
	{

		public SimpleAspectWidget( final int ID, final int posX, final int posY, final EntityPlayer player )
		{
			super( GuiEssentiaCellWorkbench.this, player, null, ID, posX, posY );
		}

		@Override
		public void mouseClicked( final Aspect withAspect )
		{
			// Ignored
		}

	}

	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 176, GUI_HEIGHT = 251;

	/**
	 * Title position.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Widget starting position.
	 */
	private static final int WIDGET_POS_X = 7, WIDGET_POS_Y = 28;

	/**
	 * Number of widgets per row.
	 */
	private static final int WIDGETS_PER_ROW = 9;

	/**
	 * Number of widget rows.
	 */
	private static final int WIDGET_ROWS = 7;

	/**
	 * How many partition widgets to make.
	 */
	private static final int NUMBER_OF_WIDGETS = 63;

	/**
	 * Cache the cell background icon.
	 */
	private static final AEStateIconsEnum CELL_BG_ICON = AEStateIconsEnum.ME_CELL_BACKGROUND;

	/**
	 * Partition to current, button info.
	 */
	private static final int BUTTON_PARTITION_CURRENT_ID = 0, BUTTON_PARTITION_CURRENT_X = -18, BUTTON_PARTITION_CURRENT_Y = 28;

	/**
	 * Clear partitioning, button info.
	 */
	private static final int BUTTON_CLEAR_ID = 1, BUTTON_CLEAR_X = -18, BUTTON_CLEAR_Y = 8;
	/**
	 * The player viewing the gui.
	 */
	private final EntityPlayer player;

	/**
	 * Title of the window
	 */
	private final String title;

	/**
	 * Array of aspect widgets used to show partition aspects.
	 */
	private final SimpleAspectWidget[] partitionWidgets;

	/**
	 * Cell Workbench.
	 */
	public final TileEssentiaCellWorkbench workbench;

	public GuiEssentiaCellWorkbench( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( new ContainerEssentiaCellWorkbench( player, world, x, y, z ) );

		// Set the player
		this.player = player;

		// Set the width and height
		this.xSize = GuiEssentiaCellWorkbench.GUI_WIDTH;
		this.ySize = GuiEssentiaCellWorkbench.GUI_HEIGHT;

		// Set the title
		this.title = ThEStrings.Gui_TitleEssentiaCellWorkbench.getLocalized();

		// Create the widget array
		this.partitionWidgets = new SimpleAspectWidget[GuiEssentiaCellWorkbench.NUMBER_OF_WIDGETS];

		// Get the workbench
		this.workbench = ( (ContainerEssentiaCellWorkbench)this.inventorySlots ).workbench;
	}

	/**
	 * Draw background
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the workbench gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.CELL_WORKBENCH.getTexture() );

		// Draw the gui texture
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

		// Bind the AE states texture
		Minecraft.getMinecraft().renderEngine.bindTexture( AEStateIconsEnum.AE_STATES_TEXTURE );

		// Draw the cell slot background
		this.drawTexturedModalRect( this.guiLeft + ContainerEssentiaCellWorkbench.CELL_SLOT_X, this.guiTop +
						ContainerEssentiaCellWorkbench.CELL_SLOT_Y, GuiEssentiaCellWorkbench.CELL_BG_ICON.getU(),
			GuiEssentiaCellWorkbench.CELL_BG_ICON.getV(), GuiEssentiaCellWorkbench.CELL_BG_ICON.getWidth(),
			GuiEssentiaCellWorkbench.CELL_BG_ICON.getHeight() );

	}

	/**
	 * Draw the foreground
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, GuiEssentiaCellWorkbench.TITLE_POS_X, GuiEssentiaCellWorkbench.TITLE_POS_Y, 0 );

		// Draw the widgets
		SimpleAspectWidget widgetUnderMouse = null, currentWidget;
		for( int index = 0; index < GuiEssentiaCellWorkbench.NUMBER_OF_WIDGETS; index++ )
		{
			// Get the widget
			currentWidget = this.partitionWidgets[index];

			// Draw the widget
			currentWidget.drawWidget();

			// Is the mouse over it?
			if( ( widgetUnderMouse == null ) && ( currentWidget.isMouseOverWidget( mouseX, mouseY ) ) )
			{
				// Mark this widget as the one under the mouse
				widgetUnderMouse = currentWidget;
			}
		}

		// Is there a widget under the mouse?
		if( widgetUnderMouse != null )
		{
			// Get the widgets tooltip
			widgetUnderMouse.getTooltip( this.tooltip );
		}
		else
		{
			// Add tooltip from buttons
			this.addTooltipFromButtons( mouseX, mouseY );
		}

		// Draw the tooltip
		if( this.tooltip.size() > 0 )
		{
			this.drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop, true );
		}
	}

	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
	{
		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );

		for( SimpleAspectWidget partitionWidget : this.partitionWidgets )
		{
			if( partitionWidget.isMouseOverWidget( mouseX, mouseY ) )
			{
				// Get the aspect of the currently held item
				Aspect itemAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( this.player.inventory.getItemStack() );

				// Get the aspect of the widget
				Aspect widgetAspect = partitionWidget.getAspect();

				// Add?
				if( ( widgetAspect == null ) && ( itemAspect != null ) )
				{
					new PacketServerEssentiaCellWorkbench().createRequestAddAspect( this.player, this.workbench, itemAspect ).sendPacketToServer();
				}
				// Remove?
				else if( ( widgetAspect != null ) && ( itemAspect == null ) )
				{
					new PacketServerEssentiaCellWorkbench().createRequestRemoveAspect( this.player, this.workbench, widgetAspect )
									.sendPacketToServer();

				}
				// Replace?
				else if( ( widgetAspect != null ) && ( itemAspect != null ) )
				{
					new PacketServerEssentiaCellWorkbench().createRequestReplaceAspect( this.player, this.workbench, widgetAspect, itemAspect )
									.sendPacketToServer();

				}

				// Stop searching
				break;
			}
		}
	}

	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Which button was clicked?
		switch ( button.id )
		{
			case GuiEssentiaCellWorkbench.BUTTON_CLEAR_ID:
				new PacketServerEssentiaCellWorkbench().createRequestClearPartitioning( this.player, this.workbench ).sendPacketToServer();
				break;

			case GuiEssentiaCellWorkbench.BUTTON_PARTITION_CURRENT_ID:
				new PacketServerEssentiaCellWorkbench().createRequestPartitionToContents( this.player, this.workbench ).sendPacketToServer();
				break;
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Create the widgets
		for( int y = 0; y < GuiEssentiaCellWorkbench.WIDGET_ROWS; y++ )
		{
			// Calculate the Y position of this row
			int rowYPosition = GuiEssentiaCellWorkbench.WIDGET_POS_Y + ( y * 18 );

			for( int x = 0; x < GuiEssentiaCellWorkbench.WIDGETS_PER_ROW; x++ )
			{
				// Calculate the X position of the widget
				int wPosX = GuiEssentiaCellWorkbench.WIDGET_POS_X + ( x * 18 );

				// Calculate the index of the widget
				int wIndex = ( x + ( y * GuiEssentiaCellWorkbench.WIDGETS_PER_ROW ) );

				// Create the widget
				this.partitionWidgets[wIndex] = new SimpleAspectWidget( wIndex, wPosX, rowYPosition, this.player );
			}
		}

		// Create the un-partition button
		this.buttonList.add( new GuiButtonSetCellPartitioningToCurrent( GuiEssentiaCellWorkbench.BUTTON_PARTITION_CURRENT_ID, this.guiLeft +
						GuiEssentiaCellWorkbench.BUTTON_PARTITION_CURRENT_X, this.guiTop + GuiEssentiaCellWorkbench.BUTTON_PARTITION_CURRENT_Y ) );

		// Create the partition to current button
		this.buttonList.add( new GuiButtonClearCellPartitioning( GuiEssentiaCellWorkbench.BUTTON_CLEAR_ID, this.guiLeft +
						GuiEssentiaCellWorkbench.BUTTON_CLEAR_X, this.guiTop + GuiEssentiaCellWorkbench.BUTTON_CLEAR_Y ) );

		// Request the list
		new PacketServerEssentiaCellWorkbench().createRequestGetPartitionList( this.player, this.workbench ).sendPacketToServer();
	}

	/**
	 * Updates the partition widgets to show the aspects in the specified list.
	 */
	@Override
	public void updateAspects( final List<Aspect> aspectList )
	{
		// Get the number of aspects in the list
		int count = aspectList.size();

		// Set the aspects
		for( int index = 0; index < count; index++ )
		{
			this.partitionWidgets[index].setAspect( aspectList.get( index ) );
		}

		// Clear the rest
		for( int index = count; index < GuiEssentiaCellWorkbench.NUMBER_OF_WIDGETS; index++ )
		{
			this.partitionWidgets[index].setAspect( null );
		}
	}

}
