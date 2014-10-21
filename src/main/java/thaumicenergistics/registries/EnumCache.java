package thaumicenergistics.registries;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.util.AEColor;

/**
 * Cached value arrays of commonly used third party enums
 * 
 * @author Nividica
 * 
 */
public class EnumCache
{
	/**
	 * AEColor
	 */
	public static final AEColor[] AE_COLOR = AEColor.values();

	/**
	 * SortDir
	 */
	public static final SortDir[] AE_SORT_DIRECTIONS = SortDir.values();

	/**
	 * SortOrder
	 */
	public static final SortOrder[] AE_SORT_ORDERS = SortOrder.values();

	/**
	 * RedstoneMode
	 */
	public static final RedstoneMode[] AE_REDSTONE_MODES = RedstoneMode.values();
}
