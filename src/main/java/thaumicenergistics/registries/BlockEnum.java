package thaumicenergistics.registries;

import net.minecraft.block.Block;
import thaumicenergistics.blocks.BlockArcaneAssembler;
import thaumicenergistics.blocks.BlockEssentiaCellWorkbench;
import thaumicenergistics.blocks.BlockEssentiaProvider;
import thaumicenergistics.blocks.BlockGearBox;
import thaumicenergistics.blocks.BlockGolemGearBox;
import thaumicenergistics.blocks.BlockInfusionProvider;
import thaumicenergistics.blocks.BlockKnowledgeInscriber;

public enum BlockEnum
{
		ESSENTIA_PROVIDER (ThEStrings.Block_EssentiaProvider, new BlockEssentiaProvider()),
		INFUSION_PROVIDER (ThEStrings.Block_InfusionProvider, new BlockInfusionProvider()),
		IRON_GEAR_BOX (ThEStrings.Block_IronGearbox, new BlockGearBox()),
		THAUMIUM_GEAR_BOX (ThEStrings.Block_ThaumiumGearbox, new BlockGolemGearBox()),
		ESSENTIA_CELL_WORKBENCH (ThEStrings.Block_EssentiaCellWorkbench, new BlockEssentiaCellWorkbench()),
		ARCANE_ASSEMBLER (ThEStrings.Block_ArcaneAssembler, new BlockArcaneAssembler()),
		KNOWLEDGE_INSCRIBER (ThEStrings.Block_KnowledgeInscriber, new BlockKnowledgeInscriber());

	private Block block;

	private ThEStrings unlocalizedName;

	/**
	 * Cache of the enum values
	 */
	public static final BlockEnum[] VALUES = BlockEnum.values();

	private BlockEnum( final ThEStrings unlocalizedName, final Block block )
	{
		// Set the block
		this.block = block;

		// Set the unlocalized name
		this.unlocalizedName = unlocalizedName;

		// Set the block's name
		block.setBlockName( this.unlocalizedName.getUnlocalized() );
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
