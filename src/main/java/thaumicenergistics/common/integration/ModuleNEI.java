package thaumicenergistics.common.integration;

import java.util.ArrayList;
import java.util.List;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_ArcaneCraftingTerminal;

/**
 * Contains all code required to integrate with Not Enough Items.
 *
 * @author Nividica
 *
 */
public class ModuleNEI
{

	/**
	 * Base class for handling item overlays.
	 *
	 * @author Nividica
	 *
	 */
	abstract class AbstractBaseOverlayHandler
		implements IOverlayHandler
	{
		/**
		 * Calls on the subclass to add an ingredient to the items array.
		 *
		 * @param ingredient
		 * @param overlayItems
		 */
		protected abstract boolean addIngredientToItems( PositionedStack ingredient, IAEItemStack[] overlayItems );

		/**
		 * Called when the items are ready to be placed in the GUI.
		 *
		 * @param overlayItems
		 */
		protected abstract void addItemsToGUI( IAEItemStack[] overlayItems );

		/**
		 * Checks with the subclass to see if this is a GUI it handles.
		 *
		 * @param gui
		 * @return
		 */
		protected abstract boolean isCorrectGUI( GuiContainer gui );

		/**
		 * Called when the user has shift-clicked the [?] button in NEI
		 */
		@Override
		public final void overlayRecipe( final GuiContainer gui, final IRecipeHandler recipeHandler, final int recipeIndex, final boolean shift )
		{
			try
			{
				// Ensure the gui is correct
				if( this.isCorrectGUI( gui ) )
				{
					// List of items
					IAEItemStack[] overlayItems = new IAEItemStack[9];

					// Assume there are no items until they are added.
					boolean hasItems = false;

					// Get the ingredients
					List<PositionedStack> ingredients = recipeHandler.getIngredientStacks( recipeIndex );

					// Get each item
					for( PositionedStack ingredient : ingredients )
					{
						// Skip nulls
						if( ( ingredient == null ) || ( ingredient.item == null ) || ( ingredient.item.getItem() == null ) )
						{
							continue;
						}

						// Pass to subclass
						hasItems |= this.addIngredientToItems( ingredient, overlayItems );
					}

					// Were any items added?
					if( hasItems )
					{
						this.addItemsToGUI( overlayItems );
					}
				}
			}
			catch( Exception e )
			{
				// Silently ignored.
			}
		}

	}

