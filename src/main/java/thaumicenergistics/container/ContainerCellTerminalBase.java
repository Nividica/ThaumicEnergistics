package thaumicenergistics.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.slot.SlotRespective;
import thaumicenergistics.gui.GuiCellTerminalBase;
import thaumicenergistics.gui.IAspectSelectorContainer;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;

public abstract class ContainerCellTerminalBase
	extends ContainerWithPlayerInventory
	implements IMEMonitorHandlerReceiver<IAEFluidStack>, IAspectSelectorContainer, IInventoryUpdateReceiver
{
	protected IMEMonitor<IAEFluidStack> monitor;
	protected List<AspectStack> aspectStackList = new ArrayList<AspectStack>();
	protected Aspect selectedAspect;
	protected EntityPlayer player;
	protected GuiCellTerminalBase guiBase;
	protected PrivateInventory inventory;
	private int lastInventorySecondSlotCount = 0;
	private long lastSoundPlaytime = 0;

	public ContainerCellTerminalBase( EntityPlayer player )
	{
		this.player = player;
		
		this.lastSoundPlaytime = System.currentTimeMillis();
	}
	
	public void attachToMonitor()
	{
		if ( ( !this.player.worldObj.isRemote ) && ( this.monitor != null ) )
		{
			this.monitor.addListener( this, null );
		}
	}
		
	protected void bindToInventory( PrivateInventory inventory )
	{
		this.inventory = inventory;
		
		this.inventory.setReceiver( this );

		this.addSlotToContainer( new SlotRespective( inventory, 0, 8, 74 ) );

		this.addSlotToContainer( new SlotFurnace( this.player, inventory, 1, 26, 74 ) );

		this.bindPlayerInventory( this.player.inventory, 104, 162 );
		
	}

	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	public abstract void forceAspectUpdate();

	public List<AspectStack> getAspectStackList()
	{
		return this.aspectStackList;
	}

	public EntityPlayer getPlayer()
	{
		return this.player;
	}

	public Aspect getSelectedAspect()
	{
		return this.selectedAspect;
	}

	@Override
	public boolean isValid( Object verificationToken )
	{
		return true;
	}

	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if ( !player.worldObj.isRemote )
		{
			if ( this.monitor != null )
			{
				this.monitor.removeListener( this );
			}
		}
	}
	
	@Override
	public void onListUpdate()
	{
	}

	@Override
	public void postChange( IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		this.aspectStackList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( monitor.getStorageList() );
	}

	public abstract void receiveSelectedAspect( Aspect selectedAspect );


	public void setGui( GuiCellTerminalBase guiCommon )
	{
		if ( guiCommon != null )
		{
			this.guiBase = guiCommon;
		}
	}

	@Override
	public abstract void setSelectedAspect( Aspect selectedAspect );

	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotnumber )
	{
		ItemStack itemstack = null;

		Slot slot = (Slot) this.inventorySlots.get( slotnumber );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack itemstack1 = slot.getStack();

			itemstack = itemstack1.copy();

			if ( this.inventory.isItemValidForSlot( 0, itemstack1 ) )
			{
				if ( ( slotnumber == 1 ) || ( slotnumber == 0 ) )
				{
					if ( !this.mergeItemStack( itemstack1, 2, 36, false ) )
					{
						return null;
					}
				}
				else if ( !this.mergeItemStack( itemstack1, 0, 1, false ) )
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
			else
			{
				return null;
			}
		}

		return itemstack;
	}

	public void updateAspectList( List<AspectStack> aspectStackList )
	{
		this.aspectStackList = aspectStackList;

		if ( this.guiBase != null )
		{
			this.guiBase.updateAspects();
		}
	}
	
	
	@Override
	public void onInventoryChanged()
	{
		if( this.player.worldObj.isRemote )
		{
			// Get the itemstack in the second slot
			ItemStack itemStack = this.inventory.getStackInSlot( 1 );
			
			// Is there anything in the second slot?
			if( itemStack != null )
			{
				// Has the count changed?
				if( this.lastInventorySecondSlotCount != itemStack.stackSize )
				{
					// Has enough time passed to play the sound again?
					if( ( System.currentTimeMillis() - this.lastSoundPlaytime ) > 900 )
					{
						// Play swimy sound
						Minecraft.getMinecraft().getSoundHandler()
						.playSound( PositionedSoundRecord.func_147674_a(new ResourceLocation("game.neutral.swim"), 1.0F) );
						
						// Set the playtime
						this.lastSoundPlaytime = System.currentTimeMillis();
					}
					
					// Set the count
					this.lastInventorySecondSlotCount = itemStack.stackSize;
				}
			}
			else
			{
				this.lastInventorySecondSlotCount = 0;
			}
		}
	}

}
