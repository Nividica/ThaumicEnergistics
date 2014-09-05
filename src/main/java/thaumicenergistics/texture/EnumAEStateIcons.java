package thaumicenergistics.texture;

import net.minecraft.util.ResourceLocation;

public enum EnumAEStateIcons
{
		REGULAR_BUTTON (240, 240),
		REDSTONE_LOW (0, 0),
		REDSTONE_HIGH (16, 0),
		REDSTONE_PULSE (32, 0),
		REDSTONE_IGNORE (48, 0),
		UPGRADE_SLOT (240, 208),
		CLEAR_GRID (96, 0),
		SORT_MODE_ALPHABETIC (0, 64),
		SORT_MODE_AMOUNT (16, 64),
		SORT_MODE_MOD (80, 64),
		SORT_MODE_INVTWEAK (64, 64),
		TAB_BUTTON (208, 0, 22, 22),
		SORT_DIR_ASC (0, 48),
		SORT_DIR_DEC (16, 48),
		WRENCH (32, 64),
		DISABLED( 0, 128 );

	/**
	 * Location of the AE states texture
	 */
	public static final ResourceLocation AE_STATES_TEXTURE = new ResourceLocation( "appliedenergistics2", "textures/guis/states.png" );

	/**
	 * Width and height of standard icons.
	 */
	private static final int STANDARD_ICON_SIZE = 16;

	/**
	 * X position in the texture this icon is located at.
	 */
	private int minU;

	/**
	 * Y position in the texture this icon is located at.
	 */
	private int minV;

	/**
	 * Width of the icon
	 */
	private int width;

	/**
	 * Height of the icon
	 */
	private int height;

	/**
	 * Standard icon constructor
	 * 
	 * @param u
	 * @param v
	 */
	private EnumAEStateIcons( int u, int v )
	{
		this( u, v, STANDARD_ICON_SIZE, STANDARD_ICON_SIZE );
	}

	/**
	 * Icon constructor
	 * 
	 * @param u
	 * @param v
	 * @param width
	 * @param height
	 */
	private EnumAEStateIcons( int u, int v, int width, int height )
	{
		// Set the u
		this.minU = u;

		// Set the v
		this.minV = v;

		// Set width
		this.width = width;

		// Set height
		this.height = height;

	}

	public int getHeight()
	{
		return this.height;
	}

	public int getU()
	{
		return this.minU;
	}

	public int getV()
	{
		return this.minV;
	}

	public int getWidth()
	{
		return this.width;
	}

}
