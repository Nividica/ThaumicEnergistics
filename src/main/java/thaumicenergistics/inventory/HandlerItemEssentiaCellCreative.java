package thaumicenergistics.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

public class HandlerItemEssentiaCellCreative
	extends HandlerItemEssentiaCell
{

	public HandlerItemEssentiaCellCreative( final ItemStack storageStack, final ISaveProvider saveProvider )
	{
		super( storageStack, saveProvider );

		this.totalBytes = 0;
		this.totalEssentiaStorage = 0;
	}

	/**
	 * The creative cell can not store new essentia.
	 */
	@Override
	public boolean canAccept( final IAEFluidStack input )
	{
		return false;
	}

	/**
	 * The creative cell can only provide essentia based on its parition table.
	 */
	@Override
	public IAEFluidStack extractItems( final IAEFluidStack request, final Actionable mode, final BaseActionSource src )
	{
		// Ensure there is a request, and that it is an essentia gas
		if( ( request != null ) && ( request.getFluid() != null ) && ( request.getFluid() instanceof GaseousEssentia ) )
		{
			// Get the aspect of the essentia
			Aspect requestAspect = ( (GaseousEssentia)request.getFluid() ).getAspect();

			// Is the cell partitioned for this aspect?
			if( ( requestAspect != null ) && ( this.partitionAspects.contains( requestAspect ) ) )
			{
				return request.copy();
			}
		}

		return null;
	}

	/**
	 * Available items based on partition table.
	 */
	@Override
	public IItemList<IAEFluidStack> getAvailableItems( final IItemList<IAEFluidStack> availableList )
	{
		for( Aspect aspect : this.partitionAspects )
		{
			// Get the gas
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( aspect );

			// Create the AE fluid stack
			availableList.add( EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( essentiaGas, 2000000000 ) );

		}

		return availableList;
	}

	/**
	 * No storage
	 */
	@Override
	public long getFreeBytes()
	{
		return 0;
	}

	/**
	 * 'Stored' essentia based on partition table.
	 */
	@Override
	public List<IAspectStack> getStoredEssentia()
	{
		// Make the list
		List<IAspectStack> storedList = new ArrayList<IAspectStack>( this.partitionAspects.size() );

		for( Aspect aspect : this.partitionAspects )
		{
			storedList.add( new AspectStack( aspect, 1 ) );
		}

		return storedList;
	}

	/**
	 * No storage
	 */
	@Override
	public long getUsedBytes()
	{
		return 0;
	}

	/**
	 * Used types based on partition table.
	 */
	@Override
	public int getUsedTypes()
	{
		return this.partitionAspects.size();
	}

	/**
	 * Creative cell can not inject.
	 */
	@Override
	public IAEFluidStack injectItems( final IAEFluidStack input, final Actionable mode, final BaseActionSource src )
	{
		// Ensure there is an input.
		if( ( input == null ) )
		{
			// No input
			return null;
		}

		// Can not inject items.
		return input.copy();
	}

	/**
	 * This is a creative cell.
	 */
	@Override
	public boolean isCreative()
	{
		return true;
	}

	/**
	 * Creative cell is always partitioned.
	 */
	@Override
	public boolean isPartitioned()
	{
		return true;
	}

	/**
	 * Creative cell can not inject.
	 */
	@Override
	public boolean isPrioritized( final IAEFluidStack input )
	{
		return false;
	}

	/**
	 * Meaniningless on creative cell.
	 */
	@Override
	public void partitionToCellContents()
	{
		// Ignored
	}

	/**
	 * Creative cell can not inject.
	 */
	@Override
	public boolean validForPass( final int pass )
	{
		return false;
	}

}
