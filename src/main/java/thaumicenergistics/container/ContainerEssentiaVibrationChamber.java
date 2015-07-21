package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.tileentities.TileEssentiaVibrationChamber;

public class ContainerEssentiaVibrationChamber
	extends Container
{

	/**
	 * The essentia vibration chamber.
	 */
	private final TileEssentiaVibrationChamber chamber;

	public ContainerEssentiaVibrationChamber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Get the tile entity
		TileEntity te = world.getTileEntity( x, y, z );

		// Ensure it is an E.V.C
		if( te instanceof TileEssentiaVibrationChamber )
		{
			// Set the chamber
			this.chamber = (TileEssentiaVibrationChamber)te;
		}
		else
		{
			// Invalid tile entity
			this.chamber = null;
		}
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return !( player instanceof FakePlayer );
	}

	public int getPowerLevel()
	{
		return 5;
	}

	public Aspect getProcessingAspect()
	{
		return Aspect.FIRE;
	}

	public float getRemainingPercent()
	{
		return 0.5F;
	}

	public AspectStack getStoredEssentia()
	{
		return null;
	}

}
