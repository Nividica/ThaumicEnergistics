package thaumicenergistics.client.gui;

import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.client.gui.buttons.GuiButtonClearCraftingGrid;
import thaumicenergistics.client.gui.buttons.GuiButtonSaveDelete;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.container.ContainerKnowledgeInscriber;
import thaumicenergistics.common.container.ContainerKnowledgeInscriber.CoreSaveState;
import thaumicenergistics.common.network.packet.server.Packet_S_KnowledgeInscriber;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.tiles.TileKnowledgeInscriber;

/**
 * {@link TileKnowledgeInscriber} GUI
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class GuiKnowledgeInscriber
	extends ThEBaseGui
{
	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 210, GUI_HEIGHT = 244;

	/**
	 * Save/Delete button position.
	 */
	private static final int BUTTON_SAVE_POS_X = 141, BUTTON_SAVE_POS_Y = 109;

	/**
	 * Clear button position.
	 */
	private static final int BUTTON_CLEAR_POS_X = 80, BUTTON_CLEAR_POS_Y = 89;

	/**
	 * Position of the title string.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Player viewing the GUI.
	 */
	private final EntityPlayer player;

	/**
	 * Save particles
	 */
	private final GuiParticleAnimator[] particles;

	/**
	 * GUI Title
	 */
	private final String title;

	/**
	 * Save / Delete button.
	 */
	private GuiButtonSaveDelete buttonSave;

	/**
	 * Clear grid button.
	 */
	private GuiButtonClearCraftingGrid buttonClear;

	/**
	 * State of the save button.
	 */
	private CoreSaveState saveState = CoreSaveState.Disabled_MissingCore;

	/**
	 * True if particles need to be drawn.
	 */
	private boolean hasParticlesToDraw = false;

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
		this.particles = new GuiParticleAnimator[ContainerKnowledgeInscriber.CRAFTING_COLS * ContainerKnowledgeInscriber.CRAFTING_ROWS];
		for( int index = 0; index < this.particles.length; ++index )
		{
			this.particles[index] = this.createSaveParticle( index );
		}
	}

	/**
	 * Creates a particle for the specified slot number.
	 *
	 * @param slotNumber
	 * @return
	 */
	private GuiParticleAnimator createSaveParticle( final int slotNumber )
	{
		int startX = ContainerKnowledgeInscriber.CRAFTING_SLOT_X +
						( ( slotNumber % ContainerKnowledgeInscriber.CRAFTING_COLS ) * ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING );

		int startY = ContainerKnowledgeInscriber.CRAFTING_SLOT_Y +
						( ( slotNumber / ContainerKnowledgeInscriber.CRAFTING_COLS ) * ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING );

		// Create the animator
		GuiParticleAnimator gpa = new GuiParticleAnimator( startX, startY,
						ContainerKnowledgeInscriber.KCORE_SLOT_X, ContainerKnowledgeInscriber.KCORE_SLOT_Y,
						0.4f, EnumGuiParticles.Knowledge );

		// Set color
		gpa.red = 0.2f;
		gpa.green = 0.5f;
		gpa.blue = 1.0f;

		// Set delay
		gpa.setDelayTime( slotNumber * 50L );

		return gpa;
	}

	/**
	 * Draws all save particles.
	 */
	private void drawSaveParticles()
	{
		// Assume all particles are drawn
		this.hasParticlesToDraw = false;

		EnumGuiParticles.Knowledge.prepareDraw();
		for( int index = 0; index < this.particles.length; ++index )
		{
			// Draw the particle
			this.hasParticlesToDraw |= this.particles[index].draw( this, false );
		}
		EnumGuiParticles.finishDraw();
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
		if( button == this.buttonSave )
		{
			// Send the request to the server
			Packet_S_KnowledgeInscriber.sendSaveDelete( this.player );
		}
		else if( button == this.buttonClear )
		{
			// Send the request to the server
			Packet_S_KnowledgeInscriber.sendClearGrid( this.player );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		this.buttonList.clear();

		// Create the save/delete button
		this.buttonSave = new GuiButtonSaveDelete( 0, this.guiLeft + GuiKnowledgeInscriber.BUTTON_SAVE_POS_X,
						this.guiTop + GuiKnowledgeInscriber.BUTTON_SAVE_POS_Y, this.saveState );
		this.buttonList.add( this.buttonSave );

		// Create the clear grid button
		this.buttonClear = new GuiButtonClearCraftingGrid( 1, this.guiLeft + GuiKnowledgeInscriber.BUTTON_CLEAR_POS_X,
						this.guiTop + GuiKnowledgeInscriber.BUTTON_CLEAR_POS_Y, 8, 8, false );
		this.buttonList.add( this.buttonClear );

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
		if( this.buttonSave != null )
		{
			this.buttonSave.setSaveState( saveState );
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
