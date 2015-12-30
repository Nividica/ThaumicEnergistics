package thaumicenergistics.texture;

import net.minecraft.util.ResourceLocation;

public enum AEStateIconsEnum
	implements IStateIconTexture
{
		// Redstone modes
		REDSTONE_LOW (0, 0),
		REDSTONE_HIGH (16, 0),
		REDSTONE_PULSE (32, 0),
		REDSTONE_IGNORE (48, 0),

		// Slot backgrounds
		VIEW_CELL_BACKGROUND (224, 64),
		UPGRADE_CARD_BACKGROUND (240, 208),
		ME_CELL_BACKGROUND (240, 0),
		PATTERN_CELL_BACKGROUND (240, 128),

		// Buttons
		REGULAR_BUTTON (240, 240),
		TAB_BUTTON (208, 0, 22, 22),

		// Sorting modes
		SORT_MODE_ALPHABETIC (0, 64),
		SORT_MODE_AMOUNT (16, 64),
		SORT_MODE_MOD (80, 64),
		SORT_MODE_INVTWEAK (64, 64),

		// Sorting directions
		SORT_DIR_ASC (0, 48),
		SORT_DIR_DEC (16, 48),

		// View types
		VIEW_TYPE_STORED (0, 16),
		VIEW_TYPE_ALL (32, 16),
		VIEW_TYPE_CRAFT (48, 16),

		// Terminal styles
		TERM_STYLE_TALL (0, 208),
		TERM_STYLE_SMALL (16, 208),

		// Search modes
		SEARCH_MODE_AUTO (48, 32),
		SEARCH_MODE_MANUAL (64, 32),
		SEARCH_MODE_NEI_AUTO (80, 32),
		SEARCH_MODE_NEI_MANUAL (96, 32),

		// Misc
		WRENCH (32, 64),
		DISABLED (0, 128),
		ENABLED (16, 128),
		SAVE (0, 176),
		DELETE (0, 192),
		CLEAR_GRID (96, 0),
		ARROW_DOWN (128, 0);

	/**
	 * Location of the AE states texture
	 */
	public static final ResourceLocation AE_STATES_TEXTURE = new ResourceLocation( "appliedenergistics2", "textures/guis/states.png" );

	/**
	 * Width and height of standard icons.
	 */
	public static final int STANDARD_ICON_SIZE = 16;

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
	private AEStateIconsEnum( final int u, final int v )
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
	private AEStateIconsEnum( final int u, final int v, final int width, final int height )
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

	@Override
	public int getHeight()
	{
		return this.height;
	}

	@Override
	public ResourceLocation getTexture()
	{
		return AEStateIconsEnum.AE_STATES_TEXTURE;
	}

	@Override
	public int getU()
	{
		return this.minU;
	}

	@Override
	public int getV()
	{
		return this.minV;
	}

	@Override
	public int getWidth()
	{
		return this.width;
	}

}
