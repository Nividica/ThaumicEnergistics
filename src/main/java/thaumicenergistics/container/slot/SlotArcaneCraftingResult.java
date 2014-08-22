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
import thaumicenergistics.util.EffectiveSide;
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

	private ContainerPartArcaneCraftingTerminal hostContianer;

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
	public SlotArcaneCraftingResult( EntityPlayer player, ContainerPartArcaneCraftingTerminal hostContianer, IInventory terminalInventory,
										IInventory slotInventory, int slotIndex, int xPos, int yPos )
	{
		// Call super
		super( player, terminalInventory, slotInventory, slotIndex, xPos, yPos );

		// Set the matrix
		this.terminalInventory = terminalInventory;

		// Set the container
		this.hostContianer = hostContianer;
	}

	@Override
	public void onPickupFromSlot( EntityPlayer player, ItemStack itemStack )
	{
		// Call the transfer
		this.onPickupFromSlotViaTransfer( player, itemStack );

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Send any changes to the client
			this.hostContianer.detectAndSendChanges();
		}
	}

	/**
	 * Similar to the onPickupFromSlot, with the key difference being
	 * that this function call will not update the client.
	 * 
	 * @param player
	 * @param itemStack
	 */
	public void onPickupFromSlotViaTransfer( EntityPlayer player, ItemStack itemStack )
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

		// From here on the server will handle the rest
		if( EffectiveSide.isClientSide() )
		{
			return;
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

			// Make a copy
			ItemStack slotStackCopy = slotStack.copy();

			// Checked at the end to see if we need to decrement the slot
			boolean shouldDecrement = true;

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
					/*
					 * Should the item stay in the crafting grid, or if it is supposed to go back to the
					 * players inventory but can't?
					 */
					if( !slotStack.getItem().doesContainerItemLeaveCraftingGrid( slotStack ) ||
									!player.inventory.addItemStackToInventory( slotContainerItem ) )
					{
						// Place it back in the grid
						this.terminalInventory.setInventorySlotContents( slotIndex, slotContainerItem );

						// Set NOT to decrement
						shouldDecrement = false;
					}
				}
			}

			// Should we decrement the inventory stack?
			if( shouldDecrement )
			{
				// Would decrementing it result in it being empty?
				if( slotStackCopy.stackSize == 1 )
				{
					// First check if we can replenish it from the ME network
					ItemStack replenishment = this.hostContianer.requestCraftingReplenishment( slotStackCopy );

					// Did we get a replenishment?
					if( replenishment != null )
					{
						// Set the slot contents to the replenishment
						this.terminalInventory.setInventorySlotContents( slotIndex, replenishment );

						// And mark not to decrement
						shouldDecrement = false;
					}
				}

				// Check again, should we decrement?
				if( shouldDecrement )
				{
					// Decrease it's size by 1
					this.terminalInventory.decrStackSize( slotIndex, 1 );
				}
			}
		}
	}

	public void setResultAspects( AspectList aspectList )
	{
		this.craftingAspects = aspectList;
	}

	public void setWand( ItemStack wand )
	{
		this.wand = wand;
	}

}
