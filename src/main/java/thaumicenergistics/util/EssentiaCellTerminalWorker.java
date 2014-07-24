package thaumicenergistics.util;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerCellTerminalBase;
import thaumicenergistics.fluids.GaseousEssentia;
import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;

public class EssentiaCellTerminalWorker
{
	protected static void decreaseInputSlot( PrivateInventory inventory )
	{
		ItemStack slot = inventory.getStackInSlot( ContainerCellTerminalBase.INPUT_SLOT_ID );

		slot.stackSize -= 1;

		if ( slot.stackSize <= 0 )
		{
			inventory.setInventorySlotContents( ContainerCellTerminalBase.INPUT_SLOT_ID, null );
			
			inventory.markDirty();
		}
	}

	protected static void drainContainer( PrivateInventory inventory, IMEMonitor<IAEFluidStack> monitor, ItemStack container,
											MachineSource machineSource )
	{
		// Get the fluid stack from the item
		IAEFluidStack containerFluid = EssentiaConversionHelper.createAEFluidStackFromItemEssentiaContainer( container );

		int proposedDrainAmount_FU = (int) containerFluid.getStackSize();

		// Simulate an injection
		IAEFluidStack notInjected = monitor.injectItems( containerFluid, Actionable.SIMULATE, machineSource );

		// Get how much was rejected
		if ( notInjected != null )
		{
			// Decrease the proposed amount
			proposedDrainAmount_FU -= (int) notInjected.getStackSize();

			// Can the network accept any?
			if ( proposedDrainAmount_FU <= 0 )
			{
				// Network full
				return;
			}
		}

		// Convert to Essentia units
		int proposedDrainAmount_EU = (int) EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( proposedDrainAmount_FU );

		// Attempt to drain the item
		ImmutablePair<Integer, ItemStack> drainedContainer = EssentiaItemContainerHelper.extractFromContainer( container, proposedDrainAmount_EU );

		// Was the drain successful?
		if ( drainedContainer == null )
		{
			return;
		}

		if ( EssentiaCellTerminalWorker.fillOutputSlot( inventory, drainedContainer.getRight() ) )
		{
			// Adjust the fill amount
			containerFluid.setStackSize( EssentiaConversionHelper.convertEssentiaAmountToFluidAmount( drainedContainer.getLeft() ) );

			// Fill to network
			monitor.injectItems( containerFluid, Actionable.MODULATE, machineSource );

			EssentiaCellTerminalWorker.decreaseInputSlot( inventory );
		}
	}

	protected static void fillContainer( PrivateInventory inventory, IMEMonitor<IAEFluidStack> monitor, ItemStack container,
											MachineSource machineSource, Aspect currentAspect )
	{
		// Is there an aspect selected?
		if ( currentAspect == null )
		{
			return;
		}

		// Get the available capacity of the container, in Essentia Units
		int containerCapacity_EU = EssentiaItemContainerHelper.getContainerCapacity( container );

		// Is there any room for more essentia?
		if ( containerCapacity_EU == 0 )
		{
			return;
		}

		// Get the gas form of the essentia
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( currentAspect );

		// Simulate an extraction from the network
		IAEFluidStack result = monitor.extractItems( EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, containerCapacity_EU ),
			Actionable.SIMULATE, machineSource );

		// Is there anything to extract?
		if ( result == null )
		{
			return;
		}

		// Get how much can be taken from the network, in Essentia Units
		int resultAmount_EU = (int) EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( result.getStackSize() );

		// Calculate the proposed amount, based on how much we need and how much
		// is available
		int proposedAmount_EU = Math.min( containerCapacity_EU, resultAmount_EU );

		// Create a new container filled to the proposed amount
		ImmutablePair<Integer, ItemStack> filledContainer = EssentiaItemContainerHelper.injectIntoContainer( container, new AspectStack(
						currentAspect, proposedAmount_EU ) );

		// Was the fill successful?
		if ( filledContainer == null )
		{
			return;
		}

		// Can we move the container from the first to the second slot?
		if ( EssentiaCellTerminalWorker.fillOutputSlot( inventory, filledContainer.getRight() ) )
		{
			// Drain the essentia from the network
			monitor.extractItems( EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, filledContainer.getLeft() ),
				Actionable.MODULATE, machineSource );

			// Decrease the stack in the first slot
			EssentiaCellTerminalWorker.decreaseInputSlot( inventory );
		}
	}

	protected static boolean fillOutputSlot( PrivateInventory inventory, ItemStack itemStack )
	{
		if ( itemStack == null )
		{
			return false;
		}

		ItemStack secondSlot = inventory.getStackInSlot( ContainerCellTerminalBase.OUTPUT_SLOT_ID );

		if ( secondSlot == null )
		{
			inventory.setInventorySlotContents( ContainerCellTerminalBase.OUTPUT_SLOT_ID, itemStack );
			
			inventory.markDirty();

			return true;
		}

		if ( ( !secondSlot.isItemEqual( itemStack ) ) || ( !ItemStack.areItemStackTagsEqual( itemStack, secondSlot ) ) )
		{
			return false;
		}

		if( inventory.incrStackSize( ContainerCellTerminalBase.OUTPUT_SLOT_ID, itemStack.stackSize ) != null )
		{
			inventory.markDirty();
			return true;
		}
		
		return false;
	}
	
	public static boolean hasWork( PrivateInventory inventory )
	{
		// Is the inventory valid?
		if ( inventory == null )
		{
			return false;
		}

		// Get the output slot
		ItemStack outputSlot = inventory.getStackInSlot( ContainerCellTerminalBase.OUTPUT_SLOT_ID );

		// Is the output slot valid and not full?
		if ( ( outputSlot != null ) && ( outputSlot.stackSize >= 64 ) )
		{
			return false;
		}

		// Is the item in the input slot an essentia container?
		if ( !EssentiaItemContainerHelper.isContainer( inventory.getStackInSlot( ContainerCellTerminalBase.INPUT_SLOT_ID ) ) )
		{
			return false;
		}
		
		return true;
	}

	public static void doWork( PrivateInventory inventory, IMEMonitor<IAEFluidStack> monitor, MachineSource machineSource, Aspect currentAspect )
	{
		// Do we have a valid monitor?
		if ( monitor == null )
		{
			return;
		}
		
		// Is there work to do?
		if( !EssentiaCellTerminalWorker.hasWork( inventory ) )
		{
			return;
		}

		// Make a copy of the input slot
		ItemStack inputSlot = inventory.getStackInSlot( ContainerCellTerminalBase.INPUT_SLOT_ID ).copy();

		// Is the container empty?
		if ( EssentiaItemContainerHelper.isContainerEmpty( inputSlot ) )
		{
			// Attempt to fill the container.
			EssentiaCellTerminalWorker.fillContainer( inventory, monitor, inputSlot, machineSource, currentAspect );
		}
		// Is the container not empty?
		else if ( EssentiaItemContainerHelper.isContainerFilled( inputSlot ) )
		{
			// Attempt to drain it.
			EssentiaCellTerminalWorker.drainContainer( inventory, monitor, inputSlot, machineSource );
		}
	}

}
