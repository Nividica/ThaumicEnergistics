package thaumicenergistics.api;

import java.util.ArrayList;
import java.util.List;
import thaumicenergistics.api.interfaces.IEssentiaGas;
import thaumicenergistics.api.registry.TEBlocks;
import thaumicenergistics.api.registry.TEConfig;
import thaumicenergistics.api.registry.TEItems;
import thaumicenergistics.api.registry.TEParts;
import thaumicenergistics.api.registry.TEPermissionsItem;
import thaumicenergistics.api.registry.TEPermissionsTile;

public class TEApi
{
	public static final TEApi instance = new TEApi();

	public final TEBlocks blocks = new TEBlocks();
	public final TEItems items = new TEItems();
	public final TEParts parts = new TEParts();
	public final List<IEssentiaGas> essentiaGases = new ArrayList<IEssentiaGas>();
	public final TEConfig config = new TEConfig();
	public final TEPermissionsTile tileIOPermissions = new TEPermissionsTile();
	public final TEPermissionsItem itemIOPermissions = new TEPermissionsItem();

	/**
	 * Private constructor
	 */
	private TEApi()
	{

	}

}
