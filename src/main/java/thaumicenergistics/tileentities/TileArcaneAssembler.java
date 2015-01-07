package thaumicenergistics.tileentities;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.integration.tc.ArcaneCraftingPattern;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import thaumicenergistics.integration.tc.IDigiVisSource;
import thaumicenergistics.inventory.HandlerKnowledgeCore;
import thaumicenergistics.items.ItemKnowledgeCore;
import thaumicenergistics.util.EffectiveSide;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileArcaneAssembler
	extends AENetworkInvTile
	implements ICraftingProvider, ICraftingWatcherHost, IInventoryUpdateReceiver
{
	private class AssemblerInventory
		extends PrivateInventory
	{

		public AssemblerInventory()
		{
			super( "ArcaneAssemblerInventory", TileArcaneAssembler.SLOT_COUNT, 1, TileArcaneAssembler.this );
		}

		@Override
		public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
		{
			// Is the slot being assigned the knowledge core slot?
			if( slotId == TileArcaneAssembler.KCORE_SLOT_INDEX )
			{
				// Ensure the item is a knowledge core.
				return( ( itemStack == null ) || ( itemStack.getItem() instanceof ItemKnowledgeCore ) );
			}

			// Assume it is not valid
			return false;
		}

	}

	/**
	 * NBT Keys
	 */
	private static final String NBTKEY_KCORE = "kcore", NBTKEY_VIS_INTERFACE = "vis_interface", NBTKEY_STORED_VIS = "stored_vis";

	/**
	 * Primal aspects.
	 */
	public static final Aspect[] PRIMALS = new Aspect[] { Aspect.AIR, Aspect.EARTH, Aspect.ENTROPY, Aspect.FIRE, Aspect.ORDER, Aspect.WATER };

	/**
	 * Total number of slots pattern+kcore
	 */
	public static final int SLOT_COUNT = 22;

	/**
	 * Index of the slot used for the knowledge core and patterns.
	 */
	public static final int KCORE_SLOT_INDEX = 0, PATTERN_SLOT_INDEX = 1;

	/**
	 * Maximum amount of stored vis.
	 */
	public static final int MAX_STORED_VIS = 100;

	/**
	 * Holds the patterns and the kcore
	 */
	private AssemblerInventory internalInventory;

	/**
	 * Is the assembler busy?
	 */
	private boolean isBusy = false;

	/**
	 * Crafting job watcher.
	 */
	private ICraftingWatcher craftingWatcher;

	/**
	 * List of items that has been requested to craft.
	 */
	private ArrayList<IAEItemStack> craftingJobs = new ArrayList<IAEItemStack>();

	/**
	 * Handles interaction with the knowledge core.
	 */
	private HandlerKnowledgeCore kCoreHandler;

	/**
	 * If true the network should be informed of changes to available patterns.
	 */
	private boolean stalePatterns = false;

	/**
	 * Data pertaining to the linked digi-vis source
	 */
	private DigiVisSourceData visSourceInfo = new DigiVisSourceData();

	/**
	 * Vis stored in the assembler.
	 */
	private AspectList storedVis = new AspectList();

	/**
	 * Number of elapsed ticks.
	 */
	private int tickCounter = 0;

	/**
	 * Creates the assembler and it's inventory.
	 */
	public TileArcaneAssembler()
	{
		this.internalInventory = new AssemblerInventory();
	}

	/**
	 * Refills the internal vis buffer.
	 */
	private void replenishVis()
	{
		IDigiVisSource visSource = null;

		try
		{
			// Get the source
			visSource = this.visSourceInfo.tryGetSource( this.gridProxy.getGrid() );
		}
		catch( GridAccessException e )
		{
		}

		// Ensure the source is reachable. 
		if( visSource == null )
		{
			return;
		}

		// Calculate how much vis is needed for each primal
		for( Aspect primal : TileArcaneAssembler.PRIMALS )
		{
			// Is any needed?
			if( TileArcaneAssembler.MAX_STORED_VIS > this.storedVis.getAmount( primal ) )
			{
				// Request the vis
				int amountDrained = visSource.consumeVis( primal, 1 );

				// Was any drained?
				if( amountDrained > 0 )
				{
					// Add to the stored amount
					this.storedVis.add( primal, amountDrained );

					// Mark for network sync
					this.markForUpdate();
				}
			}
		}

	}

	/**
	 * Updates the pattern slots to match the kcore patterns.
	 */
	private void updatePatternSlots()
	{
		Iterator<ItemStack> pIterator = null;

		// Get the list
		if( this.kCoreHandler != null )
		{
			ArrayList<ItemStack> patternOutputs = this.kCoreHandler.getStoredOutputs();
			pIterator = patternOutputs.iterator();
		}

		// Set pattern slots
		for( int index = TileArcaneAssembler.PATTERN_SLOT_INDEX; index < TileArcaneAssembler.SLOT_COUNT; index++ )
		{
			if( ( pIterator != null ) && ( pIterator.hasNext() ) )
			{
				// Set to pattern result
				this.internalInventory.slots[index] = pIterator.next();
			}
			else
			{
				// Clear slot
				this.internalInventory.slots[index] = null;
			}
		}
	}

	/**
	 * Inventory is not world accessible.
	 */
	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection whichSide )
	{
		return new int[0];
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.SMART;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.internalInventory;
	}

	/**
	 * Get's the current knowledge core handler.
	 * 
	 * @return
	 */
	public HandlerKnowledgeCore getKCoreHandler()
	{
		return this.kCoreHandler;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	/**
	 * Get's the stored vis amounts.
	 * 
	 * @return
	 */
	public AspectList getStoredVis()
	{
		return this.storedVis;
	}

	/**
	 * Returns true if a knowledge core is stored.
	 * 
	 * @return
	 */
	public boolean hasKCore()
	{
		return( this.internalInventory.slots[TileArcaneAssembler.KCORE_SLOT_INDEX] != null );
	}

	@Override
	public boolean isBusy()
	{
		return this.isBusy;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Called when the internal inventory changes.
	 */
	@Override
	public void onInventoryChanged( final IInventory sourceInventory )
	{
		// Is there a kcore?
		ItemStack kCore = this.internalInventory.getStackInSlot( TileArcaneAssembler.KCORE_SLOT_INDEX );
		if( kCore != null )
		{
			// Is it a new core?
			if( ( this.kCoreHandler == null ) || ( !this.kCoreHandler.isHandlingCore( kCore ) ) )
			{
				// Close the old handler
				if( this.kCoreHandler != null )
				{
					this.kCoreHandler.close();
				}

				// Create a new handler
				this.kCoreHandler = new HandlerKnowledgeCore( kCore );

				// Update the pattern slots
				this.updatePatternSlots();

				// Update the network
				this.stalePatterns = true;
			}
		}
		else
		{
			// Was there a core?
			if( this.kCoreHandler != null )
			{
				// No more core
				this.kCoreHandler.close();
				this.kCoreHandler = null;

				// Update the pattern slots
				this.updatePatternSlots();

				// Update the network
				this.stalePatterns = true;
			}
		}

		// Mark for save.
		this.markDirty();
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void onLoadNBT( final NBTTagCompound data )
	{
		// Is there a saved core?
		if( data.hasKey( TileArcaneAssembler.NBTKEY_KCORE ) )
		{
			// Load the saved core
			this.internalInventory.slots[TileArcaneAssembler.KCORE_SLOT_INDEX] = ItemStack.loadItemStackFromNBT( data
							.getCompoundTag( TileArcaneAssembler.NBTKEY_KCORE ) );

			// Update the pattern slots
			this.updatePatternSlots();

			// Update the network
			this.stalePatterns = true;

		}

		// Is there relay info?
		if( data.hasKey( TileArcaneAssembler.NBTKEY_VIS_INTERFACE ) )
		{
			this.visSourceInfo.readFromNBT( data, TileArcaneAssembler.NBTKEY_VIS_INTERFACE );
		}

		// Is there stored vis info?
		if( data.hasKey( TileArcaneAssembler.NBTKEY_STORED_VIS ) )
		{
			this.storedVis.readFromNBT( data, TileArcaneAssembler.NBTKEY_STORED_VIS );
		}
	}

	/**
	 * Called when the player right-clicks the assembler with a memory card in
	 * hand.
	 * 
	 * @param memoryCard
	 */
	public void onMemoryCardActivate( final EntityPlayer player, final IMemoryCard memoryCard, final ItemStack playerHolding )
	{
		// Get the stored name
		String settingsName = memoryCard.getSettingsName( playerHolding );

		// Does it contain the data about a vis source?
		if( settingsName.equals( DigiVisSourceData.SOURCE_UNLOC_NAME ) )
		{
			// Get the data
			NBTTagCompound data = memoryCard.getData( playerHolding );

			// Load the info
			this.visSourceInfo.readFromNBT( data );

			// Inform the user
			memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );

			// Mark that we need to save
			this.markDirty();
		}
		// Is the memory card empty?
		else if( settingsName.equals( "gui.appliedenergistics2.Blank" ) )
		{
			// Clear the source info
			this.visSourceInfo.clearData();

			// Inform the user
			memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );

			// Mark dirty
			this.markDirty();
		}
	}

	@TileEvent(TileEventType.NETWORK_READ)
	@SideOnly(Side.CLIENT)
	public boolean onReceiveNetworkData( final ByteBuf data )
	{
		this.storedVis.aspects.clear();

		// Read each stored aspect
		for( int i = 0; i < TileArcaneAssembler.PRIMALS.length; i++ )
		{
			// Read the stored amount
			this.storedVis.add( TileArcaneAssembler.PRIMALS[i], data.readInt() );
		}

		return true;
	}

	@Override
	public void onRequestChange( final ICraftingGrid craftingGrid, final IAEItemStack what )
	{
		// TODO Auto-generated method stub

	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void onSaveNBT( final NBTTagCompound data )
	{
		// Get the kcore
		ItemStack kCore = this.internalInventory.slots[TileKnowledgeInscriber.KCORE_SLOT];
		if( kCore != null )
		{
			// Write the kcore
			data.setTag( TileArcaneAssembler.NBTKEY_KCORE, kCore.writeToNBT( new NBTTagCompound() ) );
		}

		// Write the vis interface
		this.visSourceInfo.writeToNBT( data, TileArcaneAssembler.NBTKEY_VIS_INTERFACE );

		// Write the stored vis
		this.storedVis.writeToNBT( data, TileArcaneAssembler.NBTKEY_STORED_VIS );

	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void onSendNetworkData( final ByteBuf data ) throws IOException
	{
		// Write each stored aspect
		for( int i = 0; i < TileArcaneAssembler.PRIMALS.length; i++ )
		{
			// Write the stored amount
			data.writeInt( this.storedVis.getAmount( TileArcaneAssembler.PRIMALS[i] ) );
		}
	}

	@TileEvent(TileEventType.TICK)
	public void onTick()
	{
		// Ignored on client side.
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		// Increment the tick counter.
		this.tickCounter++ ;

		// Are the network patterns stale?
		if( this.stalePatterns )
		{
			try
			{
				// Inform the network
				this.gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.getActionableNode() ) );

				// Mark they are no longer stale
				this.stalePatterns = false;
			}
			catch( GridAccessException e )
			{
			}
		}

		// Has five ticks elapsed?
		if( this.tickCounter == 5 )
		{
			// Reset the tick counter
			this.tickCounter = 0;

			// Replenish vis stores
			if( this.visSourceInfo.getHasData() )
			{
				this.replenishVis();
			}
		}
	}

	@Override
	public void provideCrafting( final ICraftingProviderHelper craftingTracker )
	{
		if( this.kCoreHandler != null )
		{
			// Get the list of patterns
			ArrayList<ArcaneCraftingPattern> corePatterns = this.kCoreHandler.getPatterns();

			// Add each pattern
			for( ArcaneCraftingPattern pattern : corePatterns )
			{
				if( pattern != null )
				{
					craftingTracker.addCraftingOption( this, pattern );
				}
			}
		}

	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final InventoryCrafting table )
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Configures the Assembler.
	 */
	public void setupAssemblerTile()
	{
		// Ignored on client side
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Set idle power usage
			this.gridProxy.setIdlePowerUsage( 1.0D );

			// Set that we require a channel
			this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		}
	}

	@Override
	public void updateWatcher( final ICraftingWatcher newWatcher )
	{
		// Set the watcher
		this.craftingWatcher = newWatcher;

		// Clear all requests
		this.craftingJobs.clear();
	}

}
