package thaumicenergistics.common.parts;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.client.gui.GuiEssentiaStorageBus;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.common.grid.EssentiaMonitor;
import thaumicenergistics.common.inventory.HandlerEssentiaStorageBusBase;
import thaumicenergistics.common.inventory.HandlerEssentiaStorageBusDuality;
import thaumicenergistics.common.network.IAspectSlotPart;
import thaumicenergistics.common.registries.AEPartsEnum;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartEssentiaStorageBus
	extends ThEPartBase
	implements IGridTickable, ICellContainer, IAspectSlotPart, IAEAppEngInventory, IPriorityHost
{
	/**
	 * Number of filtered aspects we can have
	 */
	public static final int FILTER_SIZE = 9;

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 1.0D;

	/**
	 * NBT Keys
	 */
	private static final String NBT_KEY_PRIORITY = "Priority", NBT_KEY_FILTER = "FilterAspects#", NBT_KEY_UPGRADES = "UpgradeInventory",
					NBT_KEY_VOID = "IsVoidAllowed";

	/**
	 * "Cell" handler for the storage bus.
	 */
	private final HandlerEssentiaStorageBusBase handler = new HandlerEssentiaStorageBusDuality( this );

	/**
	 * Filter list
	 */
	private final ArrayList<Aspect> filteredAspects = new ArrayList<Aspect>( FILTER_SIZE );

	/**
	 * Upgrade inventory
	 */
	private final UpgradeInventory upgradeInventory = new StackUpgradeInventory( this.associatedItem, this, 1 );

	/**
	 * Storage bus priority
	 */
	private int priority = 0;

	/**
	 * Creates the bus
	 */
	public PartEssentiaStorageBus()
	{
		// Call super
		super( AEPartsEnum.EssentiaStorageBus, SecurityPermissions.EXTRACT, SecurityPermissions.INJECT );

		// Pre-fill the list with nulls
		for( int index = 0; index < PartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			this.filteredAspects.add( null );
		}
	}

	/**
	 * Updates the handler on the inverted state.
	 */
	private void updateInverterState()
	{
		boolean inverted = AEApi.instance().definitions().materials().cardInverter().isSameAs( this.upgradeInventory.getStackInSlot( 0 ) );
		this.handler.setInverted( inverted );
	}

	/**
	 * Adds a new filter from the specified itemstack.
	 * 
	 * @param player
	 * @param itemStack
	 * @return
	 */
	public boolean addFilteredAspectFromItemstack( final EntityPlayer player, final ItemStack itemStack )
	{
		// Get the aspect of the item
		Aspect itemAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( itemStack );

		// Is there an aspect?
		if( itemAspect != null )
		{
			// Are we already filtering this aspect?
			if( this.filteredAspects.contains( itemAspect ) )
			{
				return true;
			}

			// Add to the first open slot
			for( int index = 0; index < PartEssentiaStorageBus.FILTER_SIZE; index++ )
			{
				// Is this space empty?
				if( this.filteredAspects.get( index ) == null )
				{
					// Set the filter
					this.setAspect( index, itemAspect, player );

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Ignored
	 */
	@Override
	public void blinkCell( final int slot )
	{
		// Ignored
	}

	/**
	 * How far out from the cable bus to draw the cable graphic.
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	/**
	 * Extracts power from the network proportional to the specified essentia
	 * amount.
	 * 
	 * @param essentiaAmount
	 * @param mode
	 * @return
	 */
	public boolean extractPowerForEssentiaTransfer( final int essentiaAmount, final Actionable mode )
	{
		// Get the energy grid
		IEnergyGrid eGrid = this.getGridBlock().getEnergyGrid();

		// Ensure we have a grid
		if( eGrid == null )
		{
			return false;
		}

		// Calculate amount of power to take
		double powerDrain = EssentiaMonitor.AE_PER_ESSENTIA * essentiaAmount;

		// Extract
		return( eGrid.extractAEPower( powerDrain, mode, PowerMultiplier.CONFIG ) >= powerDrain );
	}

	/**
	 * Hit/Collision boxes.
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		// Face
		helper.addBox( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );

		// Mid
		helper.addBox( 4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D );

		// Back
		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[0];
	}

	/**
	 * Gets the 'cell' handler for the storage bus.
	 */
	@Override
	public List<IMEInventoryHandler> getCellArray( final StorageChannel channel )
	{
		// Create a new list
		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();

		// Is this the fluid channel?
		if( channel == StorageChannel.FLUIDS )
		{
			// Add our handler
			list.add( this.handler );
		}

		// Return the list
		return list;

	}

	/**
	 * Returns the client portion of the gui.
	 */
	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return new GuiEssentiaStorageBus( this, player );
	}

	/**
	 * What do we drop when removed from the world.
	 */
	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		// Get the upgrade card
		ItemStack slotStack = this.upgradeInventory.getStackInSlot( 0 );

		// Is it not null?
		if( ( slotStack != null ) && ( slotStack.stackSize > 0 ) )
		{
			// Add to the drops
			drops.add( slotStack );
		}
	}

	/**
	 * Returns the aspect in the filter slot.
	 * 
	 * @return
	 */
	@Nullable
	public Aspect getFilteredAspect( final int slotIndex )
	{
		return this.filteredAspects.get( slotIndex );
	}

	/**
	 * Determines how much power the part takes for just existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return PartEssentiaStorageBus.IDLE_POWER_DRAIN;
	}

	/**
	 * Does not produce light.
	 */
	@Override
	public int getLightLevel()
	{
		return 0;
	}

	/**
	 * Gets the priority for this storage bus.
	 */
	@Override
	public int getPriority()
	{
		return this.priority;
	}

	/**
	 * Gets the server part of the gui.
	 */
	@Override
	public Object getServerGuiElement( final EntityPlayer player )
	{
		return new ContainerPartEssentiaStorageBus( this, player );
	}

	/**
	 * Sets how often we would like ticks.
	 */
	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		// We would like a tick ever 20 MC ticks
		return new TickingRequest( 20, 20, false, false );
	}

	/**
	 * Gets the inventory that holds our upgrades.
	 * 
	 * @return
	 */
	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	/**
	 * Is voiding of essentia allowed?
	 * 
	 * @return
	 */
	public boolean isVoidAllowed()
	{
		return this.handler.isVoidAllowed();
	}

	/**
	 * Called when the upgrade inventory changes.
	 */
	@Override
	public void onChangeInventory( final IInventory inv, final int arg1, final InvOperation arg2, final ItemStack arg3, final ItemStack arg4 )
	{
		this.updateInverterState();
	}

	/**
	 * Called when a player has changed void mode via gui.
	 * 
	 * @param player
	 * @param isVoidAllowed
	 */
	public void onClientRequestSetVoidMode( final EntityPlayer player, final boolean isVoidAllowed )
	{
		// Set the mode
		this.handler.setVoidAllowed( isVoidAllowed );

		this.saveChanges();
	}

	/**
	 * /** Updates the grid and handler that a neighbor has changed.
	 */
	@Override
	public void onNeighborChanged()
	{
		// Call super
		super.onNeighborChanged();

		// Send grid update event on server side
		if( EffectiveSide.isServerSide() )
		{
			// Update the handler
			if( this.handler.onNeighborChange() )
			{
				// Send the update event
				this.postGridUpdateEvent();
			}
		}
	}

	/**
	 * Notifies the grid that the storage bus contents have changed.
	 */
	public void postGridUpdateEvent()
	{
		// Does the storage bus have a grid node?
		if( this.getActionableNode() != null )
		{
			// Get the grid.
			IGrid grid = this.getActionableNode().getGrid();

			// Does the grid node have a grid?
			if( grid != null )
			{
				// Post an update to the grid
				grid.postEvent( new MENetworkCellArrayUpdate() );
			}
		}
	}

	/**
	 * /** Reads the part data from NBT
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read the priority
		if( data.hasKey( PartEssentiaStorageBus.NBT_KEY_PRIORITY ) )
		{
			this.priority = data.getInteger( PartEssentiaStorageBus.NBT_KEY_PRIORITY );
		}

		// Read the filter list
		for( int index = 0; index < PartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			if( data.hasKey( PartEssentiaStorageBus.NBT_KEY_FILTER + index ) )
			{
				this.filteredAspects.set( index, Aspect.aspects.get( data.getString( PartEssentiaStorageBus.NBT_KEY_FILTER + index ) ) );
			}
			else
			{
				this.filteredAspects.set( index, null );
			}
		}

		// Read the upgrade inventory
		if( data.hasKey( PartEssentiaStorageBus.NBT_KEY_UPGRADES ) )
		{
			this.upgradeInventory.readFromNBT( data, PartEssentiaStorageBus.NBT_KEY_UPGRADES );

			// TODO: Is this needed or will onChange be called?
			// Update the handler inverted
			this.updateInverterState();
		}

		// Read void
		if( data.hasKey( PartEssentiaStorageBus.NBT_KEY_VOID ) )
		{
			this.handler.setVoidAllowed( data.getBoolean( PartEssentiaStorageBus.NBT_KEY_VOID ) );
		}

		// Update the handler filter list
		this.handler.setPrioritizedAspects( this.filteredAspects );
	}

	/**
	 * Renders the storage bus in the player inventory
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[2];
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[0], side, side );

		// Face
		helper.setBounds( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Mid
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderInventoryBox( renderer );

		// Color overlay
		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
		helper.setInvColor( ThEPartBase.INVENTORY_OVERLAY_COLOR );
		ts.setBrightness( 0xF000F0 );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Back
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	/**
	 * Renders the storage bus int he world.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[2];
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTexture(), side, side );

		// Front (facing jar)
		helper.setBounds( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		tessellator.setColorOpaque_I( this.getHost().getColor().blackVariant );

		if( this.isActive() )
		{
			tessellator.setBrightness( ThEPartBase.ACTIVE_FACE_BRIGHTNESS );
		}

		// Mid
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderBlock( x, y, z, renderer );

		// Back (facing bus)
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	@Override
	public void saveChanges()
	{
		this.markForSave();
	}

	/**
	 * Ensures the storage bus gets saved.
	 */
	@Override
	public void saveChanges( final IMEInventory inventory )
	{
		this.saveChanges();
	}

	/**
	 * Sets one of the filters.
	 */
	@Override
	public void setAspect( final int index, final Aspect aspect, final EntityPlayer player )
	{
		this.filteredAspects.set( index, aspect );

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Update the handler
			this.handler.setPrioritizedAspects( this.filteredAspects );

			// Update the grid
			this.postGridUpdateEvent();

			// Mark for save
			this.markForSave();
		}
	}

	@Override
	public void setPriority( final int priority )
	{
		this.priority = priority;
	}

	/**
	 * Called periodically by AE2. Passes the tick to the handler.
	 */
	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		// Update the handler.
		this.handler.tickingRequest( node, TicksSinceLastCall );

		// Keep chugging along
		return TickRateModulation.SAME;
	}

	/**
	 * Writes the storage busses state to NBT.
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Only write NBT data if saving, or wrenched.
		if( ( saveType != PartItemStack.World ) && ( saveType != PartItemStack.Wrench ) )
		{
			return;
		}

		// Write the filters
		boolean hasFilters = false;
		for( int index = 0; index < PartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			Aspect aspect = this.filteredAspects.get( index );

			if( aspect != null )
			{
				data.setString( PartEssentiaStorageBus.NBT_KEY_FILTER + index, aspect.getTag() );
				hasFilters = true;
			}
		}

		// Only save the rest if filters are set, or world save
		if( hasFilters || ( saveType == PartItemStack.World ) )
		{
			// Write the priority
			if( this.priority > 0 )
			{
				data.setInteger( PartEssentiaStorageBus.NBT_KEY_PRIORITY, this.priority );
			}

			// Write void
			if( this.handler.isVoidAllowed() )
			{
				data.setBoolean( PartEssentiaStorageBus.NBT_KEY_VOID, this.handler.isVoidAllowed() );
			}
		}

		// Write upgrades
		if( ( saveType == PartItemStack.World ) && !this.upgradeInventory.isEmpty() )
		{
			this.upgradeInventory.writeToNBT( data, PartEssentiaStorageBus.NBT_KEY_UPGRADES );
		}

	}

}
