package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.items.ItemEssentiaCell;
import thaumicenergistics.registries.ItemEnum;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

// TODO: Drop legacy support at version 1.0

public class HandlerItemEssentiaCell
	implements IMEInventoryHandler<IAEFluidStack>
{
	@Deprecated
	private static final String NBT_LEGACY_FLUID_NUMBER_KEY = "Fluid#";
	@Deprecated
	private static final String NBT_LEGACY_PREFORMATTED_FLUID_NUMBER_KEY = "PreformattedFluidName#";
	private static final String NBT_ESSENTIA_NUMBER_KEY = "Essentia#";
	private static final String NBT_SORT_KEY = "SortMode";

	/**
	 * Old fluid <-> essentia conversion multiplier.
	 */
	@Deprecated
	private static final long LEGACY_CONVERSION_MULTIPLIER = 250;

	/**
	 * Controls how many essentia can fit in a single byte.
	 */
	private static final long ESSENTIA_PER_BYTE = 2;

	/**
	 * Stores cell data
	 */
	private NBTTagCompound cellData;

	/**
	 * Essentia stored on the cell.
	 */
	private AspectStack[] storedEssentia;

	/**
	 * Total number of essentia types the cell can store.
	 */
	private int totalTypes;

	/**
	 * Total number of bytes the cell can store.
	 */
	private long totalBytes;

	/**
	 * Total number of essentia the cell can hold.
	 */
	private long totalEssentiaStorage;

	/**
	 * Number of essentia stored on the cell.
	 */
	private long usedEssentiaStorage = 0;

	/**
	 * Sorting mode when used in ME chest.
	 */
	private ComparatorMode sortMode;

	public HandlerItemEssentiaCell( final ItemStack storageStack )
	{
		// Ensure we have a NBT tag
		if( !storageStack.hasTagCompound() )
		{
			storageStack.setTagCompound( new NBTTagCompound() );
		}

		// Get the NBT tag
		this.cellData = storageStack.getTagCompound();

		// Get the total types we can store
		this.totalTypes = ( (ItemEssentiaCell)ItemEnum.ESSENTIA_CELL.getItem() ).maxTypes( storageStack );

		// Get the total bytes we can hold
		this.totalBytes = ( (ItemEssentiaCell)ItemEnum.ESSENTIA_CELL.getItem() ).maxStorage( storageStack );

		// Calculate how many essentia we can hold
		this.totalEssentiaStorage = this.totalBytes * HandlerItemEssentiaCell.ESSENTIA_PER_BYTE;

		// Setup the storage
		this.storedEssentia = new AspectStack[this.totalTypes];

		// Load stored essentia from data
		for( int index = 0; index < this.totalTypes; index++ )
		{
			// Is there a essentia tag?
			if( this.cellData.hasKey( HandlerItemEssentiaCell.NBT_ESSENTIA_NUMBER_KEY + index ) )
			{
				// Set the storage
				this.storedEssentia[index] = AspectStack.loadAspectStackFromNBT( this.cellData
								.getCompoundTag( HandlerItemEssentiaCell.NBT_ESSENTIA_NUMBER_KEY + index ) );

				// Update the stored amount
				this.usedEssentiaStorage += this.storedEssentia[index].amount;
			}
		}

		// Load legacy essentia from data
		for( int index = 0; index < this.totalTypes; index++ )
		{
			// Is there a legacy tag?
			if( this.cellData.hasKey( HandlerItemEssentiaCell.NBT_LEGACY_FLUID_NUMBER_KEY + index ) )
			{
				this.loadLegacyFluid( HandlerItemEssentiaCell.NBT_LEGACY_FLUID_NUMBER_KEY + index );
			}
		}

		// Clear legacy preformat data
		for( int index = 0; index < 63; index++ )
		{
			if( this.cellData.hasKey( HandlerItemEssentiaCell.NBT_LEGACY_PREFORMATTED_FLUID_NUMBER_KEY + index ) )
			{
				this.cellData.removeTag( HandlerItemEssentiaCell.NBT_LEGACY_PREFORMATTED_FLUID_NUMBER_KEY + index );
			}
		}

		// Load the sort mode
		if( this.cellData.hasKey( HandlerItemEssentiaCell.NBT_SORT_KEY ) )
		{
			this.sortMode = ComparatorMode.values()[this.cellData.getInteger( HandlerItemEssentiaCell.NBT_SORT_KEY )];
		}
		else
		{
			this.sortMode = ComparatorMode.MODE_ALPHABETIC;
		}
	}

	/**
	 * Attempts to add the essentia to the cell
	 * 
	 * @param aspect
	 * @param Amount
	 * @return Amount not stored
	 */
	private long addEssentiaToCell( final Aspect aspect, final long amount, final Actionable mode )
	{
		// Calculate amount to store
		long amountToStore = Math.min( amount, this.totalEssentiaStorage - this.usedEssentiaStorage );

		// Ensure we can store any
		if( amountToStore == 0 )
		{
			// Cell is full
			return amount;
		}

		// Get the slot for this aspect
		int slotIndex = this.getSlotFor( aspect );

		// Ensure there is somewhere to put the essentia
		if( slotIndex == -1 )
		{
			return amount;
		}

		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Get the slot
			AspectStack stackToAddTo = this.storedEssentia[slotIndex];

			// Is the slot null?
			if( stackToAddTo == null )
			{
				// Create the stack
				stackToAddTo = new AspectStack( aspect, 0 );

				// Store it
				this.storedEssentia[slotIndex] = stackToAddTo;
			}

			// Add to the stack
			stackToAddTo.amount += amountToStore;

			// Adjust the used amount
			this.usedEssentiaStorage += amountToStore;

			// Write the changes to the data tag
			this.writeChanges( slotIndex, stackToAddTo );
		}

		// Return the amount we could not store
		return( amount - amountToStore );

	}

	/**
	 * Attempts to extract essentia from the cell.
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @return Amount extracted.
	 */
	private long extractEssentiaFromCell( final Aspect aspect, final long amount, final Actionable mode )
	{
		// Do we have this essentia stored?
		int slotIndex = this.getSlotFor( aspect );
		if( ( slotIndex == -1 ) || ( this.storedEssentia[slotIndex] == null ) )
		{
			// Not stored.
			return 0;
		}

		// Get the slot
		AspectStack slotToExtractFrom = this.storedEssentia[slotIndex];

		// Calculate the amount to extract
		long amountToExtract = Math.min( slotToExtractFrom.amount, amount );

		// Are we modulating?
		if( mode == Actionable.MODULATE )
		{
			// Extract from the slot
			slotToExtractFrom.amount -= amountToExtract;

			// Is the slot now empty?
			if( slotToExtractFrom.amount == 0 )
			{
				// Null it
				slotToExtractFrom = null;

				// Update the storage
				this.storedEssentia[slotIndex] = null;
			}

			// Adjust the used amount
			this.usedEssentiaStorage -= amountToExtract;

			// Sync the data tag
			this.writeChanges( slotIndex, slotToExtractFrom );

		}

		return amountToExtract;
	}

	/**
	 * Finds the first matching, or empty slot and return its index.
	 * 
	 * @param aspect
	 * @return -1 if no match or empty slot found
	 */
	private int getSlotFor( final Aspect aspect )
	{
		int slot = -1;

		// Look for a match
		for( int index = 0; index < this.totalTypes; index++ )
		{
			// Get the stack
			AspectStack internalStack = this.storedEssentia[index];

			// Is the slot empty?
			if( internalStack == null )
			{
				// Is this the first empty slot we have encountered?
				if( slot == -1 )
				{
					// Set this as the empty slot to add to if no match is found.
					slot = index;
				}
				continue;
			}

			// Do the aspects match?
			if( internalStack.aspect == aspect )
			{
				// Found a match
				slot = index;

				// Stop searching
				break;
			}
		}

		return slot;
	}

	/**
	 * Adds a legacy fluid stored in the NBT data to the cell.
	 * 
	 * @param cellData
	 * @param key
	 */
	@Deprecated
	private void loadLegacyFluid( final String key )
	{
		// Load the fluid stack
		FluidStack legacyFluid = FluidStack.loadFluidStackFromNBT( this.cellData.getCompoundTag( key ) );

		// Remove the legacy key
		this.cellData.removeTag( key );

		// Ensure the fluid is not null
		if( legacyFluid == null )
		{
			return;
		}
		// Ensure the fluid is a valid essentia gas
		if( !( legacyFluid.getFluid() instanceof GaseousEssentia ) )
		{
			return;
		}

		// Convert to gas
		GaseousEssentia gas = (GaseousEssentia)legacyFluid.getFluid();

		// Get it's aspect
		Aspect aspect = gas.getAssociatedAspect();

		// Get the amount, and convert
		long amount = legacyFluid.amount / HandlerItemEssentiaCell.LEGACY_CONVERSION_MULTIPLIER;

		// Add to the cell
		this.addEssentiaToCell( aspect, amount, Actionable.MODULATE );

	}

	/**
	 * Synchronizes the data tag to the changed slot.
	 * 
	 * @param slotIndex
	 * @param fluidStack
	 */
	private void writeChanges( final int slotIndex, final AspectStack aspectStack )
	{
		// Create a new NBT
		NBTTagCompound essentiaTag = new NBTTagCompound();

		// Is there data to write?
		if( ( aspectStack != null ) && ( aspectStack.aspect != null ) && ( aspectStack.amount > 0 ) )
		{
			// Write the essentia to the tag
			aspectStack.writeToNBT( essentiaTag );

			// Update the data tag
			this.cellData.setTag( HandlerItemEssentiaCell.NBT_ESSENTIA_NUMBER_KEY + slotIndex, essentiaTag );
		}
		else
		{
			// Remove the tag, as it is now empty
			this.cellData.removeTag( HandlerItemEssentiaCell.NBT_ESSENTIA_NUMBER_KEY + slotIndex );
		}

	}

	/**
	 * Checks if the cell can accept/store the fluid.
	 */
	@Override
	public boolean canAccept( final IAEFluidStack input )
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

		// Return if there is a match or empty slot for the essentia
		return( -1 != this.getSlotFor( ( (GaseousEssentia)inputFluid ).getAssociatedAspect() ) );
	}

	/**
	 * Attempts to extract essentia gas from the cell.
	 */
	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final BaseActionSource src )
	{
		// Ensure there is a request
		if( ( request == null ) || ( request.getFluid() == null ) )
		{
			// Empty request.
			return null;
		}

		// Ensure the request is a valid gas
		if( !( request.getFluid() instanceof GaseousEssentia ) )
		{
			// Invalid fluid.
			return null;
		}

		// Get the aspect of the essentia
		Aspect requestAspect = ( (GaseousEssentia)request.getFluid() ).getAssociatedAspect();

		// Calculate the amount of essentia to extract
		long essentiaAmountRequested = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( request.getStackSize() );

		// Is the requested amount a whole essentia?
		if( essentiaAmountRequested == 0 )
		{
			// Can not extract partial amounts
			return null;
		}

		// Extract
		long extractedEssentiaAmount = this.extractEssentiaFromCell( requestAspect, essentiaAmountRequested, mode );

		// Did we extract any?
		if( extractedEssentiaAmount == 0 )
		{
			// Nothing extracted
			return null;
		}

		// Copy the request
		IAEFluidStack extractedFluid = request.copy();

		// Set the amount extracted
		extractedFluid.setStackSize( EssentiaConversionHelper.instance.convertEssentiaAmountToFluidAmount( extractedEssentiaAmount ) );

		return extractedFluid;

	}

	/**
	 * Mode required to access the cell.
	 */
	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	/**
	 * Gets the list of essentia gasses stored on the cell.
	 */
	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList<IAEFluidStack> availableList )
	{
		for( AspectStack essentiaStack : this.storedEssentia )
		{
			// Skip if null
			if( essentiaStack == null )
			{
				continue;
			}

			// Get the gas
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( essentiaStack.aspect );

			// Create the AE fluid stack
			availableList.add( EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( essentiaGas, essentiaStack.amount ) );

		}

		return availableList;
	}

	/**
	 * Which storage channel this cell is on.
	 */
	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

	/**
	 * Convenience function for determining how many bytes are free.
	 * 
	 * @return
	 */
	public long getFreeBytes()
	{
		return this.totalBytes - this.getUsedBytes();
	}

	/**
	 * TODO: Implement this
	 */
	@Override
	public int getPriority()
	{
		return 0;
	}

	/**
	 * No idea.
	 */
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

	/**
	 * Gets a list of the stored essentia on this cell.
	 * 
	 * @return
	 */
	public List<AspectStack> getStoredEssentia()
	{
		// Make the list
		List<AspectStack> storedList = new ArrayList<AspectStack>( this.totalTypes );

		// Add each non-null stack
		for( AspectStack stack : this.storedEssentia )
		{
			if( stack != null )
			{
				storedList.add( stack );
			}
		}

		return storedList;
	}

	/**
	 * Total number of bytes the cell can hold.
	 * 
	 * @return
	 */
	public long getTotalBytes()
	{
		return this.totalBytes;
	}

	/**
	 * Total number of types the cell can hold.
	 * 
	 * @return
	 */
	public int getTotalTypes()
	{
		return this.totalTypes;
	}

	/**
	 * Returns how many bytes are used.
	 * 
	 * @return
	 */
	public long getUsedBytes()
	{
		return this.usedEssentiaStorage / HandlerItemEssentiaCell.ESSENTIA_PER_BYTE;
	}

	/**
	 * Returns how many types are used.
	 * 
	 * @return
	 */
	public int getUsedTypes()
	{
		// Assume we are empty
		int typeCount = 0;

		// Count the number of valid types
		for( AspectStack stack : this.storedEssentia )
		{
			if( stack != null )
			{
				typeCount++ ;
			}
		}

		// Return the count
		return typeCount;
	}

	/**
	 * Attempts to add essentia gas to the cell.
	 */
	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final BaseActionSource src )
	{
		// Ensure we have an input.
		if( ( input == null ) || ( input.getFluid() == null ) )
		{
			// No input
			return input;
		}

		// Ensure the input is a gas
		if( !( input.getFluid() instanceof GaseousEssentia ) )
		{
			// Invalid fluid
			return input;
		}

		// Get the aspect of the gas
		Aspect essentiaAspect = ( (GaseousEssentia)input.getFluid() ).getAssociatedAspect();

		// Calculate the amount to store
		long amountToStore = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( input.getStackSize() );

		// Is the amount a whole essentia?
		if( amountToStore == 0 )
		{
			// Can not store partial amounts.
			return input;
		}

		// Get the amount not stored
		long amountNotStored = this.addEssentiaToCell( essentiaAspect, amountToStore, mode );

		// Did we store it all?
		if( amountNotStored == 0 )
		{
			// All was stored
			return null;
		}

		// Copy the input
		IAEFluidStack result = input.copy();

		// Set the size to how much was left over
		result.setStackSize( EssentiaConversionHelper.instance.convertEssentiaAmountToFluidAmount( amountNotStored ) );

		return result;
	}

	/**
	 * TODO: Implement this
	 * 
	 * @return
	 */
	public boolean isPreformatted()
	{
		/*
		for( Fluid currentFluid : this.prioritizedFluids )
		{
			if( currentFluid != null )
			{
				return true;
			}
		}
		*/

		return false;
	}

	/**
	 * TODO: Implement this.
	 */
	@Override
	public boolean isPrioritized( final IAEFluidStack input )
	{
		//return ( input != null ) && ( this.prioritizedFluids.contains( input.getFluid() ) );
		return false;
	}

	/**
	 * Sets the stored sorting mode.
	 * 
	 * @param sortMode
	 */
	public void setSortingMode( final ComparatorMode sortMode )
	{
		// Store the mode
		this.cellData.setInteger( HandlerItemEssentiaCell.NBT_SORT_KEY, sortMode.ordinal() );

		// Set the mode
		this.sortMode = sortMode;
	}

	/**
	 * No idea.
	 */
	@Override
	public boolean validForPass( final int pass )
	{
		return true;
	}

}
