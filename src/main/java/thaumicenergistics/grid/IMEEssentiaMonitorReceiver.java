package thaumicenergistics.grid;

import thaumicenergistics.aspect.AspectStack;


public interface IMEEssentiaMonitorReceiver
{
	boolean isValid( Object verificationToken );

	void postChange( Iterable<AspectStack> change );
}
