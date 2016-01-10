package thaumicenergistics.common.features;

import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraftforge.common.config.Property;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumicenergistics.api.ThEApi;
import appeng.core.FacadeConfig;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class FeatureThaumcraftFacades
	extends ThEDependencyFeatureBase
{

	/**
	 * Ensures the blocks name is clean
	 */
	private final Pattern sanitizer;

	public FeatureThaumcraftFacades()
	{
		// Set the pattern
		this.sanitizer = Pattern.compile( "[^a-zA-Z0-9]" );
	}

	/**
	 * Forces the specified stack to have a facade if possible.
	 * 
	 * @param block
	 * @param meta
	 */
	private void forceEnabled( final Block block, final int meta )
	{
		// Sanity check		
		if( block == null )
		{
			return;
		}

		// Get the uid for the block
		UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor( block );
		if( uid == null )
		{
			return;
		}

		// Get the mod and block names
		String modName = this.sanitizer.matcher( uid.modId ).replaceAll( "" );
		String blockName = this.sanitizer.matcher( uid.name ).replaceAll( "" );

		// Get the property
		Property prop = FacadeConfig.instance.get( modName, blockName + ( meta == 0 ? "" : "." + meta ), true );

		// Set it to true
		prop.set( true );

	}

	@Override
	protected boolean checkConfigs()
	{
		return ThEApi.instance().config().forceTCFacades();
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return null;
	}

	@Override
	protected void registerCrafting( final CommonDependantItems cdi )
	{
		// Amber block
		this.forceEnabled( ConfigBlocks.blockCosmeticOpaque, 0 );

		// Amber brick
		this.forceEnabled( ConfigBlocks.blockCosmeticOpaque, 1 );

		// Warded glass
		if( Config.allowMirrors )
		{
			this.forceEnabled( ConfigBlocks.blockCosmeticOpaque, 2 );
		}

		// Obsidian tile
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 1 );

		// Thaumium block
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 4 );

		// Tallow block
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 5 );

		// Arcane stone block
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 6 );

		// Arcane stone brick
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 7 );

		// Ancient stone
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 11 );

		// Ancient rock
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 12 );

		// Crusted stone
		this.forceEnabled( ConfigBlocks.blockCosmeticSolid, 14 );

		// Greatwood planks
		this.forceEnabled( ConfigBlocks.blockWoodenDevice, 6 );

		// Silverwood planks
		this.forceEnabled( ConfigBlocks.blockWoodenDevice, 7 );

		// Crusted taint
		this.forceEnabled( ConfigBlocks.blockTaint, 0 );

		// Flesh block :<
		this.forceEnabled( ConfigBlocks.blockTaint, 2 );
	}

}
