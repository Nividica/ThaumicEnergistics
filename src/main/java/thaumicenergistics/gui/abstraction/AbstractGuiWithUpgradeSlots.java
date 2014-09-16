package thaumicenergistics.gui.abstraction;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import thaumicenergistics.container.ContainerWithNetworkTool;
import thaumicenergistics.container.slot.SlotNetworkTool;
import thaumicenergistics.texture.EnumAEStateIcons;
import thaumicenergistics.util.GuiHelper;
import appeng.api.AEApi;
import appeng.parts.automation.UpgradeInventory;

public abstract class AbstractGuiWithUpgradeSlots
	extends AbstractGuiBase
{

	private static int upgradeU = EnumAEStateIcons.UPGRADE_SLOT.getU();

	private static int upgradeV = EnumAEStateIcons.UPGRADE_SLOT.getV();

	private static int upgradeWidth = EnumAEStateIcons.UPGRADE_SLOT.getWidth();

	private static int upgradeHeight = EnumAEStateIcons.UPGRADE_SLOT.getHeight();

	public AbstractGuiWithUpgradeSlots( final Container container )
	{
		super( container );
	}

	@Override
	protected void drawGuiContainerBackgroundLayer( final float alpha, final int mouseX, final int mouseY )
	{
		Minecraft.getMinecraft().renderEngine.bindTexture( EnumAEStateIcons.AE_STATES_TEXTURE );

		// Locate any upgrade or network slots
		for( int i = 0; i < this.inventorySlots.inventorySlots.size(); i++ )
		{
			// Get the slot
			Slot slot = (Slot)this.inventorySlots.inventorySlots.get( i );

			// Is it network or upgrade?
			if( ( slot instanceof SlotNetworkTool ) || ( slot.inventory instanceof UpgradeInventory ) )
			{
				// Draw background
				this.drawTexturedModalRect( this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition,
					AbstractGuiWithUpgradeSlots.upgradeU, AbstractGuiWithUpgradeSlots.upgradeV, AbstractGuiWithUpgradeSlots.upgradeWidth,
					AbstractGuiWithUpgradeSlots.upgradeHeight );
			}
		}
	}

	/**
	 * Gets the slot who contains the specified point.
	 * 
	 * @param x
	 * @param y
	 * @return Slot the point is within, null if point is within no slots.
	 */
	protected Slot getSlotAtPosition( final int x, final int y )
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

	/**
	 * Checks if the specified point is within the bounds of the specified slot.
	 * 
	 * @param slot
	 * @param x
	 * @param y
	 * @return True if the point is within the slot, false otherwise.
	 */
	protected boolean isPointWithinSlot( final Slot slot, final int x, final int y )
	{
		return GuiHelper.instance.isPointInGuiRegion( slot.yDisplayPosition, slot.xDisplayPosition, 16, 16, x, y, this.guiLeft, this.guiTop );
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked( final int mouseX, final int mouseY, final int mouseButton )
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
