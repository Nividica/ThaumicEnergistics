package thaumicenergistics.aspect;


public interface IMEEssentiaMonitorReceiver
{
	boolean isValid( Object verificationToken );

	void postChange( Iterable<AspectStack> change );
}
