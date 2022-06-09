package thaumicenergistics.common.integration;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import appeng.util.Platform;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.INEIGuiAdapter;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.client.gui.abstraction.ThEBaseGui;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.network.NetworkHandler;
import thaumicenergistics.common.network.packet.server.Packet_S_NEIRecipe;

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
	static class ACTOverlayHandler implements IOverlayHandler
	{
		/**
		 * Called when the user has shift-clicked the [?] button in NEI
		 */
		@Override
		public final void overlayRecipe( final GuiContainer gui, final IRecipeHandler recipeHandler, final int recipeIndex, final boolean shift )
		{
			try
			{
				// Ensure the gui is correct
				if( gui instanceof GuiArcaneCraftingTerminal )
				{
					List<PositionedStack> ingredients = recipeHandler.getIngredientStacks( recipeIndex );

					NBTTagCompound recipe = new NBTTagCompound();
					for( PositionedStack ingredient : ingredients )
					{
						if( ( ingredient == null ) || ( ingredient.item == null ) || ( ingredient.item.getItem() == null ) )
							continue;
						addIngredientToItems( ingredient, recipe, false );
					}
					if (testSize(recipe, 32*1024))
					{
						recipe = new NBTTagCompound();
						for( PositionedStack ingredient : ingredients )
						{
							if( ( ingredient == null ) || ( ingredient.item == null ) || ( ingredient.item.getItem() == null ) )
								continue;
							addIngredientToItems( ingredient, recipe, true );
						}
					}
					Packet_S_NEIRecipe packet = new Packet_S_NEIRecipe();
					packet.setRecipe(recipe);
					packet.player = Minecraft.getMinecraft().thePlayer;

					NetworkHandler.sendPacketToServer( packet );
				}
			}
			catch( Exception e )
			{
				// Silently ignored.
			}
		}

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

		private void packIngredient(NBTTagCompound recipe, int slotIndex, PositionedStack positionedStack, boolean limited) throws IOException
		{
			final NBTTagList tags = new NBTTagList();
			final List<ItemStack> list = new LinkedList<>();

			// prefer pure crystals.
			for( int x = 0; x < positionedStack.items.length; x++ )
			{
				if( Platform.isRecipePrioritized( positionedStack.items[x] ) )
				{
					list.add( 0, positionedStack.items[x] );
				}
				else
				{
					list.add( positionedStack.items[x] );
				}
			}

			for( final ItemStack is : list )
			{
				final NBTTagCompound tag = new NBTTagCompound();
				is.writeToNBT( tag );
				tags.appendTag( tag );
				if (limited)
				{
					final NBTTagCompound test = new NBTTagCompound();
					test.setTag( "#" + slotIndex, tags );
					if (testSize(test, 3*1024))
						break;
				}
			}
			recipe.setTag( "#" + slotIndex, tags );
		}
		// if the packet becomes too large, limit each slot contents to 3k
		protected boolean testSize(final NBTTagCompound recipe, int limit) throws IOException
		{
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream outputStream = new DataOutputStream( bytes );
			CompressedStreamTools.writeCompressed( recipe, outputStream );
			return bytes.size() > limit;
		}

		private boolean addArcaneCraftingItems( final PositionedStack ingredient, NBTTagCompound recipe, boolean limited) throws IOException
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
			packIngredient(recipe, slotIndex, ingredient, limited);

			return true;
		}

		private boolean addRegularCraftingItems( final PositionedStack ingredient, NBTTagCompound recipe, boolean limited) throws IOException
		{
			// Calculate the slot positions
			int slotX = ( ingredient.relx - ModuleNEI.NEI_REGULAR_SLOT_OFFSET_X ) / ACTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR;
			int slotY = ( ingredient.rely - ModuleNEI.NEI_REGULAR_SLOT_OFFSET_Y ) / ACTOverlayHandler.REGULAR_SLOT_INDEX_DIVISOR;

			// Calculate the slot index
			int slotIndex = slotX + ( slotY * 3 );

			// Add the item to the list
			packIngredient(recipe, slotIndex, ingredient, limited);

			return true;
		}

		protected boolean addIngredientToItems( final PositionedStack ingredient, NBTTagCompound recipe, boolean limited) throws IOException
		{
			return isArcaneHandler ? addArcaneCraftingItems( ingredient, recipe, limited ) : addRegularCraftingItems( ingredient, recipe, limited );
		}
	}

	/**
	 * Corrects the slot positions so that NEI can find and render them.
	 *
	 * @author Nividica
	 *
	 */
	public static class ACTSlotPositioner
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

	static class NEIGuiHandler extends INEIGuiAdapter {
		@Override
		public boolean handleDragNDrop(GuiContainer gui, int mousex, int mousey, ItemStack draggedStack, int button) {
			if (gui instanceof ThEBaseGui && draggedStack != null && draggedStack.getItem() != null) {
				((ThEBaseGui) gui).draggedStack = draggedStack.copy();
				draggedStack.stackSize = 0;
			}

			return super.handleDragNDrop(gui, mousex, mousey, draggedStack, button);
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
		API.registerGuiOverlay( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, "crafting", new ACTSlotPositioner());

		// Create the regular overlay handler
		ACTOverlayHandler craftingOverlayHandler = new ACTOverlayHandler(false);

		// Create the arcane overlay handler
		ACTOverlayHandler arcaneOverlayHandler = new ACTOverlayHandler(true);

		// Register the handlers
		API.registerGuiOverlayHandler( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, craftingOverlayHandler, "crafting" );
		API.registerGuiOverlayHandler( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, arcaneOverlayHandler, "arcaneshapedrecipes" );
		API.registerGuiOverlayHandler( thaumicenergistics.client.gui.GuiArcaneCraftingTerminal.class, arcaneOverlayHandler, "arcaneshapelessrecipes" );

		API.registerNEIGuiHandler(new NEIGuiHandler());

		// Hide the crafting aspect item
		API.hideItem( ItemEnum.CRAFTING_ASPECT.getStack() );

	}

}
