package thaumicenergistics.texture;

import thaumicenergistics.ThaumicEnergistics;
import net.minecraft.util.ResourceLocation;

public enum GuiTextureManager
{
	ESSENTIA_LEVEL_EMITTER( "essentia.level.emitter" ),
	ESSENTIA_STORAGE_BUS( "essentia.storage.bus" ),
	ESSENTIA_TERMINAL( "essentia.terminal" ),
	ESSENTIA_IO_BUS( "essentia.io.bus" );
	
	private ResourceLocation texture;
	
	private GuiTextureManager( String textureName )
	{
		// Create the resource location
		this.texture = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/gui/" + textureName + ".png" );
	}
	
	public ResourceLocation getTexture()
	{
		return this.texture;
	}
	
}
