package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartArcaneCraftingTerminal
	extends AEPartBase
	implements IInventory
{
	/**
	 * Number of slots in the internal inventory
	 */
	private static final int MY_INVENTORY_SIZE = 11;

	/**
	 * Index of the wand slot
	 */
	public static final int WAND_SLOT_INDEX = 9;

	/**
	 * Index of the result slot
	 */
	public static final int RESULT_SLOT_INDEX = 10;

	/**
	 * Inventory name
	 */
	private static final String INVENTORY_NBT_KEY = "TEACT_Inventory";

	/**
	 * Key used for reading/writing inventory slots to NBT
	 */
	private static final String SLOT_NBT_KEY = "Slot#";

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 1.2D;

	/**
	 * Inventory slots
	 */
	private final ItemStack[] slots = new ItemStack[AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE];

	/**
	 * Containers wishing to be notified on inventory changes
	 */
	private List<ContainerPartArcaneCraftingTerminal> listeners = new ArrayList<ContainerPartArcaneCraftingTerminal>();

	/**
	 * Creates the terminal
	 */
	public AEPartArcaneCraftingTerminal()
	{
		// Call super
		super( AEPartsEnum.ArcaneCraftingTerminal );
	}

	/**
	 * Validates that a slot is inbounds of the inventory.
	 * 
	 * @param slotIndex
	 * @return
	 */
	private boolean isSlotInRange( int slotIndex )
	{
		// Is the slot in range?
		return( ( slotIndex >= 0 ) && ( slotIndex < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE ) );
	}

	/**
	 * Notifies all listeners that our inventory contents
	 * have changed.
	 * 
	 * @param slotIndex
	 */
	private void notifyListeners( int slotIndex )
	{
		// Loop over all listeners
		for( ContainerPartArcaneCraftingTerminal listener : this.listeners )
		{
			// Ensure the listener is still there
			if( listener != null )
			{
				// Notify it
				listener.onCraftMatrixChanged( this );
			}
		}
	}

	/**
	 * Distance cable should extend to meet this block
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	@Override
	public void closeInventory()
	{
		// Ignored
	}

	/**
	 * Decreases the size of the itemstack in the specified
	 * slot by the specified amount, and returns the itemstack.
	 * Can be null.
	 */
	@Override
	public ItemStack decrStackSize( int slotIndex, int amount )
	{
		ItemStack returnStack = null;

		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Get the stack
			ItemStack slotStack = this.slots[slotIndex];

			// Is the slot not null?
			if( slotStack != null )
			{

				// Is the amount for more than or all of the slot?
				if( amount >= slotStack.stackSize )
				{
					// Set the return to a copy of the stack
					returnStack = slotStack.copy();

					// Set stack size to 0
					this.slots[slotIndex].stackSize = 0;
				}
				else
				{
					// Split the slot stack
					returnStack = slotStack.splitStack( amount );
				}

				// Is the size now 0?
				if( this.slots[slotIndex].stackSize == 0 )
				{
					// Null it out
					this.slots[slotIndex] = null;
				}

				// Notify the worker
				this.notifyListeners( slotIndex );
			}
		}

		return returnStack;
	}

	/**
	 * Collision boxes
	 */
	@Override
	public void getBoxes( IPartCollsionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	/**
	 * Gets the GUI associated with this part
	 */
	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiArcaneCraftingTerminal( this, player );
	}

	/**
	 * Determines if items should be dropped on the ground when
	 * the part has been removed.
	 */
	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		// Were we wrenched?
		if( wrenched )
		{
			// Inventory is saved when wrenched
			return;
		}

		// Loop over inventory
		for( int slotIndex = 0; slotIndex < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE; slotIndex++ )
		{
			// Skip if this is the output slot
			if( slotIndex == AEPartArcaneCraftingTerminal.RESULT_SLOT_INDEX )
			{
				continue;
			}

			// Get the stack at this index
			ItemStack slotStack = this.slots[slotIndex];

			// Did we get anything?
			if( slotStack != null )
			{
				// Add to drops
				drops.add( slotStack );
			}
		}
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartArcaneCraftingTerminal.IDLE_POWER_DRAIN;
	}

	/**
	 * Gets the inventory name.
	 */
	@Override
	public String getInventoryName()
	{
		return ThaumicEnergistics.MOD_ID + ".arcane.crafting.terminal.inventory";
	}

	/**
	 * Maximum number of allowed items per stack.
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	/**
	 * Gets the container associated with this part.
	 */
	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartArcaneCraftingTerminal( this, player );
	}

	/**
	 * Returns the internal inventory size.
	 */
	@Override
	public int getSizeInventory()
	{
		return AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE;
	}

	/**
	 * Gets the itemstack in the specified slot index.
	 * Can be null.
	 */
	@Override
	public ItemStack getStackInSlot( int slotIndex )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Return the contents of the slot
			return this.slots[slotIndex];
		}

		// Return null
		return null;
	}

	/**
	 * Returns the itemstack in the specified slot, can be null.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing( int slotIndex )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			return this.slots[slotIndex];
		}

		return null;
	}

	/**
	 * Gets the world this inventory is in.
	 * 
	 * @return
	 */
	public World getWorldObj()
	{
		return this.hostTile.getWorldObj();
	}

	/**
	 * Returns true.
	 */
	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	public static boolean isItemValidCraftingWand( ItemStack stack )
	{
		// Ensure it is not null
		if( stack == null )
		{
			return false;
		}

		// Get the item
		Item item = stack.getItem();

		// Ensure the item is not null
		if( item == null )
		{
			return false;
		}

		// Ensure it is a casting wand
		if( !( item instanceof ItemWandCasting ) )
		{
			return false;
		}

		// Ensure it is not a staff
		if( ( (ItemWandCasting)item ).isStaff( stack ) )
		{
			return false;
		}

		// Valid wand
		return true;
	}

	/**
	 * Determines if the specified itemstack is valid for the slot index.
	 */
	@Override
	public boolean isItemValidForSlot( int slotIndex, ItemStack proposedStack )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Is this the wand slot?
			if( slotIndex == AEPartArcaneCraftingTerminal.WAND_SLOT_INDEX )
			{
				// Is the item a wand?
				return AEPartArcaneCraftingTerminal.isItemValidCraftingWand( proposedStack );
			}

			// Unrestricted slot
			return true;
		}

		// Out of range
		return false;
	}

	/**
	 * Who can use this?
	 */
	@Override
	public boolean isUseableByPlayer( EntityPlayer player )
	{
		return true;
	}

	/**
	 * Ensures the inventory is saved.
	 */
	@Override
	public void markDirty()
	{
		// Mark the host tile
		this.hostTile.markDirty();
	}

	@Override
	public void openInventory()
	{
		// Ignored
	}

	/**
	 * Loads part data from NBT tag
	 */
	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		// Does the data tag have the list?
		if( data.hasKey( AEPartArcaneCraftingTerminal.INVENTORY_NBT_KEY ) )
		{
			// Get the list
			NBTTagList nbtTagList = (NBTTagList)data.getTag( AEPartArcaneCraftingTerminal.INVENTORY_NBT_KEY );

			for( int listIndex = 0; listIndex < nbtTagList.tagCount(); listIndex++ )
			{
				// Get the compound tag
				NBTTagCompound nbtCompound = nbtTagList.getCompoundTagAt( listIndex );

				// Get the slot list
				int slotIndex = nbtCompound.getByte( AEPartArcaneCraftingTerminal.SLOT_NBT_KEY );

				// Is it in range?
				if( this.isSlotInRange( slotIndex ) )
				{
					// Set the slot
					this.slots[slotIndex] = ItemStack.loadItemStackFromNBT( nbtCompound );
				}
			}
		}
	}

	/**
	 * Adds a listener to the list if not already added.
	 * 
	 * @param container
	 */
	public void registerListener( ContainerPartArcaneCraftingTerminal container )
	{
		// Is this already registered?
		if( !this.listeners.contains( container ) )
		{
			// Add to the list
			this.listeners.add( container );
		}

	}

	/**
	 * Removes a listener from the list.
	 * 
	 * @param container
	 */
	public void removeListener( ContainerPartArcaneCraftingTerminal container )
	{
		// Remove the container from the listeners
		this.listeners.remove( container );
	}

	/**
	 * Renders the part while in the inventory
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory( IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
		ts.setColorOpaque_I( AEPartBase.INVENTORY_OVERLAY_COLOR );
		helper.renderInventoryFace( BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		ts.setColorOpaque_I( AEColor.Black.mediumVariant );
		helper.renderInventoryFace( BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	/**
	 * Renders the part in the world.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic( int x, int y, int z, IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		if( this.isActive() )
		{
			tessellator.setBrightness( AEPartBase.ACTIVE_BRIGHTNESS );

			helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
			tessellator.setColorOpaque_I( this.host.getColor().blackVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

			tessellator.setColorOpaque_I( this.host.getColor().mediumVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );
		}

		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	/**
	 * Sets the contents of the specified inventory slot and
	 * updates the listeners.
	 * 
	 * @param slotIndex
	 * @param slotStack
	 * @return
	 */
	@Override
	public void setInventorySlotContents( int slotIndex, ItemStack slotStack )
	{
		if( this.setInventorySlotContentsWithoutNotify( slotIndex, slotStack ) )
		{
			// Inform the listeners
			this.notifyListeners( slotIndex );
		}
	}

	/**
	 * Sets the contents of the specified inventory slot without
	 * updating listeners.
	 * 
	 * @param slotIndex
	 * @param slotStack
	 * @return
	 */
	public boolean setInventorySlotContentsWithoutNotify( int slotIndex, ItemStack slotStack )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Set the slot
			this.slots[slotIndex] = slotStack;

			return true;
		}

		return false;
	}

	/**
	 * Saves part data to NBT tag
	 */
	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		// Create a new tag list
		NBTTagList nbtList = new NBTTagList();

		// Loop over the slots
		for( int slotId = 0; slotId < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE; slotId++ )
		{
			// Is the slot not null?
			if( this.slots[slotId] != null )
			{
				// Create the compound tag for this stack
				NBTTagCompound nbtCompound = new NBTTagCompound();

				// Set the slot ID
				nbtCompound.setByte( AEPartArcaneCraftingTerminal.SLOT_NBT_KEY, (byte)slotId );

				// Write the stack into the tag
				this.slots[slotId].writeToNBT( nbtCompound );

				// Append to the list
				nbtList.appendTag( nbtCompound );
			}
		}

		// Append the list to the data tag
		data.setTag( AEPartArcaneCraftingTerminal.INVENTORY_NBT_KEY, nbtList );
	}

}