	/**
	 * Sends a selected NEI recipe to the open A.C.T on the server.
	 *
	 * @author Nividica
	 *
	 */
	public class ACTOverlayHandler
		extends AbstractBaseOverlayHandler
	{
		/**
		 * Reduces regular slot offsets to 0, 1, or 2
		 */
		private static final int REGULAR_SLOT_INDEX_DIVISOR = 18;

		/**
		 * Reduces arcane slot Y offsets to 0,1, or 2
		 */
		private static final int ARCANE_SLOTY_INDEX_DIVISOR = 20;

		/**
		 * If true incoming ingredients are from ThaumcraftNEIPlugin
		 */
		private final boolean isArcaneHandler;

		/**
		 * Creates the handler.
		 *
		 * @param isArcane
		 * Set to true for arcane recipes
		 */
		public ACTOverlayHandler( final boolean isArcane )
		{
			this.isArcaneHandler = isArcane;
		}

		/**
		 * Adds ThaumcraftNEIPlugin ingredients to the ACT crafting grid.
		 *
		 * @return
		 */
		private boolean addArcaneCraftingItems( final PositionedStack ingredient, final IAEItemStack[] overlayItems )
		{
			// Calculate the slot positions
			int slotX = (int)Math.round( ingredient.relx / (double)ACTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR ) - 3;
			int slotY = (int)Math.round( ingredient.rely / (double)ACTOverlayHandler.ARCANE_SLOTY_INDEX_DIVISOR ) - 2;

			// Ignore the 'aspects'
			if( slotY >= 3 )
			{
				return false;
			}

			// Roundoff fix
			if( slotX == 3 )
			{
				slotX = 2;
			}

			// Calculate the slot index
			int slotIndex = slotX + ( slotY * 3 );

			// Bounds check the index
			if( ( slotIndex < 0 ) || ( slotIndex > 9 ) )
			{
				// Invalid slot
				return false;
			}

			// Add the item to the list
			overlayItems[slotIndex] = AEApi.instance().storage().createItemStack( ingredient.item );

			return true;
		}

		/**
		 * Adds NEI ingredients to the ACT crafting grid.
		 *
		 * @param ingredient
		 * @param overlayItems
		 * @return
		 */
		private boolean addRegularCraftingItems( final PositionedStack ingredient, final IAEItemStack[] overlayItems )
		{
			// Calculate the slot positions
			int slotX = ( ingredient.relx - ModuleNEI.NEI_REGULAR_SLOT_OFFSET_X ) / ACTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR;
			int slotY = ( ingredient.rely - ModuleNEI.NEI_REGULAR_SLOT_OFFSET_Y ) / ACTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR;

			// Calculate the slot index
			int slotIndex = slotX + ( slotY * 3 );

			// Add the item to the list
			overlayItems[slotIndex] = AEApi.instance().storage().createItemStack( ingredient.item );

			return true;
		}

		@Override
		protected boolean addIngredientToItems( final PositionedStack ingredient, final IAEItemStack[] overlayItems )
		{
			// Arcane?
			if( this.isArcaneHandler )
			{
				// Pass to arcane handler
				return this.addArcaneCraftingItems( ingredient, overlayItems );
			}

			// Pass to regular handler
			return this.addRegularCraftingItems( ingredient, overlayItems );
		}

		@Override
		protected void addItemsToGUI( final IAEItemStack[] overlayItems )
		{
			// Send the list to the server
			Packet_S_ArcaneCraftingTerminal.sendSetCrafting_NEI( Minecraft.getMinecraft().thePlayer, overlayItems );
		}

		@Override
		protected boolean isCorrectGUI( final GuiContainer gui )
		{
			return( gui instanceof GuiArcaneCraftingTerminal );
		}
	}

	/**
	 * Corrects the slot positions so that NEI can find and render them.
	 *
	 * @author Nividica
	 *
	 */
	public class ACTSlotPositioner
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
					ps.relx += ContainerPartArcaneCraftingTerminal.CRAFTING_SLOT_X_POS - ModuleNEI.NEI_REGULAR_SLOT_OFFSET_X;
					ps.rely += ContainerPartArcaneCraftingTerminal.CRAFTING_SLOT_Y_POS - ModuleNEI.NEI_REGULAR_SLOT_OFFSET_Y;
				}
			}

			return stacks;
		}

	}

	/**
	 * Starting X offset for slots in NEI
	 */
	static final int NEI_REGULAR_SLOT_OFFSET_X = 25;

	/**
	 * Starting Y offset for slots in NEI
	 */
	static final int NEI_REGULAR_SLOT_OFFSET_Y = 6;

	/**
	 * Integrates with Not Enough Items
	 *
	 * @throws Exception
	 */
	public ModuleNEI() throws Exception
	{
		// Register the ACT overlays
		API.registerGuiOverlay( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, "crafting", new ACTSlotPositioner() );

		// Create the regular overlay handler
		ACTOverlayHandler craftingOverlayHandler = new ACTOverlayHandler( false );

		// Create the arcane overlay handler
		ACTOverlayHandler arcaneOverlayHandler = new ACTOverlayHandler( true );

		// Register the handlers
		API.registerGuiOverlayHandler( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, craftingOverlayHandler, "crafting" );
		API.registerGuiOverlayHandler( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, arcaneOverlayHandler, "arcaneshapedrecipes" );
		API.registerGuiOverlayHandler( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, arcaneOverlayHandler,
			"arcaneshapelessrecipes" );
	}

}
