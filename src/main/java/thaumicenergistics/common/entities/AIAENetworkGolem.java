package thaumicenergistics.common.entities;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumicenergistics.common.grid.WirelessAELink;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;

public abstract class AIAENetworkGolem
	extends EntityAIBase
{

	protected class NetworkHandler
		extends WirelessAELink
	{
		private static final int NETWORK_COOLDOWN = 15;

		/**
		 * The maximum number of items the golem can inject/extract per update.
		 */
		private final int maxItems;

		/**
		 * The maximum amount of fluid, in mb, the golem can inject/extract per update.
		 */
		private final int maxFluids;

		/**
		 * The maximum amount of essentia the golem can inject/extract per update.
		 */
		private final int maxEssentia;

		/**
		 * How many ticks until the golem can interact with the network again.
		 */
		private int actionTimer = 0;

		public NetworkHandler( final String encryptionKey, final int maxItems, final int maxFluids, final int maxEssentia )
		{
			// Call super
			super( null, encryptionKey );

			// Set maximums
			this.maxItems = maxItems;
			this.maxFluids = maxFluids;
			this.maxEssentia = maxEssentia;
		}

		/**
		 * Gets the item monitor if possible.
		 * 
		 * @return
		 */
		private IMEMonitor<IAEItemStack> getNetworkItems()
		{
			// Check connectivity
			if( !this.canInteractWithNetwork() )
			{
				return null;
			}

			// Get the item monitor
			return this.getItemInventory();
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
		 * Checks if the network can be interacted with.
		 * 
		 * @return
		 */
		public boolean canInteractWithNetwork()
		{
			// Is the interaction timer cooling down?
			if( this.actionTimer > 0 )
			{
				return false;
			}

			// Connected to network?
			if( !this.isConnected() )
			{
				return false;
			}

			return true;
		}

		/**
		 * Called to decrement the action timer.
		 * 
		 * @return True if cooled down.
		 */
		public boolean cooldownTick()
		{
			if( this.actionTimer > 0 )
			{
				--this.actionTimer;
				return false;
			}
			return true;
		}

		/**
		 * Deposits the itemstack into the AE system.
		 */
		public void depositStack( final ItemStack stack )
		{
			// Get the item monitor
			IMEMonitor<IAEItemStack> monitor = this.getNetworkItems();
			if( monitor == null )
			{
				return;
			}

			// Create the AE stack
			IAEItemStack aeStack = AEApi.instance().storage().createItemStack( stack );

			// Set size
			int depositSize = Math.min( stack.stackSize, this.maxItems );
			aeStack.setStackSize( depositSize );

			// Deposit
			IAEItemStack rejected = monitor.injectItems( aeStack, Actionable.MODULATE, this.actionSource );
			if( rejected != null )
			{
				depositSize -= (int)rejected.getStackSize();
			}

			// Reduce stack by number of items deposited
			stack.stackSize -= depositSize;

			// Reset network timer
			this.actionTimer = NETWORK_COOLDOWN;
		}

	}

	/**
	 * Handles AE network communication.
	 */
	public final NetworkHandler network;

	/**
	 * The golem the script is attached to.
	 */
	protected final EntityGolemBase golem;

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
	 * Base      8, 24, 32
	 * Advanced 16, 48, 64
	 * </PRE>
	 */
	private static final int[] ESS_RATES = new int[] { 8, 24, 32 };

	public AIAENetworkGolem( final EntityGolemBase golem, final String encKey )
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
		this.network = new NetworkHandler( encKey, maxItems, maxFluids, maxEssentia );
	}

	/**
	 * Return true if network interaction is required this update.
	 * 
	 * @return
	 */
	protected abstract boolean needsNetworkInteraction();

	@Override
	public final boolean shouldExecute()
	{
		// Check the network timer
		if( this.network.cooldownTick() )
		{
			return this.needsNetworkInteraction();
		}
		return false;
	}

}
