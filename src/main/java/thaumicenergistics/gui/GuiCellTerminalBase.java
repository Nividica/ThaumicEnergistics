package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerCellTerminalBase;
import thaumicenergistics.gui.widget.AspectWidgetComparator;
import thaumicenergistics.gui.widget.IAspectSelectorGui;
import thaumicenergistics.gui.widget.WidgetAspectSelector;
import thaumicenergistics.render.GuiTextureManager;
import thaumicenergistics.util.GuiHelper;

public class GuiCellTerminalBase
	extends GuiContainer
	implements IAspectSelectorGui
{

	protected static final int WIDGETS_PER_ROW = 9;
	protected static final int ROWS_PER_PAGE = 4;
	protected static final int WIDGETS_PER_PAGE = WIDGETS_PER_ROW * ROWS_PER_PAGE;

	
	protected EntityPlayer player;
	protected int currentScroll = 0;
	protected GuiTextField searchBar;
	protected List<WidgetAspectSelector> aspectWidgets = new ArrayList<WidgetAspectSelector>();
	protected List<WidgetAspectSelector> matchingSearchWidgets = new ArrayList<WidgetAspectSelector>();
	protected String searchTerm = "";
	public AspectStack currentAspect;
	protected ContainerCellTerminalBase containerBase;
	

	public GuiCellTerminalBase( EntityPlayer player, ContainerCellTerminalBase container)
	{
		super( container );

		this.containerBase = ( (ContainerCellTerminalBase) this.inventorySlots );

		this.containerBase.setGui( this );

		this.player = player;

		this.xSize = 176;

		this.ySize = 204;

		
	}

	private void updateScrollPosition()
	{
		// Get the mouse wheel movement
		int deltaMouseWheel = Mouse.getDWheel();

		if ( deltaMouseWheel < 0 )
		{
			this.currentScroll++ ;
		}
		else if ( deltaMouseWheel > 0 )
		{
			this.currentScroll-- ;
		}

		// Lower Bounds check the scrolling
		if ( this.currentScroll < 0 )
		{
			this.currentScroll = 0;
		}
		else
		{
			// Get how many rows is required for the display-able widgets
			int requiredRows = (int) Math.ceil( (float) this.matchingSearchWidgets.size() / (float) GuiCellTerminalBase.WIDGETS_PER_ROW );

			// Subtract from the required rows the starting row
			int rowsToDraw = requiredRows - this.currentScroll;

			// Calculate how many blank rows that would leave
			int blankRows = GuiCellTerminalBase.ROWS_PER_PAGE - rowsToDraw;

			// Would that scroll leave any blank rows?
			if ( blankRows > 0 )
			{
				// Subtract the blank rows from the scroll, bounding to 0
				this.currentScroll = Math.max( 0, this.currentScroll - blankRows );
			}

		}
	}

	private void updateSearch()
	{
		// Clear the matching widgets
		this.matchingSearchWidgets.clear();

		// Examine each of the possible widgets
		for( WidgetAspectSelector currentWidget : this.aspectWidgets )
		{
			// Is the search term in this aspects tag?
			if ( ( this.searchTerm == "" ) || ( currentWidget.getAspect().getTag().contains( this.searchTerm ) ) )
			{
				this.matchingSearchWidgets.add( currentWidget );
			}
		}

		// Sort the results
		Collections.sort( this.matchingSearchWidgets, new AspectWidgetComparator() );

	}

	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int sizeX, int sizeY )
	{
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_TERMINAL.getTexture() );

		this.drawTexturedModalRect( this.guiLeft, this.guiTop - 18, 0, 0, this.xSize, this.ySize );

		this.searchBar.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( StatCollector.translateToLocal( ThaumicEnergistics.MODID + ".aeparts.essentia.terminal.name" ), 9, -12, 0 );

		this.drawWidgets( mouseX, mouseY );

		if ( this.currentAspect != null )
		{
			long currentFluidAmount = this.currentAspect.amount;

			String amountToText = Long.toString( currentFluidAmount );

			String aspectName = this.currentAspect.aspect.getName();

			this.fontRendererObj.drawString( StatCollector.translateToLocal( ThaumicEnergistics.MODID + ".tooltip.amount" ) + ": " + amountToText,
				45, 83, 0 );

			this.fontRendererObj.drawString( StatCollector.translateToLocal( ThaumicEnergistics.MODID + ".tooltip.aspect" ) + ": " + aspectName, 45,
				73, 0 );
		}
	}

	@Override
	protected void keyTyped( char key, int keyID )
	{
		this.searchBar.textboxKeyTyped( key, keyID );

		if ( keyID == Keyboard.KEY_ESCAPE )
		{
			this.mc.thePlayer.closeScreen();
		}
		else
		{
			// Get the search term
			this.searchTerm = this.searchBar.getText().trim().toLowerCase();

			// Re-search the widgets
			this.updateSearch();
		}

	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseBtn )
	{
		super.mouseClicked( mouseX, mouseY, mouseBtn );

		this.searchBar.mouseClicked( mouseX, mouseY, mouseBtn );

		int listSize = this.matchingSearchWidgets.size();

		int index = 0;

		// Rows
		for( int y = 0; y < 4; y++ )
		{
			// Columns
			for( int x = 0; x < 9; x++ )
			{
				// Calculate the index
				index = ( ( y + this.currentScroll ) * 9 ) + x;

				// Is the index in bounds?
				if ( index < listSize )
				{
					// Get the widget at this index
					WidgetAspectSelector widget = this.matchingSearchWidgets.get( index );

					// Is the mouse over this widget?
					if ( GuiHelper.isPointInGuiRegion( ( x * WidgetAspectSelector.WIDGET_WIDTH ) + 7, ( y * WidgetAspectSelector.WIDGET_WIDTH ) - 1,
						WidgetAspectSelector.WIDGET_HEIGHT, WidgetAspectSelector.WIDGET_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
					{
						// Play clicky sound
						Minecraft.getMinecraft().getSoundHandler()
										.playSound( PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F) );
						
						// Send the click to the widget ( args ignored for
						// WidgetAspectSelector )
						widget.mouseClicked( 0, 0, 0, 0 );

						// Stop searching
						return;
					}
				}
				else
				{
					// Stop searching
					return;
				}
			}
		}

	}

	public void drawWidgets( int mouseX, int mouseY )
	{

		if ( !this.matchingSearchWidgets.isEmpty() )
		{
			// Get the scroll position
			this.updateScrollPosition();

			// Calculate the starting index
			int startingIndex = this.currentScroll * GuiCellTerminalBase.WIDGETS_PER_ROW;

			// Calculate the ending index
			int endingIndex = Math.min( this.matchingSearchWidgets.size(), startingIndex + WIDGETS_PER_PAGE );

			// Set the starting positions
			int widgetPosX = 7;
			int widgetPosY = -1;
			int widgetColumnPosition = 1;

			// Holder for the widget under the mouse
			WidgetAspectSelector widgetUnderMouse = null;
			int widgetUnderMousePosX = 0;
			int widgetUnderMousePosY = 0;

			for( int index = startingIndex; index < endingIndex; index++ )
			{
				// Draw the widget
				this.matchingSearchWidgets.get( index ).drawWidget( widgetPosX, widgetPosY );

				// Is the mouse over this widget?
				if ( ( widgetUnderMouse == null ) &&
								GuiHelper.isPointInGuiRegion( widgetPosX, widgetPosY, WidgetAspectSelector.WIDGET_HEIGHT,
									WidgetAspectSelector.WIDGET_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
				{
					// Set the widget and its position
					widgetUnderMouse = this.matchingSearchWidgets.get( index );
					widgetUnderMousePosX = widgetPosX;
					widgetUnderMousePosY = widgetPosY;
				}

				// Increment the column position
				widgetColumnPosition++ ;

				// Are we done with this row?
				if ( widgetColumnPosition > GuiCellTerminalBase.WIDGETS_PER_ROW )
				{
					// Reset X
					widgetPosX = 7;

					// Reset column position
					widgetColumnPosition = 1;

					// Increment y
					widgetPosY += WidgetAspectSelector.WIDGET_HEIGHT;
				}
				else
				{
					// Increment the x position
					widgetPosX += WidgetAspectSelector.WIDGET_WIDTH;
				}
			}

			// Was the mouse over a widget?
			if ( widgetUnderMouse != null )
			{
				// Have the widget draw its tooltip
				widgetUnderMouse.drawTooltip( widgetUnderMousePosX, widgetUnderMousePosY, mouseX, mouseY );
			}
		}
		else
		{
			this.currentScroll = 0;
		}
	}

	@Override
	public IAspectSelectorContainer getContainer()
	{
		return this.containerBase;
	}

	@Override
	public AspectStack getCurrentAspect()
	{
		return this.currentAspect;
	}

	@Override
	public int guiLeft()
	{
		return this.guiLeft;
	}

	@Override
	public int guiTop()
	{
		return this.guiTop;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		Mouse.getDWheel();

		this.updateAspects();

		this.searchBar = new GuiTextField( this.fontRendererObj, this.guiLeft + 100, this.guiTop - 12, 69, 10 )
		{
			private int xPos = 0;
			private int yPos = 0;
			private int width = 0;
			private int height = 0;

			@Override
			public void mouseClicked( int x, int y, int mouseBtn )
			{
				boolean flag = GuiHelper.isPointInRegion( this.xPos, this.yPos, this.height, this.width, x, y );

				if ( flag && ( mouseBtn == 3 ) )
				{
					this.setText( "" );
				}
			}
		};

		this.searchBar.setEnableBackgroundDrawing( false );

		this.searchBar.setFocused( true );

		this.searchBar.setMaxStringLength( 15 );

	}

	public void updateAspects()
	{
		this.aspectWidgets = new ArrayList<WidgetAspectSelector>();

		for( AspectStack aspectStack : this.containerBase.getAspectStackList() )
		{
			// Create the widget
			this.aspectWidgets.add( new WidgetAspectSelector( this, aspectStack ) );
		}

		this.updateSearch();

		this.updateSelectedAspect();
	}

	public void updateSelectedAspect()
	{
		this.currentAspect = null;

		for( AspectStack aspectStack : this.containerBase.getAspectStackList() )
		{
			if ( aspectStack.aspect == this.containerBase.getSelectedAspect() )
			{
				this.currentAspect = aspectStack;

				return;
			}
		}

	}

}
