package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.registries.ItemEnum;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public class HandlerItemEssentiaCell
	implements IMEInventoryHandler<IAEFluidStack>
{
	private static final String NBT_FLUID_NUMBER_KEY = "Fluid#";
	private static final String NBT_PREFORMATTED_FLUID_NUMBER_KEY = "PreformattedFluidName#";
	private static final String NBT_SORT_KEY = "SortMode";

	private NBTTagCompound stackTag;

	private ArrayList<FluidStack> fluidStacks = new ArrayList<FluidStack>();

	private ArrayList<Fluid> prioritizedFluids = new ArrayList<Fluid>();

	private int totalTypes;

	private long totalBytes;

	private ComparatorMode sortMode;

	public HandlerItemEssentiaCell( ItemStack storageStack )
	{
		// Ensure we have a NBT tag
		if( !storageStack.hasTagCompound() )
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
			if( priorityFluid != null )
			{
				// Add to the priority list
				this.prioritizedFluids.add( priorityFluid );
			}
		}

		// Load the sort mode
		if( this.stackTag.hasKey( HandlerItemEssentiaCell.NBT_SORT_KEY ) )
		{
			this.sortMode = ComparatorMode.values()[this.stackTag.getInteger( HandlerItemEssentiaCell.NBT_SORT_KEY )];
		}
		else
		{
			this.sortMode = ComparatorMode.MODE_ALPHABETIC;
		}
	}

	private boolean preformattedOrContainsFluid( Fluid fluid )
	{
		// Are there any prioritized fluids?
		for( Fluid currentFluid : this.prioritizedFluids )
		{
			if( currentFluid != null )
			{
				return this.prioritizedFluids.contains( currentFluid );
			}
		}

		return true;
	}

	private void writeFluidToSlot( int slotID, FluidStack fluidStack )
	{
		NBTTagCompound fluidNBT = new NBTTagCompound();

		if( ( fluidStack != null ) && ( fluidStack.getFluid() != null ) && ( fluidStack.amount > 0 ) )
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
		if( input == null )
		{
			return false;
		}

		// Get the fluid
		Fluid inputFluid = input.getFluid();

		// Is the fluid an essentia gas?
		if( !( inputFluid instanceof GaseousEssentia ) )
		{
			return false;
		}

		// Search for empty slots or matches
		for( FluidStack fluidStack : this.fluidStacks )
		{
			if( ( fluidStack == null ) || ( inputFluid.getID() == fluidStack.getFluid().getID() ) )
			{
				return this.preformattedOrContainsFluid( inputFluid );
			}
		}

		return false;
	}

	@Override
	public IAEFluidStack extractItems( IAEFluidStack request, Actionable mode, BaseActionSource src )
	{
		// Is there no request, or we do not have the fluid, return.
		if( ( request == null ) || ( !this.preformattedOrContainsFluid( request.getFluid() ) ) )
		{
			return null;
		}

		// Loop over all stored fluids
		for( int slotNumber = 0; slotNumber < this.fluidStacks.size(); slotNumber++ )
		{
			// Get the stack
			FluidStack currentStack = this.fluidStacks.get( slotNumber );

			// Is the stack valid and does it match the requested fluid?
			if( ( currentStack != null ) && ( currentStack.fluidID == request.getFluid().getID() ) )
			{
				// Calculate the amount left over after the extraction
				long endAmount = currentStack.amount - request.getStackSize();

				IAEFluidStack removedStack;

				// Is there any left over?
				if( endAmount > 0L )
				{
					// Copy the request
					removedStack = request.copy();

					// Get the fluid stack to write back into the cell
					FluidStack toWrite = new FluidStack( currentStack.fluidID, (int)endAmount );

					//currentFluids.set( i, toWrite );

					// Are we modulating?
					if( mode == Actionable.MODULATE )
					{
						// Write the fluid to the slot
						this.writeFluidToSlot( slotNumber, toWrite );
					}
				}
				else
				{
					// None left over, returned stack will be all of the current stack
					removedStack = AEApi.instance().storage().createFluidStack( currentStack.copy() );

					// Are we modulating?
					if( mode == Actionable.MODULATE )
					{
						// Set the slot as empty
						this.writeFluidToSlot( slotNumber, null );
					}
				}

				// Return the fluid stack we were able to remove
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
			if( stack != null )
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

	public List<AspectStack> getAvailableAspects()
	{
		List<AspectStack> aspectList = new ArrayList<AspectStack>( this.fluidStacks.size() );

		for( FluidStack fluidStack : this.fluidStacks )
		{
			if( fluidStack != null )
			{
				aspectList.add( new AspectStack( ( (GaseousEssentia)fluidStack.getFluid() ).getAssociatedAspect(), EssentiaConversionHelper.instance
								.convertFluidAmountToEssentiaAmount( fluidStack.amount ) ) );
			}
		}

		return aspectList;
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

	/**
	 * Gets the stored sorting mode.
	 * 
	 * @return
	 */
	public ComparatorMode getSortingMode()
	{
		return this.sortMode;
	}

	@Override
	public IAEFluidStack injectItems( IAEFluidStack input, Actionable mode, BaseActionSource src )
	{
		// Is the input empty, or can we not accept this fluid?
		if( ( input == null ) || ( !this.canAccept( input ) ) )
		{
			return input;
		}

		// Assume we can not add anything
		IAEFluidStack notAdded = input.copy();

		// Cache the free bytes amount
		long cFreeBytes = this.freeBytes();

		int storeInSlot = -1;

		// Search for: #1 a matching fluid. #2 The first empty slot.
		for( int fluidSlotIndex = 0; fluidSlotIndex < this.fluidStacks.size(); fluidSlotIndex++ )
		{
			// Get this slot
			FluidStack currentStack = this.fluidStacks.get( fluidSlotIndex );

			// Is the slot empty?
			if( currentStack == null )
			{
				// Have we have not assigned a slot?
				if( storeInSlot == -1 )
				{
					// Assign this slot to store into
					storeInSlot = fluidSlotIndex;

					// Keep searching, there may yet be a matching fluid
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

		// Did we find a match or empty slot?
		if( storeInSlot > -1 )
		{
			// Get the fluid we are writing
			Fluid fluidToWrite = input.getFluid();

			// Get the amount to add to the slot
			long amountToAdd = notAdded.getStackSize();

			// Does the amount to add exceed the free space?
			if( amountToAdd > cFreeBytes )
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
			if( mode == Actionable.MODULATE )
			{
				// Set how much this slot will now hold
				long newStoredAmount = amountToAdd;

				// Is there some already stored?
				if( this.fluidStacks.get( storeInSlot ) != null )
				{
					// Add in the existing amount
					newStoredAmount += this.fluidStacks.get( storeInSlot ).amount;
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
			if( currentFluid != null )
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

	/**
	 * Sets the stored sorting mode.
	 * 
	 * @param sortMode
	 */
	public void setSortingMode( ComparatorMode sortMode )
	{
		// Store the mode
		this.stackTag.setInteger( HandlerItemEssentiaCell.NBT_SORT_KEY, sortMode.ordinal() );

		// Set the mode
		this.sortMode = sortMode;
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
			if( stack != null )
			{
				typeCount++ ;
			}
		}

		return typeCount;
	}

	@Override
	public boolean validForPass( int pass )
	{
		return true;
	}

}
