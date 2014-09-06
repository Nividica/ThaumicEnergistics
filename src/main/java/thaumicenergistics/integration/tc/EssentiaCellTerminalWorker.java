package thaumicenergistics.integration.tc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.ContainerCellTerminalBase;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;

public final class EssentiaCellTerminalWorker
{
	/**
	 * Singleton
	 */
	public static final EssentiaCellTerminalWorker instance = new EssentiaCellTerminalWorker();

	/**
	 * Private constructor
	 */
	private EssentiaCellTerminalWorker()
	{

	}

	private final void decreaseInputSlot( final PrivateInventory inventory )
	{
		ItemStack slot = inventory.getStackInSlot( ContainerCellTerminalBase.INPUT_SLOT_ID );

		slot.stackSize -= 1;

		if( slot.stackSize <= 0 )
		{
			inventory.setInventorySlotContents( ContainerCellTerminalBase.INPUT_SLOT_ID, null );

			inventory.markDirty();
		}
	}

	private final boolean drainContainer( final PrivateInventory inventory, final IMEMonitor<IAEFluidStack> monitor, final ItemStack container,
											final MachineSource machineSource )
	{
		// Get the fluid stack from the item
		IAEFluidStack containerFluid = EssentiaConversionHelper.instance.createAEFluidStackFromItemEssentiaContainer( container );

		int proposedDrainAmount_FU = (int)containerFluid.getStackSize();

		// Simulate an injection
		IAEFluidStack notInjected = monitor.injectItems( containerFluid, Actionable.SIMULATE, machineSource );

		// Get how much was rejected
		if( notInjected != null )
		{
			// Decrease the proposed amount
			proposedDrainAmount_FU -= (int)notInjected.getStackSize();

			// Can the network accept any?
			if( proposedDrainAmount_FU <= 0 )
			{
				// Network full
				return false;
			}
		}

		// Convert to Essentia units
		int proposedDrainAmount_EU = (int)EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( proposedDrainAmount_FU );

		// Attempt to drain the item
		ImmutablePair<Integer, ItemStack> drainedContainer = EssentiaItemContainerHelper.instance.extractFromContainer( container,
			proposedDrainAmount_EU );

		// Was the drain successful?
		if( drainedContainer == null )
		{
			return false;
		}

		if( this.fillOutputSlot( inventory, drainedContainer.getRight() ) )
		{
			// Adjust the fill amount
			containerFluid.setStackSize( EssentiaConversionHelper.instance.convertEssentiaAmountToFluidAmount( drainedContainer.getLeft() ) );

			// Fill to network
			monitor.injectItems( containerFluid, Actionable.MODULATE, machineSource );

			this.decreaseInputSlot( inventory );

			// Work was done
			return true;
		}

		return false;
	}

	private final boolean fillContainer( final PrivateInventory inventory, final IMEMonitor<IAEFluidStack> monitor, final ItemStack container,
											final MachineSource machineSource, final Aspect currentAspect )
	{
		// Is there an aspect selected?
		if( currentAspect == null )
		{
			return false;
		}

		// Get the available capacity of the container, in Essentia Units
		int containerCapacity_EU = EssentiaItemContainerHelper.instance.getContainerCapacity( container );

		// Is there any room for more essentia?
		if( containerCapacity_EU == 0 )
		{
			return false;
		}

		// Get the gas form of the essentia
		GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( currentAspect );

		// Simulate an extraction from the network
		IAEFluidStack result = monitor.extractItems(
			EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( essentiaGas, containerCapacity_EU ), Actionable.SIMULATE,
			machineSource );

		// Is there anything to extract?
		if( result == null )
		{
			return false;
		}

		// Get how much can be taken from the network, in Essentia Units
		int resultAmount_EU = (int)EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( result.getStackSize() );

		// Calculate the proposed amount, based on how much we need and how much
		// is available
		int proposedAmount_EU = Math.min( containerCapacity_EU, resultAmount_EU );

		// Create a new container filled to the proposed amount
		ImmutablePair<Integer, ItemStack> filledContainer = EssentiaItemContainerHelper.instance.injectIntoContainer( container, new AspectStack(
						currentAspect, proposedAmount_EU ) );

		// Was the fill successful?
		if( filledContainer == null )
		{
			return false;
		}

		// Can we move the container from the first to the second slot?
		if( this.fillOutputSlot( inventory, filledContainer.getRight() ) )
		{
			// Drain the essentia from the network
			monitor.extractItems( EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( essentiaGas, filledContainer.getLeft() ),
				Actionable.MODULATE, machineSource );

			// Decrease the stack in the first slot
			this.decreaseInputSlot( inventory );

			// Work was done
			return true;
		}

		return false;
	}

