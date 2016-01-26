package thaumicenergistics.common.network.packet.client;

import thaumicenergistics.common.network.WrapperPacket;

/**
 * Client packet wrapper.
 * 
 * @author Nividica
 * 
 */
public class WrapperPacket_C
	extends WrapperPacket
{
	public WrapperPacket_C()
	{

	}

	public WrapperPacket_C( final ThEClientPacket packet )
	{
		super( packet );
	}
}
