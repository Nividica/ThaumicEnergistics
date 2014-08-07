package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaIOBus;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.gui.GuiEssentiatIO;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import thaumicenergistics.network.packet.client.PacketClientEssentiaIOBus;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.EssentiaTileContainerHelper;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.definitions.Materials;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEFluidStack;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AEPartEssentiaIO
	extends AEPartBase
	implements IGridTickable, IInventoryUpdateReceiver, IAspectSlotPart, IAEAppEngInventory
{
	private final static int BASE_TRANSFER_PER_SECOND = 4;

	private final static int ADDITIONAL_TRANSFER_PER_SECOND = 7;

	private final static int MINIMUM_TICKS_PER_OPERATION = 10;

	private final static int MAXIMUM_TICKS_PER_OPERATION = 40;

	private final static int MAXIMUM_TRANSFER_PER_SECOND = 64;

	private final static int MINIMUM_TRANSFER_PER_SECOND = 1;

	private final static int MAX_FILTER_SIZE = 9;

	private final static int BASE_SLOT_INDEX = 4;

	private final static int[] TIER2_INDEXS = { 0, 2, 6, 8 };

	private final static int[] TIER1_INDEXS = { 1, 3, 5, 7 };

	private final static int UPGRADE_INVENTORY_SIZE = 4;

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.7;

	/**
	 * The amount of power required to transfer 1 essentia.
	 */
	private static final double POWER_DRAIN_PER_ESSENTIA = 0.5;

	private static final RedstoneMode[] REDSTONE_MODES = RedstoneMode.values();

	protected List<Aspect> filteredAspects = new ArrayList<Aspect>( AEPartEssentiaIO.MAX_FILTER_SIZE );

	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

	protected byte filterSize;

	protected byte upgradeSpeedCount = 0;

	protected boolean redstoneControlled;

	private boolean lastRedstone;

	private int[] availableFilterSlots = { AEPartEssentiaIO.BASE_SLOT_INDEX };

	private UpgradeInventory upgradeInventory = new UpgradeInventory( this.associatedItem, this, AEPartEssentiaIO.UPGRADE_INVENTORY_SIZE );

	private List<ContainerPartEssentiaIOBus> listeners = new ArrayList<ContainerPartEssentiaIOBus>();

	public AEPartEssentiaIO( AEPartsEnum associatedPart )
	{
		super( associatedPart );

		// Initialize the list
		for( int index = 0; index < AEPartEssentiaIO.MAX_FILTER_SIZE; index++ )
		{
			this.filteredAspects.add( null );
		}
	}

	public void addListener( ContainerPartEssentiaIOBus container )
	{
		if( !this.listeners.contains( container ) )
		{
			this.listeners.add( container );
		}
	}

	public void removeListener( ContainerPartEssentiaIOBus container )
	{
		this.listeners.remove( container );
	}

	private int getTransferAmountPerSecond()
	{
		return BASE_TRANSFER_PER_SECOND + ( this.upgradeSpeedCount * ADDITIONAL_TRANSFER_PER_SECOND );
	}

	private boolean canDoWork()
	{
		boolean canWork = true;

		if( this.redstoneControlled )
		{
			switch ( this.getRedstoneMode() )
			{
				case HIGH_SIGNAL:
					canWork = this.redstonePowered;

					break;
				case IGNORE:
					break;

				case LOW_SIGNAL:
					canWork = !this.redstonePowered;

					break;
				case SIGNAL_PULSE:
					canWork = false;
					break;
			}
		}

		return canWork;

	}

	private boolean takePowerFromNetwork( int essentiaAmount, Actionable mode )
	{
		// Get the energy grid
		IEnergyGrid eGrid = this.gridBlock.getEnergyGrid();

		// Ensure we have a grid
		if( eGrid == null )
		{
			return false;
		}

		// Calculate amount of power to take
		double powerDrain = AEPartEssentiaIO.POWER_DRAIN_PER_ESSENTIA * essentiaAmount;

		// Extract
		return( eGrid.extractAEPower( powerDrain, mode, PowerMultiplier.CONFIG ) >= powerDrain );
	}

	protected boolean extractEssentiaFromNetwork( int amountToFillContainer )
	{
		// Get the aspect in the container
		Aspect aspectToMatch = EssentiaTileContainerHelper.getAspectInContainer( this.facingContainer );

		// Do we have the power to transfer this amount?
		if( !this.takePowerFromNetwork( amountToFillContainer, Actionable.SIMULATE ) )
		{
			// Not enough power
			return false;
		}

		// Loop over all aspect filters
		for( Aspect filterAspect : this.filteredAspects )
		{
			// Can we transfer?
			if( ( filterAspect == null ) || ( !this.aspectTransferAllowed( filterAspect ) ) )
			{
				// Invalid or not allowed
				continue;
			}

			// Are we searching for a match?
			if( ( aspectToMatch != null ) && ( filterAspect != aspectToMatch ) )
			{
				// Not a match
				continue;
			}

			// Can we inject any of this into the container
			if( EssentiaTileContainerHelper.injectIntoContainer( this.facingContainer, 1, filterAspect, Actionable.SIMULATE ) < 1 )
			{
				// Container will not accept any of this
				continue;
			}

			// Get the gas form of the essentia
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( filterAspect );

			// Create the fluid stack
			IAEFluidStack toExtract = EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, amountToFillContainer );

			// Simulate a network extraction
			IAEFluidStack extractedStack = this.extractFluid( toExtract, Actionable.SIMULATE );

			// Were we able to extract any?
			if( ( extractedStack != null ) && ( extractedStack.getStackSize() > 0 ) )
			{
				// Fill the container
				int filledAmount = (int)EssentiaTileContainerHelper.injectIntoContainer( this.facingContainer, extractedStack, Actionable.MODULATE );

				// Were we able to fill the container?
				if( filledAmount == 0 )
				{
					continue;
				}

				// Take the power required for the filled amount
				this.takePowerFromNetwork( (int)EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( filledAmount ), Actionable.MODULATE );

				// Take from the network
				this.extractFluid( EssentiaConversionHelper.createAEFluidStackInFluidUnits( essentiaGas, filledAmount ), Actionable.MODULATE );

				// Done
				return true;
			}
		}

		return false;

	}

	protected boolean injectEssentaToNetwork( int amountToDrainFromContainer )
	{
		// Get the aspect in the container
		Aspect aspectToDrain = EssentiaTileContainerHelper.getAspectInContainer( this.facingContainer );

		if( ( aspectToDrain == null ) || ( !this.aspectTransferAllowed( aspectToDrain ) ) )
		{
			return false;
		}

		// Simulate a drain from the container
		FluidStack drained = EssentiaTileContainerHelper.extractFromContainer( this.facingContainer, amountToDrainFromContainer, aspectToDrain,
			Actionable.SIMULATE );

		// Was any drained?
		if( drained == null )
		{
			return false;
		}

		// Create the fluid stack
		IAEFluidStack toFill = AEApi.instance().storage().createFluidStack( drained );

		// Simulate inject into the network
		IAEFluidStack notInjected = this.injectFluid( toFill, Actionable.SIMULATE );

		// Was any not injected?
		if( notInjected != null )
		{
			// Calculate how much was injected into the network
			int amountInjected = (int)( toFill.getStackSize() - notInjected.getStackSize() );

			// None could be injected
			if( amountInjected == 0 )
			{
				return false;
			}

			// Convert from fluid units to essentia units
			amountInjected = (int)EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( amountInjected );

			// Some was unable to be injected, adjust the drain amounts
			amountToDrainFromContainer = amountInjected;
			toFill.setStackSize( amountInjected );
		}

		// Do we have the power to inject?
		if( !this.takePowerFromNetwork( amountToDrainFromContainer, Actionable.SIMULATE ) )
		{
			// Not enough power
			return false;
		}

		// Take power
		this.takePowerFromNetwork( amountToDrainFromContainer, Actionable.MODULATE );

		// Inject
		this.injectFluid( toFill, Actionable.MODULATE );

		// Drain
		EssentiaTileContainerHelper.extractFromContainer( this.facingContainer, amountToDrainFromContainer, aspectToDrain, Actionable.MODULATE );

		return true;
	}

	public abstract boolean aspectTransferAllowed( Aspect aspect );

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	public abstract boolean doWork( int transferAmount );

	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiEssentiatIO( this, player );
	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	public RedstoneMode getRedstoneMode()
	{
		return this.redstoneMode;
	}

	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartEssentiaIOBus( this, player );
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode arg0 )
	{
		return new TickingRequest( MINIMUM_TICKS_PER_OPERATION, MAXIMUM_TICKS_PER_OPERATION, false, false );
	}

	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	@Override
	public boolean onActivate( EntityPlayer player, Vec3 position )
	{
		boolean activated = super.onActivate( player, position );

		this.onInventoryChanged( null );

		return activated;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		if( inv == this.upgradeInventory )
		{
			this.onInventoryChanged( inv );
		}
	}

	@Override
	public void onInventoryChanged( IInventory sourceInventory )
	{
		int oldFilterSize = this.filterSize;

		this.filterSize = 0;
		this.redstoneControlled = false;
		this.upgradeSpeedCount = 0;

		Materials aeMaterals = AEApi.instance().materials();

		for( int i = 0; i < this.upgradeInventory.getSizeInventory(); i++ )
		{
			ItemStack slotStack = this.upgradeInventory.getStackInSlot( i );

			if( slotStack != null )
			{
				if( aeMaterals.materialCardCapacity.sameAs( slotStack ) )
				{
					this.filterSize++ ;
				}
				else if( aeMaterals.materialCardRedstone.sameAs( slotStack ) )
				{
					this.redstoneControlled = true;
				}
				else if( aeMaterals.materialCardSpeed.sameAs( slotStack ) )
				{
					this.upgradeSpeedCount++ ;
				}
			}
		}

		// Did the filter size change?
		if( oldFilterSize != this.filterSize )
		{
			this.resizeAvailableArray();
		}

		try
		{
			if( this.host.getLocation().getWorld().isRemote )
			{
				return;
			}
		}
		catch( Throwable ignored )
		{
		}

		this.notifyListenersOfFilterSizeChange();

		this.notifyListenersOfRedstoneControlledChange();
	}

	private void notifyListenersOfRedstoneControlledChange()
	{
		for( ContainerPartEssentiaIOBus listener : this.listeners )
		{
			listener.setRedstoneControlled( this.redstoneControlled );
		}
	}

	private void notifyListenersOfFilterSizeChange()
	{
		for( ContainerPartEssentiaIOBus listener : this.listeners )
		{
			listener.setFilterSize( this.filterSize );
		}
	}

	private void notifyListenersOfRedstoneModeChange()
	{
		for( ContainerPartEssentiaIOBus listener : this.listeners )
		{
			listener.setRedstoneMode( this.redstoneMode );
		}

	}

	private void resizeAvailableArray()
	{
		// Resize the available slots
		this.availableFilterSlots = new int[1 + ( this.filterSize * 4 )];

		// Add the base slot
		this.availableFilterSlots[0] = AEPartEssentiaIO.BASE_SLOT_INDEX;

		if( this.filterSize < 2 )
		{
			// Reset tier 2 slots
			for( int i = 0; i < AEPartEssentiaIO.TIER2_INDEXS.length; i++ )
			{
				this.filteredAspects.set( AEPartEssentiaIO.TIER2_INDEXS[i], null );
			}

			if( this.filterSize < 1 )
			{
				// Reset tier 1 slots
				for( int i = 0; i < AEPartEssentiaIO.TIER1_INDEXS.length; i++ )
				{
					this.filteredAspects.set( AEPartEssentiaIO.TIER1_INDEXS[i], null );
				}
			}
			else
			{
				// Tier 1 slots
				System.arraycopy( AEPartEssentiaIO.TIER1_INDEXS, 0, this.availableFilterSlots, 1, 4 );
			}
		}
		else
		{
			// Add both
			System.arraycopy( AEPartEssentiaIO.TIER1_INDEXS, 0, this.availableFilterSlots, 1, 4 );
			System.arraycopy( AEPartEssentiaIO.TIER2_INDEXS, 0, this.availableFilterSlots, 5, 4 );
		}
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

		if( this.redstonePowered )
		{
			if( !this.lastRedstone )
			{
				/*
				 * NOTE: Known Issue: More than 1 redstone pulse per second will cause this to
				 * operate too fast.
				 */
				this.doWork( this.getTransferAmountPerSecond() );
			}
		}

		this.lastRedstone = this.redstonePowered;
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );

		// Read redstone mode
		this.redstoneMode = RedstoneMode.values()[data.getInteger( "redstoneMode" )];

		for( int index = 0; index < AEPartEssentiaIO.MAX_FILTER_SIZE; index++ )
		{
			String aspectTag = data.getString( "AspectFilter#" + index );

			if( !aspectTag.equals( "" ) )
			{
				this.filteredAspects.set( index, Aspect.aspects.get( aspectTag ) );
			}
		}

		this.upgradeInventory.readFromNBT( data, "upgradeInventory" );

		this.onInventoryChanged( this.upgradeInventory );
	}

	@Override
	public final boolean readFromStream( ByteBuf stream ) throws IOException
	{
		return super.readFromStream( stream );
	}

	@Override
	public void saveChanges()
	{
		// TODO: Eh? This seems redundant
		this.host.markForSave();
	}

	/**
	 * Called when a player has clicked the redstone button in the gui.
	 * 
	 * @param player
	 */
	public void onClientRequestChangeRedstoneMode( EntityPlayer player )
	{
		// Get the current ordinal, and increment it
		int nextOrdinal = this.redstoneMode.ordinal() + 1;

		// Bounds check
		if( nextOrdinal >= AEPartEssentiaIO.REDSTONE_MODES.length )
		{
			nextOrdinal = 0;
		}

		// Set the mode
		this.redstoneMode = AEPartEssentiaIO.REDSTONE_MODES[nextOrdinal];

		// Notify listeners
		this.notifyListenersOfRedstoneModeChange();
	}

	/**
	 * Called when a client gui is requesting a full update.
	 * 
	 * @param player
	 */
	public void onClientRequestFullUpdate( EntityPlayer player )
	{
		// Set the filter list
		new PacketClientAspectSlot().createFilterListUpdate( this.filteredAspects, player ).sendPacketToPlayer();

		// Set the state of the bus
		new PacketClientEssentiaIOBus().createFullUpdate( player, this.redstoneMode, this.filterSize, this.redstoneControlled ).sendPacketToPlayer();
	}

	@Override
	public final void setAspect( int index, Aspect aspect, EntityPlayer player )
	{
		// Set the filter
		this.filteredAspects.set( index, aspect );

		// TODO: Should really let all clients know about this
		
		// Update the client
		new PacketClientAspectSlot().createFilterListUpdate( this.filteredAspects, player ).sendPacketToPlayer();
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		if( this.canDoWork() )
		{
			// Calculate the amount to transfer per second
			int transferAmountPerSecond = this.getTransferAmountPerSecond();

			// Calculate amount to transfer this operation
			int transferAmount = (int)( transferAmountPerSecond * ( ticksSinceLastCall / 20.F ) );

			// Clamp
			if( transferAmount < MINIMUM_TRANSFER_PER_SECOND )
			{
				transferAmount = MINIMUM_TRANSFER_PER_SECOND;
			}
			else if( transferAmount > MAXIMUM_TRANSFER_PER_SECOND )
			{
				transferAmount = MAXIMUM_TRANSFER_PER_SECOND;
			}

			if( this.doWork( transferAmount ) )
			{
				return TickRateModulation.URGENT;
			}
		}

		return TickRateModulation.IDLE;
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );

		// Write the redstone mode
		data.setInteger( "redstoneMode", this.redstoneMode.ordinal() );

		for( int i = 0; i < AEPartEssentiaIO.MAX_FILTER_SIZE; i++ )
		{
			Aspect aspect = this.filteredAspects.get( i );
			String aspectTag = "";

			if( aspect != null )
			{
				aspectTag = aspect.getTag();
			}

			data.setString( "AspectFilter#" + i, aspectTag );
		}

		this.upgradeInventory.writeToNBT( data, "upgradeInventory" );
	}

	@Override
	public final void writeToStream( ByteBuf stream ) throws IOException
	{
		super.writeToStream( stream );
	}

	public boolean addFilteredAspectFromItemstack( EntityPlayer player, ItemStack itemStack )
	{
		Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( itemStack );

		if( itemAspect != null )
		{
			// Are we already filtering this aspect?
			if( this.filteredAspects.contains( itemAspect ) )
			{
				return true;
			}

			// Add to the first open slot
			for( int avalibleIndex = 0; avalibleIndex < this.availableFilterSlots.length; avalibleIndex++ )
			{
				int filterIndex = this.availableFilterSlots[avalibleIndex];

				// Is this space empty?
				if( this.filteredAspects.get( filterIndex ) == null )
				{
					// Is this server side?
					if( !player.worldObj.isRemote )
					{
						// Set the filter
						this.setAspect( filterIndex, itemAspect, player );
					}

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Called client-side to keep the client-side part in sync
	 * with the server-side part. This aids in keeping the
	 * gui in sync even in high network lag enviroments. 
	 * @param filteredAspects
	 */
	@SideOnly(Side.CLIENT)
	public void receiveFilterList( List<Aspect> filteredAspects )
	{
		this.filteredAspects = filteredAspects;
	}

	/**
	 * Called client-side to keep the client-side part in sync
	 * with the server-side part. This aids in keeping the
	 * gui in sync even in high network lag enviroments. 
	 * @param filterSize
	 */
	@SideOnly(Side.CLIENT)
	public void receiveFilterSize( byte filterSize )
	{
		this.filterSize = filterSize;

		this.resizeAvailableArray();
	}

	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		// Were we wrenched?
		if( wrenched )
		{
			// No drops
			return;
		}

		// Add upgrades to drops
		for( int slotIndex = 0; slotIndex < AEPartEssentiaIO.UPGRADE_INVENTORY_SIZE; slotIndex++ )
		{
			// Get the upgrade card in this slot
			ItemStack slotStack = this.upgradeInventory.getStackInSlot( slotIndex );

			// Is it not null?
			if( ( slotStack != null ) && ( slotStack.stackSize > 0 ) )
			{
				// Add to the drops
				drops.add( slotStack );
			}
		}
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaIO.IDLE_POWER_DRAIN;
	}

}
