package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.container.ContainerDistillationEncoder;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.texture.GuiTextureManager;

public class GuiDistillationEncoder
	extends AbstractGuiBase
{
	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 176, GUI_HEIGHT = 234;

	/**
	 * Position of the title string.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * Title of the gui.
	 */
	private final String title;

	public GuiDistillationEncoder( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( new ContainerDistillationEncoder( player, world, x, y, z ) );

		// Set the title
		this.title = ThEStrings.Block_DistillationEncoder.getLocalized();

		// Set the GUI size
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT;
	}

	/**
	 * Draw the background.
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		// Full white
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		// Bind the encoder gui texture
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.DISTILLATION_ENCODER.getTexture() );

		// Draw the gui texture
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );
	}

	/**
	 * Draw the foreground.
	 */
	@Override
	protected void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, TITLE_POS_X, TITLE_POS_Y, 0 );

	}

	@Override
	protected void onButtonClicked( final GuiButton button, final int mouseButton )
	{

	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();
	}
}
