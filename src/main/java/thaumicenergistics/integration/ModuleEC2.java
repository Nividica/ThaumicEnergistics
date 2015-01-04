package thaumicenergistics.integration;

import java.lang.reflect.Method;
import thaumicenergistics.fluids.GaseousEssentia;

public class ModuleEC2
{

	public ModuleEC2() throws Exception
	{
		// Get the API
		Class ECApi_Class = Class.forName( "extracells.api.ECApi" );

		// Get the 'instance' method
		Method ECApi_instance = ECApi_Class.getDeclaredMethod( "instance" );

		// Invoke the 'instance' method
		Object ECApi = ECApi_instance.invoke( null, new Object[0] );

		// Get the blacklist methods
		Method addFluidToShowBlacklist = ECApi.getClass().getDeclaredMethod( "addFluidToShowBlacklist", Class.class );
		Method addFluidToStorageBlacklist = ECApi.getClass().getDeclaredMethod( "addFluidToStorageBlacklist", Class.class );

		// Invoke the blacklist methods
		addFluidToShowBlacklist.invoke( ECApi, GaseousEssentia.class );
		addFluidToStorageBlacklist.invoke( ECApi, GaseousEssentia.class );

		// TODO: Make this a config option
	}

}
