package thaumicenergistics.api;

import javax.annotation.Nonnull;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Provides the required functionality of a wireless terminal.
 * Presumably this interface would be implemented on an Item, but that is
 * not a requirement.
 *
 * @author Nividica
 *
 */
public interface IThEWirelessEssentiaTerminal
	extends INetworkEncodable, IAEItemPowerStorage
{
	/**
	 * Gets the tag used to store the terminal data.
	 *
	 * @param terminalItemstack
	 * @return
	 */
	@Nonnull
	NBTTagCompound getWETerminalTag( @Nonnull ItemStack terminalItemstack );
}
