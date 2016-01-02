package thaumicenergistics.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.client.gui.buttons.GuiButtonEncodePattern;
import thaumicenergistics.client.textures.AEStateIconsEnum;
import thaumicenergistics.client.textures.GuiTextureManager;
import thaumicenergistics.common.container.ContainerDistillationEncoder;
import thaumicenergistics.common.network.packet.server.Packet_S_DistillationEncoder;
import thaumicenergistics.common.registries.ThEStrings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDistillationEncoder
	extends ThEBaseGui
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
	 * Position of the encode button
	 */
	private static final int BUTTON_ENCODE_POS_X = 146, BUTTON_ENCODE_POS_Y = 94;

	/**
	 * Title of the gui.
	 */
	private final String title;

	/**
	 * The encode button.
	 */
	private GuiButtonEncodePattern buttonEncode;

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
		if( button == this.buttonEncode )
		{
			Packet_S_DistillationEncoder.sendEncodePattern(
							( (ContainerDistillationEncoder)this.inventorySlots ).getPlayer() );
		}
	}

	@Override
	public void initGui()
	{
		// Call super
		super.initGui();

		// Create the encode button
		this.buttonEncode = new GuiButtonEncodePattern( 0, BUTTON_ENCODE_POS_X + this.guiLeft, BUTTON_ENCODE_POS_Y + this.guiTop,
						AEStateIconsEnum.STANDARD_ICON_SIZE,
						AEStateIconsEnum.STANDARD_ICON_SIZE );
		this.buttonList.add( this.buttonEncode );
	}
}
