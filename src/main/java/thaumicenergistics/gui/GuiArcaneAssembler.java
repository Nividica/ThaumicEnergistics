package thaumicenergistics.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerArcaneAssembler;
import thaumicenergistics.gui.abstraction.AbstractGuiBase;
import thaumicenergistics.texture.GuiTextureManager;

public class GuiArcaneAssembler
	extends AbstractGuiBase
{
	/**
	 * Gui size.
	 */
	private static final int GUI_WIDTH = 211, GUI_HEIGHT = 197;

	/**
	 * Title position.
	 */
	private static final int TITLE_POS_X = 6, TITLE_POS_Y = 6;

	/**
	 * The player who is looking at the GUI.
	 */
	private EntityPlayer player;

	/**
	 * Title displayed on the GUI
	 */
	private String title;

	public GuiArcaneAssembler( final EntityPlayer player, final World world, final int X, final int Y, final int Z )
	{
		// Call super
		super( new ContainerArcaneAssembler( player, world, X, Y, Z ) );

		// Set the player
		this.player = player;

		// Set the GUI size
		this.xSize = GuiArcaneAssembler.GUI_WIDTH;
		this.ySize = GuiArcaneAssembler.GUI_HEIGHT;

		// Set the title
		this.title = StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".gui.arcane.assembler.title" );
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
		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ARCANE_ASSEMBLER.getTexture() );

		// Draw the gui texture
		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize );

		// Draw the bars

		int fakeAmount = (int)( ( System.currentTimeMillis() / 100 ) % 17 );
		if( fakeAmount > 0 )
		{
			this.drawTexturedModalRect( this.guiLeft + 41, this.guiTop + 87 + ( 16 - fakeAmount ), 41, 202 + ( 16 - fakeAmount ), 94, fakeAmount );
		}
	}

	/**
	 * Draw the foreground
	 */
	@Override
	public void drawGuiContainerForegroundLayer( final int mouseX, final int mouseY )
	{
		// Draw the title
		this.fontRendererObj.drawString( this.title, GuiArcaneAssembler.TITLE_POS_X, GuiArcaneAssembler.TITLE_POS_Y, 0 );
	}

}
