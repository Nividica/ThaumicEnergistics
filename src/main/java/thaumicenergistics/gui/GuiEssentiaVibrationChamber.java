package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.container.ContainerEssentiaVibrationChamber;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.texture.GuiTextureManager;

public class GuiEssentiaVibrationChamber
	extends AbstractGuiBase
{

	/**
	 * The width of the gui
	 */
	private static final int GUI_WIDTH = 176;

	/**
	 * The height of the gui
	 */
	private static final int GUI_HEIGHT = 82;

	/**
	 * Thaumcraft's alchemy furnace GUI texture
	 */
	private static final ResourceLocation alchemyFurnaceTexture = new ResourceLocation( "Thaumcraft", "textures/gui/gui_alchemyfurnace.png" );

	public GuiEssentiaVibrationChamber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Create the container and call super
		super( new ContainerEssentiaVibrationChamber( player, world, x, y, z ) );

		// Set the width and height
		this.xSize = GuiEssentiaVibrationChamber.GUI_WIDTH;
		this.ySize = GuiEssentiaVibrationChamber.GUI_HEIGHT;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// No tint + no cutout
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the chamber gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_VIBRATION_CHAMBER.getTexture() );

		// Draw the GUI
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

		int powerOffset = mouseY % 18;

		// Draw the power bar
		if( powerOffset > 0 )
		{
			this.drawTexturedModalRect( this.guiLeft + 96, this.guiTop + 32 + powerOffset, 51, 83 + powerOffset, 6, 18 - powerOffset );
		}

		// Bind the alchemy furnace gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( new ResourceLocation( "thaumcraft", "textures/gui/gui_alchemyfurnace.png" ) );

		int storedOffset = 48 - ( ( mouseX / 20 ) % 48 );

		// Draw stored essentia amount underlay
		if( storedOffset > 0 )
		{
			this.drawTexturedModalRect( this.guiLeft + 62, this.guiTop + 16 + storedOffset, 200, storedOffset, 8, 48 - storedOffset );
		}

		// Enable blending
		GL11.glEnable( GL11.GL_BLEND );

		// Set the blending mode
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		// Draw stored essentia amount overlay
		this.drawTexturedModalRect( this.guiLeft + 61, this.guiTop + 12, 232, 0, 11, 56 );

		int progressOffset = 20 - ( mouseX % 20 );

		// Draw fire/progress
		if( progressOffset > 0 )
		{
			this.drawTexturedModalRect( this.guiLeft + 78, this.guiTop + 30 + progressOffset, 177, progressOffset, 15, 20 - progressOffset );
		}

		// Disable blending
		GL11.glDisable( GL11.GL_BLEND );

	}

	@Override
	public void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{

	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();
	}

}
