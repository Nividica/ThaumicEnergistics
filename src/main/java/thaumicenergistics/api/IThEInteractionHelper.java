package thaumicenergistics.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.storage.IAspectStack;

/**
 * Contains miscellaneous functionality intended to help other moders interact with ThE.
 *
 * @author Nividica
 *
 */
public interface IThEInteractionHelper
{
	/**
	 * Converts an amount of milibuckets to an amount of Essentia.
	 *
	 * @return
	 */
	long convertEssentiaAmountToFluidAmount( long essentiaAmount );

	/**
	 * Converts an amount of Essentia to an amount of milibuckets.
	 *
	 * @return
	 */
	long convertFluidAmountToEssentiaAmount( long milibuckets );

	/**
	 * Creates a new instance of an aspect stack.
	 *
	 * @param aspect
	 * @param stackSize
	 * @return
	 */
	@Nonnull
	IAspectStack createAspectStack( @Nullable Aspect aspect, long stackSize );

	/**
	 * Returns the Arcane Crafting Terminals GUI class.
	 */
	@SideOnly(Side.CLIENT)
	Class getArcaneCraftingTerminalGUIClass();

	/**
	 * Opens the wireless gui for the specified player.
	 * The item the player is holding is used for the settings and power.
	 * Must be called from the server side.
	 *
	 * @param player
	 */
	void openWirelessTerminalGui( @Nonnull EntityPlayer player );

	/**
	 * The {@code IThEWirelessEssentiaTerminal} is assumed to be the item the player is holding, and is no longer needs to be passed in.
	 *
	 *
	 * @param player
	 * @param terminalInterface
	 * is now ignored
	 * @see #openWirelessTerminalGui(EntityPlayer)
	 */
	@Deprecated
	void openWirelessTerminalGui( @Nonnull EntityPlayer player, IThEWirelessEssentiaTerminal terminalInterface );

	/**
	 * Registers a handler to receive golem events.
	 *
	 * @param handler
	 */
	void registerGolemHookHandler( @Nonnull IGolemHookHandler handler );

	/**
	 * Attempts to set the Arcane Crafting Terminals recipe to the items
	 * specified for the current player.<br>
	 * Call is ignored if player does not have an A.C.T. GUI open.<br>
	 * The items array should be of size 9. Items will be placed in the crafting
	 * grid according to index where
	 * <ul>
	 * <li>0 = Top-Left</li>
	 * <li>1 = Top-Middle</li>
	 * <li>2 = Top-Right</li>
	 * <li>etc</li>
	 * </ul>
	 * Null items are allowed.
	 */
	@SideOnly(Side.CLIENT)
	void setArcaneCraftingTerminalRecipe( @Nonnull ItemStack[] items );
}
