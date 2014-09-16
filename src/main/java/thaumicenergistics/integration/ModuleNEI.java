package thaumicenergistics.integration;

import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import codechicken.nei.api.API;

public class ModuleNEI
{

	public ModuleNEI() throws Exception
	{

		// Register the crafting overlay
		API.registerGuiOverlay( GuiArcaneCraftingTerminal.class, "crafting" );
	}

}
