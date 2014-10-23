package thaumicenergistics.integration;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.IRecipeHandler;

/**
 * Contains all code required to integrate with Not Enough Items.
 * 
 * @author Nividica
 * 
 */
public class ModuleNEI
{
	/**
	 * Passes a selected NEI recipe to the server's A.C.T.
	 * 
	 * @author Nividica
	 * 
	 */
	public class ACTOverlayHandler
		implements IOverlayHandler
	{
		/**
		 * Called when the user has shift-clicked the [?] button in NEI
		 */
		@Override
		public void overlayRecipe( final GuiContainer gui, final IRecipeHandler recipeHandler, final int recipeIndex, final boolean shift )
		{
			// Ensure the gui is the ACT
			if( gui instanceof GuiArcaneCraftingTerminal )
			{
				// List of items
				IAEItemStack[] items = new IAEItemStack[9];

				// Assume there are no items until they are added.
				boolean hasItems = false;

				// Get the ingredients
				List<PositionedStack> ingredients = recipeHandler.getIngredientStacks( recipeIndex );

				// Get each item
				for( PositionedStack ingredient : ingredients )
				{
					// Calculate the slot positions
					int slotX = ( ingredient.relx - ModuleNEI.NEI_SLOT_OFFSET_X ) / ModuleNEI.SLOT_INDEX_DIVISOR;
					int slotY = ( ingredient.rely - ModuleNEI.NEI_SLOT_OFFSET_Y ) / ModuleNEI.SLOT_INDEX_DIVISOR;

					// Calculate the slot index
					int slotIndex = slotX + ( slotY * 3 );

					// Add the item to the list
					items[slotIndex] = AEApi.instance().storage().createItemStack( ingredient.item );

					hasItems = true;
				}

				// Were any items added?
				if( hasItems )
				{
					// Send the list to the server
					new PacketServerArcaneCraftingTerminal().createNEIRequestSetCraftingGrid( Minecraft.getMinecraft().thePlayer, items )
									.sendPacketToServer();
				}
			}
		}
	}

	/**
	 * Corrects the slot positions so that NEI can find and render them.
	 * 
	 * @author Nividica
	 * 
	 */
	public class ACTSlotFinder
		implements IStackPositioner
	{

		@Override
		public ArrayList<PositionedStack> positionStacks( final ArrayList<PositionedStack> stacks )
		{
			// Adjust the position of the ghost stacks to match the crafting grid slots
			for( PositionedStack ps : stacks )
			{
				// Ensure there is a stack
				if( ps != null )
				{
					// Adjust it's position
					ps.relx += ContainerPartArcaneCraftingTerminal.CRAFTING_SLOT_X_POS - ModuleNEI.NEI_SLOT_OFFSET_X;
					ps.rely += ContainerPartArcaneCraftingTerminal.CRAFTING_SLOT_Y_POS - ModuleNEI.NEI_SLOT_OFFSET_Y;
				}
			}

			return stacks;
		}

	}

	/**
	 * Starting X offset for slots in NEI
	 */
	private static final int NEI_SLOT_OFFSET_X = 25;

	/**
	 * Starting Y offset for slots in NEI
	 */
	private static final int NEI_SLOT_OFFSET_Y = 6;

	/**
	 * Reduces slot offset to 0, 1, or 2
	 */
	private static final int SLOT_INDEX_DIVISOR = 18;

	/**
	 * Integrates with Not Enough Items
	 * 
	 * @throws Exception
	 */
	public ModuleNEI() throws Exception
	{

		// Register the ACT overlay
		API.registerGuiOverlay( GuiArcaneCraftingTerminal.class, "crafting", new ACTSlotFinder() );

		// Create the overlay handler
		ACTOverlayHandler overlayHandler = new ACTOverlayHandler();

		// Register the handler
		API.registerGuiOverlayHandler( GuiArcaneCraftingTerminal.class, overlayHandler, "crafting" );
	}

}
