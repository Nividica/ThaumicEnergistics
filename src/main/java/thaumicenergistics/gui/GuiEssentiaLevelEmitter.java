package thaumicenergistics.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaLevelEmitter;
import thaumicenergistics.gui.buttons.ButtonRedstoneModes;
import thaumicenergistics.gui.widget.DigitTextField;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaEmitter;
import thaumicenergistics.parts.AEPartEssentiaLevelEmitter;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.GuiHelper;
import appeng.api.config.RedstoneMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the level emitter.
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaLevelEmitter
	extends GuiWidgetHost
	implements IAspectSlotGui
{
	/**
	 * Normal button labels
	 */
	private static String[] BUTTON_LABELS = { "-1", "-10", "-100", "+1", "+10", "+100" };

	/**
	 * Button labels when shift is being pressed.
	 */
	private static String[] BUTTON_LABELS_SHIFTED = { "-100", "-1000", "-10000", "+100", "+1000", "+10000" };

	/**
	 * Width of the gui
	 */
	private static final int GUI_X_SIZE = 176;

	/**
	 * Height of the gui
	 */
	private static final int GUI_Y_SIZE = 184;

	/**
	 * The button index of the redstone mode button.
	 */
	private static final int REDSTONE_MODE_BUTTON_INDEX = 6;

	/**
	 * X position of the title string.
	 */
	private static final int TITLE_POS_X = 8;

	/**
	 * Y position of the title string.
	 */
	private static final int TITLE_POS_Y = -4;

	/**
	 * X offset for the amount field.
	 */
	private static final int AMOUNT_OFFSET_X = 25;

	/**
	 * Y offset for the amount field.
	 */
	private static final int AMOUNT_OFFSET_Y = 35;

	/**
	 * Width of the amount field.
	 */
	private static final int AMOUNT_WIDTH = 80;

	/**
	 * Height of the amount field.
	 */
	private static final int AMOUNT_HEIGHT = 10;

	/**
	 * Horizontal padding between buttons.
	 */
	private static final int BUTTON_PADDING_HORZ = 8;

	/**
	 * Vertical padding between buttons.
	 */
	private static final int BUTTON_PADDING_VERT = 22;

	/**
	 * X position to start drawing buttons.
	 */
	private static final int BUTTON_POS_X = 19;

	/**
	 * Y position to start drawing buttons.
	 */
	private static final int BUTTON_POS_Y = 17;

	/**
	 * Width of the buttons.
	 */
	private static final int BUTTON_WIDTH = 42;

	/**
	 * Height of the buttons.
	 */
	private static final int BUTTON_HEIGHT = 20;

	/**
	 * Number of button rows.
	 */
	private static final int BUTTON_ROWS = 2;

	/**
	 * Number of button columns.
	 */
	private static final int BUTTON_COLUMNS = 3;

	/**
	 * X position of the redstone button.
	 */
	private static final int REDSTONE_BUTTON_POS_X = -18;

	/**
	 * Y position of the redstone button.
	 */
	private static final int REDSTONE_BUTTON_POS_Y = 2;

	/**
	 * Width and height of the redstone button.
	 */
	private static final int REDSTONE_BUTTON_SIZE = 16;

	/**
	 * The maximum number of characters that can be typed in the amount field.
	 */
	private static final int AMOUNT_MAX_CHARS = 10;

	/**
	 * Amount text field
	 */
	private DigitTextField amountField;

	/**
	 * AE part associated with the gui.
	 */
	private AEPartEssentiaLevelEmitter part;

	/**
	 * Player viewing the gui.
	 */
	private EntityPlayer player;

	/**
	 * Filter slot.
	 */
	private WidgetAspectSlot aspectFilterSlot;

	/**
	 * Create the GUI.
	 * 
	 * @param part
	 * AE part associated with the gui.
	 * @param player
	 * Player viewing the gui.
	 */
	public GuiEssentiaLevelEmitter( AEPartEssentiaLevelEmitter part, EntityPlayer player )
	{
		// Call super
		super( new ContainerPartEssentiaLevelEmitter( part, player ) );

		// Set the player
		this.player = player;

		// Set the part
		this.part = part;

		// Create the filter slot
		this.aspectFilterSlot = new WidgetAspectSlot( this, this.player, this.part, 123, 30 );

		// Request an update
		new PacketServerEssentiaEmitter().createUpdateRequest( this.part, this.player ).sendPacketToServer();
	}

	/**
	 * Draw the gui background
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( float f, int i, int j )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Set the texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_LEVEL_EMITTER.getTexture() );

		// Calculate the position
		int posX = ( this.width - GUI_X_SIZE ) / 2;
		int posY = ( this.height - GUI_Y_SIZE ) / 2;

		// Draw the gui texture.
		this.drawTexturedModalRect( posX, posY, 0, 0, GUI_X_SIZE, GUI_Y_SIZE );
	}

	public boolean setFilteredAspectFromItemstack( ItemStack itemStack )
	{
		Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( itemStack );

		if( itemAspect != null )
		{
			this.aspectFilterSlot.setAspect( itemAspect );

			return true;
		}

		return false;
	}

	/**
	 * Called when the player types a key
	 */
	@Override
	protected void keyTyped( char key, int keyID )
	{
		// Pass to super
		super.keyTyped( key, keyID );

		// Ensure they was numeric and the string isnt too long to parse, or backspace
		if( ( Character.isDigit( key ) && ( this.amountField.getText().length() < GuiEssentiaLevelEmitter.AMOUNT_MAX_CHARS ) ) ||
						( keyID == Keyboard.KEY_BACK ) )
		{
			// Pass to the amount field
			this.amountField.textboxKeyTyped( key, keyID );

			// Convert the text field into a long
			long wantedAmount = 0;
			try
			{
				wantedAmount = Long.parseLong( this.amountField.getText() );
			}
			catch( NumberFormatException _ )
			{
			}

			// Update the server
			new PacketServerEssentiaEmitter().createWantedAmountUpdate( wantedAmount, this.part, this.player ).sendPacketToServer();
		}
	}

	/**
	 * Called when the player clicks a mouse button
	 */
	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseBtn )
	{
		// Pass to super
		super.mouseClicked( mouseX, mouseY, mouseBtn );

		// Is the mouse over the widget?
		if( this.aspectFilterSlot.isMouseOverWidget( mouseX, mouseY ) )
		{
			// Pass to the widget
			this.aspectFilterSlot.mouseClicked( EssentiaItemContainerHelper.getAspectInContainer( this.player.inventory.getItemStack() ) );
		}
	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	public void actionPerformed( GuiButton button )
	{
		// Get the index of the button that was clicked
		int index = button.id;

		// Was one of the amount buttons pressed?
		if( ( index >= 0 ) && ( index < GuiEssentiaLevelEmitter.BUTTON_LABELS.length ) )
		{
			try
			{
				// Read the adjustment
				int adjustment = Integer.parseInt( button.displayString );

				// Update the server
				new PacketServerEssentiaEmitter().createWantedAmountAdjustment( adjustment, this.part, this.player ).sendPacketToServer();
			}
			catch( NumberFormatException _ )
			{
			}
		}
		// Was the redstone mode button pressed?
		else if( index == GuiEssentiaLevelEmitter.REDSTONE_MODE_BUTTON_INDEX )
		{
			// Update the server
			new PacketServerEssentiaEmitter().createRedstoneModeToggle( this.part, this.player ).sendPacketToServer();
		}
	}

	/**
	 * Draw the foreground
	 */
	@Override
	public void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( AEPartsEnum.EssentiaLevelEmitter.getStatName(), GuiEssentiaLevelEmitter.TITLE_POS_X,
			GuiEssentiaLevelEmitter.TITLE_POS_Y, 0 );

		// Draw underlay when mouse is over slot.
		if( this.aspectFilterSlot.isMouseOverWidget( mouseX, mouseY ) )
		{
			this.aspectFilterSlot.drawMouseHoverUnderlay();
		}

		// Draw the filter widget
		this.aspectFilterSlot.drawWidget();

		// Draw the text field
		this.amountField.drawTextBox();

		// Is the mouse over any buttons?
		for( Object obj : this.buttonList )
		{
			// Cast to button
			GuiButton currentButton = (GuiButton)obj;

			// Is the mouse over it?
			if( GuiHelper.isPointInRegion( currentButton.xPosition, currentButton.yPosition, currentButton.width, currentButton.height, mouseX,
				mouseY ) )
			{
				// Is it the redstone button?
				if( currentButton instanceof ButtonRedstoneModes )
				{
					( (ButtonRedstoneModes)currentButton ).drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop );
				}
				
				// Stop searching
				break;
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen( int x, int y, float f )
	{
		// Call super
		super.drawScreen( x, y, f );

		// Get the number of buttons
		int count = GuiEssentiaLevelEmitter.BUTTON_LABELS.length;

		// Loop over the buttons
		for( int buttonIndex = 0; buttonIndex < count; buttonIndex++ )
		{
			// Get the button for this index
			GuiButton currentButton = (GuiButton)this.buttonList.get( buttonIndex );

			// Is shift being held?
			if( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
			{
				// Shifted label
				currentButton.displayString = ( BUTTON_LABELS_SHIFTED[buttonIndex] );
			}
			else
			{
				// Normal label
				currentButton.displayString = ( BUTTON_LABELS[buttonIndex] );
			}
		}
	}

	/**
	 * Sets up the gui
	 */
	@Override
	public void initGui()
	{
		// Enable repeat keys
		Keyboard.enableRepeatEvents( true );

		// Calculate the left most X position
		int guiLeftX = ( this.width - GUI_X_SIZE ) / 2;

		// Calculate the top most Y position
		int guiTopY = ( this.height - GUI_Y_SIZE ) / 2;

		// Create the amount field
		this.amountField = new DigitTextField( this.fontRendererObj, GuiEssentiaLevelEmitter.AMOUNT_OFFSET_X,
						GuiEssentiaLevelEmitter.AMOUNT_OFFSET_Y, GuiEssentiaLevelEmitter.AMOUNT_WIDTH, GuiEssentiaLevelEmitter.AMOUNT_HEIGHT );

		// Start focused
		this.amountField.setFocused( true );

		// Draw in the forground
		this.amountField.setEnableBackgroundDrawing( false );

		// Text color white
		this.amountField.setTextColor( 0xFFFFFF );

		// Reset the button list
		this.buttonList.clear();

		for( int row = 0; row < GuiEssentiaLevelEmitter.BUTTON_ROWS; row++ )
		{
			// Calculate the y position of this row
			int yPos = ( guiTopY + GuiEssentiaLevelEmitter.BUTTON_POS_Y ) +
							( row * ( GuiEssentiaLevelEmitter.BUTTON_HEIGHT + GuiEssentiaLevelEmitter.BUTTON_PADDING_VERT ) );

			for( int column = 0; column < GuiEssentiaLevelEmitter.BUTTON_COLUMNS; column++ )
			{
				// Calculate the button index
				int buttonIndex = ( row * 3 ) + column;

				// Calculate the x position of the button
				int xPos = ( ( guiLeftX + GuiEssentiaLevelEmitter.BUTTON_POS_X ) + ( column * ( GuiEssentiaLevelEmitter.BUTTON_WIDTH + GuiEssentiaLevelEmitter.BUTTON_PADDING_HORZ ) ) );

				this.buttonList.add( new GuiButton( buttonIndex, xPos, yPos, GuiEssentiaLevelEmitter.BUTTON_WIDTH,
								GuiEssentiaLevelEmitter.BUTTON_HEIGHT, GuiEssentiaLevelEmitter.BUTTON_LABELS[buttonIndex] ) );
			}
		}

		// Add the redstone mode button
		this.buttonList.add( new ButtonRedstoneModes( GuiEssentiaLevelEmitter.REDSTONE_MODE_BUTTON_INDEX, guiLeftX +
						GuiEssentiaLevelEmitter.REDSTONE_BUTTON_POS_X, guiTopY + GuiEssentiaLevelEmitter.REDSTONE_BUTTON_POS_Y,
						GuiEssentiaLevelEmitter.REDSTONE_BUTTON_SIZE, GuiEssentiaLevelEmitter.REDSTONE_BUTTON_SIZE, RedstoneMode.LOW_SIGNAL, true ) );

		// Call super
		super.initGui();
	}

	/**
	 * Called when the gui is closing.
	 */
	@Override
	public void onGuiClosed()
	{
		// Disable repeat keys
		Keyboard.enableRepeatEvents( false );
	}

	/**
	 * Returns the amount
	 * 
	 * @param amount
	 */
	public void onServerUpdateWantedAmount( long amount )
	{
		this.amountField.setText( Long.toString( amount ) );
	}

	/**
	 * Sets the redstone mode
	 * 
	 * @param mode
	 */
	public void onServerUpdateRedstoneMode( RedstoneMode mode )
	{
		( (ButtonRedstoneModes)this.buttonList.get( GuiEssentiaLevelEmitter.REDSTONE_MODE_BUTTON_INDEX ) ).setRedstoneMode( mode );
	}

	/**
	 * Sets the filtered aspect
	 */
	@Override
	public void updateAspects( List<Aspect> aspectList )
	{
		if( ( aspectList == null ) || ( aspectList.isEmpty() ) )
		{
			this.aspectFilterSlot.setAspect( null );
		}
		else
		{
			this.aspectFilterSlot.setAspect( aspectList.get( 0 ) );
		}
	}

}
