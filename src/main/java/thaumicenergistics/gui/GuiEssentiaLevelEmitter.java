package thaumicenergistics.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaEmitter;
import thaumicenergistics.gui.widget.DigitTextField;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.gui.widget.WidgetRedstoneModes;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.PacketEssentiaEmitter;
import thaumicenergistics.parts.AEPartEssentiaLevelEmitter;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.GuiHelper;
import appeng.api.config.RedstoneMode;

public class GuiEssentiaLevelEmitter extends GuiContainer implements IAspectSlotGui
{
	private static String[] BUTTON_LABELS = { "-1", "-10", "-100", "+1", "+10", "+100" };
	private static String[] BUTTON_LABELS_SHIFTED = { "-100", "-1000", "-10000", "+100", "+1000", "+10000" };

	public static final int XSIZE = 176;
	public static final int YSIZE = 184;

	private DigitTextField amountField;
	private AEPartEssentiaLevelEmitter part;
	private EntityPlayer player;
	private WidgetAspectSlot aspectSlot;

	public GuiEssentiaLevelEmitter(AEPartEssentiaLevelEmitter part, EntityPlayer player)
	{
		super( new ContainerPartEssentiaEmitter( player ) );

		this.player = player;

		this.part = part;

		this.aspectSlot = new WidgetAspectSlot( this.player, this.part, 123, 30 );

		new PacketEssentiaEmitter( false, this.part, this.player ).sendPacketToServer();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( float f, int i, int j )
	{
		// this.drawDefaultBackground();

		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_LEVEL_EMITTER.getTexture() );

		int posX = ( this.width - XSIZE ) / 2;

		int posY = ( this.height - YSIZE ) / 2;

		this.drawTexturedModalRect( posX, posY, 0, 0, XSIZE, YSIZE );
	}

	@Override
	protected void keyTyped( char key, int keyID )
	{
		super.keyTyped( key, keyID );

		if ( ( "0123456789".contains( String.valueOf( key ) ) ) || ( keyID == 14 ) )
		{
			this.amountField.textboxKeyTyped( key, keyID );

			new PacketEssentiaEmitter( this.amountField.getText(), this.part, this.player ).sendPacketToServer();
		}
	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseBtn )
	{
		super.mouseClicked( mouseX, mouseY, mouseBtn );

		if ( GuiHelper.isPointInGuiRegion( this.aspectSlot.getPosX(), this.aspectSlot.getPosY(), 18, 18, mouseX, mouseY, this.guiLeft, this.guiTop ) )
		{
			this.aspectSlot.mouseClicked( EssentiaItemContainerHelper.getAspectInContainer( this.player.inventory.getItemStack() ) );
		}
	}

	@Override
	public void actionPerformed( GuiButton button )
	{
		int ID = button.id;

		if ( ( ID >= 0 ) && ( ID <= 5 ) )
		{
			new PacketEssentiaEmitter( Integer.parseInt( button.displayString ), this.part, this.player ).sendPacketToServer();
		}
		else if ( ID == 6 )
		{
			new PacketEssentiaEmitter( true, this.part, this.player ).sendPacketToServer();
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( AEPartsEnum.EssentiaLevelEmitter.getStatName(), 10, -15, 0 );

		this.aspectSlot.drawWidget();

		GuiHelper.renderOverlay( this.zLevel, this.guiLeft, this.guiTop, this.aspectSlot, mouseX, mouseY );
	}

	@Override
	public void drawScreen( int x, int y, float f )
	{
		this.drawDefaultBackground();

		int count = Math.min( 6, this.buttonList.size() );

		for( int i = 0; i < count; i++ )
		{
			GuiButton currentButton = (GuiButton) this.buttonList.get( i );

			if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
			{
				currentButton.displayString = ( BUTTON_LABELS_SHIFTED[i] );
			}
			else
			{
				currentButton.displayString = ( BUTTON_LABELS[i] );
			}
		}

		super.drawScreen( x, y, f );

		this.amountField.drawTextBox();
	}

	@Override
	public void initGui()
	{
		int posX = ( this.width - XSIZE ) / 2;
		int posY = ( this.height - YSIZE ) / 2;

		this.amountField = new DigitTextField( this.fontRendererObj, posX + 24, posY + 44, 80, 10 );

		this.amountField.setFocused( true );

		this.amountField.setEnableBackgroundDrawing( false );

		this.amountField.setTextColor( 0xFFFFFF );

		this.buttonList.clear();

		this.buttonList.add( new GuiButton( 0, ( posX + 65 ) - 46, posY + 14, 42, 20, BUTTON_LABELS[0] ) );

		this.buttonList.add( new GuiButton( 1, ( posX + 115 ) - 46, posY + 14, 42, 20, BUTTON_LABELS[1] ) );

		this.buttonList.add( new GuiButton( 2, ( posX + 165 ) - 46, posY + 14, 42, 20, BUTTON_LABELS[2] ) );

		this.buttonList.add( new GuiButton( 3, ( posX + 65 ) - 46, ( posY + 64 ) - 2, 42, 20, BUTTON_LABELS[3] ) );

		this.buttonList.add( new GuiButton( 4, ( posX + 115 ) - 46, ( posY + 64 ) - 2, 42, 20, BUTTON_LABELS[4] ) );

		this.buttonList.add( new GuiButton( 5, ( posX + 165 ) - 46, ( posY + 64 ) - 2, 42, 20, BUTTON_LABELS[5] ) );

		this.buttonList.add( new WidgetRedstoneModes( 6, posX - 18, posY + 2, 16, 16, RedstoneMode.LOW_SIGNAL, true ) );

		super.initGui();
	}

	public void setAmountField( long amount )
	{
		this.amountField.setText( Long.toString( amount ) );
	}

	public void setRedstoneMode( RedstoneMode mode )
	{
		( (WidgetRedstoneModes) this.buttonList.get( 6 ) ).setRedstoneMode( mode );
	}

	@Override
	public void updateAspects( List<Aspect> aspectList )
	{
		if ( ( aspectList == null ) || ( aspectList.isEmpty() ) )
		{
			this.aspectSlot.setAspect( null );
		}
		else
		{
			this.aspectSlot.setAspect( aspectList.get( 0 ) );
		}
	}

}
