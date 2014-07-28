package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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

public class AEPartArcaneCraftingTerminal
	extends AEPartBase
	implements IInventory
{
	private static final int MY_INVENTORY_SIZE = 11;

	public static final int WAND_SLOT_INDEX = 9;

	public static final int RESULT_SLOT_INDEX = 10;

	private static final String INVENTORY_NBT_KEY = "TEACT_Inventory";

	private static final String SLOT_NBT_KEY = "Slot#";

	private final ItemStack[] slots = new ItemStack[AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE];

	private List<ContainerPartArcaneCraftingTerminal> listeners = new ArrayList<ContainerPartArcaneCraftingTerminal>();

	public AEPartArcaneCraftingTerminal()
	{
		// Call super
		super( AEPartsEnum.ArcaneCraftingTerminal );
	}

	public void registerListener( ContainerPartArcaneCraftingTerminal container )
	{
		// Is this already registered?
		if( !this.listeners.contains( container ) )
		{
			// Add to the list
			this.listeners.add( container );
		}
		
	}

	public void removeListener( ContainerPartArcaneCraftingTerminal container )
	{
		// Remove the container from the listeners
		this.listeners.remove( container );
	}

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

	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiArcaneCraftingTerminal( this, player );
	}

	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartArcaneCraftingTerminal( this, player );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	@Override
	public void getBoxes( IPartCollsionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	@Override
	public void renderInventory( IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
		ts.setColorOpaque_I( AEPartBase.inventoryOverlayColor );
		helper.renderInventoryFace( BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		ts.setColorOpaque_I( AEColor.Black.mediumVariant );
		helper.renderInventoryFace( BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	@Override
	public void renderStatic( int x, int y, int z, IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		if( this.isActive() )
		{

			helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
			ts.setColorOpaque_I( this.host.getColor().blackVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

			ts.setColorOpaque_I( this.host.getColor().mediumVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );
		}

		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

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

	@Override
	public void removeFromWorld()
	{

		// Is this server side?
		if( !this.hostTile.getWorldObj().isRemote )
		{
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
					// Drop it on the ground
					this.dropInventoryItemOnGround( slotStack );
				}
			}
		}

		// Pass to super
		super.removeFromWorld();
	}

	private boolean isSlotInRange( int slotIndex )
	{
		// Is the slot in range?
		return( ( slotIndex >= 0 ) && ( slotIndex < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE ) );
	}

	@Override
	public int getSizeInventory()
	{
		return AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE;
	}

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

	@Override
	public void setInventorySlotContents( int slotIndex, ItemStack slotStack )
	{	
		if( this.setInventorySlotContentsWithoutNotify( slotIndex, slotStack ) )
		{
			// Inform the listeners
			this.notifyListeners( slotIndex );
		}
	}

	@Override
	public String getInventoryName()
	{
		return ThaumicEnergistics.MOD_ID + ".arcane.crafting.terminal.inventory";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{
		// Mark the host tile
		this.hostTile.markDirty();
	}

	@Override
	public boolean isUseableByPlayer( EntityPlayer player )
	{
		return true;
	}

	@Override
	public void openInventory()
	{
		// Ignored
	}

	@Override
	public void closeInventory()
	{
		// Ignored
	}

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
				if( proposedStack.getItem() instanceof ItemWandCasting )
				{
					// Valid wand
					return true;
				}

				// Invalid wand
				return false;
			}

			// Unrestricted slot
			return true;
		}

		// Out of range
		return false;
	}

	public World getWorldObj()
	{
		return this.hostTile.getWorldObj();
	}

}
