package thaumicenergistics.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import cpw.mods.fml.common.FMLCommonHandler;

public class SlotArcaneCraftingResult
	extends SlotCrafting
{
	/**
	 * The inventory of the terminal
	 */
	private IInventory terminalInventory;
	
	private AspectList craftingAspects = null;
	
	private ItemStack wand;

	/**
	 * Creates the slot
	 * 
	 * @param player
	 * @param terminalInventory
	 * @param slotInventory
	 * @param slotIndex
	 * @param xPos
	 * @param yPos
	 */
	public SlotArcaneCraftingResult( EntityPlayer player, IInventory terminalInventory, IInventory slotInventory, int slotIndex, int xPos, int yPos )
	{
		// Call super
		super( player, terminalInventory, slotInventory, slotIndex, xPos, yPos );

		// Set the matrix
		this.terminalInventory = terminalInventory;
	}
	
	public void setResultAspects( AspectList aspectList )
	{
		this.craftingAspects = aspectList;
	}
	
	public void setWand( ItemStack wand )
	{
		this.wand = wand;
	}

	@Override
	public void onPickupFromSlot( EntityPlayer player, ItemStack itemStack )
	{
		// Not really sure what this does, but "SlotCrafting" does it, so I shall also!
		FMLCommonHandler.instance().firePlayerCraftingEvent( player, itemStack, this.terminalInventory );

		// Seems to fire achievements, and handles the item creation
		this.onCrafting( itemStack );

		// Is this an arcane recipe?
		if( this.craftingAspects != null )
		{
			// Consume wand vis
			( (ItemWandCasting)this.wand.getItem() ).consumeAllVisCrafting( this.wand, player, this.craftingAspects, true );
			
		}

		// Loop over all crafting slots
		for( int slotIndex = 0; slotIndex < ContainerPartArcaneCraftingTerminal.CRAFTING_GRID_TOTAL_SIZE; slotIndex++ )
		{
			// Get the itemstack in this slot
			ItemStack slotStack = this.terminalInventory.getStackInSlot( slotIndex );
			
			// Is there a stack?
			if( slotStack == null )
			{
				// Next
				continue;
			}
			
			// Decrease it's size by 1
			this.terminalInventory.decrStackSize( slotIndex, 1 );
			
			// Does the item in the slotstack have a container?
			if( slotStack.getItem().hasContainerItem( slotStack ) )
			{
				// Get the container item
				ItemStack slotContainerItem = slotStack.getItem().getContainerItem( slotStack );
				
				// Is the container item damage-able?
				if( slotContainerItem.isItemStackDamageable() )
				{
					// Did we kill it?
					if( slotContainerItem.getItemDamage() >= slotContainerItem.getMaxDamage() )
					{
						// Still not sure about these forge events, really need to read up on them
						// But again "SlotCrafting" does this, so shall I
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, slotContainerItem ) );
						
						// Null out the container item
						slotContainerItem = null;
					}
				}
				
				// Did we not kill the container item?
				if( slotContainerItem != null )
				{
					// Should the item go back to the player inventory?
					if( slotStack.getItem().doesContainerItemLeaveCraftingGrid( slotStack ) )
					{
						// Attempt to place it in the players inventory
						if( player.inventory.addItemStackToInventory( slotContainerItem ) )
						{
							// Could place it, next
							continue;
						}
						
						// Could not place it back in the players inventory
					}

					// Place it back in the grid
					this.terminalInventory.setInventorySlotContents( slotIndex, slotContainerItem );
				}
			}
		}
	}

}
