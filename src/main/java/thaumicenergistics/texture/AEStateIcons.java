package thaumicenergistics.texture;

import net.minecraft.util.ResourceLocation;

public enum AEStateIcons
{
		BLANK_BUTTON (240, 240),
		REDSTONE_LOW (0, 0),
		REDSTONE_HIGH (16, 0),
		REDSTONE_PULSE (32, 0),
		REDSTONE_IGNORE (48, 0),
		UPGRADE_SLOT (240, 208),
		CLEAR_GRID (96, 0),
		SORT_ALPHABETIC (0, 64),
		SORT_AMOUNT (16, 64);

	/**
	 * Location of the AE states texture
	 */
	public static final ResourceLocation AE_STATES_TEXTURE = new ResourceLocation( "appliedenergistics2", "textures/guis/states.png" );

	/**
	 * Width and height of the icons
	 */
	public static final int ICON_SIZE = 16;

	/**
	 * X position in the texture this icon is located at.
	 */
	private int minU;

	/**
	 * Y position in the texture this icon is located at.
	 */
	private int minV;

	private AEStateIcons( int u, int v )
	{
		// Set the u
		this.minU = u;

		// Set the v
		this.minV = v;
	}

	public int getU()
	{
		return this.minU;
	}

	public int getV()
	{
		return this.minV;
	}

}
