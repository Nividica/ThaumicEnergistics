package thaumicenergistics.implementaion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.Blocks;
import thaumicenergistics.api.IConfig;
import thaumicenergistics.api.IEssentiaGas;
import thaumicenergistics.api.IInteractionHelper;
import thaumicenergistics.api.ITransportPermissions;
import thaumicenergistics.api.Items;
import thaumicenergistics.api.Parts;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.fluids.GaseousEssentia;
import com.google.common.collect.ImmutableList;

public class API
	extends ThEApi
{
	private final ThEBlocks blocks = new ThEBlocks();
	private final ThEItems items = new ThEItems();
	private final ThEParts parts = new ThEParts();
	private final List<IEssentiaGas> essentiaGases = new ArrayList<IEssentiaGas>();
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
	public Blocks blocks()
	{
		return this.blocks;
	}

	@Override
	public IConfig config()
	{
		return ThaumicEnergistics.config;
	}

	@Override
	public ImmutableList<List<IEssentiaGas>> essentiaGases()
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
	public IInteractionHelper interact()
	{
		return this.interactionHelper;
	}

	@Override
	public Items items()
	{
		return this.items;
	}

	@Override
	public Parts parts()
	{
		return this.parts;
	}

	@Override
	public ITransportPermissions transportPermissions()
	{
		return this.transportPermissions;
	}

}
