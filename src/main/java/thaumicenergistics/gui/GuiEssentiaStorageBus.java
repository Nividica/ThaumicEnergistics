package thaumicenergistics.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.gui.widget.WidgetAspectSlot;
import thaumicenergistics.gui.widget.WidgetRedstoneModes;
import thaumicenergistics.network.IAspectSlotGui;
import thaumicenergistics.network.packet.PacketEssentiaStorageBus;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import thaumicenergistics.texture.GuiTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.GuiHelper;
import appeng.api.AEApi;

public class GuiEssentiaStorageBus
	extends GuiContainer
	implements IAspectSlotGui
{
	private EntityPlayer player;
	private List<WidgetAspectSlot> aspectSlotList = new ArrayList<WidgetAspectSlot>();
	private List<Aspect> filteredAspects = new ArrayList<Aspect>();
	private boolean hasNetworkTool;

	public GuiEssentiaStorageBus(AEPartEssentiaStorageBus part, EntityPlayer player)
	{
		super( new ContainerPartEssentiaStorageBus( part, player ) );

		( (ContainerPartEssentiaStorageBus) this.inventorySlots ).setGui( this );

		this.player = player;

		for( int x = 0; x < 9; x++ )
		{
			for( int y = 0; y < 6; y++ )
			{
				this.aspectSlotList.add( new WidgetAspectSlot( this.player, part, ( x * 6 ) + y, ( 18 * x ) + 7, ( 18 * y ) + 17 ) );
			}
		}

		new PacketEssentiaStorageBus( player, part ).sendPacketToServer();

		this.hasNetworkTool = ( this.inventorySlots.getInventory().size() > 40 );

		this.xSize = ( this.hasNetworkTool ? 246 : 211 );

		this.ySize = 222;
	}

	private boolean isMouseOverSlot( Slot slot, int x, int y )
	{
		return GuiHelper.isPointInGuiRegion( slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y, this.guiLeft, this.guiTop );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY )
	{
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		Minecraft.getMinecraft().renderEngine.bindTexture( GuiTextureManager.ESSENTIA_STORAGE_BUS.getTexture() );

		this.drawTexturedModalRect( this.guiLeft, this.guiTop, 0, 0, 176, 222 );

		this.drawTexturedModalRect( this.guiLeft + 179, this.guiTop, 179, 0, 32, 86 );

		if ( this.hasNetworkTool )
		{
			this.drawTexturedModalRect( this.guiLeft + 179, this.guiTop + 93, 178, 93, 68, 68 );
		}

	}

	@Override
	protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
	{
		super.drawGuiContainerForegroundLayer( mouseX, mouseY );

		boolean overlayRendered = false;

		for( int i = 0; i < 54; i++ )
		{
			this.aspectSlotList.get( i ).drawWidget();

			if ( ( !overlayRendered ) && ( this.aspectSlotList.get( i ).canRender() ) )
			{
				overlayRendered = GuiHelper.renderOverlay( this.zLevel, this.guiLeft, this.guiTop, this.aspectSlotList.get( i ), mouseX, mouseY );
			}
		}

		for( Object button : this.buttonList )
		{
			if ( ( button instanceof WidgetRedstoneModes ) )
			{
				( (WidgetRedstoneModes) button ).drawTooltip( this.guiLeft, this.guiTop );
			}
		}
	}

	protected Slot getSlotAtPosition( int x, int y )
	{
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get( i );

			if ( this.isMouseOverSlot( slot, x, y ) )
			{
				return slot;
			}
		}

		return null;
	}

	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
	{
		Slot slot = this.getSlotAtPosition( mouseX, mouseY );

		if ( ( slot != null ) && ( slot.getStack() != null ) && ( slot.getStack().isItemEqual( AEApi.instance().items().itemNetworkTool.stack( 1 ) ) ) )
		{
			return;
		}

		super.mouseClicked( mouseX, mouseY, mouseButton );

		for( WidgetAspectSlot aspectSlot : this.aspectSlotList )
		{
			if ( GuiHelper.isPointInGuiRegion( aspectSlot.getPosX(), aspectSlot.getPosY(), 18, 18, mouseX, mouseY, this.guiLeft, this.guiTop ) )
			{
				// Get the aspect of the currently held item
				Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( this.player.inventory.getItemStack() );

				// Is there an aspect?
				if ( itemAspect != null )
				{
					// Are we already filtering for this aspect?
					if ( this.filteredAspects.contains( itemAspect ) )
					{
						// Ignore
						return;
					}

				}

				aspectSlot.mouseClicked( itemAspect );

				break;
			}
		}
	}

	public void shiftClick( ItemStack itemStack )
	{
		Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( itemStack );

		if ( itemAspect != null )
		{
			// Are we already filtering this aspect?
			if ( this.filteredAspects.contains( itemAspect ) )
			{
				return;
			}

			for( WidgetAspectSlot aspectSlot : this.aspectSlotList )
			{
				if ( aspectSlot.canRender() && ( aspectSlot.getAspect() == null ) )
				{
					aspectSlot.mouseClicked( itemAspect );
					return;
				}
			}
		}
	}

	@Override
	public void updateAspects( List<Aspect> aspectList )
	{
		int count = Math.min( this.aspectSlotList.size(), aspectList.size() );

		for( int i = 0; i < count; i++ )
		{
			this.aspectSlotList.get( i ).setAspect( aspectList.get( i ) );
		}

		this.filteredAspects = aspectList;

	}

}
