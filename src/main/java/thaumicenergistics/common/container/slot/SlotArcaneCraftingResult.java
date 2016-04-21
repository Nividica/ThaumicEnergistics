package thaumicenergistics.common.container.slot;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThELog;

/**
 * Slot that holds the result of an arcane crafting recipe.
 *
 * @author Nividica
 *
 */
public class SlotArcaneCraftingResult
	extends SlotCrafting
{
	/**
	 * The inventory of the terminal
	 */
	private IInventory terminalInventory;

	/**
	 * Aspects required for this craft.
	 */
	private AspectList craftingAspects = null;

	/**
	 * Itemstack that represents the wand.
	 */
	private ItemStack wand;

	/**
	 * The wand item.
	 */
	private ItemWandCasting wandItem;

	/**
	 * The container that hosts this slot.
	 */
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
	public SlotArcaneCraftingResult(	final EntityPlayer player, final ContainerPartArcaneCraftingTerminal hostContianer,
										final IInventory terminalInventory, final IInventory slotInventory, final int slotIndex, final int xPos,
										final int yPos )
	{
		// Call super
		super( player, terminalInventory, slotInventory, slotIndex, xPos, yPos );

		// Set the matrix
		this.terminalInventory = terminalInventory;

		// Set the container
		this.hostContianer = hostContianer;
	}

	@Override
	public void onPickupFromSlot( final EntityPlayer player, final ItemStack itemStack )
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
	public void onPickupFromSlotViaTransfer( final EntityPlayer player, final ItemStack itemStack )
	{
		// Fire crafting event
		FMLCommonHandler.instance().firePlayerCraftingEvent( player, itemStack, this.terminalInventory );

		// Seems to fire achievements, and handles the item creation
		this.onCrafting( itemStack );

		if( EffectiveSide.isClientSide() )
		{
			// No more work on the client side.
			return;
		}

		ThELog.info( "%d", itemStack.stackSize );

		// Arcane recipe?
		if( ( this.craftingAspects != null ) )
		{
			// Consume wand vis
			this.wandItem.consumeAllVisCrafting( this.wand, player, this.craftingAspects, true );

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

			// Checked at the end to see if we need to decrement the slot
			boolean shouldDecrement = true;

			// Does the item have a container?
			if( slotStack.getItem().hasContainerItem( slotStack ) )
			{
				// Get the container item
				ItemStack slotContainerStack = slotStack.getItem().getContainerItem( slotStack );

				// Is the container item damage-able?
				if( slotContainerStack.isItemStackDamageable() )
				{
					// Did we kill it?
					if( slotContainerStack.getItemDamage() >= slotContainerStack.getMaxDamage() )
					{
						// Fire forge event
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( player, slotContainerStack ) );

						// Null the container stack
						slotContainerStack = null;
					}
				}

				// Did we not kill the container item?
				if( slotContainerStack != null )
				{
					/*
					 * Should the item stay in the crafting grid, or if it is supposed to go back to the
					 * players inventory but can't?
					 */
					if( !slotStack.getItem().doesContainerItemLeaveCraftingGrid( slotStack ) ||
									!player.inventory.addItemStackToInventory( slotContainerStack ) )
					{
						// Place it back in the grid
						this.terminalInventory.setInventorySlotContents( slotIndex, slotContainerStack );

						// Set NOT to decrement
						shouldDecrement = false;
					}
				}
			}

			// If decrementing it would result in it being empty, ask the ME system for a replenishment.
			if( shouldDecrement && ( slotStack.stackSize == 1 ) )
			{
				// First check if we can replenish it from the ME network
				ItemStack replenishment = this.hostContianer.requestCraftingReplenishment( slotStack );

				// Did we get a replenishment?
				if( replenishment != null )
				{
					// Did the item change?
					if( !ItemStack.areItemStacksEqual( replenishment, slotStack ) )
					{
						// Set the slot contents to the replenishment
						this.terminalInventory.setInventorySlotContents( slotIndex, replenishment );
					}

					// And mark not to decrement
					shouldDecrement = false;
				}
			}

			// Decrement the stack?
			if( shouldDecrement )
			{
				this.terminalInventory.decrStackSize( slotIndex, 1 );
			}

		}
	}

	public void setResultAspects( final AspectList aspectList )
	{
		this.craftingAspects = aspectList;
	}

	public void setWand( final ItemStack wand )
	{
		if( wand != null )
		{
			this.wand = wand;
			this.wandItem = (ItemWandCasting)wand.getItem();
		}
		else
		{
			this.wand = null;
			this.wandItem = null;
		}
	}

}
