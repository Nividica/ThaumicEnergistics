package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.container.slot.SlotNetworkTool;
import thaumicenergistics.container.slot.SlotRespective;
import thaumicenergistics.gui.GuiEssentiaStorageBus;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;

public class ContainerPartEssentiaStorageBus extends ContainerWithPlayerInventory
{

	private GuiEssentiaStorageBus guiBusAspectStorage;

	public ContainerPartEssentiaStorageBus(AEPartEssentiaStorageBus part, EntityPlayer player)
	{
		this.addSlotToContainer( new SlotRespective( part.getUpgradeInventory(), 0, 187, 8 ) );

		this.bindPlayerInventory( player.inventory, 140, 198 );

		for( int i = 0; i < player.inventory.getSizeInventory(); i++ )
		{
			ItemStack stack = player.inventory.getStackInSlot( i );

			if ( ( stack != null ) && ( stack.isItemEqual( AEApi.instance().items().itemNetworkTool.stack( 1 ) ) ) )
			{
				DimensionalCoord coord = part.getHost().getLocation();

				IGuiItem guiItem = (IGuiItem) stack.getItem();

				INetworkTool networkTool = (INetworkTool) guiItem.getGuiObject( stack, coord.getWorld(), coord.x, coord.y, coord.z );

				for( int j = 0; j < 3; j++ )
				{
					for( int k = 0; k < 3; k++ )
					{
						this.addSlotToContainer( new SlotNetworkTool( networkTool, j + ( k * 3 ), 187 + ( k * 18 ), ( j * 18 ) + 102 ) );
					}
				}

				return;
			}
		}

	}

	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	public void setGui( GuiEssentiaStorageBus guiBusAspectStorage )
	{
		this.guiBusAspectStorage = guiBusAspectStorage;
	}

	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotnumber )
	{
		if ( this.guiBusAspectStorage != null )
		{
			this.guiBusAspectStorage.shiftClick( this.getSlot( slotnumber ).getStack() );
		}

		ItemStack itemstack = null;

		Slot slot = (Slot) this.inventorySlots.get( slotnumber );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack itemstack1 = slot.getStack();

			itemstack = itemstack1.copy();

			if ( slotnumber < 36 )
			{
				if ( !this.mergeItemStack( itemstack1, 36, this.inventorySlots.size(), true ) )
				{
					return null;
				}
			}
			else if ( !this.mergeItemStack( itemstack1, 0, 36, false ) )
			{
				return null;
			}
			if ( itemstack1.stackSize == 0 )
			{
				slot.putStack( null );
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

}
