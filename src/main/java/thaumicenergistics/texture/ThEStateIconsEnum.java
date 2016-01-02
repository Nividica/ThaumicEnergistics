package thaumicenergistics.texture;

import net.minecraft.util.ResourceLocation;
import thaumicenergistics.common.ThaumicEnergistics;

public enum ThEStateIconsEnum
	implements IStateIconTexture
{
	SWAP (0, 0);

	/**
	 * Location of the AE states texture
	 */
	public static final ResourceLocation THE_STATES_TEXTURE = new ResourceLocation( ThaumicEnergistics.MOD_ID, "textures/gui/states.png" );

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
	private ThEStateIconsEnum( final int u, final int v )
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
	private ThEStateIconsEnum( final int u, final int v, final int width, final int height )
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
		return ThEStateIconsEnum.THE_STATES_TEXTURE;
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
