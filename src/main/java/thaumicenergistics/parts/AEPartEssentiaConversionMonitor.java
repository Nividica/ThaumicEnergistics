package thaumicenergistics.parts;

import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.WorldSettings;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class AEPartEssentiaConversionMonitor
	extends AEPartEssentiaStorageMonitor
{
	/**
	 * The number of ticks considered to be a double click.
	 */
	private static long DOUBLE_CLICK_TICKS = 2 * 20;

	/**
	 * The last player ID that deposited essentia
	 */
	private int depositedPlayerID = -1;

	/**
	 * The tick count of the last deposited essentia
	 */
	private int depositedTick = 0;

	/**
	 * The aspect that was last deposited.
	 */
	private Aspect depositedAspect = null;

	public AEPartEssentiaConversionMonitor()
	{
		super( AEPartsEnum.EssentiaConversionMonitor );

		this.darkCornerTexture = CableBusTextures.PartConversionMonitor_Colored;
		this.lightCornerTexture = CableBusTextures.PartConversionMonitor_Dark;
	}

	/**
	 * Drains the container at the specified index.
	 * 
	 * @param player
	 * @param slotIndex
	 * @param mustMatchAspect
	 * @return
	 */
	private boolean drainEssentiaContainer( final EntityPlayer player, final int slotIndex, final Aspect mustMatchAspect )
	{
		// Get the container
		ItemStack container = player.inventory.getStackInSlot( slotIndex );

		// Create request
		IAEFluidStack request = EssentiaConversionHelper.INSTANCE.createAEFluidStackFromItemEssentiaContainer( container );

		// Is there anything to request?
		if( request == null )
		{
			return false;
		}

		// Get the aspect
		Aspect containerAspect = ( (GaseousEssentia)request.getFluid() ).getAspect();

		// Is there a must match aspect?
		if( mustMatchAspect != null )
		{
			// Do the aspects match?
			if( containerAspect != mustMatchAspect )
			{
				// Mismatch
				return false;
			}
		}

		// Calculate how much to take from the container
		int drainAmount_E = (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( request.getStackSize() );

		// Is there enough power?
		if( !this.extractPowerForEssentiaTransfer( drainAmount_E, Actionable.SIMULATE ) )
		{
			// Not enough power.
			return false;
		}

		// Inject fluid
		IAEFluidStack rejected = this.injectFluid( request, Actionable.MODULATE, new PlayerSource( player, this ) );

		// How much is left over?
		int rejectedAmount_E = 0;
		if( rejected != null )
		{
			rejectedAmount_E = (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( rejected.getStackSize() );
		}

		// Update the drain amount
		drainAmount_E = drainAmount_E - rejectedAmount_E;

		if( drainAmount_E <= 0 )
		{
			// Could not inject
			return false;
		}

		// Drain the container
		ImmutablePair<Integer, ItemStack> drained = EssentiaItemContainerHelper.INSTANCE.extractFromContainer( container, drainAmount_E );

		// Update the player inventory
		player.inventory.decrStackSize( slotIndex, 1 );
		if( drained != null )
		{
			player.inventory.addItemStackToInventory( drained.right );
		}

		// Extract power
		this.extractPowerForEssentiaTransfer( drainAmount_E, Actionable.MODULATE );

		// Set the last aspect
		this.depositedAspect = containerAspect;

		return true;

	}

	/**
	 * Fills the container the player is holding.
	 * 
	 * @param player
	 * @param heldItem
	 * @return
	 */
	private boolean fillEssentiaContainer( final EntityPlayer player, final ItemStack heldItem )
	{
		// Is the item being held a label?
		if( EssentiaItemContainerHelper.INSTANCE.isLabel( heldItem ) )
		{
			// Set the label type
			EssentiaItemContainerHelper.INSTANCE.setLabelAspect( heldItem, this.trackedEssentia.getAspect() );
			return true;
		}

		// Does the player have extract permission?
		if( !this.doesPlayerHavePermission( player, SecurityPermissions.EXTRACT ) )
		{
			return false;
		}

		// Get how much is in the container
		int containerAmount = EssentiaItemContainerHelper.INSTANCE.getContainerStoredAmount( heldItem );

		// Is there existing essentia in the container?
		if( containerAmount > 0 )
		{
			// Get the container aspect
			Aspect containerAspect = EssentiaItemContainerHelper.INSTANCE.getAspectInContainer( heldItem );

			// Ensure it matches the tracker
			if( this.trackedEssentia.getAspect() != containerAspect )
			{
				return false;
			}
		}

		// Is there a jar label?
		Aspect jarLabelAspect = EssentiaItemContainerHelper.INSTANCE.getJarLabelAspect( heldItem );
		if( jarLabelAspect != null )
		{
			// Ensure it matches the tracker
			if( this.trackedEssentia.getAspect() != jarLabelAspect )
			{
				return false;
			}
		}

		// Get how much the container can hold
		int containerCapacity = EssentiaItemContainerHelper.INSTANCE.getContainerCapacity( heldItem );

		// Calculate how much to fill
		int amountToFill_E = containerCapacity - containerAmount;

		// Is the container full?
		if( amountToFill_E <= 0 )
		{
			// Container is full
			return false;
		}

		// Is there enough power?
		if( !this.extractPowerForEssentiaTransfer( amountToFill_E, Actionable.SIMULATE ) )
		{
			// Not enough power
			return false;
		}

		// Create the fluid stack
		IAEFluidStack request = EssentiaConversionHelper.INSTANCE
						.createAEFluidStackInEssentiaUnits( this.trackedEssentia.getAspect(), amountToFill_E );

		// Set the player source
		PlayerSource playerSource = new PlayerSource( player, this );

		// Request the fluid
		IAEFluidStack extracted = this.extractFluid( request, Actionable.SIMULATE, playerSource );

		// Was any extracted?
		if( ( extracted == null ) || ( extracted.getStackSize() <= 0 ) )
		{
			// None extracted
			return false;
		}
		// Update values based on how much was extracted
		request.setStackSize( extracted.getStackSize() );
		amountToFill_E = (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( request.getStackSize() );

		// Fill the container
		ImmutablePair<Integer, ItemStack> filledContainer = EssentiaItemContainerHelper.INSTANCE.injectIntoContainer( heldItem, new AspectStack(
						this.trackedEssentia.getAspect(), amountToFill_E ) );

		// Could the container be filled?
		if( filledContainer == null )
		{
			return false;
		}

		// Take original container
		player.inventory.decrStackSize( player.inventory.currentItem, 1 );

		// Add filled container
		InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
		ItemStack rejected = adaptor.addItems( filledContainer.right );
		if( rejected != null )
		{
			// Get the host tile entity and side
			TileEntity te = this.getHostTile();
			ForgeDirection side = this.getSide();

			List<ItemStack> list = Collections.singletonList( rejected );
			Platform.spawnDrops( player.worldObj, te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ, list );
		}

		if( player.openContainer != null )
		{
			player.openContainer.detectAndSendChanges();
		}

		// Extract the fluid
		this.extractFluid( request, Actionable.MODULATE, playerSource );

		// Take power
		this.extractPowerForEssentiaTransfer( amountToFill_E, Actionable.MODULATE );

		// Done
		return true;
	}

	/**
	 * Drains all containers in the players inventory.
	 * 
	 * @param player
	 */
	private void insertAllEssentiaIntoNetwork( final EntityPlayer player )
	{
		ItemStack tracking = null;
		int prevStackSize = 0;

		for( int slotIndex = 0; slotIndex < player.inventory.getSizeInventory(); ++slotIndex )
		{
			// Get the stack
			tracking = player.inventory.getStackInSlot( slotIndex );

			// Is it null or empty?
			if( ( tracking == null ) || ( ( prevStackSize = tracking.stackSize ) == 0 ) )
			{
				// Empty slot
				continue;
			}

			// Attempt to drain
			this.drainEssentiaContainer( player, slotIndex, this.depositedAspect );

			// Get the updated stack
			tracking = player.inventory.getStackInSlot( slotIndex );

			// Is there anything left
			if( ( tracking != null ) && ( tracking.stackSize > 0 ) )
			{
				// Did the stack size change?
				if( prevStackSize != tracking.stackSize )
				{
					// Visit this slot again
					--slotIndex;
				}
			}

		}
	}

	/**
	 * Sets the last interaction trackers.
	 * 
	 * @param player
	 */
	private void markFirstClick( final EntityPlayer player )
	{
		// Set the ID
		this.depositedPlayerID = WorldSettings.getInstance().getPlayerID( player.getGameProfile() );

		// Set the time
		this.depositedTick = MinecraftServer.getServer().getTickCounter();
	}

	/**
	 * Returns true if double clicked.
	 * 
	 * @param player
	 * @return
	 */
	private boolean wasDoubleClick( final EntityPlayer player )
	{
		// Is this the same player that just used the monitor?
		if( ( this.depositedPlayerID != -1 ) && ( this.depositedPlayerID == WorldSettings.getInstance().getPlayerID( player.getGameProfile() ) ) )
		{
			// Was it a double click?
			if( MinecraftServer.getServer().getTickCounter() - this.depositedTick <= AEPartEssentiaConversionMonitor.DOUBLE_CLICK_TICKS )
			{
				// Reset last interaction trackers
				this.depositedPlayerID = -1;
				this.depositedTick = 0;
				return true;
			}
		}

		// Wrong player, or time between clicks to long.
		return false;
	}

	/**
	 * Extracts fluid from the ME network.
	 * 
	 * @param toExtract
	 * @param mode
	 * @return
	 */
	protected final IAEFluidStack extractFluid( final IAEFluidStack toExtract, final Actionable mode, final BaseActionSource source )
	{
		IMEMonitor<IAEFluidStack> monitor = this.getGridBlock().getFluidMonitor();

		if( monitor == null )
		{
			return null;
		}

		return monitor.extractItems( toExtract, mode, source );
	}

	// TODO: This should be generalized/abstracted better. This particular functionality is duplicated all over the place and needs to be centralized.
	/**
	 * Injects fluid into the ME network.
	 * Returns what was not stored.
	 * 
	 * @param toInject
	 * @param mode
	 * @return
	 */
	protected final IAEFluidStack injectFluid( final IAEFluidStack toInject, final Actionable mode, final BaseActionSource source )
	{
		IMEMonitor<IAEFluidStack> monitor = this.getGridBlock().getFluidMonitor();

		if( monitor == null )
		{
			return null;
		}

		return monitor.injectItems( toInject, mode, source );
	}

	/**
	 * Attempts to fill an essentia container if monitor locked
	 */
	@Override
	protected boolean onActivatedWithEssentiaContainerOrLabel( final EntityPlayer player, final ItemStack heldItem )
	{
		// Is there nothing being tracked, or is the monitor unlocked?
		if( !this.trackedEssentia.isValid() || !this.isLocked() )
		{
			// Pass to super
			return super.onActivatedWithEssentiaContainerOrLabel( player, heldItem );
		}

		// Fill the container
		return this.fillEssentiaContainer( player, heldItem );

	}

	@Override
	public boolean onShiftActivate( final EntityPlayer player, final Vec3 position )
	{
		// Ignore client side.
		if( EffectiveSide.isClientSide() )
		{
			return true;
		}

		// Permission and activation checks
		if( !activationCheck( player ) )
		{
			return false;
		}

		// Does the player have inject permission?
		if( !this.doesPlayerHavePermission( player, SecurityPermissions.INJECT ) )
		{
			return false;
		}

		// Get the item the player is holding
		ItemStack heldItem = player.getCurrentEquippedItem();

		// Is the player holding an essentia container?
		if( !EssentiaItemContainerHelper.INSTANCE.isContainerOrLabel( heldItem ) )
		{
			// Not holding container
			return false;
		}

		// Shift-right-clicking attempts to insert the essentia into the network.
		// Shift-double-right-clicking attempts to insert all essentia in the players inventory into the network.

		// Is the item being held a label?
		if( EssentiaItemContainerHelper.INSTANCE.isLabel( heldItem ) )
		{
			// Can't do anything with a label.
			return false;
		}

		// Was it a double click?
		if( this.wasDoubleClick( player ) )
		{
			// Attempt to insert all essentia
			this.insertAllEssentiaIntoNetwork( player );
			return true;
		}

		// Drain the container
		boolean didDrain = this.drainEssentiaContainer( player, player.inventory.currentItem, null );
		if( didDrain )
		{
			this.markFirstClick( player );
		}

		return didDrain;
	}

}
