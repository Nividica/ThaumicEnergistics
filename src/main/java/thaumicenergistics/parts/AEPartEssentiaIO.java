package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Arrays;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaIOBus;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.gui.GuiEssentiatIO;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.PacketAspectSlot;
import thaumicenergistics.network.packet.PacketEssentiaIOBus;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.util.EssentiaTileContainerHelper;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.RedstoneMode;
import appeng.api.definitions.Materials;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.data.IAEFluidStack;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AEPartEssentiaIO extends AEPartBase implements IGridTickable, IInventoryUpdateReceiver, IAspectSlotPart, IAEAppEngInventory
{
	// Constants
	private final static byte baseTransferPerSecond = 4;
	private final static byte additionalTransferPerUpgrade = 7;
	private final static byte minimumTicksPerOpertion = 10;
	private final static byte maximumTicksPerOpertion = 40;
	private final static byte maximumTransferPerSecond = 64;
	private final static byte minimumTransferPerSecond = 1;

	protected Aspect[] filterAspects = new Aspect[9];
	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
	protected byte filterSize;
	protected byte upgradeSpeedCount = 0;
	protected boolean redstoneControlled;
	private boolean lastRedstone;

	private UpgradeInventory upgradeInventory = new UpgradeInventory( this.associatedItem, this, 4 );

	public AEPartEssentiaIO(AEPartsEnum associatedPart)
	{
		super( associatedPart );
	}

	private int getTransferAmountPerSecond()
	{
		return baseTransferPerSecond + ( this.upgradeSpeedCount * additionalTransferPerUpgrade );
	}

	private boolean canDoWork()
	{
		boolean canWork = true;

		if ( this.redstoneControlled )
		{
			switch ( this.getRedstoneMode() )
			{
				case HIGH_SIGNAL:
					canWork = this.redstonePowered;

					break;
				case IGNORE:
					break;

				case LOW_SIGNAL:
					canWork = !this.redstonePowered;

					break;
				case SIGNAL_PULSE:
					canWork = false;
					break;
			}
		}

		return canWork;

	}

	protected boolean extractEssentiaFromNetwork( int amountToFill )
	{

		for( Aspect aspect : this.filterAspects )
		{
			if ( aspect != null && ( this.aspectTransferAllowed( aspect ) ) )
			{
				GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( aspect );

				IAEFluidStack gasStack = this.extractFluid( EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, amountToFill ),
					Actionable.SIMULATE );

				if ( gasStack != null )
				{
					// Fill the container
					int filledAmount = (int) EssentiaTileContainerHelper.fillContainer( this.facingContainer, gasStack, true );

					// Take from the network
					this.extractFluid( EssentiaConversionHelper.createAEFluidStackInFluidUnits( essentiaGas, filledAmount ), Actionable.MODULATE );

					// Done
					return true;
				}
			}
		}

		return false;

	}

	protected boolean injectEssentaToNetwork( int amountToDrain )
	{
		Aspect aspectToDrain = this.facingContainer.getAspects().getAspects()[0];

		if ( ( aspectToDrain == null ) || ( !this.aspectTransferAllowed( aspectToDrain ) ) )
		{
			return false;
		}

		// Simulate a drain from the container
		FluidStack drained = EssentiaTileContainerHelper.drainContainer( this.facingContainer, amountToDrain, aspectToDrain, false );

		// Was any drained?
		if ( drained == null )
		{
			return false;
		}

		// Inject into the network
		IAEFluidStack toFill = AEApi.instance().storage().createFluidStack( drained );
		IAEFluidStack notInjected = this.injectFluid( toFill, Actionable.MODULATE );

		// Was any not injected?
		if ( notInjected != null )
		{
			// Calculate how much was injected into the network
			int amountInjected = (int) ( toFill.getStackSize() - notInjected.getStackSize() );

			// None could be injected
			if ( amountInjected == 0 )
			{
				return false;
			}

			// Convert from fluid units to essentia units
			amountInjected = (int) EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( amountInjected );

			// Some was unable to be injected, only take what was injected from
			// container
			EssentiaTileContainerHelper.drainContainer( this.facingContainer, amountInjected, aspectToDrain, true );
			return true;
		}

		// All was injected, take the full drain request amount from the
		// container
		EssentiaTileContainerHelper.drainContainer( this.facingContainer, amountToDrain, aspectToDrain, true );

		return true;
	}

	public abstract boolean aspectTransferAllowed( Aspect aspect );

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	public abstract boolean doWork( int transferAmount );

	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiEssentiatIO( this, player );
	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	public RedstoneMode getRedstoneMode()
	{
		return this.redstoneMode;
	}

	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartEssentiaIOBus( this, player );
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode arg0 )
	{
		return new TickingRequest( minimumTicksPerOpertion, maximumTicksPerOpertion, false, false );
	}

	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	public void loopRedstoneMode( EntityPlayer player )
	{
		if ( ( this.redstoneMode.ordinal() + 1 ) < RedstoneMode.values().length )
		{
			this.redstoneMode = RedstoneMode.values()[this.redstoneMode.ordinal() + 1];
		}
		else
		{
			this.redstoneMode = RedstoneMode.values()[0];
		}
		new PacketEssentiaIOBus( this.redstoneControlled ).sendPacketToPlayer( player );
	}

	@Override
	public boolean onActivate( EntityPlayer player, Vec3 position )
	{
		boolean activated = super.onActivate( player, position );

		this.onInventoryChanged();

		return activated;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		if ( inv == this.upgradeInventory )
		{
			this.onInventoryChanged();
		}
	}

	@Override
	public void onInventoryChanged()
	{
		this.filterSize = 0;
		this.redstoneControlled = false;
		this.upgradeSpeedCount = 0;

		Materials aeMaterals = AEApi.instance().materials();

		for( int i = 0; i < this.upgradeInventory.getSizeInventory(); i++ )
		{
			ItemStack slotStack = this.upgradeInventory.getStackInSlot( i );

			if ( slotStack != null )
			{
				if ( aeMaterals.materialCardCapacity.sameAs( slotStack ) )
				{
					this.filterSize++ ;
				}
				else if ( aeMaterals.materialCardRedstone.sameAs( slotStack ) )
				{
					this.redstoneControlled = true;
				}
				else if ( aeMaterals.materialCardSpeed.sameAs( slotStack ) )
				{
					this.upgradeSpeedCount++ ;
				}
			}
		}

		if ( this.filterSize < 2 )
		{
			this.filterAspects[0] = null;
			this.filterAspects[2] = null;
			this.filterAspects[6] = null;
			this.filterAspects[8] = null;

			if ( this.filterSize < 1 )
			{
				this.filterAspects[1] = null;
				this.filterAspects[3] = null;
				this.filterAspects[5] = null;
				this.filterAspects[7] = null;
			}
		}

		try
		{
			if ( this.host.getLocation().getWorld().isRemote )
			{
				return;
			}
		}
		catch( Throwable ignored )
		{
		}

		new PacketEssentiaIOBus( this.filterSize ).sendPacketToAllPlayers();
		new PacketEssentiaIOBus( this.redstoneControlled ).sendPacketToAllPlayers();
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		
		if ( this.redstonePowered )
		{
			if ( !this.lastRedstone )
			{
				/*
				 * NOTE: Known Issue: More than 1 redstone pulse per second will cause this to
				 * operate too fast.
				 */
				this.doWork( this.getTransferAmountPerSecond() );
			}
		}

		this.lastRedstone = this.redstonePowered;
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );

		// Read redstone mode
		this.redstoneMode = RedstoneMode.values()[data.getInteger( "redstoneMode" )];

		for( int i = 0; i < this.filterAspects.length; i++ )
		{
			String aspectTag = data.getString( "AspectFilter#" + i );

			if ( !aspectTag.equals( "" ) )
			{
				this.filterAspects[i] = Aspect.aspects.get( aspectTag );
			}
		}

		this.upgradeInventory.readFromNBT( data, "upgradeInventory" );

		this.onInventoryChanged();
	}

	@Override
	public final boolean readFromStream( ByteBuf stream ) throws IOException
	{
		return super.readFromStream( stream );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public final void renderDynamic( double x, double y, double z, IPartRenderHelper helper, RenderBlocks renderer )
	{
	}

	@Override
	public void saveChanges()
	{
		this.host.markForSave();
	}

	public void sendInformation( EntityPlayer player )
	{
		new PacketAspectSlot( Arrays.asList( this.filterAspects ) ).sendPacketToPlayer( player );

		new PacketEssentiaIOBus( this.redstoneMode ).sendPacketToPlayer( player );

		new PacketEssentiaIOBus( this.filterSize ).sendPacketToPlayer( player );
	}

	@Override
	public final void setAspect( int index, Aspect aspect, EntityPlayer player )
	{
		this.filterAspects[index] = aspect;

		new PacketAspectSlot( Arrays.asList( this.filterAspects ) ).sendPacketToPlayer( player );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		if ( this.canDoWork() )
		{
			// Calculate the amount to transfer per second
			int transferAmountPerSecond = this.getTransferAmountPerSecond();

			// Calculate amount to transfer this operation
			int transferAmount = (int) ( transferAmountPerSecond * ( ticksSinceLastCall / 20.F ) );

			// Clamp
			if ( transferAmount < minimumTransferPerSecond )
			{
				transferAmount = minimumTransferPerSecond;
			}
			else if ( transferAmount > maximumTransferPerSecond )
			{
				transferAmount = maximumTransferPerSecond;
			}

			if ( this.doWork( transferAmount ) )
			{
				return TickRateModulation.URGENT;
			}
		}

		return TickRateModulation.IDLE;
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );

		// Write the redstone mode
		data.setInteger( "redstoneMode", this.redstoneMode.ordinal() );

		for( int i = 0; i < this.filterAspects.length; i++ )
		{
			Aspect aspect = this.filterAspects[i];
			String aspectTag = "";

			if ( aspect != null )
			{
				aspectTag = aspect.getTag();
			}

			data.setString( "AspectFilter#" + i, aspectTag );
		}

		this.upgradeInventory.writeToNBT( data, "upgradeInventory" );
	}

	@Override
	public final void writeToStream( ByteBuf stream ) throws IOException
	{
		super.writeToStream( stream );
	}

}
