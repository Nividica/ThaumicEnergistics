package thaumicenergistics.integration.tc;

import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.TileVisNode;
import thaumicenergistics.parts.AEPartVisInterface;
import appeng.api.util.DimensionalCoord;

public class SubTileVisProvider
	extends TileVisNode
{
	/**
	 * How far vis can travel from the source. I think >.>
	 */
	private static final int VIS_RANGE = 1;

	/**
	 * The interface associated with this source.
	 */
	private AEPartVisInterface visInterface;

	/**
	 * Where in the world this provider is.
	 */
	private WorldCoordinates location;

	public SubTileVisProvider( final AEPartVisInterface parent )
	{
		// Set the interface
		this.visInterface = parent;

		// Get the location of the interface
		DimensionalCoord aeCoords = this.visInterface.getLocation();

		// Get the direction the interface is facing
		ForgeDirection face = this.visInterface.getCableSide();

		// Set the subtile's position to just infront of the interface.
		this.xCoord = aeCoords.x + face.offsetX;
		this.yCoord = aeCoords.y + face.offsetY;
		this.zCoord = aeCoords.z + face.offsetZ;
		this.worldObj = aeCoords.getWorld();
		this.location = new WorldCoordinates( this.xCoord, this.yCoord, this.zCoord, this.worldObj.provider.dimensionId );

	}

	/**
	 * Updates should come from the source interface.
	 */
	@Override
	public boolean canUpdate()
	{
		return true;
	}

	/**
	 * Attempts to consume vis from the interface.
	 */
	@Override
	public int consumeVis( final Aspect aspect, final int amountRequested )
	{
		// Ensure: This node is valid
		// The interface is valid
		// The interface is facing a relay
		if( this.isInvalid() || ( this.visInterface == null ) || ( !this.visInterface.isVisProvider() ) || ( this.visInterface.getRelay() == null ) )
		{
			return 0;
		}

		return this.visInterface.consumeVis( aspect, amountRequested );
	}

	/**
	 * Returns the location of the interface
	 */
	@Override
	public WorldCoordinates getLocation()
	{
		return this.location;
	}

	/**
	 * How far can vis travel from the interface?
	 */
	@Override
	public int getRange()
	{
		return SubTileVisProvider.VIS_RANGE;
	}

	/**
	 * Invalidates the provider.
	 */
	@Override
	public void invalidate()
	{
		this.removeThisNode();
	}

	/**
	 * If the interface is a source.
	 */
	@Override
	public boolean isSource()
	{
		return this.visInterface.isVisProvider();
	}

}
