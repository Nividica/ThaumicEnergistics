package thaumicenergistics.texture;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import thaumicenergistics.ThaumicEnergistics;

public enum BlockTextureManager
{
		BUS_SIDE (TextureTypes.Part, new String[] { "bus.side" }),

		BUS_BORDER (TextureTypes.Part, new String[] { "bus.border" }),

		BUS_COLOR (TextureTypes.Part, new String[] { "bus.color.border", "bus.color.light" }),

		ESSENTIA_IMPORT_BUS (TextureTypes.Part, new String[] { "essentia.import.bus.face", "essentia.import.bus.overlay",
						"essentia.import.bus.chamber" }),

		ESSENTIA_LEVEL_EMITTER (TextureTypes.Part, new String[] { "essentia.level.emitter.base", "essentia.level.emitter.active",
						"essentia.level.emitter.inactive" }),

		ESSENTIA_STORAGE_BUS (TextureTypes.Part, new String[] { "essentia.storage.bus.face", "essentia.storage.bus.overlay" }),

		ESSENTIA_EXPORT_BUS (TextureTypes.Part, new String[] { "essentia.export.bus.face", "essentia.export.bus.overlay" }),

		ESSENTIA_TERMINAL (TextureTypes.Part, new String[] { "essentia.terminal.overlay.dark", "essentia.terminal.overlay.medium",
						"essentia.terminal.overlay.light" }),

		ESSENTIA_PROVIDER (TextureTypes.Block, new String[] { "essentia.provider", "essentia.provider.overlay" }),

		INFUSION_PROVIDER (TextureTypes.Block, new String[] { "infusion.provider", "infusion.provider.overlay" }),

		ARCANE_CRAFTING_TERMINAL (TextureTypes.Part, new String[] { "arcane.crafting.overlay1", "arcane.crafting.overlay2",
						"arcane.crafting.overlay3", "arcane.crafting.side", "arcane.crafting.overlay4" }),

		VIS_RELAY_INTERFACE (TextureTypes.Part, new String[] { "vis.interface", "vis.interface.runes" }),

		GEAR_BOX (TextureTypes.Block, new String[] { "gear.box", "golem.gear.box" }),

		ESSENTIA_CELL_WORKBENCH (TextureTypes.Block, new String[] { "essentia.cell.workbench.top", "essentia.cell.workbench.bottom",
						"essentia.cell.workbench.side" }),

		GASEOUS_ESSENTIA (TextureTypes.Block, new String[] { "essentia.gas" });

	private enum TextureTypes
	{
			Block,
			Part;
	}

	private TextureTypes textureType;

	private String[] textureNames;

	private IIcon[] textures;

	/**
	 * Cache of the enum values
	 */
	public static final BlockTextureManager[] VALUES = BlockTextureManager.values();

	private BlockTextureManager( final TextureTypes textureType, final String[] textureNames )
	{
		this.textureType = textureType;
		this.textureNames = textureNames;
		this.textures = new IIcon[this.textureNames.length];
	}

	public IIcon getTexture()
	{
		return this.textures[0];
	}

	public IIcon[] getTextures()
	{
		return this.textures;
	}

	public void registerTexture( final TextureMap textureMap )
	{
		if( textureMap.getTextureType() == 0 )
		{
			String header = ThaumicEnergistics.MOD_ID + ":";

			if( this.textureType == TextureTypes.Part )
			{
				header += "parts/";
			}

			for( int i = 0; i < this.textureNames.length; i++ )
			{
				this.textures[i] = textureMap.registerIcon( header + this.textureNames[i] );
			}
		}
	}
}
