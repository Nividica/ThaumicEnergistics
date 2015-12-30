package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.items.ItemCraftingAspect;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.util.DistillationPatternHelper;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.AEApi;
import com.google.common.base.Optional;

public class ContainerDistillationEncoder
	extends ContainerWithPlayerInventory
{
	/**
	 * Y position for the player and hotbar inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 152, HOTBAR_INV_POSITION_Y = PLAYER_INV_POSITION_Y + 58;

	/**
	 * Player who opened the GUI.
	 */
	protected EntityPlayer player;

	/**
	 * Constructor.
	 * 
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public ContainerDistillationEncoder( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Set the player
		this.player = player;

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, PLAYER_INV_POSITION_Y, HOTBAR_INV_POSITION_Y );
	}

	/**
	 * Can interact with any real player.
	 */
	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return !( player instanceof FakePlayer );
	}

	@Override
	public ItemStack slotClick( final int slotID, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		// Is this client side?
		if( EffectiveSide.isClientSide() )
		{
			// Do nothing.
			return null;
		}

		// TODO: Debug code
		// Create a new pattern
		Optional<ItemStack> oStack = AEApi.instance().definitions().items().encodedPattern().maybeStack( 1 );
		if( oStack.isPresent() )
		{
			DistillationPatternHelper h = new DistillationPatternHelper();
			h.input = new ItemStack( Item.getItemById( 331 ) );

			h.output = ItemEnum.CRAFTING_ASPECT.getStack( 2 );
			ItemCraftingAspect.setAspect( h.output, Aspect.ENERGY );

			ItemStack pattern = oStack.get();
			h.writeToPattern( pattern );

			this.player.inventory.addItemStackToInventory( pattern );
			this.player.inventory.markDirty();
			this.detectAndSendChanges();
		}

		return null;
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		return null;
	}

}
