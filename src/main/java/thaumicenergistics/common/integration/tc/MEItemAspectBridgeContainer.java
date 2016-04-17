package thaumicenergistics.common.integration.tc;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumicenergistics.common.inventory.TheInternalInventory;

/**
 * Allows the Thaumcraft item aspect renderer (the aspects you see while holding
 * down shift and mousing over and item) to be aware of the added slots and
 * their contents.
 *
 * @author Nividica
 *
 */
@SideOnly(Side.CLIENT)
public class MEItemAspectBridgeContainer
	extends Container
{
	/**
	 * Reference to TC's client event handler, which houses the renderAspects
	 * function I need.
	 */
	private static WeakReference<ClientTickEventsFML> thaumcraftClientEvents = new WeakReference<ClientTickEventsFML>( null );

	/**
	 * Internal inventory that mirrors the ME items displayed by the A.C.T
	 */
	private TheInternalInventory internalInventory;

	/**
	 * Constructs a pseudo container that TC can use to 'see' the items in the
	 * ME network.
	 *
	 * @param inventorySize
	 * @throws Exception
	 * When the TC rendering code can not be found, the bridge will not
	 * construct.
	 */
	public MEItemAspectBridgeContainer( final int inventorySize ) throws Exception
	{
		// Create the inventory
		this.internalInventory = new TheInternalInventory( "TC Inventory Bridge", inventorySize, 1 );

		// Have we hooked to TC's event handler?
		if( thaumcraftClientEvents.get() == null )
		{
			// Access the listenerOwners field
			Field loField = EventBus.class.getDeclaredField( "listenerOwners" );
			loField.setAccessible( true );

			// Get the owners
			Map<Object, ModContainer> owners = (Map<Object, ModContainer>)loField.get( FMLCommonHandler.instance().bus() );

			// Attempt to locate TC's event handler
			for( Entry<Object, ModContainer> set : owners.entrySet() )
			{
				Object Owner = set.getKey();

				if( Owner instanceof thaumcraft.client.lib.ClientTickEventsFML )
				{
					thaumcraftClientEvents = new WeakReference<ClientTickEventsFML>( (ClientTickEventsFML)Owner );
					break;
				}
			}

			// Get rid of the references, cause I'm paranoid.
			owners = null;
			loField = null;

			// Did we locate it?
			if( thaumcraftClientEvents == null )
			{
				throw new Exception( "Unable to find TC event handler" );
			}
		}

	}

	/**
	 * Adds a slot to the container. The index and position should perfectly
	 * match the widgets they represent.
	 *
	 * @param index
	 * @param posX
	 * @param posY
	 */
	public void addSlot( final int index, final int posX, final int posY )
	{
		// Create the slot
		Slot bridgeSlot = new Slot( this.internalInventory, index, posX, posY );

		// Add the slot
		this.addSlotToContainer( bridgeSlot );
	}

	@Override
	public boolean canInteractWith( final EntityPlayer p_75145_1_ )
	{
		// Ignored
		return false;
	}

	/**
	 * Calls on TC to render the items aspects. This should only be called from
	 * a GUI's drawScreen event.
	 *
	 * @param gui
	 * @param player
	 */
	public void renderAspects( final GuiContainer gui, final EntityPlayer player )
	{
		ClientTickEventsFML tceh = thaumcraftClientEvents.get();

		if( tceh != null )
		{
			// Get the actual container
			Container actualContainer = gui.inventorySlots;

			// Set the container to this bridge
			gui.inventorySlots = this;

			// Call render
			tceh.renderAspectsInGui( gui, player );

			// Re-set the container to the original
			gui.inventorySlots = actualContainer;
		}
	}

	/**
	 * Sets the itemstack of the slot. When you update a widget, update this as
	 * well.
	 *
	 * @param index
	 * @param stack
	 */
	public void setSlot( final int index, final ItemStack stack )
	{
		this.getSlot( index ).putStack( stack );
	}

}
