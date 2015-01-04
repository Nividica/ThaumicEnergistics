package thaumicenergistics.tileentities;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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

	public IInventory getInventory()
	{
		return this.internalInventory;
	}
}
