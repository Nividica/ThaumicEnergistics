package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.container.ContainerKnowledgeInscriber;
import thaumicenergistics.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.gui.buttons.GuiButtonSaveDelete;
import thaumicenergistics.network.packet.server.Packet_S_KnowledgeInscriber;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.GuiTextureManager;

public class GuiKnowledgeInscriber
	extends AbstractGuiBase
{
	private class SaveParticle
	{
		private int startX, startY;
		private int distX, distY;
		private float percent;
		private int slot;

		public SaveParticle( final int slotNumber )
		{
			// Set percentage
			this.percent = 1.0f;

			// Set slot
			this.slot = slotNumber;

			// Calculate starting postion
			this.startX = ContainerKnowledgeInscriber.CRAFTING_SLOT_X + (
							( slotNumber % ContainerKnowledgeInscriber.CRAFTING_COLS )
							* ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING );

			this.startY = ContainerKnowledgeInscriber.CRAFTING_SLOT_Y + (
							( slotNumber / ContainerKnowledgeInscriber.CRAFTING_COLS )
							* ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING );

			// Calculate distances
			this.distX = ContainerKnowledgeInscriber.KCORE_SLOT_X - this.startX;
			this.distY = ContainerKnowledgeInscriber.KCORE_SLOT_Y - this.startY;
		}

		/**
		 * Draws the particle. Assumes prepare and finish are called elsewhere.
		 * 
		 * @param gui
		 * @param percentDelta
		 * @param frame
		 */
		public void draw( final Gui gui, final float percentDelta, final int frame )
		{
			// Add delta
			this.percent += percentDelta;

			// Bounds check
			if( this.percent >= 1.0f )
			{
				this.percent = 1.0f;
			}
			else if( this.percent < 0 )
			{
				return;
			}

			// Calculate X and Y
			int X = this.startX + (int)( this.distX * this.percent );
			int Y = this.startY + (int)( this.distY * this.percent );

			// Draw
			GuiParticle.Knowledge.drawParticle( gui, X, Y, frame, 0.2f, 0.5f, 1.0f, false );
		}

		/**
		 * Returns true if the particle is not finished drawing
		 * 
		 * @return
		 */
		public boolean notFinished()
		{
			return( this.percent < 1.0f );
		}

		/**
		 * Prepares the particle to be draw again.
		 */
		public void reset()
		{
			// Set percentage
			this.percent = -( this.slot / 8.0f );
		}
	}

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
	private GuiButtonSaveDelete saveButton;

	/**
	 * GUI Title
	 */
	private String title;

	/**
	 * State of the save button.
	 */
	private CoreSaveState saveState = CoreSaveState.Disabled_MissingCore;

	/**
	 * Save particles
	 */
	private SaveParticle[] particles;

	/**
	 * True if particles need to be drawn.
	 */
	private boolean hasParticlesToDraw = false;

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

		// Set title
		this.title = ThEStrings.Block_KnowledgeInscriber.getLocalized();

		// Setup the particles
		this.particles = new SaveParticle[ContainerKnowledgeInscriber.CRAFTING_COLS * ContainerKnowledgeInscriber.CRAFTING_ROWS];
		for( int index = 0; index < this.particles.length; ++index )
		{
			this.particles[index] = new SaveParticle( index );
		}
	}

	private void drawSaveParticles()
	{
		int time = (int)( System.currentTimeMillis() % Integer.MAX_VALUE );

		// Calculate frame number
		int frame = ( time / 125 );

		// Set percent delta
		// NOTE: This should be time based, not FPS based. But lazy.
		float percentDelta = 0.03f;

		// Assume all particles are drawn
		this.hasParticlesToDraw = false;

		GuiParticle.Knowledge.prepareDraw();
		for( int index = 0; index < this.particles.length; ++index )
		{
			// Draw the particle
			this.particles[index].draw( this, percentDelta, frame );

			// Is the particle not done?
			this.hasParticlesToDraw |= this.particles[index].notFinished();
		}
		GuiParticle.finishDraw();
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
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, GuiKnowledgeInscriber.TITLE_POS_X, GuiKnowledgeInscriber.TITLE_POS_Y, 0 );

		// Any particles to draw?
		if( this.hasParticlesToDraw )
		{
			this.drawSaveParticles();
		}
	}

	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{
		// Was the clicked button the save button?
		if( button == this.saveButton )
		{
			// Send the request to the server
			Packet_S_KnowledgeInscriber.sendSaveDelete( this.player );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		this.buttonList.clear();

		// Create the save/delete button
		this.saveButton = new GuiButtonSaveDelete( GuiKnowledgeInscriber.BUTTON_SAVE_ID, this.guiLeft + GuiKnowledgeInscriber.BUTTON_SAVE_POS_X,
						this.guiTop + GuiKnowledgeInscriber.BUTTON_SAVE_POS_Y, this.saveState );
		this.buttonList.add( this.saveButton );

		// Request full update
		Packet_S_KnowledgeInscriber.sendFullUpdateRequest( this.player );
	}

	/**
	 * Called when the server sends a change in the save/load button
	 * functionality.
	 * 
	 * @param saveState
	 * @param justSaved
	 */
	public void onReceiveSaveState( final CoreSaveState saveState, final boolean justSaved )
	{
		// Set the state
		this.saveState = saveState;

		// Update the button
		if( this.saveButton != null )
		{
			this.saveButton.setSaveState( saveState );
		}

		// Was a pattern just saved?
		if( justSaved )
		{
			// Reset all particles
			for( int index = 0; index < this.particles.length; ++index )
			{
				this.particles[index].reset();
			}
			this.hasParticlesToDraw = true;
		}
	}
}
