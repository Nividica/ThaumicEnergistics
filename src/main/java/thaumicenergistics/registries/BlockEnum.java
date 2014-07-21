package thaumicenergistics.registries;

import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.blocks.BlockEssentiaProvider;
import net.minecraft.block.Block;

public enum BlockEnum
{
	ESSENTIA_PROVIDER( "essentia.provider", new BlockEssentiaProvider() );
	
	private Block block;
	
	private String unlocalizedName;
	
	private BlockEnum( String unlocalizedName, Block block )
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
