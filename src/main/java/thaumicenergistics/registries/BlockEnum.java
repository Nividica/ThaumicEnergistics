package thaumicenergistics.registries;

import net.minecraft.block.Block;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.blocks.BlockArcaneAssembler;
import thaumicenergistics.blocks.BlockEssentiaCellWorkbench;
import thaumicenergistics.blocks.BlockEssentiaProvider;
import thaumicenergistics.blocks.BlockGearBox;
import thaumicenergistics.blocks.BlockGolemGearBox;
import thaumicenergistics.blocks.BlockInfusionProvider;

public enum BlockEnum
{
		ESSENTIA_PROVIDER ("essentia.provider", new BlockEssentiaProvider()),
		INFUSION_PROVIDER ("infusion.provider", new BlockInfusionProvider()),
		IRON_GEAR_BOX ("gear.box", new BlockGearBox()),
		THAUMIUM_GEAR_BOX ("golem.gear.box", new BlockGolemGearBox()),
		ESSENTIA_CELL_WORKBENCH ("essentia.cell.workbench", new BlockEssentiaCellWorkbench()),
		ARCANE_ASSEMBLER ("arcane.assembler", new BlockArcaneAssembler());

	private Block block;

	private String unlocalizedName;

	/**
	 * Cache of the enum values
	 */
	public static final BlockEnum[] VALUES = BlockEnum.values();

	private BlockEnum( final String unlocalizedName, final Block block )
	{
		// Set the block
		this.block = block;

		// Set the unlocalized name
		this.unlocalizedName = ThaumicEnergistics.MOD_ID + ".block." + unlocalizedName;

		// Set the block's name
		block.setBlockName( this.unlocalizedName );
	}

	// Return the block
	public Block getBlock()
	{
		return this.block;
	}

	// Return the name
	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}
}
