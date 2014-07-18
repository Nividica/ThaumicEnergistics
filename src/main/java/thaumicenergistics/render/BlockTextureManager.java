package thaumicenergistics.render;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import thaumicenergistics.ThaumicEnergistics;

public enum BlockTextureManager
{
		BUS_SIDE (TextureTypes.Part, new String[] { "bus.side" }),
		BUS_BORDER (TextureTypes.Part, new String[] { "bus.border" }),
		BUS_COLOR (TextureTypes.Part, new String[] { "bus.color.border", "bus.color.light" }),
		ESSENTIA_IMPORT_BUS (TextureTypes.Part, new String[] { "essentia.import.bus.face", "essentia.import.bus.overlay", "essentia.import.bus.chamber" }),
		ESSENTIA_LEVEL_EMITTER (TextureTypes.Part, new String[] { "essentia.level.emitter.face.1",
						"essentia.level.emitter.face.2",
						"essentia.level.emitter.face.3" }),
		ESSENTIA_STORAGE_BUS (TextureTypes.Part, new String[] { "essentia.storage.bus.face", "essentia.storage.bus.overlay" }),
		ESSENTIA_EXPORT_BUS (TextureTypes.Part, new String[] { "essentia.export.bus.face", "essentia.export.bus.overlay" }),
		ESSENTIA_STORAGE_CELL_CHEST (TextureTypes.Block, new String[] { "essentia.storage.cell.chest.overlay" }),
		ESSENTIA_TERMINAL (TextureTypes.Part, new String[] { "essentia.terminal.overlay.1",
						"essentia.terminal.overlay.2",
						"essentia.terminal.overlay.3" });

	private enum TextureTypes
	{
			Block,
			Part;
	}

	private TextureTypes textureType;
	
	private String[] textureNames;

	private IIcon[] textures;

	private BlockTextureManager(TextureTypes textureType, String[] textureNames)
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

	public void registerTexture( TextureMap textureMap )
	{
		if ( textureMap.getTextureType() == 0 )
		{
			String header = ThaumicEnergistics.MODID + ":";

			if ( this.textureType == TextureTypes.Part )
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
