package thaumicenergistics.fluids;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import cpw.mods.fml.common.FMLLog;

public class GaseousEssentia extends Fluid
{
	public static LinkedHashMap<Aspect, GaseousEssentia> gasList = new LinkedHashMap<Aspect, GaseousEssentia>();

	private static void create( Aspect aspect )
	{
		// Ensure this has not already been register
		if ( gasList.containsKey( aspect ) )
		{
			// Return the existing fluid
			return;
		}

		// Create the name
		String gasName = "gaseous" + aspect.getTag() + "essentia";

		// Create the fluid
		GaseousEssentia newGas = new GaseousEssentia( gasName, aspect );

		// Register the fluid
		if ( FluidRegistry.registerFluid( newGas ) )
		{
			// Add to the list
			gasList.put( aspect, newGas );
		}
		else
		{
			FMLLog.warning( "", ThaumicEnergistics.MODID + ": Unable to register '" + aspect.getTag() + "' as fluid." );
		}

	}

	public static GaseousEssentia getGasFromAspect( Aspect aspect )
	{
		return GaseousEssentia.gasList.get( aspect );
	}

	public static void registerGases()
	{
		// Create a gas for each essentia type
		for( Entry<String, Aspect> aspectType : Aspect.aspects.entrySet() )
		{
			// Get the aspect
			Aspect aspect = aspectType.getValue();

			// Create and register
			GaseousEssentia.create( aspect );
		}

	}

	private Aspect associatedAspect;

	private GaseousEssentia(String gasName, Aspect aspect)
	{
		super( gasName );

		this.associatedAspect = aspect;

		this.setLuminosity( 7 );

		this.setDensity( -4 );

		this.setViscosity( 3000 );

		this.setGaseous( true );

	}

	public Aspect getAssociatedAspect()
	{
		return this.associatedAspect;
	}

	@Override
	public int getColor()
	{
		if ( this.associatedAspect != null )
		{
			return this.associatedAspect.getColor();
		}

		return super.getColor();
	}

	@Override
	public String getLocalizedName()
	{
		return StatCollector.translateToLocal( "thaumicenergistics.fluid.gaseous" ) + " " + this.associatedAspect.getName();
	}

}
