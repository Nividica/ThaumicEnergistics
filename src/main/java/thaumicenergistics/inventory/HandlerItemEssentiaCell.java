package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.util.EssentiaConversionHelper;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.Lists;

public class HandlerItemEssentiaCell
	implements IMEInventoryHandler<IAEFluidStack>
{
	private static final String NBT_FLUID_NUMBER_KEY = "Fluid#";
	private static final String NBT_PREFORMATTED_FLUID_NUMBER_KEY = "PreformattedFluidName#";

	private NBTTagCompound stackTag;
	private ArrayList<FluidStack> fluidStacks = new ArrayList<FluidStack>();
	private ArrayList<Fluid> prioritizedFluids = new ArrayList<Fluid>();
	private int totalTypes;
	private long totalBytes;

	public HandlerItemEssentiaCell( ItemStack storageStack )
	{
		// Ensure we have a NBT tag
		if ( !storageStack.hasTagCompound() )
		{
			storageStack.setTagCompound( new NBTTagCompound() );
		}

		// Get the NBT tag
		this.stackTag = storageStack.getTagCompound();

		// Get the total types we can store
		this.totalTypes = ( (ItemEssentiaCell)ItemEnum.ESSENTIA_CELL.getItem() ).maxTypes( storageStack );

		// Get the total bytes we can hold
		this.totalBytes = ( ( (ItemEssentiaCell)ItemEnum.ESSENTIA_CELL.getItem() ).maxStorage( storageStack ) ) * ItemEssentiaCell.CONVERSION_SIZE;

		// Load the fluids from the NBT tag
		for( int i = 0; i < this.totalTypes; i++ )
		{
			this.fluidStacks.add( FluidStack.loadFluidStackFromNBT( this.stackTag.getCompoundTag( HandlerItemEssentiaCell.NBT_FLUID_NUMBER_KEY + i ) ) );
		}

		// Load the pre-formated list
		for( int i = 0; i < 63; i++ )
		{
			// Get the fluid
			Fluid priorityFluid = FluidRegistry.getFluid( this.stackTag.getString( HandlerItemEssentiaCell.NBT_PREFORMATTED_FLUID_NUMBER_KEY + i ) );

			// Ensure it is not null
			if ( priorityFluid != null )
			{
				// Add to the priority list
				this.prioritizedFluids.add( priorityFluid );
			}
		}
	}

	private boolean preformattedOrContainsFluid( Fluid fluid )
	{
		// Are there any prioritized fluids?
		for( Fluid currentFluid : this.prioritizedFluids )
		{
			if ( currentFluid != null )
			{
				return this.prioritizedFluids.contains( currentFluid );
			}
		}

		return true;
	}

	private void writeFluidToSlot( int slotID, FluidStack fluidStack )
	{
		NBTTagCompound fluidNBT = new NBTTagCompound();

		if ( ( fluidStack != null ) && ( fluidStack.getFluid() != null ) && ( fluidStack.amount > 0 ) )
		{
			fluidStack.writeToNBT( fluidNBT );

			this.stackTag.setTag( HandlerItemEssentiaCell.NBT_FLUID_NUMBER_KEY + slotID, fluidNBT );
		}
		else
		{
			this.stackTag.removeTag( HandlerItemEssentiaCell.NBT_FLUID_NUMBER_KEY + slotID );
		}

		this.fluidStacks.set( slotID, fluidStack );
	}

	@Override
	public boolean canAccept( IAEFluidStack input )
	{
		// Ensure there is an input
		if ( input == null )
		{
			return false;
		}

		// Get the fluid
		Fluid inputFluid = input.getFluid();

		// Is the fluid an essentia gas?
		if ( !( inputFluid instanceof GaseousEssentia ) )
		{
			return false;
		}

		// Search for empty slots or matches
		for( FluidStack fluidStack : this.fluidStacks )
		{
			if ( ( fluidStack == null ) || ( inputFluid.getID() == fluidStack.getFluid().getID() ) )
			{
				return this.preformattedOrContainsFluid( inputFluid );
			}
		}

		return false;
	}

	@Override
	public IAEFluidStack extractItems( IAEFluidStack request, Actionable mode, BaseActionSource src )
	{
		if ( ( request == null ) || ( !preformattedOrContainsFluid( request.getFluid() ) ) )
		{
			return null;
		}
		
		List<FluidStack> currentFluids = Lists.newArrayList( this.fluidStacks );
		
		for( int i = 0; i < this.fluidStacks.size(); i++ )
		{
			FluidStack currentStack = this.fluidStacks.get( i );
			
			if ( ( currentStack != null ) && ( currentStack.fluidID == request.getFluid().getID() ) )
			{
				long endAmount = currentStack.amount - request.getStackSize();
				IAEFluidStack removedStack;
				if ( endAmount > 0L )
				{
					removedStack = request.copy();
					FluidStack toWrite = new FluidStack( currentStack.fluidID, (int)endAmount );
					currentFluids.set( i, toWrite );
					if ( mode == Actionable.MODULATE )
					{
						writeFluidToSlot( i, toWrite );
					}
				}
				else
				{
					removedStack = AEApi.instance().storage().createFluidStack( currentStack.copy() );
					if ( mode == Actionable.MODULATE )
					{
						writeFluidToSlot( i, null );
					}
				}
				return removedStack;
			}
		}
		
		return null;
	}

	public long freeBytes()
	{
		// Assume we are empty
		long storedFluidAmount_FU = 0;

		// Get how many mb of fluid we have stored
		for( FluidStack stack : this.fluidStacks )
		{
			if ( stack != null )
			{
				storedFluidAmount_FU += stack.amount;
			}
		}

		return this.totalBytes - storedFluidAmount_FU;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems( IItemList<IAEFluidStack> out )
	{
		for( FluidStack fluidStack : this.fluidStacks )
		{
			out.add( AEApi.instance().storage().createFluidStack( fluidStack ) );
		}

		return out;
	}

	public List<AspectStack> getAvailableAspects()
	{
		List<AspectStack> aspectList = new ArrayList<AspectStack>( this.fluidStacks.size() );

		for( FluidStack fluidStack : this.fluidStacks )
		{
			if ( fluidStack != null )
			{
				aspectList.add( new AspectStack( ( (GaseousEssentia)fluidStack.getFluid() ).getAssociatedAspect(), EssentiaConversionHelper
								.convertFluidAmountToEssentiaAmount( fluidStack.amount ) ) );
			}
		}

		return aspectList;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public IAEFluidStack injectItems( IAEFluidStack input, Actionable mode, BaseActionSource src )
	{
		if ( ( input == null ) || ( !this.canAccept( input ) ) )
		{
			return input;
		}

		IAEFluidStack notAdded = input.copy();

		List<FluidStack> currentFluids = Lists.newArrayList( this.fluidStacks );

		// Cache the free bytes amount
		long cFreeBytes = this.freeBytes();
		
		int storeInSlot = -1;

		// Loop over all fluid slots
		for( int fluidSlotIndex = 0; fluidSlotIndex < currentFluids.size(); fluidSlotIndex++ )
		{
			// Get this slot
			FluidStack currentStack = currentFluids.get( fluidSlotIndex );

			// Is the slot empty?
			if( currentStack == null )
			{
				// Have we have not assigned a slot?
				if( storeInSlot == -1 )
				{
					// Assign this slot to store into
					storeInSlot = fluidSlotIndex;
				}
			}
			else
			{
				// Do the types match?
				if( ( input.getFluid().getID() == currentStack.fluidID ) )
				{
					// Store in this slot
					storeInSlot = fluidSlotIndex;
					
					// Stop searching
					break;
				}
			}
		}
		
		if( storeInSlot > -1 )
		{
			// Get the fluid we are writing
			Fluid fluidToWrite = input.getFluid();
	
			// Get the amount to add to the slot
			long amountToAdd = notAdded.getStackSize();
	
			// Does the amount to add exceed the free space?
			if ( amountToAdd > cFreeBytes )
			{
				// Adjust the amount not added
				notAdded.setStackSize( amountToAdd - cFreeBytes );
	
				// Adjust the amount to add
				amountToAdd = cFreeBytes;
			}
			else
			{
				// We can write the full amount
				notAdded = null;
			}
	
			// Are we not simulating?
			if ( mode == Actionable.MODULATE )
			{
				// Set how much this slot will now hold
				long newStoredAmount = amountToAdd;
				
				// Is there some already stored?
				if( this.fluidStacks.get( storeInSlot ) != null )
				{
					// Add in the existing amount
					newStoredAmount+= this.fluidStacks.get( storeInSlot ).amount;
				}
		
				// Write to the slot
				this.writeFluidToSlot( storeInSlot, new FluidStack( fluidToWrite, (int)newStoredAmount ) );
			}
		}

		return notAdded;
	}

	public boolean isPreformatted()
	{
		for( Fluid currentFluid : this.prioritizedFluids )
		{
			if ( currentFluid != null )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isPrioritized( IAEFluidStack input )
	{
		return ( input != null ) && ( this.prioritizedFluids.contains( input.getFluid() ) );
	}

	public long totalBytes()
	{
		return this.totalBytes;
	}

	public int totalTypes()
	{
		return this.totalTypes;
	}

	public long usedBytes()
	{
		return this.totalBytes - this.freeBytes();
	}

	public int usedTypes()
	{
		int typeCount = 0;

		for( FluidStack stack : this.fluidStacks )
		{
			if ( stack != null )
			{
				typeCount++ ;
			}
		}

		return typeCount;
	}

}
