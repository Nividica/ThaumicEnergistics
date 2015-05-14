package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.AEApi;
import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.client.texture.CableBusTextures;
import appeng.core.localization.PlayerMessages;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaStorageMonitor
	extends AbstractAEPartBase
	implements IPartStorageMonitor, IStackWatcherHost
{
	/**
	 * All the data about what is being tracked.
	 * 
	 * @author Nividica
	 * 
	 */
	private class TrackingInformation
	{
		/**
		 * Is there valid data?
		 */
		public boolean isValid = false;

		/**
		 * Faux itemstack
		 */
		public IAEItemStack asItemStack;

		/**
		 * Fluid stack
		 */
		public IAEFluidStack asGas;

		/**
		 * Aspect
		 */
		public Aspect asAspect;

		/**
		 * Amount in essentia units.
		 */
		public long aspectAmount;

		public TrackingInformation()
		{
			// Create the faux itemstack
			ItemStack is = new ItemStack( new Item()
			{
				@Override
				public String getItemStackDisplayName( final ItemStack ignored_ )
				{
					return this.getUnlocalizedName();
				}

				@Override
				public String getUnlocalizedName()
				{
					return TrackingInformation.this.asAspect.getName();
				}

				@Override
				public String getUnlocalizedName( final ItemStack ignored )
				{
					return this.getUnlocalizedName();
				}
			} );

			// Set the AE item stack
			this.asItemStack = AEApi.instance().storage().createItemStack( is );
		}

		/**
		 * Sets what is being tracked, or clears the data if null.
		 * 
		 * @param fs
		 */
		public void setTracked( final IAEFluidStack fs )
		{
			if( fs != null )
			{
				// Set gas
				this.asGas = fs;

				// Set aspect
				this.asAspect = ( (GaseousEssentia)fs.getFluid() ).getAspect();

				// Set valid
				this.isValid = true;

				// Set aspect amount
				this.updateTrackedAmount( fs.getStackSize() );
			}
			else
			{
				// Clear all and set invalid
				this.asGas = null;
				this.asAspect = null;
				this.aspectAmount = 0;
				this.isValid = false;
			}
		}

		/**
		 * Updates the fluidstack and aspect amounts.
		 * 
		 * @param fluidAmount
		 */
		public void updateTrackedAmount( final long fluidAmount )
		{
			// Ensure we are valid
			if( this.isValid )
			{
				// Update amounts
				this.asGas.setStackSize( fluidAmount );
				this.aspectAmount = EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( fluidAmount );
			}
		}

	}

	/**
	 * How much power does the monitor require?
	 */
	private static final double IDLE_DRAIN = 0.0625;

	/**
	 * NBT Keys
	 */
	private static final String NBT_KEY_LOCKED = "Locked", NBT_KEY_TRACKED_ASPECT = "TrackedAspect";

	/**
	 * Watches the ME network for changes.
	 */
	private IStackWatcher essentiaWatcher;

	/**
	 * True if the monitor is locked, and can not have its tracked essentia
	 * changed.
	 */
	private boolean monitorLocked = false;

	private TrackingInformation trackedEssentia = new TrackingInformation();

	/**
	 * If true the cached render list needs to be updated.
	 */
	@SideOnly(Side.CLIENT)
	private boolean updateDisplayList = false;

	/**
	 * ID of the cached render list.
	 */
	@SideOnly(Side.CLIENT)
	private Integer cachedDisplayList = null;

	public AEPartEssentiaStorageMonitor()
	{
		super( AEPartsEnum.EssentiaStorageMonitor );
	}

	/**
	 * Updates the watcher to the tracked essentia.
	 */
	private void configureWatcher()
	{
		// Clear any existing watched value
		if( this.essentiaWatcher != null )
		{
			this.essentiaWatcher.clear();
		}

		// Is there an essentia being tracked?
		if( this.trackedEssentia.isValid )
		{
			// Configure the watcher
			this.essentiaWatcher.add( this.trackedEssentia.asGas );

			// Get the storage grid
			IStorageGrid storageGrid = this.gridBlock.getStorageGrid();

			// Ensure there is a grid.
			if( storageGrid != null )
			{
				// Update the amount.
				this.updateTrackedEssentiaAmount( storageGrid.getFluidInventory() );
			}
		}
	}

	/**
	 * Returns true if the lock state was changed by this activation.
	 * 
	 * @param player
	 * @param heldItem
	 * @return
	 */
	private boolean didItemChangeLockState( final EntityPlayer player, final ItemStack heldItem )
	{
		// Is the item a wrench?
		if( !player.isSneaking() && Platform.isWrench( player, heldItem, this.hostTile.xCoord, this.hostTile.yCoord, this.hostTile.zCoord ) )
		{
			// Update the locked state
			this.monitorLocked = !this.monitorLocked;

			// Report to the player
			if( this.monitorLocked )
			{
				// Locked
				player.addChatMessage( PlayerMessages.isNowLocked.get() );
			}
			else
			{
				// Unlocked
				player.addChatMessage( PlayerMessages.isNowUnlocked.get() );
			}

			// Mark for sync & save
			this.host.markForUpdate();
			this.host.markForSave();

			return true;

		}

		return false;
	}

	/**
	 * Returns true if the tracker was changed.
	 * 
	 * @param heldItem
	 * @return
	 */
	private boolean didItemChangeTracker( final ItemStack heldItem )
	{
		// Is the players hand empty?
		if( heldItem == null )
		{
			// Clear the tracker
			this.trackedEssentia.setTracked( null );

			// Update watcher
			this.configureWatcher();

			// Mark for sync & save
			this.host.markForUpdate();
			this.host.markForSave();

			return true;
		}

		// Is the item an essentia container?
		if( !EssentiaItemContainerHelper.instance.isContainerOrLabel( heldItem ) )
		{
			return false;
		}

		// Get the aspect
		Aspect heldAspect = EssentiaItemContainerHelper.instance.getAspectInContainer( heldItem );

		// Ensure there is an aspect
		if( heldAspect == null )
		{
			return false;
		}

		// Get the essentia gas
		GaseousEssentia gas = GaseousEssentia.getGasFromAspect( heldAspect );

		// Ensure there is a gas
		if( gas == null )
		{
			return false;
		}

		// Create an AE fluid stack of size 0, and set it as the tracked gas
		this.trackedEssentia.setTracked( EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( gas, 0 ) );

		// Reconfigure the watcher
		this.configureWatcher();

		// Mark for sync & save
		this.host.markForUpdate();
		this.host.markForSave();

		return true;
	}

	/**
	 * Renders the aspect onto the screen.
	 * 
	 * @param tessellator
	 * @param aspect
	 */
	@SideOnly(Side.CLIENT)
	private void renderAspect( final Tessellator tessellator, final Aspect aspect )
	{
		// Get the aspect color
		Color aspectColor = new Color( aspect.getColor() );

		// Disable lighting
		GL11.glDisable( GL11.GL_LIGHTING );

		// Only draw if the image alpha is greater than the magic number
		GL11.glAlphaFunc( GL11.GL_GREATER, 0.004F );

		// Enable blending
		GL11.glEnable( GL11.GL_BLEND );

		// Specify the blending mode
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );

		// Set the color
		GL11.glColor4f( aspectColor.getRed() / 255.0F, aspectColor.getGreen() / 255.0F, aspectColor.getBlue() / 255.0F, 0.9F );
		tessellator.setColorRGBA_F( aspectColor.getRed() / 255.0F, aspectColor.getGreen() / 255.0F, aspectColor.getBlue() / 255.0F, 0.9F );

		// Center the aspect
		GL11.glTranslated( -0.20D, -0.25D, 0.0D );

		// Bind the aspect image
		Minecraft.getMinecraft().renderEngine.bindTexture( aspect.getImage() );

		// Add the vertex points
		double size = 0.38D;
		double zDepth = -0.265D;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV( 0.0D, size, zDepth, 0.0D, 1.0D ); // Bottom left
		tessellator.addVertexWithUV( size, size, zDepth, 1.0D, 1.0D ); // Bottom right
		tessellator.addVertexWithUV( size, 0.0D, zDepth, 1.0D, 0.0D ); // Top right
		tessellator.addVertexWithUV( 0.0D, 0.0D, zDepth, 0.0D, 0.0D ); // Top left

		// Draw!
		tessellator.draw();

		// Disable blending
		GL11.glDisable( GL11.GL_BLEND );

		// Enable lighting
		GL11.glEnable( GL11.GL_LIGHTING );
	}

	/**
	 * Renders the aspect and amount onto the screen.
	 * Note: Method originally from Applied Energistics 2,
	 * PartStorageMonitor.java
	 * 
	 * @param tessellator
	 * @param aspect
	 */
	@SideOnly(Side.CLIENT)
	private void renderScreen( final Tessellator tessellator, final Aspect aspect, final long aspectAmount )
	{
		// Push the OpenGL attributes
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		// Adjust position based on cable side
		GL11.glTranslated( this.cableSide.offsetX * 0.77, this.cableSide.offsetY * 0.77, this.cableSide.offsetZ * 0.77 );

		// Adjust scale and/or rotation based on cable side
		switch ( this.cableSide )
		{
			case DOWN:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
				break;

			case EAST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
				break;

			case NORTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				break;

			case SOUTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
				break;

			case UP:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
				break;

			case WEST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
				break;

			default:
				break;

		}

		// Push the OpenGL matrix
		GL11.glPushMatrix();

		try
		{
			// Calculate the brightness for the lightmap
			int brightness = 16 << 20 | 16 << 4;
			int brightnessComponent1 = brightness % 65536;
			int brightnessComponent2 = brightness / 65536;

			// Set the lightmap
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, brightnessComponent1 * 0.8F, brightnessComponent2 * 0.8F );

			// Render the aspect
			this.renderAspect( tessellator, aspect );
		}
		catch( Exception e )
		{
		}

		// Pop the OpenGL matrix
		GL11.glPopMatrix();

		// Move below the screen image
		GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

		// Convert the amount to a string
		final String renderedStackSize = ReadableNumberConverter.INSTANCE.toWideReadableForm( aspectAmount );

		// Get the font renderer
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

		// Adjust position based on string width
		GL11.glTranslatef( -0.5f * fr.getStringWidth( renderedStackSize ), 0.0f, -1.0f );

		// Render the string
		fr.drawString( renderedStackSize, 0, 0, 0 );

		// Pop the OpenGL attributes
		GL11.glPopAttrib();
	}

	/**
	 * Updates the tracked essentia amount.
	 * 
	 * @param fluidInventory
	 */
	private void updateTrackedEssentiaAmount( final IMEMonitor<IAEFluidStack> fluidInventory )
	{
		// Ensure there is something to track
		if( !this.trackedEssentia.isValid )
		{
			return;
		}

		// Reset the amount
		this.trackedEssentia.updateTrackedAmount( 0 );

		// Get the amount in the network
		IAEFluidStack stored = fluidInventory.getStorageList().findPrecise( this.trackedEssentia.asGas );

		// Was there anything found?
		if( stored != null )
		{
			// Set the amount
			this.trackedEssentia.updateTrackedAmount( stored.getStackSize() );
		}
	}

	/**
	 * No GUI to open.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void finalize() throws Throwable
	{
		// Call super
		super.finalize();

		// Dealoc the cached render list
		if( this.cachedDisplayList != null )
		{
			GLAllocation.deleteDisplayLists( this.cachedDisplayList );
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	/**
	 * Collision boxes
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return CableBusTextures.PartMonitor_Colored.IIcon;
	}

	/**
	 * What essentia gas is being displayed?
	 */
	@Override
	public IAEStack<?> getDisplayed()
	{
		if( this.trackedEssentia.isValid )
		{
			return this.trackedEssentia.asItemStack;
		}

		return null;
	}

	/**
	 * Gets how much power the monitor requires.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaStorageMonitor.IDLE_DRAIN;
	}

	/**
	 * Get the monitor brightness.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isActive() ? AbstractAEPartBase.ACTIVE_TERMINAL_LIGHT_LEVEL : 0 );
	}

	/**
	 * Is the monitor locked?
	 */
	@Override
	public boolean isLocked()
	{
		return this.monitorLocked;
	}

	/**
	 * Is the monitor on?
	 */
	@Override
	public boolean isPowered()
	{
		return this.isActive();
	}

	/**
	 * Called when a player right clicks the monitor.
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
		// Ignore client side.
		if( EffectiveSide.isClientSide() )
		{
			return true;
		}

		// Is the monitor off?
		if( !this.isActive() )
		{
			return false;
		}

		// Does the player have permission to interact with this device?
		if( !Platform.hasPermissions( this.getLocation(), player ) )
		{
			return false;
		}

		// Get the item the player is holding
		ItemStack heldItem = player.getCurrentEquippedItem();

		// Was the lock state changed?
		if( didItemChangeLockState( player, heldItem ) )
		{
			return true;
		}

		// Is the terminal locked?
		if( this.monitorLocked )
		{
			return false;
		}

		// Return if the item changes the essentia tracker
		return didItemChangeTracker( heldItem );
	}

	/**
	 * Called by the watcher when the essentia amount changes.
	 */
	@Override
	public void onStackChange( final IItemList o, final IAEStack fullStack, final IAEStack diffStack, final BaseActionSource src,
								final StorageChannel chan )
	{
		// Is there an essentia being tracked?
		if( !this.trackedEssentia.isValid )
		{
			// Not tracking anything
			return;
		}

		// Is there any in the system?
		if( fullStack != null )
		{
			// Update the amount
			this.trackedEssentia.updateTrackedAmount( fullStack.getStackSize() );
		}
		else
		{
			// None in system, set amount to 0
			this.trackedEssentia.updateTrackedAmount( 0 );
		}

		// Mark for sync
		this.host.markForUpdate();

	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read locked
		if( data.hasKey( AEPartEssentiaStorageMonitor.NBT_KEY_LOCKED ) )
		{
			this.monitorLocked = data.getBoolean( AEPartEssentiaStorageMonitor.NBT_KEY_LOCKED );
		}

		// Read tracked
		if( data.hasKey( AEPartEssentiaStorageMonitor.NBT_KEY_TRACKED_ASPECT ) )
		{
			// Read the aspect
			Aspect trackedAspect = Aspect.getAspect( data.getString( AEPartEssentiaStorageMonitor.NBT_KEY_TRACKED_ASPECT ) );

			// Get the gas
			GaseousEssentia gas = GaseousEssentia.getGasFromAspect( trackedAspect );

			if( gas != null )
			{
				// Create the fluid stack
				IAEFluidStack fs = EssentiaConversionHelper.instance.createAEFluidStackInEssentiaUnits( gas, 0 );

				// Set the tracker
				this.trackedEssentia.setTracked( fs );
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean readFromStream( final ByteBuf stream ) throws IOException
	{
		boolean redraw = false;

		// Call super
		redraw |= super.readFromStream( stream );

		// Read locked state
		this.monitorLocked = stream.readBoolean();

		// Is there any tracking info to read?
		if( stream.readBoolean() )
		{
			// Read the tracked fluid
			IAEFluidStack tk = AEApi.instance().storage().readFluidFromPacket( stream );

			// Update the tracker
			this.trackedEssentia.setTracked( tk );

			// Mark for screen redraw
			this.updateDisplayList = true;
		}
		else
		{
			// Clear the tracker
			this.trackedEssentia.setTracked( null );

			// Mark for screen redraw
			this.updateDisplayList = true;
		}

		return redraw;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderDynamic( final double x, final double y, final double z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Skip if nothing to draw
		if( ( this.isActive == false ) || ( !this.trackedEssentia.isValid ) )
		{
			return;
		}

		// Does the cached display list need to be created?
		if( this.cachedDisplayList == null )
		{
			// Ask OpenGL for a display list
			this.cachedDisplayList = GLAllocation.generateDisplayLists( 1 );

			// Mark for update
			this.updateDisplayList = true;
		}

		// Push the OpenGL matrix
		GL11.glPushMatrix();

		// Move to the center of the monitor
		GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );

		// Does the display list need to be updated?
		if( this.updateDisplayList )
		{
			// Mark that it is being updated
			this.updateDisplayList = false;

			// Ask OpenGL to create a new compiled list, and run it when done.
			GL11.glNewList( this.cachedDisplayList, GL11.GL_COMPILE_AND_EXECUTE );

			// Add the screen render to the list
			this.renderScreen( Tessellator.instance, this.trackedEssentia.asAspect, this.trackedEssentia.aspectAmount );

			// End the list and run it
			GL11.glEndList();
		}
		else
		{
			// Run the cached list
			GL11.glCallList( this.cachedDisplayList );
		}

		// Pop the OpenGL matrix
		GL11.glPopMatrix();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, side, side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Face bounds
		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );

		// Edges
		helper.setInvColor( AEColor.White.whiteVariant );
		helper.renderInventoryFace( CableBusTextures.PartConversionMonitor_Dark.getIcon(), ForgeDirection.SOUTH, renderer );

		// Conversion icon
		helper.setInvColor( AEColor.Transparent.whiteVariant );
		helper.renderInventoryFace( CableBusTextures.PartConversionMonitor_Bright.getIcon(), ForgeDirection.SOUTH, renderer );

		// Phial
		helper.setInvColor( AEColor.Black.blackVariant );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );

	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		// Main block
		helper.setTexture( side, side, side, side, side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Light up if active
		if( this.isActive() )
		{
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_FACE_BRIGHTNESS );
		}

		// Dark face
		tessellator.setColorOpaque_I( this.host.getColor().blackVariant );
		helper.renderFace( x, y, z, CableBusTextures.PartConversionMonitor_Dark.getIcon(), ForgeDirection.SOUTH, renderer );

		// Medium face
		tessellator.setColorOpaque_I( this.host.getColor().mediumVariant );
		helper.renderFace( x, y, z, CableBusTextures.PartConversionMonitor_Colored.getIcon(), ForgeDirection.SOUTH, renderer );

		// Bright face
		tessellator.setColorOpaque_I( this.host.getColor().whiteVariant );
		helper.renderFace( x, y, z, CableBusTextures.PartConversionMonitor_Bright.getIcon(), ForgeDirection.SOUTH, renderer );

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );

	}

	/**
	 * Needs dynamic renderer.
	 */
	@Override
	public boolean requireDynamicRender()
	{
		return true;
	}

	@Override
	public boolean showNetworkInfo( final MovingObjectPosition where )
	{
		return false;
	}

	/**
	 * Called when a new watcher is given to the monitor by the host.
	 */
	@Override
	public void updateWatcher( final IStackWatcher newWatcher )
	{
		// Set the watcher
		this.essentiaWatcher = newWatcher;

		// Configure it
		this.configureWatcher();
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Don't write anything if the monitor is dropping.
		if( this.nbtCalledForDrops )
		{
			return;
		}

		// Write locked
		if( this.monitorLocked )
		{
			data.setBoolean( AEPartEssentiaStorageMonitor.NBT_KEY_LOCKED, this.monitorLocked );
		}

		// Write tracked data if valid
		if( this.trackedEssentia.isValid )
		{
			// Write the aspect
			data.setString( AEPartEssentiaStorageMonitor.NBT_KEY_TRACKED_ASPECT, this.trackedEssentia.asAspect.getTag() );
		}
	}

	@Override
	public void writeToStream( final ByteBuf stream ) throws IOException
	{
		// Call super
		super.writeToStream( stream );

		// Write locked
		stream.writeBoolean( this.monitorLocked );

		// Write if valid
		stream.writeBoolean( this.trackedEssentia.isValid );

		if( this.trackedEssentia.isValid )
		{
			// Write the tracker
			this.trackedEssentia.asGas.writeToPacket( stream );
		}

	}
}
