package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerKnowledgeInscriber;
import thaumicenergistics.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.gui.buttons.ButtonSaveDelete;
import thaumicenergistics.network.packet.server.PacketServerKnowledgeInscriber;
import thaumicenergistics.texture.GuiTextureManager;

public class GuiKnowledgeInscriber
	extends AbstractGuiBase
{
	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 210, GUI_HEIGHT = 244;

	/**
	 * Save/Delete button.
	 */
	private static final int BUTTON_SAVE_ID = 0, BUTTON_SAVE_POS_X = 141, BUTTON_SAVE_POS_Y = 109;

	/**
	 * Position of the title string.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Save / Delete button.
	 */
	private ButtonSaveDelete saveButton;

	/**
	 * GUI Title
	 */
	private String title;

	/**
	 * State of the save button.
	 */
	private CoreSaveState saveState = CoreSaveState.Disabled_MissingCore;

	/**
	 * Player viewing the GUI.
	 */
	private EntityPlayer player;

	public GuiKnowledgeInscriber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( new ContainerKnowledgeInscriber( player, world, x, y, z ) );

		// Set the player
		this.player = player;

		// Set the GUI size
		this.xSize = GuiKnowledgeInscriber.GUI_WIDTH;
		this.ySize = GuiKnowledgeInscriber.GUI_HEIGHT;

		// TODO: Localize title
		this.title = "Knowledge Inscriber";
	}

	@Override
	protected void actionPerformed( final GuiButton button )
	{
		// Was the clicked button the save button?
		if( button == this.saveButton )
		{
			// Send the request to the server
			new PacketServerKnowledgeInscriber().createRequestSaveDelete( this.player ).sendPacketToServer();
		}
	}

	/**
	 * Draw background
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Calculate the color shifts
		long slowTime = ( System.currentTimeMillis() / 100 );
		float redBounce = Math.abs( 1.0F - ( ( ( slowTime % 50 ) / 50.0F ) * 2.0F ) );
		float greenBounce = 1.0F - redBounce;

		// Shift color
		GL11.glColor4f( redBounce, greenBounce, 1.0F, 1.0F );

		// Bind the research background
		Minecraft.getMinecraft().renderEngine.bindTexture( new ResourceLocation( ThaumicEnergistics.MOD_ID,
						"textures/research/Research.Background.png" ) );

		// Calculate the X position
		int xpos = (int)( slowTime % 10 );

		// Draw the background
		this.drawTexturedModalRect( this.guiLeft + 80, this.guiTop + 106, 55 - xpos, 25, 35, 20 );

		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the workbench gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.KNOWLEDGE_INSCRIBER.getTexture() );

		// Draw the gui texture
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );
	}

	/**
	 * Draw the foreground
	 */
	@Override
	public void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, GuiKnowledgeInscriber.TITLE_POS_X, GuiKnowledgeInscriber.TITLE_POS_Y, 0 );

		// Get the tooltip from the buttons
		if( this.addTooltipFromButtons( mouseX, mouseY ) )
		{
			// Draw the tooltip
			this.drawTooltip( mouseX - this.guiLeft, mouseY - this.guiTop, true );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		this.buttonList.clear();

		// Create the save/delete button
		this.saveButton = new ButtonSaveDelete( GuiKnowledgeInscriber.BUTTON_SAVE_ID, this.guiLeft + GuiKnowledgeInscriber.BUTTON_SAVE_POS_X,
						this.guiTop + GuiKnowledgeInscriber.BUTTON_SAVE_POS_Y, this.saveState );
		this.buttonList.add( this.saveButton );

		// Request full update
		new PacketServerKnowledgeInscriber().createRequestFullUpdate( this.player ).sendPacketToServer();
	}

	/**
	 * Called when the server sends a change in the save/load button
	 * functionality.
	 * 
	 * @param saveState
	 */
	public void onReceiveSaveState( final CoreSaveState saveState )
	{
		// Set the state
		this.saveState = saveState;

		// Update the button
		if( this.saveButton != null )
		{
			this.saveButton.setSaveState( saveState );
		}
	}
}
