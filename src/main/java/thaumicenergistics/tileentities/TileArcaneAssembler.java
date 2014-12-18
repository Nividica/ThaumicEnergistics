package thaumicenergistics.tileentities;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.common.FMLCommonHandler;

public class TileArcaneAssembler
	extends AENetworkInvTile
	implements ICraftingProvider, ICraftingWatcherHost
{
	private class AssemblerInventory
		extends PrivateInventory
	{
		/**
		 * Index of the slot used for the knowledge core.
		 */
		public static final int KCORE_SLOT_INDEX = 9;

		public AssemblerInventory()
		{
			super( "ArcaneAssemblerInventory", 10, 1 );
		}

	}

	private AssemblerInventory internalInventory;

	private boolean isBusy = false;

	ICraftingWatcher craftingWatcher;

	public TileArcaneAssembler()
	{
		this.internalInventory = new AssemblerInventory();

	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection whichSide )
	{
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
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

	@Override
	public void onRequestChange( final ICraftingGrid craftingGrid, final IAEItemStack what )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void provideCrafting( final ICraftingProviderHelper craftingTracker )
	{
		// TODO Auto-generated method stub

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
			this.gridProxy.setIdlePowerUsage( 0.0D );

			// Set that we require a channel
			this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		}
	}

	@Override
	public void updateWatcher( final ICraftingWatcher newWatcher )
	{
		// Set the watcher
		this.craftingWatcher = newWatcher;
	}

}
