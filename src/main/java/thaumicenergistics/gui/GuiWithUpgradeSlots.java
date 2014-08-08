package thaumicenergistics.gui;

import appeng.api.AEApi;
import appeng.parts.automation.UpgradeInventory;
import thaumicenergistics.container.ContainerWithNetworkTool;
import thaumicenergistics.container.slot.SlotNetworkTool;
import thaumicenergistics.texture.AEStateIcons;
import thaumicenergistics.util.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public abstract class GuiWithUpgradeSlots
	extends GuiContainer
{

	private static int upgradeU = AEStateIcons.UPGRADE_SLOT.getU();

	private static int upgradeV = AEStateIcons.UPGRADE_SLOT.getV();

	public GuiWithUpgradeSlots( Container container )
	{
		super( container );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( float alpha, int mouseX, int mouseY )
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( AEStateIcons.AE_STATES_TEXTURE );

		// Locate any upgrade or network slots
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			// Get the slot
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			// Is it network or upgrade?
			if( ( slot instanceof SlotNetworkTool ) || ( slot.inventory instanceof UpgradeInventory ) )
			{
				// Draw background
				this.drawTexturedModalRect( this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, GuiWithUpgradeSlots.upgradeU,
					GuiWithUpgradeSlots.upgradeV, AEStateIcons.ICON_SIZE, AEStateIcons.ICON_SIZE );
			}
		}
	}
	

	/**
	 * Checks if the specified point is within the bounds of the specified slot.
	 * 
	 * @param slot
	 * @param x
	 * @param y
	 * @return True if the point is within the slot, false otherwise.
	 */
	protected boolean isPointWithinSlot( Slot slot, int x, int y )
	{
		return GuiHelper.isPointInGuiRegion( slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y, this.guiLeft, this.guiTop );
	}
	
	/**
	 * Gets the slot who contains the specified point.
	 * @param x
	 * @param y
	 * @return Slot the point is within, null if point is within no slots.
	 */
	protected Slot getSlotAtPosition( int x, int y )
	{
		// Loop over all slots
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			// Get the slot
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			// Is the point within the slot?
			if( this.isPointWithinSlot( slot, x, y ) )
			{
				// Return the slot
				return slot;
			}
		}

		// Point was not within any slot
		return null;
	}

	// TODO: Check subclasses for same check and remove
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
	{
		// Is our container one that could have a network tool?
		if( this.inventorySlots instanceof ContainerWithNetworkTool )
		{
			// Do we have a network tool?
			if( ( (ContainerWithNetworkTool)this.inventorySlots ).hasNetworkTool() )
			{
				// Get the slot the mouse was clicked over
				Slot slot = this.getSlotAtPosition( mouseX, mouseY );

				// Was the slot the network tool?
				if( ( slot != null ) && ( slot.getStack() != null ) &&
								( slot.getStack().isItemEqual( AEApi.instance().items().itemNetworkTool.stack( 1 ) ) ) )
				{
					// Do not allow any interaction with the network tool slot.
					return;
				}
			}
		}
		
		// Call super
		super.mouseClicked( mouseX, mouseY, mouseButton );
	}

}
