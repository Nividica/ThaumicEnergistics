package thaumicenergistics.tileentities;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.inventory.HandlerKnowledgeCore;
import thaumicenergistics.items.ItemKnowledgeCore;
import thaumicenergistics.util.PrivateInventory;

public class TileKnowledgeInscriber
	extends TileEntity
{
	public static final int KCORE_SLOT = 0;
	public static final int PATTERN_SLOT = KCORE_SLOT + 1;
	public static final int CRAFTING_MATRIX_SLOT = HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS + PATTERN_SLOT;
	public static final int CRAFTING_RESULT_SLOT = CRAFTING_MATRIX_SLOT + 9;

	private static final String NBTKEY_KCORE = "kcore";

	private PrivateInventory internalInventory = new PrivateInventory( "knowledge.inscriber", 32, 64 )
	{
		@Override
		public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
		{
			if( itemStack == null )
			{
				return true;
			}

			if( slotId == TileKnowledgeInscriber.KCORE_SLOT )
			{
				return( itemStack.getItem() instanceof ItemKnowledgeCore );
			}

			return true;
		}
	};

	public TileKnowledgeInscriber()
	{
	}

	/**
	 * Gets the inscriber's inventory.
	 * 
	 * @return
	 */
	public IInventory getInventory()
	{
		return this.internalInventory;
	}

	/**
	 * Returns true if there is a stored KCore.
	 * 
	 * @return
	 */
	public boolean hasKCore()
	{
		return( this.internalInventory.slots[TileKnowledgeInscriber.KCORE_SLOT] != null );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Is there a saved core?
		if( data.hasKey( TileKnowledgeInscriber.NBTKEY_KCORE ) )
		{
			// Load the saved core
			this.internalInventory.slots[TileKnowledgeInscriber.KCORE_SLOT] = ItemStack.loadItemStackFromNBT( data
							.getCompoundTag( TileKnowledgeInscriber.NBTKEY_KCORE ) );
		}
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Get the kcore
		ItemStack kCore = this.internalInventory.slots[TileKnowledgeInscriber.KCORE_SLOT];
		if( kCore != null )
		{
			// Write the kcore
			data.setTag( TileKnowledgeInscriber.NBTKEY_KCORE, kCore.writeToNBT( new NBTTagCompound() ) );
		}
	}
}
