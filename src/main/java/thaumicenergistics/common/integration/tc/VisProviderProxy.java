package thaumicenergistics.common.integration.tc;

import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.TileVisNode;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.tiles.TileVisRelay;
import thaumicenergistics.common.parts.PartVisInterface;
import appeng.api.util.DimensionalCoord;

public class VisProviderProxy
	extends TileVisNode
{
	/**
	 * How far vis can travel from the source. I think >.>
	 * TODO: Test vis range
	 */
	private static final int VIS_RANGE = 1;

	/**
	 * The interface associated with this source.
	 */
	private PartVisInterface visInterface;

	/**
	 * Where in the world this provider is.
	 */
	private WorldCoordinates location;

	/**
	 * What vis 'channel' does this node respond on?
	 */
	private byte attunement = -1;

	public VisProviderProxy( final PartVisInterface parent )
	{
		// Set the interface
		this.visInterface = parent;

		// Get the location of the interface
		DimensionalCoord aeCoords = this.visInterface.getLocation();

		// Get the direction the interface is facing
		ForgeDirection face = this.visInterface.getSide();

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
	 * Gets the attunement for this source.
	 */
	@Override
	public byte getAttunement()
	{
		return this.attunement;
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
		return VisProviderProxy.VIS_RANGE;
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

	@Override
	public void updateEntity()
	{
		byte relayAttunement = -1;

		// Is there an interface?
		if( this.visInterface != null )
		{
			// Get the relay
			TileVisRelay relay = this.visInterface.getRelay();

			// Is there a relay?
			if( relay != null )
			{
				// Get it's attunement
				relayAttunement = relay.getAttunement();
			}
		}

		// Has the attunement changed?
		if( this.attunement != relayAttunement )
		{
			// Update our attunement
			this.attunement = relayAttunement;

			// Remove the node
			this.removeThisNode();

			// Mark for refresh
			this.nodeRefresh = true;

			// Re-register the node
			VisNetHandler.addSource( this.worldObj, this );
		}

		// Call super
		super.updateEntity();
	}

}
