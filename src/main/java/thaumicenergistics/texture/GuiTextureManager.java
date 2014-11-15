package thaumicenergistics.texture;

import net.minecraft.util.ResourceLocation;
import thaumicenergistics.ThaumicEnergistics;

public enum GuiTextureManager
{
		ESSENTIA_LEVEL_EMITTER ("essentia.level.emitter"),
		ESSENTIA_STORAGE_BUS ("essentia.storage.bus"),
		ESSENTIA_TERMINAL ("essentia.terminal"),
		ESSENTIA_IO_BUS ("essentia.io.bus"),
		ARCANE_CRAFTING_TERMINAL ("arcane.crafting"),
		PRIORITY ("priority"),
		CELL_WORKBENCH ("essentia.cell.workbench");

	private ResourceLocation texture;

	private GuiTextureManager( final String textureName )
	{
		// Create the resource location
		this.texture = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/gui/" + textureName + ".png" );
	}

	public ResourceLocation getTexture()
	{
		return this.texture;
	}

}