	private final boolean fillOutputSlot( final PrivateInventory inventory, final ItemStack itemStack )
	{
		if( itemStack == null )
		{
			return false;
		}

		ItemStack secondSlot = inventory.getStackInSlot( ContainerCellTerminalBase.OUTPUT_SLOT_ID );

		if( secondSlot == null )
		{
			inventory.setInventorySlotContents( ContainerCellTerminalBase.OUTPUT_SLOT_ID, itemStack );

			inventory.markDirty();

			return true;
		}

		if( ( !secondSlot.isItemEqual( itemStack ) ) || ( !ItemStack.areItemStackTagsEqual( itemStack, secondSlot ) ) )
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

	public final boolean doWork( final PrivateInventory inventory, final IMEMonitor<IAEFluidStack> monitor, final MachineSource machineSource,
									final Aspect currentAspect, final EntityPlayer player )
	{
		boolean allowedToExtract = true;
		boolean allowedToInject = true;

		// Ensure we have a valid monitor?
		if( monitor == null )
		{
			return false;
		}

		// Do we have a machine source?
		if( machineSource != null )
		{
			// Get the source node
			IGridNode sourceNode = machineSource.via.getActionableNode();

			// Ensure there is a node
			if( sourceNode == null )
			{
				return false;
			}

			// Get the security grid for the node.
			ISecurityGrid sGrid = sourceNode.getGrid().getCache( ISecurityGrid.class );

			// Get permissions
			allowedToExtract = sGrid.hasPermission( player, SecurityPermissions.EXTRACT );
			allowedToInject = sGrid.hasPermission( player, SecurityPermissions.INJECT );
		}

		// Make a copy of the input slot
		ItemStack inputSlot = inventory.getStackInSlot( ContainerCellTerminalBase.INPUT_SLOT_ID ).copy();

		// Is the container empty?
		if( EssentiaItemContainerHelper.instance.isContainerEmpty( inputSlot ) )
		{
			// Does the player have extract permission?
			if( allowedToExtract )
			{
				// Attempt to fill the container.
				return this.fillContainer( inventory, monitor, inputSlot, machineSource, currentAspect );
			}
		}
		// Is the container not empty?
		else if( EssentiaItemContainerHelper.instance.isContainerFilled( inputSlot ) )
		{
			// Does the player have inject permission?
			if( allowedToInject )
			{
				// Attempt to drain it.
				return this.drainContainer( inventory, monitor, inputSlot, machineSource );
			}
		}

		return false;
	}

	public final boolean hasWork( final PrivateInventory inventory )
	{
		// Is the inventory valid?
		if( inventory == null )
		{
			return false;
		}

		// Get the output slot
		ItemStack outputSlot = inventory.getStackInSlot( ContainerCellTerminalBase.OUTPUT_SLOT_ID );

		// Is the output slot valid and not full?
		if( ( outputSlot != null ) && ( outputSlot.stackSize >= 64 ) )
		{
			return false;
		}

		// Is the item in the input slot an essentia container?
		if( !EssentiaItemContainerHelper.instance.isContainer( inventory.getStackInSlot( ContainerCellTerminalBase.INPUT_SLOT_ID ) ) )
		{
			return false;
		}

		return true;
	}

}
