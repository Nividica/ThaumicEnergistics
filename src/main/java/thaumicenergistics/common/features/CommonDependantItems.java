package thaumicenergistics.common.features;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import com.google.common.base.Optional;

/**
 * Every TC/AE item that any feature depends on.
 * The items can be an ItemStack, a String, or null.
 * 
 * @author Nividica
 * 
 */
public class CommonDependantItems
{
	// MC Items ==========================
	public String VanillaGlass = "blockGlass";
	public String IronIngot = "ingotIron";
	public ItemStack VanillaLapis;
	public ItemStack RedstoneDust;
	public ItemStack RedstoneTorch;
	public String Cobblestone = "cobblestone";
	public String NetherQuartz = "gemQuartz";

	// TC Items ==========================
	public ItemStack ZombieBrain;
	public ItemStack Thaumonomicon;
	public ItemStack AirShard;
	public ItemStack FireShard;
	public ItemStack WaterShard;
	public ItemStack EarthShard;
	public ItemStack OrderShard;
	public ItemStack EntropyShard;
	public ItemStack BallanceShard;
	public ItemStack EtheralEssence;
	public ItemStack SalisMundus;
	public Object WardedGlass;
	public ItemStack ThaumiumIngot;
	public ItemStack ArcaneWorkTable;
	public ItemStack WardedJar;
	public ItemStack FilterTube;
	public ItemStack EssentiaMirror;
	public ItemStack VisFilter;
	public ItemStack QuickSilverDrop;
	public ItemStack Thaumometer;

	// AE Items =========================
	public Object VibrantGlass;
	public ItemStack EngineeringProcessor;
	public ItemStack CalculationProcessor;
	public ItemStack LogicProcessor;
	public String IlluminatedPanel = "itemIlluminatedPanel";
	public ItemStack MolecularAssembler;
	public ItemStack CertusQuartz;
	public ItemStack ChargedCertusQuartz;
	public ItemStack PureCertusQuartz;
	public Object QuartzGlass;
	public Object MECellWorkbench;
	public ItemStack METerminal;
	public ItemStack MEP2P;
	public ItemStack MEInterface;
	public ItemStack WirelessReceiver;
	public ItemStack DenseCell;
	public ItemStack FormationCore;
	public ItemStack AnnihilationCore;
	public ItemStack CertusWrench;
	public ItemStack VibrationChamber;

	/**
	 * Populates the common items
	 */
	public CommonDependantItems()
	{
		// MC items
		this.VanillaLapis = new ItemStack( (Item)Item.itemRegistry.getObject( "dye" ), 1, 4 );
		this.RedstoneDust = new ItemStack( (Item)Item.itemRegistry.getObject( "redstone" ) );
		this.RedstoneTorch = new ItemStack( net.minecraft.init.Blocks.redstone_torch );

		// AE items
		this.populateAEItems();

		// TC items
		this.populateTCItems();
	}

	/**
	 * Attempts to get the specified AE item.
	 * Returns a stack of size 1, or null.
	 * 
	 * @param def
	 * @return
	 */
	private ItemStack getAEItem( final IItemDefinition def )
	{
		// Ensure there is a definition
		if( def == null )
		{
			return null;
		}

		// Attempt to get a stack
		Optional<ItemStack> item = def.maybeStack( 1 );

		// Was a stack retrieved?
		if( item.isPresent() )
		{
			// Return the stack
			return item.get();
		}

		// Stack was not retrieved
		return null;
	}

	/**
	 * Gets an item based on if it is available and allowed by the specified
	 * config flag.
	 * 
	 * @param configDependency
	 * @param preferred
	 * @param alt
	 * @return
	 */
	private Object getItemOrAlt( final boolean configDependency, final ItemStack preferred, final Object alt )
	{
		if( ( preferred == null ) || ( !configDependency ) )
		{
			return alt;
		}

		return preferred;
	}

	/**
	 * Gets an AE item, or if unavailable sets the item to the specified
	 * alternative.
	 * 
	 * @param def
	 * @param alt
	 * @return
	 */
	private Object getItemOrAlt( final IItemDefinition def, final Object alt )
	{
		ItemStack defItem = this.getAEItem( def );
		return( defItem == null ? alt : defItem );
	}

