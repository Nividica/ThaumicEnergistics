package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.AbstractContainerCellTerminalBase;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.gui.GuiEssentiaCellTerminal;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaTerminal
	extends AbstractAEPartRotateable
{

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.5D;

	/**
	 * Key used to read and write sorting mode from/to NBT.
	 */
	private static final String SORT_MODE_NBT_KEY = "sortMode";

	/**
	 * Key used to read and write inventory from/to NBT.
	 */
	private static final String INVENTORY_NBT_KEY = "slots";

	/**
	 * Default sorting mode for the terminal.
	 */
	private static final ComparatorMode DEFAULT_SORT_MODE = ComparatorMode.MODE_ALPHABETIC;

	/**
	 * List of currently opened containers.
	 */
	private List<ContainerEssentiaTerminal> listeners = new ArrayList<ContainerEssentiaTerminal>();

	/**
	 * Tracks if the inventory has been locked for work.
	 */
	private boolean inventoryLocked = false;

	/**
	 * The sorting mode used to display aspects.
	 */
	private ComparatorMode sortMode = AEPartEssentiaTerminal.DEFAULT_SORT_MODE;

	private PrivateInventory inventory = new PrivateInventory( ThaumicEnergistics.MOD_ID + ".part.aspect.terminal", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( final int slotId, final ItemStack itemStack )
		{
			// Get the type
			AspectItemType iType = EssentiaItemContainerHelper.INSTANCE.getItemType( itemStack );

			// True if jar or jar label
			return ( iType == AspectItemType.EssentiaContainer ) || ( iType == AspectItemType.JarLabel );
		}
	};

	public AEPartEssentiaTerminal()
	{
		super( AEPartsEnum.EssentiaTerminal );
	}

	public void addListener( final AbstractContainerCellTerminalBase container )
	{
		if( container instanceof ContainerEssentiaTerminal )
		{
			this.listeners.add( (ContainerEssentiaTerminal)container );
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	/**
	 * Checks if the specified player can open the gui.
	 */
	@Override
	public boolean doesPlayerHavePermissionToOpenGui( final EntityPlayer player )
	{
		return true;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 4.0D, 4.0D, 13.0D, 12.0D, 12.0D, 14.0D );

		helper.addBox( 5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 13.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[3];
	}

	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return GuiEssentiaCellTerminal.NewEssentiaTerminalGui( this, player );
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		// Inventory is saved when wrenched.
		if( wrenched )
		{
			return;
		}

		// Loop over inventory
		for( int slotIndex = 0; slotIndex < 2; slotIndex++ )
		{
			// Get the stack at this index
			ItemStack slotStack = this.inventory.getStackInSlot( slotIndex );

			// Did we get anything?
			if( slotStack != null )
			{
				// Add to drops
				drops.add( slotStack );
			}
		}
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaTerminal.IDLE_POWER_DRAIN;
	}

	public PrivateInventory getInventory()
	{
		return this.inventory;
	}

	/**
	 * Light level based on if terminal is active.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isActive() ? AbstractAEPartBase.ACTIVE_TERMINAL_LIGHT_LEVEL : 0 );
	}

	@Override
	public Object getServerGuiElement( final EntityPlayer player )
	{
		return new ContainerEssentiaTerminal( this, player );
	}

	/**
	 * Gets the current sorting mode
	 * 
	 * @return
	 */
	public ComparatorMode getSortingMode()
	{
		return this.sortMode;
	}

	/**
	 * Attempts to lock the terminal's inventory so that
	 * changes can be made.
	 * 
	 * @return True if the lock was acquired, false otherwise.
	 */
	@Deprecated
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
	 * Informs all open containers to update their respective clients
	 * that the sorting mode has changed.
	 */
	public void notifyListenersSortingModeChanged()
	{
		for( ContainerEssentiaTerminal listener : this.listeners )
		{
			listener.onSortingModeChanged( this.sortMode );
		}
	}

	/**
	 * Called when a player has changed sorting modes.
	 * 
	 * @param sortMode
	 */
	public void onClientRequestSortingModeChange( final ComparatorMode sortMode )
	{
		// Set the sort mode
		this.sortMode = sortMode;

		// Update clients
		this.notifyListenersSortingModeChanged();

		// Mark that we need saving
		this.markForSave();

	}

	/**
	 * Called to read our saved state
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read the sorting mode
		if( data.hasKey( SORT_MODE_NBT_KEY ) )
		{
			this.sortMode = ComparatorMode.VALUES[data.getInteger( SORT_MODE_NBT_KEY )];
		}

		// Read inventory
		if( data.hasKey( AEPartEssentiaTerminal.INVENTORY_NBT_KEY ) )
		{
			this.inventory.readFromNBT( data, AEPartEssentiaTerminal.INVENTORY_NBT_KEY );
		}
	}

	public void removeListener( final ContainerEssentiaTerminal containerAspectTerminal )
	{
		this.listeners.remove( containerAspectTerminal );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[3];

		helper.setTexture( side );
		helper.setBounds( 4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F );
		helper.renderInventoryBox( renderer );

		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		ts.setBrightness( 0xD000D0 );

		helper.setInvColor( 0xFFFFFF );

		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], ForgeDirection.SOUTH, renderer );

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
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[3];

		// Cable connector
		helper.setTexture( side );
		helper.setBounds( 4.0F, 4.0F, 13.0F, 12.0F, 12.0F, 14.0F );
		helper.renderBlock( x, y, z, renderer );

		// Mid-block
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Light up if active
		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_FACE_BRIGHTNESS );
		}

		// Rotate the face texture
		this.rotateRenderer( renderer, false );

		// Base face
		ts.setColorOpaque_I( 0xFFFFFF );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[4], ForgeDirection.SOUTH, renderer );

		// Dark colored face
		helper.setBounds( 3.0F, 3.0F, 15.0F, 13.0F, 13.0F, 16.0F );
		ts.setColorOpaque_I( this.getHost().getColor().blackVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		// Standard colored face
		ts.setColorOpaque_I( this.getHost().getColor().mediumVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Light colored face
		ts.setColorOpaque_I( this.getHost().getColor().whiteVariant );
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		// Reset rotation
		this.rotateRenderer( renderer, true );

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 12.0F, 11.0F, 11.0F, 13.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );

	}

	/**
	 * Unlocks the terminal so that other threads can
	 * perform work. This should only be called from
	 * a thread that owns the lock.
	 */
	@Deprecated
	public void unlockInventory()
	{
		// Ensure only 1 thread can access the lock at a time
		synchronized( this.inventory )
		{
			this.inventoryLocked = false;
		}
	}

	/**
	 * Called to save our state
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Write the sorting mode
		if( this.sortMode != AEPartEssentiaTerminal.DEFAULT_SORT_MODE )
		{
			data.setInteger( SORT_MODE_NBT_KEY, this.sortMode.ordinal() );
		}

		// Write inventory
		if( !this.inventory.isEmpty() )
		{
			this.inventory.writeToNBT( data, AEPartEssentiaTerminal.INVENTORY_NBT_KEY );
		}
	}

}
