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
import thaumicenergistics.grid.IMEEssentiaMonitor;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.PlayerSource;
import appeng.client.texture.CableBusTextures;
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
		AspectStack request = EssentiaItemContainerHelper.INSTANCE.getAspectStackFromContainer( container );

		// Is there anything to request?
		if( request == null )
		{
			return false;
		}

		// Is there a must match aspect?
		if( mustMatchAspect != null )
		{
			// Do the aspects match?
			if( request.aspect != mustMatchAspect )
			{
				// Mismatch
				return false;
			}
		}

		// Get the monitor
		IMEEssentiaMonitor essMonitor = this.getGridBlock().getEssentiaMonitor();
		if( essMonitor == null )
		{
			return false;
		}

		// Inject the essentia
		long rejected = essMonitor.injectEssentia( request.aspect, request.stackSize, Actionable.MODULATE, new PlayerSource( player, this ), true );

		// Adjust the request
		request.stackSize -= rejected;

		if( request.stackSize <= 0 )
		{
			// Could not inject
			return false;
		}

		// Drain the container
		ImmutablePair<Integer, ItemStack> drained = EssentiaItemContainerHelper.INSTANCE.extractFromContainer( container, request );

		// Update the player inventory
		player.inventory.decrStackSize( slotIndex, 1 );
		if( drained != null )
		{
			player.inventory.addItemStackToInventory( drained.right );
		}

		// Set the last aspect
		this.depositedAspect = request.aspect;

		return true;

	}

	/**
	 * Fills the container the player is holding.
	 * 
	 * @param player
	 * @param heldItem
	 * @return
	 */
	private boolean fillEssentiaContainer( final EntityPlayer player, final ItemStack heldItem, final AspectItemType itemType )
	{
		// Is the item being held a label?
		if( itemType == AspectItemType.JarLabel )
		{
			// Set the label type
			EssentiaItemContainerHelper.INSTANCE.setLabelAspect( heldItem, this.trackedEssentia.getAspectStack().aspect );
			return true;
		}

		// Is the item not a container?
		if( itemType != AspectItemType.EssentiaContainer )
		{
			// Not a container
			return false;
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
			if( this.trackedEssentia.getAspectStack().aspect != containerAspect )
			{
				return false;
			}
		}

		// Is there a jar label?
		Aspect jarLabelAspect = EssentiaItemContainerHelper.INSTANCE.getJarLabelAspect( heldItem );
		if( jarLabelAspect != null )
		{
			// Ensure it matches the tracker
			if( this.trackedEssentia.getAspectStack().aspect != jarLabelAspect )
			{
				return false;
			}
		}

		// Get the monitor
		IMEEssentiaMonitor essMonitor = this.getGridBlock().getEssentiaMonitor();
		if( essMonitor == null )
		{
			return false;
		}

		// Get how much the container can hold
		int containerCapacity = EssentiaItemContainerHelper.INSTANCE.getContainerCapacity( heldItem );

		// Create the request
		AspectStack fillRequest = new AspectStack( this.trackedEssentia.getAspectStack().aspect, containerCapacity - containerAmount );

		// Is the container full?
		if( fillRequest.stackSize <= 0 )
		{
			// Container is full
			return false;
		}

		// Set the player source
		PlayerSource playerSource = new PlayerSource( player, this );

		// Simulate the request
		long extractedAmount = essMonitor.extractEssentia( fillRequest.aspect, fillRequest.stackSize, Actionable.SIMULATE, playerSource, true );

		// Was any extracted?
		if( extractedAmount <= 0 )
		{
			// None extracted
			return false;
		}

		// Update values based on how much was extracted
		fillRequest.stackSize = extractedAmount;

		// Fill the container
		ImmutablePair<Integer, ItemStack> filledContainer = EssentiaItemContainerHelper.INSTANCE.injectIntoContainer( heldItem, fillRequest );

		// Could the container be filled?
		if( filledContainer == null )
		{
			return false;
		}

		// Take original container
		player.inventory.decrStackSize( player.inventory.currentItem, 1 );

		// Add filled container
		InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
		ItemStack rejectedItem = adaptor.addItems( filledContainer.right );
		if( rejectedItem != null )
		{
			// Get the host tile entity and side
			TileEntity te = this.getHostTile();
			ForgeDirection side = this.getSide();

			List<ItemStack> list = Collections.singletonList( rejectedItem );
			Platform.spawnDrops( player.worldObj, te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ, list );
		}

		// Update the client
		if( player.openContainer != null )
		{
			player.openContainer.detectAndSendChanges();
		}

		// Extract the essentia
		essMonitor.extractEssentia( fillRequest.aspect, fillRequest.stackSize, Actionable.MODULATE, playerSource, true );

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
		this.depositedPlayerID = AEApi.instance().registries().players().getID( player.getGameProfile() );

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
		if( ( this.depositedPlayerID != -1 ) && ( this.depositedPlayerID == AEApi.instance().registries().players().getID( player.getGameProfile() ) ) )
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
	 * Attempts to fill an essentia container if monitor locked
	 */
	@Override
	protected boolean onActivateWithAspectItem( final EntityPlayer player, final ItemStack heldItem, final AspectItemType itemType )
	{
		// Is there nothing being tracked, or is the monitor unlocked?
		if( !this.trackedEssentia.isValid() || !this.isLocked() )
		{
			// Pass to super
			return super.onActivateWithAspectItem( player, heldItem, itemType );
		}

		// Fill the container
		return this.fillEssentiaContainer( player, heldItem, itemType );

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
		if( EssentiaItemContainerHelper.INSTANCE.getItemType( heldItem ) != AspectItemType.EssentiaContainer )
		{
			// Not holding valid container
			return false;
		}

		// Shift-right-clicking attempts to insert the essentia into the network.
		// Shift-double-right-clicking attempts to insert all essentia in the players inventory into the network.

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
