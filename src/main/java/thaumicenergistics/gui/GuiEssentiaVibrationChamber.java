package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerEssentiaVibrationChamber;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.GuiHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEssentiaVibrationChamber
	extends AbstractGuiBase
{

	/**
	 * The width & height of the gui
	 */
	private static final int GUI_WIDTH = 176, GUI_HEIGHT = 82;

	/**
	 * Dimensions and location of the power bar.
	 */
	private static final int POWER_POS_X = 96, POWER_POS_Y = 32, POWER_TEX_U = 51, POWER_TEX_V = 83, POWER_WIDTH = 6, POWER_HEIGHT = 18;

	/**
	 * Dimensions and location of the essentia storage level bar.
	 */
	private static final int STORED_POS_X = 62, STORED_POS_Y = 16, STORED_TEX_X = 200, STORED_WIDTH = 8, STORED_HEIGHT = 48;

	/**
	 * Dimensions and location of the essentia storage vial.
	 */
	private static final int VIAL_POS_X = 61, VIAL_POS_Y = 12, VIAL_TEX_X = 232, VIAL_WIDTH = 11, VIAL_HEIGHT = 56;

	/**
	 * Dimensions and location of the progress fire.
	 */
	private static final int FIRE_POS_X = 78, FIRE_POS_Y = 30, FIRE_TEX_X = 177, FIRE_WIDTH = 15, FIRE_HEIGHT = 20;

	/**
	 * Thaumcraft's alchemy furnace GUI texture
	 */
	private static final ResourceLocation alchemyFurnaceTexture = new ResourceLocation( "thaumcraft", "textures/gui/gui_alchemyfurnace.png" );

	/**
	 * Reference to the container.
	 */
	private final ContainerEssentiaVibrationChamber container;

	public GuiEssentiaVibrationChamber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Create the container and call super
		super( new ContainerEssentiaVibrationChamber( player, world, x, y, z ) );
		this.container = (ContainerEssentiaVibrationChamber)this.inventorySlots;

		// Set the width and height
		this.xSize = GuiEssentiaVibrationChamber.GUI_WIDTH;
		this.ySize = GuiEssentiaVibrationChamber.GUI_HEIGHT;
	}

	/**
	 * Draws elements from the Thaumcraft texture.
	 */
	private void drawTC_Textures()
	{
		// Bind the alchemy furnace gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiEssentiaVibrationChamber.alchemyFurnaceTexture );

		// Draw stored essentia amount underlay
		int storedHeightOffset = (int)( GuiEssentiaVibrationChamber.STORED_HEIGHT * this.container.getStoredEssentiaPercent() );

		this.drawTexturedModalRect( this.guiLeft + GuiEssentiaVibrationChamber.STORED_POS_X, this.guiTop + GuiEssentiaVibrationChamber.STORED_POS_Y +
						storedHeightOffset, GuiEssentiaVibrationChamber.STORED_TEX_X, storedHeightOffset, GuiEssentiaVibrationChamber.STORED_WIDTH,
			GuiEssentiaVibrationChamber.STORED_HEIGHT - storedHeightOffset );

		// Enable alpha blending
		GL11.glEnable( GL11.GL_BLEND );

		// Set the blending mode
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		// Draw stored essentia amount overlay (Vial)
		this.drawTexturedModalRect( this.guiLeft + GuiEssentiaVibrationChamber.VIAL_POS_X, this.guiTop + GuiEssentiaVibrationChamber.VIAL_POS_Y,
			GuiEssentiaVibrationChamber.VIAL_TEX_X, 0, GuiEssentiaVibrationChamber.VIAL_WIDTH, GuiEssentiaVibrationChamber.VIAL_HEIGHT );

		// Draw fire/progress
		int progressHeightOffset = (int)( GuiEssentiaVibrationChamber.FIRE_HEIGHT * this.container.getTicksRemainingPercent() );

		this.drawTexturedModalRect( this.guiLeft + GuiEssentiaVibrationChamber.FIRE_POS_X, this.guiTop + GuiEssentiaVibrationChamber.FIRE_POS_Y +
						progressHeightOffset, GuiEssentiaVibrationChamber.FIRE_TEX_X, progressHeightOffset, GuiEssentiaVibrationChamber.FIRE_WIDTH,
			GuiEssentiaVibrationChamber.FIRE_HEIGHT - progressHeightOffset );

		// Disable blending
		GL11.glDisable( GL11.GL_BLEND );
	}

	/**
	 * Draws elements from the Thaumic Energistics texture.
	 */
	private void drawThE_Textures()
	{

		// Bind the chamber gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTexture() );

		// Draw the GUI
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

		// Calculate the height
		int powerBarHeightOffset = (int)( GuiEssentiaVibrationChamber.POWER_HEIGHT * this.container.getPowerPercent() );

		// Draw the power bar
		this.drawTexturedModalRect(
			this.guiLeft + GuiEssentiaVibrationChamber.POWER_POS_X,
			this.guiTop + GuiEssentiaVibrationChamber.POWER_POS_Y + powerBarHeightOffset,
			GuiEssentiaVibrationChamber.POWER_TEX_U,
			GuiEssentiaVibrationChamber.POWER_TEX_V + powerBarHeightOffset,
			GuiEssentiaVibrationChamber.POWER_WIDTH,
			GuiEssentiaVibrationChamber.POWER_HEIGHT - powerBarHeightOffset );

	}

	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// No tint + no cutout
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Draw from the thaumic energistics texture
		this.drawThE_Textures();

		// Draw from the thaumcraft texture
		this.drawTC_Textures();

	}

	@Override
	public void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Is the mouse over the essentia storage?
		if( GuiHelper.INSTANCE.isPointInGuiRegion( GuiEssentiaVibrationChamber.VIAL_POS_Y, GuiEssentiaVibrationChamber.VIAL_POS_X,
			GuiEssentiaVibrationChamber.VIAL_HEIGHT, GuiEssentiaVibrationChamber.VIAL_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Get the stored Aspect
			Aspect storedAspect = this.container.getStoredEssentiaAspect();

			// Get the stored amount
			int storedAmount = this.container.getStoredEssentiaAmount();

			// Anything stored?
			if( ( storedAmount > 0 ) && ( storedAspect != null ) )
			{
				// Add stored info
				this.tooltip.add( String.format( "%s x %d", storedAspect.getName(), storedAmount ) );
			}
			else
			{
				// Empty
				this.tooltip.add( "Empty" );
			}
		}
		// Is the mouse over the fire?
		else if( GuiHelper.INSTANCE.isPointInGuiRegion( GuiEssentiaVibrationChamber.FIRE_POS_Y, GuiEssentiaVibrationChamber.FIRE_POS_X,
			GuiEssentiaVibrationChamber.FIRE_HEIGHT, GuiEssentiaVibrationChamber.FIRE_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Get the aspect being processed
			Aspect processingAspect = this.container.getProcessingAspect();

			// Anything being processed?
			if( processingAspect != null )
			{
				// Calculate the seconds remaining
				float secondsRemaining = ( this.container.getTicksRemaining() / 20.0F );

				// Add the aspect
				this.tooltip.add( String.format( "Processing: %s", processingAspect.getName() ) );

				// Add the time
				this.tooltip.add( String.format( "Time Remaining: %.0fs", secondsRemaining ) );
			}
		}
		// Is the mouse over the power bar?
		else if( GuiHelper.INSTANCE.isPointInGuiRegion( GuiEssentiaVibrationChamber.POWER_POS_Y, GuiEssentiaVibrationChamber.POWER_POS_X,
			GuiEssentiaVibrationChamber.POWER_HEIGHT, GuiEssentiaVibrationChamber.POWER_WIDTH, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			// Add the power per tick
			this.tooltip.add( String.format( "%.0f AE/t", this.container.getPowerPerTick() ) );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();
	}

}
