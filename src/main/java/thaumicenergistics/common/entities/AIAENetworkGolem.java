package thaumicenergistics.common.entities;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.common.grid.WirelessAELink;
import thaumicenergistics.common.integration.tc.GolemUpgradeTypes;
import thaumicenergistics.common.items.ItemGolemWirelessBackpack;

/**
 * Base AI script for golems wearing the {@link ItemGolemWirelessBackpack}.
 *
 * @author Nividica
 *
 */
public abstract class AIAENetworkGolem
	extends EntityAIBase
{
	protected class NetworkHandler
		extends WirelessAELink
	{
		/**
		 * The maximum number of items the golem can inject/extract per update.
		 */
		private final int maxItemRate;

		/**
		 * The maximum amount of fluid, in mb, the golem can inject/extract per update.
		 */
		private final int maxFluidRate;

		/**
		 * The maximum amount of essentia the golem can inject/extract per update.
		 */
		private final int maxEssentiaRate;

		/**
		 * Data associated with the wireless state of the backpack.
		 */
		public final WirelessGolemHandler.WirelessServerData wirelessStateData;

		public NetworkHandler( final WirelessGolemHandler.WirelessServerData wsd, final int maxItems, final int maxFluids, final int maxEssentia )
		{
			// Call super
			super( null, wsd.encryptionKey );

			// Set maximums
			this.maxItemRate = maxItems;
			this.maxFluidRate = maxFluids;
			this.maxEssentiaRate = maxEssentia;

			// Set state data
			this.wirelessStateData = wsd;

			// Attempt to connect to AP
			this.isConnected();

		}

		@Override
		protected int getUserPositionX()
		{
			return (int)AIAENetworkGolem.this.golem.posX;
		}

		@Override
		protected int getUserPositionY()
		{
			return (int)AIAENetworkGolem.this.golem.posY;
		}

		@Override
		protected int getUserPositionZ()
		{
			return (int)AIAENetworkGolem.this.golem.posZ;
		}

		@Override
		protected World getUserWorld()
		{
			return AIAENetworkGolem.this.golem.worldObj;
		}

		@Override
		protected boolean hasPowerToCommunicate()
		{
			// MAGIC GOLEM POWER! YEAH!!
			return true;
		}

		/**
		 * Attempts to deposit the itemstack into the AE system.
		 * The {@code stack.stacksize} will change according to how many items were left over after the deposit.
		 *
		 * @param stack
		 */
		public void depositStack( final ItemStack stack )
		{
			// Get the item monitor
			IMEMonitor<IAEItemStack> monitor = this.getItemInventory();
			if( monitor == null )
			{
				return;
			}

			// Create the AE stack
			IAEItemStack aeStack = AEApi.instance().storage().createItemStack( stack );

			// Set size
			int depositSize = Math.min( stack.stackSize, this.maxItemRate );
			aeStack.setStackSize( depositSize );

			// Deposit
			IAEItemStack rejected = AEApi.instance().storage().poweredInsert( this.getEnergyGrid(), monitor, aeStack, this.actionSource );
			if( rejected != null )
			{
				depositSize -= (int)rejected.getStackSize();
			}

			// Reduce stack by number of items deposited
			stack.stackSize -= depositSize;
		}

		/**
		 * Extracts essentia from the network.
		 *
		 * @param aspect
		 * @param amount
		 * @param mode
		 * @return The amount extracted.
		 */
		public long extractEssentia( final Aspect aspect, final int amount, final Actionable mode )
		{
			// Get the essentia monitor
			IMEEssentiaMonitor monitor = this.getEssentiaInventory();
			if( monitor == null )
			{
				return 0;
			}

			return monitor.extractEssentia( aspect, Math.min( amount, this.maxEssentiaRate ), mode, this.actionSource, mode == Actionable.MODULATE );

		}

		/**
		 * Extracts fluids from the network.
		 *
		 * @param target
		 * @return
		 */
		public FluidStack extractFluid( final FluidStack target )
		{
			// Get the fluid monitor
			IMEMonitor<IAEFluidStack> monitor = this.getFluidInventory();
			if( monitor == null )
			{
				return null;
			}

			// Get power grid
			IEnergyGrid eGrid = this.getEnergyGrid();
			if( eGrid == null )
			{
				return null;
			}

			// Calculate request size
			long requestSize = Math.min( target.amount, this.maxFluidRate );

			// Calculate power required
			double pwrReq = requestSize / 100;
			if( eGrid.extractAEPower( pwrReq, Actionable.SIMULATE, PowerMultiplier.CONFIG ) < pwrReq )
			{
				// Not enough power
				return null;
			}

			// Create the fluid stack
			IAEFluidStack aeRequest = AEApi.instance().storage().createFluidStack( target );

			// Set size
			aeRequest.setStackSize( requestSize );

			// Extract
			IAEFluidStack extracted = monitor.extractItems( aeRequest, Actionable.MODULATE, this.actionSource );
			if( extracted == null )
			{
				return null;
			}

			// Take power
			pwrReq = extracted.getStackSize() / 100;
			eGrid.extractAEPower( pwrReq, Actionable.MODULATE, PowerMultiplier.CONFIG );

			return extracted.getFluidStack();

		}

		/**
		 * Extracts items from the network.
		 *
		 * @param target
		 * @return Extracted stack.
		 */
		public ItemStack extractStack( final ItemStack target )
		{
			// Get the item monitor
			IMEMonitor<IAEItemStack> monitor = this.getItemInventory();
			if( monitor == null )
			{
				return null;
			}

			// Create the AE stack
			IAEItemStack aeRequest = AEApi.instance().storage().createItemStack( target );

			// Set size
			aeRequest.setStackSize( Math.min( target.stackSize, this.maxItemRate ) );

			// Extract
			IAEItemStack extracted = AEApi.instance().storage().poweredExtraction( this.getEnergyGrid(), monitor, aeRequest, this.actionSource );
			if( extracted == null )
			{
				return null;
			}

			return extracted.getItemStack();
		}

		/**
		 * Inserts essentia into the network.
		 *
		 * @param aspect
		 * @param amount
		 * @return Amount injected
		 */
		public long insertEssentia( final Aspect aspect, final int amount )
		{
			// Get the essentia monitor
			IMEEssentiaMonitor monitor = this.getEssentiaInventory();
			if( monitor == null )
			{
				return 0;
			}

			// Calculate the amount to inject
			int amountToInject = Math.min( amount, this.maxEssentiaRate );

			// Attempt to inject
			long amountRejected = monitor.injectEssentia( aspect, amountToInject, Actionable.MODULATE, this.actionSource, true );

			// Return the amount that was injected
			return amountToInject - amountRejected;
		}

		/**
		 * Checks if the network can be interacted with.
		 *
		 * @return
		 */
		@Override
		public boolean isConnected()
		{
			// Connected to network?
			this.wirelessStateData.isInRange = super.isConnected();
			return this.wirelessStateData.isInRange;
		}

	}

	private static final int NETWORK_COOLDOWN = 20;

	/**
	 * <PRE>
	 * Base     100, 250, 500
	 * Advanced 200, 500, 1000
	 * </PRE>
	 */
	private static final int[] FLUID_RATES = new int[] { 100, 250, 500 };

	/**
	 * <PRE>
	 * Base      8, 24, 32
	 * Advanced 16, 48, 64
	 * </PRE>
	 */
	private static final int[] ITEM_RATES = new int[] { 8, 24, 32 };

	/**
	 * <PRE>
	 * Base     4, 12, 16
	 * Advanced 8, 24, 32
	 * </PRE>
	 */
	private static final int[] ESS_RATES = new int[] { 4, 12, 16 };

	/**
	 * How many ticks until the golem can interact with the network again.
	 */
	private int actionTimer = 0;

	/**
	 * Handles AE network communication.
	 */
	public final NetworkHandler network;

	/**
	 * The golem the script is attached to.
	 */
	protected final EntityGolemBase golem;

	public AIAENetworkGolem( final EntityGolemBase golem, final WirelessGolemHandler.WirelessServerData wsd )
	{
		// Set the golem
		this.golem = golem;

		// Get rate multiplier
		int rateMult = ( golem.advanced ? 2 : 1 );

		// Get number of order upgrades
		int orderUpgrades = this.golem.getUpgradeAmount( GolemUpgradeTypes.Order.upgradeID );
		if( orderUpgrades > 2 )
		{
			orderUpgrades = 2;
		}
		else if( orderUpgrades < 0 )
		{
			orderUpgrades = 0;
		}

		// Set interaction maximums
		int maxItems = ITEM_RATES[orderUpgrades] * rateMult;
		int maxEssentia = ESS_RATES[orderUpgrades] * rateMult;
		int maxFluids = FLUID_RATES[orderUpgrades] * rateMult;

		// Create the network handler
		this.network = new NetworkHandler( wsd, maxItems, maxFluids, maxEssentia );
	}

	/**
	 * Called to decrement the action timer.
	 *
	 * @return True if cooled down.
	 */
	private boolean cooledDown()
	{
		if( this.actionTimer > 0 )
		{
			--this.actionTimer;
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	private void restartNetworkCooldown()
	{
		// Reset network timer
		this.actionTimer = NETWORK_COOLDOWN;
	}

	/**
	 * Return true if network interaction is required this update.
	 *
	 * @return
	 */
	protected abstract boolean needsNetworkNow();

	@Override
	public final boolean shouldExecute()
	{
		// Check the network timer
		if( this.cooledDown() )
		{
			this.restartNetworkCooldown();

			// IsConnected needs to be checked before needsNetwork
			// This ensures the wireless state data is kept up-to-date
			if( this.network.isConnected() )
			{
				return this.needsNetworkNow();
			}
		}
		return false;
	}

	@Override
	public abstract void updateTask();

}
