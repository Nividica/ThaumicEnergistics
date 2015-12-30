package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.container.ContainerPriority;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.gui.buttons.GuiButtonAETab;
import thaumicenergistics.gui.widget.DigitTextField;
import thaumicenergistics.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.network.packet.server.Packet_S_Priority;
import thaumicenergistics.parts.AbstractAEPartBase;
import thaumicenergistics.texture.AEStateIconsEnum;
import thaumicenergistics.texture.GuiTextureManager;
import appeng.core.localization.GuiText;
import appeng.helpers.IPriorityHost;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the priority window
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiPriority
	extends AbstractGuiBase
{
	private static class AdjustmentButtonDef
	{
		enum EnumButtonWidth
		{
				Small (25),
				Medium (35),
				Large (45);

			public final int width;

			private EnumButtonWidth( final int width )
			{
				this.width = width;
			}
		}

		/**
		 * Height of all buttons
		 */
		private static final int BUTTON_HEIGHT = 20;

		/**
		 * Width of the button
		 */
		public final int width;

		/**
		 * Caption of the button
		 */
		public final String caption;

		/**
		 * Amount the button represents
		 */
		public final int amount;

		/**
		 * Creates the button definition
		 * 
		 * @param buttonWidth
		 * @param amount
		 */
		public AdjustmentButtonDef( final EnumButtonWidth buttonWidth, final int amount )
		{
			// Set the width
			this.width = buttonWidth.width;

			// Set the caption
			this.caption = ( amount > 0 ? "+" : "" ) + Integer.toString( amount );

			// Set the amount
			this.amount = amount;
		}

		/**
		 * Creates a GUI button based of this definition
		 * 
		 * @param ID
		 * @param xPosition
		 * @param yPosition
		 * @return
		 */
		public GuiButton makeButton( final int ID, final int xPosition, final int yPosition )
		{
			return new GuiButton( ID, xPosition, yPosition, this.width, AdjustmentButtonDef.BUTTON_HEIGHT, this.caption );
		}

	}

	private static final AdjustmentButtonDef[] ADJUSTMENT_BUTTONS = new AdjustmentButtonDef[] {
					new AdjustmentButtonDef( AdjustmentButtonDef.EnumButtonWidth.Small, 1 ),
					new AdjustmentButtonDef( AdjustmentButtonDef.EnumButtonWidth.Medium, 10 ),
					new AdjustmentButtonDef( AdjustmentButtonDef.EnumButtonWidth.Large, 100 ),
					new AdjustmentButtonDef( AdjustmentButtonDef.EnumButtonWidth.Small, -1 ),
					new AdjustmentButtonDef( AdjustmentButtonDef.EnumButtonWidth.Medium, -10 ),
					new AdjustmentButtonDef( AdjustmentButtonDef.EnumButtonWidth.Large, -100 ) };

	/**
	 * The width of the gui
	 */
	private static final int GUI_WIDTH = 176;

	/**
	 * The height of the gui
	 */
	private static final int GUI_HEIGHT = 107;

	/**
	 * ID of the switch tab
	 */
	private static final int PART_SWITCH_BUTTON_ID = 0;

	/**
	 * X position of the switch tab
	 */
	private static final int PART_SWITCH_BUTTON_X_POSITION = 154;

	/**
	 * Starting ID of the adjustment buttons
	 */
	private static final int ADJUSTMENT_BUTTONS_ID = 1;

	/**
	 * Starting X position for each row of buttons
	 */
	private static final int ADJUSTMENT_BUTTONS_X_POSITION = 35;

	/**
	 * Y position for each row
	 */
	private static final int[] ADJUSTMENT_BUTTONS_Y_POSITION = new int[] { 30, 70 };

	/**
	 * Amount of empty space between buttons
	 */
	private static final int ADJUSTMENT_BUTTONS_X_PADDING = 5;

	/**
	 * X offset for the amount field.
	 */
	private static final int AMOUNT_OFFSET_X = 61;

	/**
	 * Y offset for the amount field.
	 */
	private static final int AMOUNT_OFFSET_Y = 57;

	/**
	 * Width of the amount field.
	 */
	private static final int AMOUNT_WIDTH = 80;

	/**
	 * Height of the amount field.
	 */
	private static final int AMOUNT_HEIGHT = 10;

	/**
	 * The maximum number of characters that can be typed in the amount field.
	 */
	private static final int AMOUNT_MAX_CHARS = 8;

	/**
	 * X position of the title string.
	 */
	private static final int TITLE_POS_X = 6;

	/**
	 * Y position of the title string.
	 */
	private static final int TITLE_POS_Y = 6;

	/**
	 * The part we are adjusting the priority of
	 */
	private final AbstractAEPartBase part;

	/**
	 * The player viewing the gui.
	 */
	private final EntityPlayer player;

	/**
	 * Title of the window
	 */
	private final String title;

	/**
	 * Amount text field
	 */
	private DigitTextField amountField;

	/**
	 * Create the gui.
	 * 
	 * @param host
	 * @param player
	 */
	public GuiPriority( final IPriorityHost host, final EntityPlayer player )
	{
		// Call super and pass the priority container
		super( new ContainerPriority( host, player ) );

		// Get the part
		this.part = (AbstractAEPartBase)host;

		// Set the player
		this.player = player;

		// Set the width and height
		this.xSize = GuiPriority.GUI_WIDTH;
		this.ySize = GuiPriority.GUI_HEIGHT;

		// Set the title
		this.title = GuiText.Priority.getLocal();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the priority gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.PRIORITY.getTexture() );

		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

	}

	/**
	 * Draw the foreground
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, GuiPriority.TITLE_POS_X, GuiPriority.TITLE_POS_Y, 0 );

		// Draw the text field
		this.amountField.drawTextBox();
	}

	/**
	 * Called when the player types a key
	 */
	@Override
	protected void keyTyped( final char key, final int keyID )
	{
		// Pass to super
		super.keyTyped( key, keyID );

		// Ensure the string isn't too long to parse
		if( this.amountField.getText().length() < GuiPriority.AMOUNT_MAX_CHARS )
		{
			// Pass to the amount field
			this.amountField.textboxKeyTyped( key, keyID );

			// Check if they are intending to type a negative number
			if( this.amountField.getText().equals( "-" ) )
			{
				return;
			}

			// Convert the text field into a long
			int newPriority = 0;
			try
			{
				newPriority = Integer.parseInt( this.amountField.getText() );
			}
			catch( NumberFormatException e )
			{
			}

			// Update the server
			Packet_S_Priority.sendPriority( newPriority, this.player );
		}
	}

	/**
	 * Called when a button is clicked.
	 */
	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Was the priority button clicked?
		if( button.id == GuiPriority.PART_SWITCH_BUTTON_ID )
		{
			// Get the storage buses host 
			TileEntity host = this.part.getHostTile();

			// Ask the server to change to the priority gui
			Packet_S_ChangeGui.sendGuiChange( this.part, this.player, host.getWorldObj(), host.xCoord, host.yCoord, host.zCoord );
			return;

		}

		// Assume it was an adjustment button
		try
		{
			// Get the definition
			AdjustmentButtonDef abDef = GuiPriority.ADJUSTMENT_BUTTONS[button.id - GuiPriority.ADJUSTMENT_BUTTONS_ID];

			// Send the adjustment to the server
			Packet_S_Priority.sendPriorityDelta( abDef.amount, this.player );
		}
		catch( IndexOutOfBoundsException e )
		{
			return;
		}

	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Enable repeat keys
		Keyboard.enableRepeatEvents( true );

		// Create the part switch button
		this.buttonList.add( new GuiButtonAETab( GuiPriority.PART_SWITCH_BUTTON_ID, this.guiLeft + GuiPriority.PART_SWITCH_BUTTON_X_POSITION,
						this.guiTop, AEStateIconsEnum.WRENCH, this.part.getUnlocalizedName() ) );

		// Create the adjustment buttons
		int buttonXPosition = GuiPriority.ADJUSTMENT_BUTTONS_X_POSITION;
		int buttonYPosition = GuiPriority.ADJUSTMENT_BUTTONS_Y_POSITION[0];
		for( int adjustmentButtonIndex = 0; adjustmentButtonIndex < GuiPriority.ADJUSTMENT_BUTTONS.length; adjustmentButtonIndex++ )
		{
			// Get the button def
			AdjustmentButtonDef def = GuiPriority.ADJUSTMENT_BUTTONS[adjustmentButtonIndex];

			// Calculate the ID
			int ID = GuiPriority.ADJUSTMENT_BUTTONS_ID + adjustmentButtonIndex;

			// Are we moving into row 2?
			if( adjustmentButtonIndex == ( GuiPriority.ADJUSTMENT_BUTTONS.length / 2 ) )
			{
				// Reset X
				buttonXPosition = GuiPriority.ADJUSTMENT_BUTTONS_X_POSITION;

				// Set Y
				buttonYPosition = GuiPriority.ADJUSTMENT_BUTTONS_Y_POSITION[1];
			}

			// Add the button
			this.buttonList.add( def.makeButton( ID, this.guiLeft + buttonXPosition, this.guiTop + buttonYPosition ) );

			// Increment X
			buttonXPosition += def.width + GuiPriority.ADJUSTMENT_BUTTONS_X_PADDING;
		}
		// Create the amount field
		this.amountField = new DigitTextField( this.fontRendererObj, GuiPriority.AMOUNT_OFFSET_X, GuiPriority.AMOUNT_OFFSET_Y,
						GuiPriority.AMOUNT_WIDTH, GuiPriority.AMOUNT_HEIGHT );

		// Start focused
		this.amountField.setFocused( true );

		// Draw in the forground
		this.amountField.setEnableBackgroundDrawing( false );

		// Text color white
		this.amountField.setTextColor( 0xFFFFFFFF );

		// Ask for the priority from the server
		Packet_S_Priority.sendPriorityRequest( this.player );
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
	 * Called when the server sends the priority.
	 * 
	 * @param priority
	 */
	public void onServerSendPriority( final int priority )
	{
		// Set the textbox text
		this.amountField.setText( Integer.toString( priority ) );
	}

}
