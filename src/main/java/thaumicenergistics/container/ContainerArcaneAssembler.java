package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumicenergistics.container.slot.SlotRestrictive;
import thaumicenergistics.tileentities.TileArcaneAssembler;

public class ContainerArcaneAssembler
	extends ContainerWithPlayerInventory
{

	/**
	 * Y position for the player inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 115;

	/**
	 * Y position for the hotbar inventory.
	 */
	private static final int HOTBAR_INV_POSITION_Y = PLAYER_INV_POSITION_Y + 58;

	/**
	 * Starting position of the pattern slots.
	 */
	private static final int PATTERN_SLOT_X = 26, PATTERN_SLOT_Y = 25;

	/**
	 * Number of rows and columns of the pattern slots.
	 */
	private static final int PATTERN_ROWS = 3, PATTERN_COLS = 7;

	/**
	 * Position of the kcore slot.
	 */
	private static final int KCORE_SLOT_X = 187, KCORE_SLOT_Y = 8;

	/**
	 * Reference to the arcane assembler
	 */
	private TileArcaneAssembler assembler;

	/**
	 * The player who owns this container
	 */
	private EntityPlayer player;

	public ContainerArcaneAssembler( final EntityPlayer player, final World world, final int X, final int Y, final int Z )
	{
		// Set the player
		this.player = player;

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, ContainerArcaneAssembler.PLAYER_INV_POSITION_Y, ContainerArcaneAssembler.HOTBAR_INV_POSITION_Y );

		// Get the assembler
		this.assembler = (TileArcaneAssembler)world.getTileEntity( X, Y, Z );

		// Get the assemblers inventory
		IInventory asmInv = this.assembler.getInternalInventory();

		// Create the slots
		for( int row = 0; row < ContainerArcaneAssembler.PATTERN_ROWS; row++ )
		{
			for( int col = 0; col < ContainerArcaneAssembler.PATTERN_COLS; col++ )
			{
				// Calculate the index
				int index = ( row * ContainerArcaneAssembler.PATTERN_COLS ) + col;

				// Create the slot
				SlotRestrictive slot = new SlotRestrictive( asmInv, index, ContainerArcaneAssembler.PATTERN_SLOT_X + ( 18 * col ),
								ContainerArcaneAssembler.PATTERN_SLOT_Y + ( 18 * row ) );

				// Add the slot
				this.addSlotToContainer( slot );
			}
		}

		// Add the kcore slot
		this.addSlotToContainer( new SlotRestrictive( asmInv, TileArcaneAssembler.KCORE_SLOT_INDEX, ContainerArcaneAssembler.KCORE_SLOT_X,
						ContainerArcaneAssembler.KCORE_SLOT_Y ) );

	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return true;
	}

	/**
	 * Called when the player shift+clicks on a slot
	 */
	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		return null;
	}

}
