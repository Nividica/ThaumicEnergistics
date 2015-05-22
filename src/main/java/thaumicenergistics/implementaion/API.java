package thaumicenergistics.implementaion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IThEBlocks;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.IThEEssentiaGas;
import thaumicenergistics.api.IThEInteractionHelper;
import thaumicenergistics.api.IThETransportPermissions;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.IThEParts;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.fluids.GaseousEssentia;
import com.google.common.collect.ImmutableList;

public class API
	extends ThEApi
{
	private final ThEBlocks blocks = new ThEBlocks();
	private final ThEItems items = new ThEItems();
	private final ThEParts parts = new ThEParts();
	private final List<IThEEssentiaGas> essentiaGases = new ArrayList<IThEEssentiaGas>();
	private final ThETransportPermissions transportPermissions = new ThETransportPermissions();
	private final ThEInteractionHelper interactionHelper = new ThEInteractionHelper();

	/**
	 * Create the API instance.
	 */
	public static final API instance = new API();

	/**
	 * Private constructor
	 */
	private API()
	{
	}

	@Override
	public IThEBlocks blocks()
	{
		return this.blocks;
	}

	@Override
	public IThEConfig config()
	{
		return ThaumicEnergistics.config;
	}

	@Override
	public ImmutableList<List<IThEEssentiaGas>> essentiaGases()
	{
		// Do we need to update?
		if( this.essentiaGases.size() != GaseousEssentia.gasList.size() )
		{
			// Clear the list
			this.essentiaGases.clear();

			// Get the iterator
			Iterator<Entry<Aspect, GaseousEssentia>> iterator = GaseousEssentia.gasList.entrySet().iterator();

			// Add all gasses
			while( iterator.hasNext() )
			{
				this.essentiaGases.add( iterator.next().getValue() );
			}
		}

		return ImmutableList.of( this.essentiaGases );
	}

	@Override
	public IThEInteractionHelper interact()
	{
		return this.interactionHelper;
	}

	@Override
	public IThEItems items()
	{
		return this.items;
	}

	@Override
	public IThEParts parts()
	{
		return this.parts;
	}

	@Override
	public IThETransportPermissions transportPermissions()
	{
		return this.transportPermissions;
	}

}
