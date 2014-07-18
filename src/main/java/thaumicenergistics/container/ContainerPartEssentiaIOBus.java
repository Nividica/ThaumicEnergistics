package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.container.slot.SlotNetworkTool;
import thaumicenergistics.container.slot.SlotRespective;
import thaumicenergistics.gui.GuiEssentiatIO;
import thaumicenergistics.parts.AEPartEssentiaIO;
import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;

public class ContainerPartEssentiaIOBus extends ContainerWithPlayerInventory
{
	private GuiEssentiatIO guiBusAspectIO;

	public ContainerPartEssentiaIOBus(AEPartEssentiaIO part, EntityPlayer player)
	{
		for( int i = 0; i < 4; i++ )
		{
			this.addSlotToContainer( new SlotRespective( part.getUpgradeInventory(), i, 187, ( i * 18 ) + 8 ) );
		}

		this.bindPlayerInventory( player.inventory, 102, 160 );

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
			}
		}
	}

	@Override
	protected void retrySlotClick( int par1, int par2, boolean par3, EntityPlayer player )
	{
	}

	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	public void setGui( GuiEssentiatIO gui )
	{
		this.guiBusAspectIO = gui;
	}

	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotId )
	{	
		if ( ( this.guiBusAspectIO != null ) && ( this.guiBusAspectIO.shiftClick( this.getSlot( slotId ).getStack() ) ) )
		{
			return ( (Slot) this.inventorySlots.get( slotId ) ).getStack();
		}

		ItemStack itemStack = null;

		Slot slot = (Slot) this.inventorySlots.get( slotId );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack slotStack = slot.getStack();

			itemStack = slotStack.copy();

			if ( slotId < 36 )
			{
				if ( !this.mergeItemStack( slotStack, 36, this.inventorySlots.size(), true ) )
				{
					return null;
				}
			}
			else if ( !this.mergeItemStack( slotStack, 0, 36, false ) )
			{
				return slotStack;
			}

			if ( slotStack.stackSize == 0 )
			{
				slot.putStack( null );
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemStack;
	}

}