	private void populateAEItems()
	{
		// Get a reference to the AE blocks, materials, and parts
		IBlocks aeBlocks = AEApi.instance().definitions().blocks();
		IMaterials aeMaterials = AEApi.instance().definitions().materials();
		IParts aeParts = AEApi.instance().definitions().parts();

		this.VibrantGlass = this.getItemOrAlt( aeBlocks.quartzVibrantGlass(), this.VanillaGlass );
		this.EngineeringProcessor = this.getAEItem( aeMaterials.engProcessor() );
		this.CalculationProcessor = this.getAEItem( aeMaterials.calcProcessor() );
		this.LogicProcessor = this.getAEItem( aeMaterials.logicProcessor() );
		this.MolecularAssembler = this.getAEItem( aeBlocks.molecularAssembler() );
		this.CertusQuartz = this.getAEItem( aeMaterials.certusQuartzCrystal() );
		this.ChargedCertusQuartz = this.getAEItem( aeMaterials.certusQuartzCrystalCharged() );
		this.PureCertusQuartz = this.getAEItem( aeMaterials.purifiedCertusQuartzCrystal() );
		this.QuartzGlass = this.getItemOrAlt( aeBlocks.quartzGlass(), this.VanillaGlass );
		this.MECellWorkbench = this.getItemOrAlt( aeBlocks.cellWorkbench(), new ItemStack( Blocks.crafting_table ) );
		this.METerminal = this.getAEItem( aeParts.terminal() );
		this.MEP2P = this.getAEItem( aeParts.p2PTunnelME() );
		this.MEInterface = this.getAEItem( aeBlocks.iface() );
		this.WirelessReceiver = this.getAEItem( aeMaterials.wireless() );
		this.DenseCell = (ItemStack)this.getItemOrAlt( aeBlocks.energyCellDense(), this.getAEItem( aeBlocks.energyCell() ) );
		this.AnnihilationCore = this.getAEItem( aeMaterials.annihilationCore() );
		this.FormationCore = this.getAEItem( aeMaterials.formationCore() );
		this.CertusWrench = this.getAEItem( AEApi.instance().definitions().items().certusQuartzWrench() );
		this.VibrationChamber = this.getAEItem( aeBlocks.vibrationChamber() );

	}

	private void populateTCItems()
	{
		this.ZombieBrain = new ItemStack( ConfigItems.itemZombieBrain );
		this.Thaumonomicon = new ItemStack( ConfigItems.itemThaumonomicon );
		this.AirShard = new ItemStack( ConfigItems.itemShard, 1, 0 );
		this.FireShard = new ItemStack( ConfigItems.itemShard, 1, 1 );
		this.WaterShard = new ItemStack( ConfigItems.itemShard, 1, 2 );
		this.EarthShard = new ItemStack( ConfigItems.itemShard, 1, 3 );
		this.OrderShard = new ItemStack( ConfigItems.itemShard, 1, 4 );
		this.EntropyShard = new ItemStack( ConfigItems.itemShard, 1, 5 );
		this.BallanceShard = new ItemStack( ConfigItems.itemShard, 1, 6 );
		this.EtheralEssence = new ItemStack( ConfigItems.itemWispEssence );
		this.SalisMundus = new ItemStack( ConfigItems.itemResource, 1, 14 );
		this.WardedGlass = this.getItemOrAlt( Config.wardedStone, new ItemStack( ConfigBlocks.blockCosmeticOpaque, 1, 2 ), this.VanillaGlass );
		this.ThaumiumIngot = new ItemStack( ConfigItems.itemResource, 1, 2 );
		this.ArcaneWorkTable = new ItemStack( ConfigBlocks.blockTable, 1, 15 );
		this.WardedJar = new ItemStack( ConfigBlocks.blockJar, 1, 0 );
		this.FilterTube = new ItemStack( ConfigBlocks.blockTube, 1, 3 );
		this.EssentiaMirror = (ItemStack)this.getItemOrAlt( Config.allowMirrors, new ItemStack( ConfigBlocks.blockMirror, 1, 6 ), this.WardedJar );
		this.VisFilter = new ItemStack( ConfigItems.itemResource, 1, 8 );
		this.QuickSilverDrop = new ItemStack( ConfigItems.itemNugget, 1, 5 );
		this.Thaumometer = new ItemStack( ConfigItems.itemThaumometer );
	}
}
