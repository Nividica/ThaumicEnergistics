package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerCellTerminalBase;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.gui.GuiEssentiaTerminal;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaTerminal
	extends AEPartBase
{
	private List<ContainerEssentiaTerminal> containers = new ArrayList<ContainerEssentiaTerminal>();

	private MachineSource machineSource = new MachineSource( this );
	
	/**
	 * Tracks if the inventory has been locked for work.
	 */
	private boolean inventoryLocked = false;

	private PrivateInventory inventory = new PrivateInventory( ThaumicEnergistics.MOD_ID + ".part.aspect.terminal", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( int slotId, ItemStack itemStack )
		{
			return EssentiaItemContainerHelper.isContainer( itemStack );
		}
	};

	public AEPartEssentiaTerminal()
	{
		super( AEPartsEnum.EssentiaTerminal );
	}
	
	public MachineSource getTerminalMachineSource()
	{
		return this.machineSource;
	}

	public void addContainer( ContainerCellTerminalBase container )
	{
		if ( container instanceof ContainerEssentiaTerminal )
		{
			this.containers.add( (ContainerEssentiaTerminal) container );
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	@Override
	public void getBoxes( IPartCollsionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 14.0D );

		helper.addBox( 5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 13.0D );
	}

	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiEssentiaTerminal( this, player );
	}

	public PrivateInventory getInventory()
	{
		return this.inventory;
	}

	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerEssentiaTerminal( this, player );
	}

	@Override
	public boolean onActivate( EntityPlayer player, Vec3 position )
	{
		if ( this.isActive() )
		{
			return super.onActivate( player, position );
		}

		return false;
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.inventory.readFromNBT( data.getTagList( "inventory", 10 ) );
	}

	public void removeContainer( ContainerEssentiaTerminal containerAspectTerminal )
	{
		this.containers.remove( containerAspectTerminal );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();

		helper.setTexture( side );
		helper.setBounds( 4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F );
		helper.renderInventoryBox( renderer );

		helper.setTexture( side, side, side, BlockTextureManager.BUS_BORDER.getTexture(), side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		ts.setBrightness( 0xD000D0 );

		helper.setInvColor( 0xFFFFFF );

		helper.renderInventoryFace( BlockTextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer );

		helper.setBounds( 3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16.0F );

		helper.setInvColor( AEColor.Transparent.blackVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		helper.setInvColor( AEColor.Transparent.mediumVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		helper.setInvColor( AEColor.Transparent.whiteVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( int x, int y, int z, IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();

		helper.setTexture( side );
		helper.setBounds( 4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F );
		helper.renderBlock( x, y, z, renderer );

		helper.setTexture( side, side, side, BlockTextureManager.BUS_BORDER.getTexture(), side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		if ( this.isActive() )
		{
			Tessellator.instance.setBrightness( 0xD000D0 );
		}

		ts.setColorOpaque_I( 0xFFFFFF );

		helper.renderFace( x, y, z, BlockTextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer );

		helper.setBounds( 3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16.0F );
		ts.setColorOpaque_I( this.host.getColor().blackVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		ts.setColorOpaque_I( this.host.getColor().mediumVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		ts.setColorOpaque_I( this.host.getColor().whiteVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );

	}
	

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setTag( "inventory", this.inventory.writeToNBT() );
	}
	
	/**
	 * Attempts to lock the terminal's inventory so that
	 * changes can be made.
	 * @return True if the lock was acquired, false otherwise.
	 */
	public boolean lockInventoryForWork()
	{
		boolean gotLock = false;
		
		// Ensure only 1 thread can access the lock at a time
		synchronized( this.inventory )
		{
			// Is the inventory not locked?
			if( !this.inventoryLocked )
			{
				// Mark it is now locked
				this.inventoryLocked = true;
				
				// Mark that this thread got the lock
				gotLock = true;
			}
		}
		
		// Return if this thread got the lock or not
		return gotLock;
	}
	
	/**
	 * Unlocks the terminal so that other threads can
	 * perform work. This should only be called from
	 * a thread that owns the lock.
	 */
	public void unlockInventory()
	{
		// Ensure only 1 thread can access the lock at a time
		synchronized( this.inventory )
		{
			this.inventoryLocked = false;
		}
	}
	
	@Override
	public void removeFromWorld()
	{
		// Is this server side?
		if ( !this.hostTile.getWorldObj().isRemote )
		{
			// Loop over inventory
			for( int index = 0; index < 2; index++ )
			{
				// Get the stack at this index
				ItemStack slotStack = this.inventory.getStackInSlot( index );
				
				// Did we get anything?
				if( slotStack != null )
				{
					// Drop it on the ground
					this.dropInventoryItemOnGround( slotStack );
				}
			}
		}
		
		// Pass to super
		super.removeFromWorld();
	}

}
