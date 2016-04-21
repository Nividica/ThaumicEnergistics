package thaumicenergistics.common.tiles;

import java.io.IOException;
import java.util.*;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.*;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.localization.WailaText;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.me.GridAccessException;
import appeng.parts.automation.BlockUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.client.gui.ThEGuiHelper;
import thaumicenergistics.common.blocks.BlockArcaneAssembler;
import thaumicenergistics.common.integration.IWailaSource;
import thaumicenergistics.common.integration.tc.ArcaneCraftingPattern;
import thaumicenergistics.common.integration.tc.DigiVisSourceData;
import thaumicenergistics.common.integration.tc.VisCraftingHelper;
import thaumicenergistics.common.inventory.HandlerKnowledgeCore;
import thaumicenergistics.common.inventory.TheInternalInventory;
import thaumicenergistics.common.items.ItemKnowledgeCore;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * Allows auto-crafting arcane recipes.
 *
 * @author Nividica
 *
 */
public class TileArcaneAssembler
	extends AENetworkInvTile
	implements ICraftingProvider, IWailaSource
{
	private class AAInv
		extends TheInternalInventory
	{

		public AAInv()
		{
			super( "ArcaneAssemblerInventory", TileArcaneAssembler.SLOT_COUNT, 64 );
		}

		@Override
		public boolean isItemValidForSlot( final int slotIndex, final ItemStack itemStack )
		{
			if( slotIndex == TileArcaneAssembler.KCORE_SLOT_INDEX )
			{
				return ( itemStack == null ) || ( itemStack.getItem() instanceof ItemKnowledgeCore );
			}

			return true;
		}

		@Override
		public void markDirty()
		{
			TileArcaneAssembler.this.markDirty();
		}

		@Override
		public void setInventorySlotContents( final int slotIndex, final ItemStack itemStack )
		{
			// Call super
			super.setInventorySlotContents( slotIndex, itemStack );

			// Core changed?
			if( slotIndex == TileArcaneAssembler.KCORE_SLOT_INDEX )
			{
				TileArcaneAssembler.this.flag_CoreChanged = true;
			}

			// Armor changed?
			if( slotIndex >= TileArcaneAssembler.DISCOUNT_ARMOR_INDEX )
			{
				TileArcaneAssembler.this.flag_RecalcVis = true;
			}
		}

	}

	/**
	 * NBT Keys
	 */
	private static final String NBTKEY_VIS_INTERFACE = "vis_interface",
					NBTKEY_UPGRADES = "upgradeCount", NBTKEY_UPGRADEINV = "upgrades", NBTKEY_CRAFTING = "isCrafting",
					NBTKEY_CRAFTING_PATTERN = "pattern";

	/**
	 * NBT Key for stored vis.
	 */
	public static final String NBTKEY_STORED_VIS = "stored_vis";

	/**
	 * Used to simulate floating point amounts of vis.
	 */
	private static final int CVIS_MULTIPLER = 10;

	/**
	 * The number of ticks required to craft an item without any upgrades.
	 */
	private static final int BASE_TICKS_PER_CRAFT = 20;

	/**
	 * Primal aspects.
	 */
	public static final Aspect[] PRIMALS = new Aspect[] { Aspect.AIR, Aspect.WATER, Aspect.FIRE, Aspect.ORDER, Aspect.ENTROPY, Aspect.EARTH };

	/**
	 * Total number of slots. 1 KCore + 21 Patterns + 1 Target + 4 Discount
	 * Armor
	 */
	public static final int SLOT_COUNT = 27;

	/**
	 * Index of the slots.
	 */
	public static final int KCORE_SLOT_INDEX = 0, PATTERN_SLOT_INDEX = 1, TARGET_SLOT_INDEX = 22, DISCOUNT_ARMOR_INDEX = 23;

	/**
	 * Maximum amount of stored centi-vis.
	 */
	public static final int MAX_STORED_CVIS = 150 * TileArcaneAssembler.CVIS_MULTIPLER;

	/**
	 * Power usage.
	 */
	public static double IDLE_POWER = 0.0D, ACTIVE_POWER = 1.5D, WARP_POWER_PERCENT = 0.15;

	/**
	 * Holds the patterns and the kcore.
	 * Saved and loaded by AEInvTile due to getInternalInventory() call.
	 */
	private final AAInv internalInventory;

	/**
	 * Holds the upgrades
	 */
	private UpgradeInventory upgradeInventory;

	/**
	 * Is the assembler busy crafting?
	 */
	private boolean isCrafting = false;

	/**
	 * The pattern currently being crafted.
	 */
	private ArcaneCraftingPattern currentPattern = null;

	/**
	 * Handles interaction with the knowledge core.
	 */
	private final HandlerKnowledgeCore kCoreHandler;

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
	private int visTickCounter = 0, craftTickCounter = 0, delayTickCounter = 0;

	/**
	 * Source of all actions.
	 */
	private MachineSource mySource;

	/**
	 * True when the assembler is online.
	 */
	private boolean isActive = false;

	/**
	 * The number of installed upgrades.
	 */
	private int upgradeCount = 0;

	/**
	 * How much discount each aspect is receiving.
	 */
	private Hashtable<Aspect, Float> visDiscount = new Hashtable<Aspect, Float>();

	/**
	 * How much additional power is required due to warp.
	 */
	private float warpPowerMultiplier = 1.0F;

	/**
	 * When true will send network update periodically.
	 */
	private boolean delayedUpdate = false;

	/**
	 * True when the core has changed.
	 */
	boolean flag_CoreChanged = false;

	/**
	 * True if the vis discount needs to be re-calculated.
	 */
	boolean flag_RecalcVis = false;

	/**
	 * Creates the assembler and it's inventory.
	 */
	public TileArcaneAssembler()
	{
		// Create the internal inventory
		this.internalInventory = new AAInv();

		// Create the upgrade inventory
		this.upgradeInventory = new BlockUpgradeInventory( ThEApi.instance().blocks().ArcaneAssembler.getBlock(), this,
						BlockArcaneAssembler.MAX_SPEED_UPGRADES );

		// Set the machine source
		this.mySource = new MachineSource( this );

		// Prep the discount table
		for( Aspect primal : TileArcaneAssembler.PRIMALS )
		{
			this.visDiscount.put( primal, VisCraftingHelper.INSTANCE.getScepterVisModifier( primal ) );
		}

		// Create the handler
		this.kCoreHandler = new HandlerKnowledgeCore();

		// Set idle power usage
		this.getProxy().setIdlePowerUsage( TileArcaneAssembler.IDLE_POWER );

		// Require a channel
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	/**
	 * Get's the NBT tag for the just-crafted assembler.
	 *
	 * @return
	 */
	public static NBTTagCompound getCraftTag()
	{
		NBTTagCompound data = new NBTTagCompound();

		// Create the vis list
		AspectList vis = new AspectList();

		// Add each primal
		for( Aspect primal : TileArcaneAssembler.PRIMALS )
		{
			vis.add( primal, TileArcaneAssembler.MAX_STORED_CVIS );
		}

		// Write into the tag
		vis.writeToNBT( data, TileArcaneAssembler.NBTKEY_STORED_VIS );

		return data;
	}

	/**
	 * Calculates how much vis discount applies to each aspect
	 */
	private void calculateVisDiscounts()
	{
		this.flag_RecalcVis = false;
		float discount;

		for( Aspect primal : TileArcaneAssembler.PRIMALS )
		{
			// Get the discount from the scepter
			discount = VisCraftingHelper.INSTANCE.getScepterVisModifier( primal );

			// Factor in the discount armor
			discount -= VisCraftingHelper.INSTANCE.calculateArmorDiscount( this.internalInventory, TileArcaneAssembler.DISCOUNT_ARMOR_INDEX, 4,
				primal );

			this.visDiscount.put( primal, discount );
		}

		// Reset warp power multiplier
		this.warpPowerMultiplier = 1.0F;

		// Calculate warp power multiplier
		this.warpPowerMultiplier += VisCraftingHelper.INSTANCE.calculateArmorWarp( this.internalInventory, TileArcaneAssembler.DISCOUNT_ARMOR_INDEX,
			4 ) * TileArcaneAssembler.WARP_POWER_PERCENT;
	}

	private void craftingTick()
	{
		// Null check
		if( this.currentPattern == null )
		{
			this.isCrafting = false;
		}

		// Is there enough vis to complete the crafting?
		if( !this.hasEnoughVisForCraft() )
		{
			return;
		}

		// Has the assembler finished crafting?
		if( this.craftTickCounter >= this.ticksPerCraft() )
		{
			try
			{
				// Get the storage grid
				IStorageGrid storageGrid = this.getProxy().getStorage();

				// Simulate placing the items
				boolean rejected = false;
				for( IAEItemStack output : this.currentPattern.getAllResults() )
				{
					IAEItemStack rejectedResult = storageGrid.getItemInventory().injectItems( output, Actionable.SIMULATE,
						this.mySource );
					if( ( rejectedResult != null ) && ( rejectedResult.getStackSize() > 0 ) )
					{
						rejected = true;
						break;
					}
				}

				// Were all items accepted?
				if( !rejected )
				{
					// Inject into the network
					for( IAEItemStack output : this.currentPattern.getAllResults() )
					{
						storageGrid.getItemInventory().injectItems( output, Actionable.MODULATE, this.mySource );
					}

					// Mark the assembler as no longer crafting
					this.isCrafting = false;
					this.internalInventory.setInventorySlotContents( TileArcaneAssembler.TARGET_SLOT_INDEX, null );
					this.currentPattern = null;

					// Mark for network update
					this.markForDelayedUpdate();
				}
			}
			catch( GridAccessException e )
			{
			}
		}
		else
		{
			try
			{
				// Calculate power required
				double powerRequired = ( TileArcaneAssembler.ACTIVE_POWER + ( ( TileArcaneAssembler.ACTIVE_POWER * this.upgradeCount ) / 2.0D ) ) *
								this.warpPowerMultiplier;

				// Attempt to take power
				IEnergyGrid eGrid = this.getProxy().getEnergy();
				double powerExtracted = eGrid.extractAEPower( powerRequired, Actionable.MODULATE, PowerMultiplier.CONFIG );

				if( ( powerExtracted - powerRequired ) <= 0.0D )
				{
					// Increment the counter
					this.craftTickCounter++ ;

					if( this.craftTickCounter >= this.ticksPerCraft() )
					{
						// Take the required vis
						Aspect[] requiredAspects = this.currentPattern.getCachedAspects();
						for( Aspect aspect : requiredAspects )
						{
							// Calculate the required amount
							int requiredAmount = this.getRequiredAmountForAspect( aspect );

							// Take the vis
							this.storedVis.reduce( aspect, requiredAmount );
						}

						this.markForDelayedUpdate();
					}
				}
			}
			catch( GridAccessException e )
			{
			}
		}

	}

	/**
	 * Helper function to calculate how much vis is required for the specified
	 * aspect for the current recipe.
	 *
	 * @param aspect
	 * @return
	 */
	private int getRequiredAmountForAspect( final Aspect aspect )
	{
		return (int)Math.ceil(
			this.visDiscount.get( aspect ) * ( this.currentPattern.getAspectCost( aspect ) * TileArcaneAssembler.CVIS_MULTIPLER ) );
	}

	private boolean hasEnoughVisForCraft()
	{
		// Null check
		if( this.currentPattern == null )
		{
			return false;
		}

		// Get the required aspects
		Aspect[] requiredAspects = this.currentPattern.getCachedAspects();

		// Check each aspect required by the pattern
		for( Aspect aspect : requiredAspects )
		{
			// Calculate the required amount
			int requiredAmount = this.getRequiredAmountForAspect( aspect );

			// Is there not enough?
			if( this.storedVis.getAmount( aspect ) < requiredAmount )
			{
				return false;
			}
		}

		// Has enough of all aspects
		return true;
	}

	/**
	 * Returns true if the tile is connected to the AE network and active.
	 *
	 * @return
	 */
	private boolean isActive()
	{
		// Are we server side?
		if( EffectiveSide.isServerSide() )
		{
			// Do we have a proxy and grid node?
			if( ( this.getProxy() != null ) && ( this.getProxy().getNode() != null ) )
			{
				// Get the grid node activity
				this.isActive = this.getProxy().getNode().isActive();
			}
		}

		return this.isActive;
	}

	/**
	 * Marks the tile for a delayed update.
	 */
	private void markForDelayedUpdate()
	{
		this.delayedUpdate = true;
	}

	/**
	 * Refills the internal vis buffer.
	 */
	private void replenishVis()
	{
		IDigiVisSource visSource = null;

		// Ensure the grid is ready
		if( !this.getProxy().isReady() )
		{
			return;
		}

		try
		{
			// Get the source
			visSource = this.visSourceInfo.tryGetSource( this.getProxy().getGrid() );
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
			int numNeeded = TileArcaneAssembler.MAX_STORED_CVIS - this.storedVis.getAmount( primal );

			// Is any needed?
			if( numNeeded > 0 )
			{
				// Request the vis
				int amountDrained = visSource.consumeVis( primal, numNeeded );

				// Was any drained?
				if( amountDrained > 0 )
				{
					// Add to the stored amount
					this.storedVis.add( primal, amountDrained );

					// Mark for network sync
					this.markForDelayedUpdate();
				}
			}
		}

	}

	/**
	 * The number of ticks required to craft an item.
	 */
	private int ticksPerCraft()
	{
		return TileArcaneAssembler.BASE_TICKS_PER_CRAFT - ( 4 * this.upgradeCount );
	}

	/**
	 * Updates the handler to the current core.
	 */
	private void updateCoreHandler()
	{
		this.flag_CoreChanged = false;
		// Is there a kcore?
		ItemStack kCore = this.internalInventory.getStackInSlot( TileArcaneAssembler.KCORE_SLOT_INDEX );
		if( kCore != null )
		{
			// Is it a new core?
			if( !TileArcaneAssembler.this.kCoreHandler.isHandlingCore( kCore ) )
			{
				// Open the core
				TileArcaneAssembler.this.kCoreHandler.open( kCore );

				// Update the pattern slots
				TileArcaneAssembler.this.updatePatternSlots();
			}
		}
		else
		{
			// Was there a core?
			if( TileArcaneAssembler.this.kCoreHandler.hasCore() )
			{
				// No more core
				TileArcaneAssembler.this.kCoreHandler.close();

				// Update the pattern slots
				TileArcaneAssembler.this.updatePatternSlots();
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
		for( int index = TileArcaneAssembler.PATTERN_SLOT_INDEX; index < ( TileArcaneAssembler.PATTERN_SLOT_INDEX +
						HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS ); index++ )
		{
			if( ( pIterator != null ) && ( pIterator.hasNext() ) )
			{
				// Set to pattern result
				this.internalInventory.setInventorySlotContents( index, pIterator.next() );
			}
			else
			{
				// Clear slot
				this.internalInventory.setInventorySlotContents( index, null );
			}
		}

		this.stalePatterns = true;
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack the visually represents this tile
		return ThEApi.instance().blocks().ArcaneAssembler.getStack();

	}

	@Override
	public void addWailaInformation( final List<String> tooltip )
	{
		// Is it active?
		if( this.isActive() )
		{
			// Get activity string from AppEng2
			tooltip.add( WailaText.DeviceOnline.getLocal() );
		}
		else
		{
			// Get activity string from AppEng2
			tooltip.add( WailaText.DeviceOffline.getLocal() );
		}

		// Is the assembler crafting anything?
		if( ( this.isCrafting ) && this.internalInventory.getHasStack( TileArcaneAssembler.TARGET_SLOT_INDEX ) )
		{
			// Add what is being crafted and the percent it is complete
			tooltip.add( String.format( "%s, %.0f%%", this.internalInventory.getStackInSlot( TileArcaneAssembler.TARGET_SLOT_INDEX ).getDisplayName(),
				( ( this.getPercentComplete() * 100.0F ) ) ) );
		}

		// Build vis amounts
		StringBuilder strAspects = new StringBuilder();
		for( int i = 0; i < TileArcaneAssembler.PRIMALS.length; i++ )
		{
			// Get the primal
			Aspect primal = TileArcaneAssembler.PRIMALS[i];

			// Change color
			strAspects.append( ThEGuiHelper.INSTANCE.getAspectChatColor( primal ) );

			// Add amount
			strAspects.append( ( this.storedVis.getAmount( primal ) / (float)TileArcaneAssembler.CVIS_MULTIPLER ) );

			// Are there more primals after this one?
			if( ( i + 1 ) < TileArcaneAssembler.PRIMALS.length )
			{
				// Add a gray pipe
				strAspects.append( EnumChatFormatting.WHITE.toString() );
				strAspects.append( " | " );
			}
		}

		// Add vis amounts
		tooltip.add( strAspects.toString() );
	}

	@MENetworkEventSubscribe
	public final void channelEvent( final MENetworkChannelsChanged event )
	{
		// Mark for update
		this.markForUpdate();
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

	/**
	 * Called when the block is broken to get any additional items.
	 *
	 * @return
	 */
	@Override
	public void getDrops( final World world, final int x, final int y, final int z, final ArrayList<ItemStack> drops )
	{
		// Add the kCore
		ItemStack kCore = this.internalInventory.getStackInSlot( TileArcaneAssembler.KCORE_SLOT_INDEX );
		if( kCore != null )
		{
			drops.add( kCore );
		}

		// Add upgrades
		for( int i = 0; i < this.upgradeInventory.getSizeInventory(); i++ )
		{
			ItemStack upgrade = this.upgradeInventory.getStackInSlot( i );

			if( upgrade != null )
			{
				drops.add( upgrade );
			}
		}

		// Add armor
		for( int i = 0; i < 4; i++ )
		{
			ItemStack armor = this.internalInventory.getStackInSlot( TileArcaneAssembler.DISCOUNT_ARMOR_INDEX + i );

			if( armor != null )
			{
				drops.add( armor );
			}
		}
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
	 * Returns the percent the current crafting job is complete.
	 *
	 * @return
	 */
	public float getPercentComplete()
	{
		float percent = 0.0F;

		if( this.isCrafting )
		{
			percent = Math.min( ( (float)this.craftTickCounter / this.ticksPerCraft() ), 1.0F );
		}

		return percent;
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

	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	/**
	 * Returns the discount for the specified aspect.
	 *
	 * @param aspect
	 * @return
	 */
	public float getVisDiscountForAspect( final Aspect aspect )
	{
		return this.visDiscount.get( aspect );
	}

	@Override
	public boolean isBusy()
	{
		return this.isCrafting;
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

	@Override
	public boolean isUseableByPlayer( final EntityPlayer player )
	{
		if( EffectiveSide.isClientSide() )
		{
			// Ignored on the client side.
			return false;
		}

		// Check basic usability
		if( !this.internalInventory.isUseableByPlayer( player, this ) )
		{
			return false;
		}

		// Check the security grid
		try
		{
			// Get the security grid
			ISecurityGrid sGrid = this.getProxy().getSecurity();

			// Return true if the player has inject and extract permissions
			return( ( sGrid.hasPermission( player, SecurityPermissions.INJECT ) ) && ( sGrid.hasPermission( player, SecurityPermissions.EXTRACT ) ) );
		}
		catch( GridAccessException e )
		{
			return false;
		}
	}

	/**
	 * Called when the tile entity is about to be destroyed by a block break.
	 */
	public void onBreak()
	{
		this.isCrafting = false;
	}

	/**
	 * Called when the upgrade inventory changes
	 */
	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		// Reset the upgrade count
		this.upgradeCount = 0;

		IMaterials aeMaterals = AEApi.instance().definitions().materials();

		// Look for speed cards
		for( int i = 0; i < this.upgradeInventory.getSizeInventory(); i++ )
		{
			ItemStack slotStack = this.upgradeInventory.getStackInSlot( i );

			if( slotStack != null )
			{
				if( aeMaterals.cardSpeed().isSameAs( slotStack ) )
				{
					this.upgradeCount++ ;
				}
			}
		}

		if( EffectiveSide.isServerSide() )
		{
			// Mark for save
			this.markDirty();

			// Mark for network update
			this.markForUpdate();
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

			// Ensure there is valid data
			if( this.visSourceInfo.hasSourceData() )
			{
				// Inform the user
				memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
			}

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

	@Override
	public void onReady()
	{
		// Call super
		super.onReady();

		// Setup the kcore handler
		this.updateCoreHandler();

		// Is there a knowledge core?
		if( this.internalInventory.getHasStack( TileArcaneAssembler.KCORE_SLOT_INDEX ) )
		{
			// Is there a pattern?
			if( this.currentPattern != null )
			{
				// Set the patterns core
				this.currentPattern.setKnowledgeCore( this.internalInventory.getStackInSlot( TileArcaneAssembler.KCORE_SLOT_INDEX ) );
			}
		}
		else if( this.currentPattern != null )
		{
			// Clear the pattern
			this.isCrafting = false;
			this.currentPattern = null;
		}

		// Recalculate the vis discounts
		this.calculateVisDiscounts();
	}

	@TileEvent(TileEventType.NETWORK_READ)
	@SideOnly(Side.CLIENT)
	public boolean onReceiveNetworkData( final ByteBuf stream )
	{
		// Read the active state
		this.isActive = stream.readBoolean();

		// Read the crafting status
		this.isCrafting = stream.readBoolean();
		if( this.isCrafting )
		{
			// Read the crafting progress
			this.craftTickCounter = stream.readInt();

			// Add  particles
			for( int i = 0; i < 2; i++ )
			{
				Thaumcraft.proxy.blockRunes( this.worldObj, this.xCoord, this.yCoord, this.zCoord, 0.5F, 0.0F, 0.5F, 10, -0.1F );
			}
		}

		// Clear old vis values
		this.storedVis.aspects.clear();

		// Read each stored aspect
		for( int i = 0; i < TileArcaneAssembler.PRIMALS.length; i++ )
		{
			// Read the stored amount
			this.storedVis.add( TileArcaneAssembler.PRIMALS[i], stream.readInt() );
		}

		// Read upgrade count
		this.upgradeCount = stream.readInt();

		return true;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void onSendNetworkData( final ByteBuf stream ) throws IOException
	{
		// Write the active state
		stream.writeBoolean( this.isActive() );

		// Write if the assembler is crafting
		stream.writeBoolean( this.isCrafting );
		if( this.isCrafting )
		{
			// Write the crafting progress
			stream.writeInt( this.craftTickCounter );
		}

		// Write each stored aspect
		for( int i = 0; i < TileArcaneAssembler.PRIMALS.length; i++ )
		{
			// Write the stored amount
			stream.writeInt( this.storedVis.getAmount( TileArcaneAssembler.PRIMALS[i] ) );
		}

		// Write the upgrade count
		stream.writeInt( this.upgradeCount );
	}

	@TileEvent(TileEventType.TICK)
	public void onTick()
	{
		// Vis discounts need to be updated?
		if( this.flag_RecalcVis )
		{
			this.calculateVisDiscounts();
		}

		if( EffectiveSide.isClientSide() )
		{
			if( ( this.isCrafting ) && ( this.craftTickCounter < TileArcaneAssembler.BASE_TICKS_PER_CRAFT ) )
			{
				this.craftTickCounter++ ;
			}

			// Ignore the rest on client side.
			return;
		}

		// Core changed?
		if( this.flag_CoreChanged )
		{
			this.updateCoreHandler();
		}

		// Ensure the assembler is active
		if( !this.isActive() )
		{
			return;
		}

		// Are the network patterns stale?
		if( this.stalePatterns && this.getProxy().isReady() )
		{
			try
			{
				// Inform the network
				this.getProxy().getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.getActionableNode() ) );

				// Mark they are no longer stale
				this.stalePatterns = false;
			}
			catch( GridAccessException e )
			{
			}
		}

		// Is the assembler linked to a vis relay?
		if( this.visSourceInfo.hasSourceData() )
		{
			// Increment the vis tick counter.
			this.visTickCounter++ ;

			// Has five ticks elapsed?
			if( this.visTickCounter == 5 )
			{
				// Reset the tick counter
				this.visTickCounter = 0;

				// Replenish vis stores
				this.replenishVis();
			}
		}

		// Is there a delayed update queued?
		if( this.delayedUpdate )
		{
			// Increase the counter
			this.delayTickCounter++ ;

			// Have 5 ticks elapsed?
			if( this.delayTickCounter >= 5 )
			{
				// Mark for an update
				this.markForUpdate();

				// Reset the trackers
				this.delayedUpdate = false;
				this.delayTickCounter = 0;
			}
		}

		// Is the assembler crafting?
		if( this.isCrafting )
		{
			// Tick crafting
			this.craftingTick();
		}
	}

	@MENetworkEventSubscribe
	public final void powerEvent( final MENetworkPowerStatusChange event )
	{
		// Mark for update
		this.markForUpdate();
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
		if( ( !this.isCrafting ) && ( patternDetails instanceof ArcaneCraftingPattern ) )
		{
			// Mark that crafting has begun
			this.isCrafting = true;

			// Reset the crafting tick counter
			this.craftTickCounter = 0;

			// Set the pattern that is being crafted
			this.currentPattern = (ArcaneCraftingPattern)patternDetails;

			// Set the target item
			this.internalInventory.setInventorySlotContents( TileArcaneAssembler.TARGET_SLOT_INDEX, this.currentPattern.getResult().getItemStack() );

			// AE effects
			try
			{
				NetworkRegistry.TargetPoint where = new NetworkRegistry.TargetPoint( this.worldObj.provider.dimensionId, this.xCoord, this.yCoord,
								this.zCoord, 32.0D );
				appeng.core.sync.network.NetworkHandler.instance.sendToAllAround( new PacketAssemblerAnimation( this.xCoord, this.yCoord,
								this.zCoord, (byte)( 10 + ( 9 * this.upgradeCount ) ), this.currentPattern.getResult() ),
					where );
			}
			catch( IOException e )
			{
			}

			return true;
		}

		return false;
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_ArcaneAssembler( final NBTTagCompound data )
	{
		// Is there relay info?
		if( data.hasKey( TileArcaneAssembler.NBTKEY_VIS_INTERFACE ) )
		{
			this.visSourceInfo.readFromNBT( data, TileArcaneAssembler.NBTKEY_VIS_INTERFACE );
		}

		// Read vis levels
		this.readVisLevelsFromNBT( data );

		// Read upgrade count
		if( data.hasKey( TileArcaneAssembler.NBTKEY_UPGRADES ) )
		{
			this.upgradeCount = data.getInteger( TileArcaneAssembler.NBTKEY_UPGRADES );
		}

		// Read upgrade inventory
		this.upgradeInventory.readFromNBT( data, TileArcaneAssembler.NBTKEY_UPGRADEINV );

		// Read the crafting status
		if( data.hasKey( TileArcaneAssembler.NBTKEY_CRAFTING ) )
		{
			this.isCrafting = data.getBoolean( TileArcaneAssembler.NBTKEY_CRAFTING );
		}

		// Read the pattern
		if( data.hasKey( TileArcaneAssembler.NBTKEY_CRAFTING_PATTERN ) )
		{
			this.currentPattern = new ArcaneCraftingPattern( null, data.getCompoundTag( TileArcaneAssembler.NBTKEY_CRAFTING_PATTERN ) );
		}

	}

	/**
	 * Reads the stored vis levels.
	 *
	 * @param data
	 */
	public void readVisLevelsFromNBT( final NBTTagCompound data )
	{
		if( data.hasKey( TileArcaneAssembler.NBTKEY_STORED_VIS ) )
		{
			this.storedVis.readFromNBT( data, TileArcaneAssembler.NBTKEY_STORED_VIS );
		}
	}

	/**
	 * Sets the owner of this tile.
	 *
	 * @param player
	 */
	public void setOwner( final EntityPlayer player )
	{
		this.getProxy().setOwner( player );
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_ArcaneAssembler( final NBTTagCompound data )
	{
		// Write the vis interface
		this.visSourceInfo.writeToNBT( data, TileArcaneAssembler.NBTKEY_VIS_INTERFACE );

		// Write the stored vis
		this.writeVisLevelsToNBT( data );

		// Write the number of upgrades
		data.setInteger( TileArcaneAssembler.NBTKEY_UPGRADES, this.upgradeCount );

		// Write the upgrade inventory
		this.upgradeInventory.writeToNBT( data, TileArcaneAssembler.NBTKEY_UPGRADEINV );

		// Write the crafting state
		data.setBoolean( TileArcaneAssembler.NBTKEY_CRAFTING, this.isCrafting );

		// Write the current pattern
		if( this.currentPattern != null )
		{
			data.setTag( TileArcaneAssembler.NBTKEY_CRAFTING_PATTERN, this.currentPattern.writeToNBT( new NBTTagCompound() ) );
		}

	}

	/**
	 * Writes the stored vis levels.
	 *
	 * @param data
	 */
	public void writeVisLevelsToNBT( final NBTTagCompound data )
	{
		if( this.storedVis.size() > 0 )
		{
			this.storedVis.writeToNBT( data, TileArcaneAssembler.NBTKEY_STORED_VIS );
		}
	}

}
