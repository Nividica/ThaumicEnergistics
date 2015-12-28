package thaumicenergistics.network.packet.server;

import thaumicenergistics.network.packet.ThEServerPacket;
import thaumicenergistics.network.packet.WrapperPacket;

public class WrapperPacket_S
	extends WrapperPacket
{
	public WrapperPacket_S()
	{

	}

	public WrapperPacket_S( final ThEServerPacket packet )
	{
		super( packet );
	}
}
