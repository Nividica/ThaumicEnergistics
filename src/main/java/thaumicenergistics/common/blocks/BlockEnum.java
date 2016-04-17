package thaumicenergistics.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import thaumicenergistics.common.items.ItemBlockArcaneAssembler;
import thaumicenergistics.common.items.ItemBlockEssentiaVibrationChamber;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * Enumeration of all ThE blocks.
 *
 * @author Nividica
 *
 */
public enum BlockEnum
{
		ESSENTIA_PROVIDER (ThEStrings.Block_EssentiaProvider, new BlockEssentiaProvider()),
		INFUSION_PROVIDER (ThEStrings.Block_InfusionProvider, new BlockInfusionProvider()),
		IRON_GEAR_BOX (ThEStrings.Block_IronGearbox, new BlockGearBox()),
		THAUMIUM_GEAR_BOX (ThEStrings.Block_ThaumiumGearbox, new BlockGolemGearBox()),
		ESSENTIA_CELL_WORKBENCH (ThEStrings.Block_EssentiaCellWorkbench, new BlockEssentiaCellWorkbench()),
		ARCANE_ASSEMBLER (ThEStrings.Block_ArcaneAssembler, new BlockArcaneAssembler(), ItemBlockArcaneAssembler.class),
		KNOWLEDGE_INSCRIBER (ThEStrings.Block_KnowledgeInscriber, new BlockKnowledgeInscriber()),
		ESSENTIA_VIBRATION_CHAMBER (ThEStrings.Block_EssentiaVibrationChamber, new BlockEssentiaVibrationChamber(),
			ItemBlockEssentiaVibrationChamber.class),
		DISTILLATION_ENCODER (ThEStrings.Block_DistillationEncoder, new BlockDistillationEncoder());

	/**
	 * Cache of the enum values
	 */
	public static final BlockEnum[] VALUES = BlockEnum.values();

	/**
	 * The block object.
	 */
	private final Block block;

	/**
	 * Unlocalized name of the block.
	 */
	private final ThEStrings unlocalizedName;

	/**
	 * Custom item
	 */
	public final Class<? extends ItemBlock> itemBlockClass;

	/**
	 * Constructs the block with the default itemblock.
	 *
	 * @param unlocalizedName
	 * @param block
	 */
	private BlockEnum( final ThEStrings unlocalizedName, final Block block )
	{
		this( unlocalizedName, block, ItemBlock.class );
	}

	/**
	 * Constructs the block with the specified itemBlock.
	 *
	 * @param unlocalizedName
	 * @param block
	 * @param itemBlockClass
	 */
	private BlockEnum( final ThEStrings unlocalizedName, final Block block, final Class<? extends ItemBlock> itemBlockClass )
	{
		// Set the block
		this.block = block;

		// Set the unlocalized name
		this.unlocalizedName = unlocalizedName;

		// Set the block's name
		block.setBlockName( this.unlocalizedName.getUnlocalized() );

		// Set item
		this.itemBlockClass = itemBlockClass;
	}

	// Return the block
	public Block getBlock()
	{
		return this.block;
	}

	// Return the name
	public String getUnlocalizedName()
	{
		return this.unlocalizedName.getUnlocalized();
	}
}
