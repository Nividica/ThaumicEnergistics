package thaumicenergistics.api.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.common.entities.golems.EntityGolemBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Defines the methods required to interact with the golem hook system.</br>
 * The {@code handlerData} object can be anything that you would like and will never be touched by anything
 * other than the {@code IGolemHookHandler} that created it. It simply serves as a means to attach instance
 * data to a golem.<br/>
 * <hr/>
 * <strong>Keeping data synchronized.</strong><br/>
 * If you have data you need to send to the client, you can register that data when {@link #addDefaultSyncEntries} is called. In order to reduce the
 * amount of data that is sent to clients, you can only register individual characters of data.<br/>
 * Keep in mind that the handlerData on the server-side will not automatically match the handlerData on the client side. You must perform the
 * synchronization yourself. The only data shared between the server and client are the registered sync characters.<br/>
 * <strong>Example situation</strong>
 * <ol>
 * <li>[BothSides] Handler receives call to {@link #addDefaultSyncEntries}<br/>
 * Registers character with default value of 'n'<br/>
 * Receives ID of 13.</li>
 * <li>[Server] Handler receives call to {@link #customInteraction} an creates a complex object as its handler data.</li>
 * <li>[Server] Handler receives call to {@link #setupGolem}.<br/>
 * Handler sees that it's handlerData is not null, and verifies the data.<br/>
 * Handler then sets character ID 13 to 'y'</li>
 * <li>Data is sync'd and both sides have a value of 'y' in character ID 13.</li>
 * <li>[Client] Handler receives call to {@link #onSyncDataChanged}<br/>
 * Handlers sees that character ID 13 has a value of 'y'<br/>
 * Handler then sets its handlerData to String("Active")</li>
 * <li>[Client] Handler receives call to {@link #renderGolem}<br/>
 * Handler sees that its handlerData is not null, verifies that it equals String("Active")<br/>
 * Handler then performs its render.</li>
 * </ol>
 * In the above situation, the handlerData on both sides differs, but the complex object is only need on the server side. The client only needs to
 * know that the server-side handler has placed a 'y'(yes) value in the sync data, and therefore the server expects all clients to perform the special
 * render.<br/>
 * Following the above situation, if later during a call to setupGolem the server-side handler sets the sync character to 'n'(no), when the client
 * receives the update, the client-side handlerData can be set to, for example, null. This will let the handler know to not perform the special
 * render.<br/>
 * </br> If you want to see an example of a class that implements this, lookup my {@code WirelessGolemHandler} class on the ThE github.
 * 
 * @author Nividica
 * 
 * @see IGolemHookSyncRegistry
 * 
 */
public interface IGolemHookHandler
{

	/**
	 * Any data you wish to sync between the server and clients can be added here.<br/>
	 * The added bytes are considered the default state, and will be added to all golems in that state.
	 * 
	 * @param syncRegistry
	 */
	public void addDefaultSyncEntries( @Nonnull IGolemHookSyncRegistry syncRegistry );

	/**
	 * Return true if you can handle this interaction.<br/>
	 * 
	 * @param golem
	 * @param handlerData
	 * Handler data attached to the golem.
	 * @param player
	 * @param side
	 * Server or Client
	 * @return True will cause customInteraction to be called.<br>
	 * If any handlers return true, the setupGolem method will be called once all handlers
	 * have finished the interaction.
	 */
	public boolean canHandleInteraction( @Nonnull final EntityGolemBase golem, @Nullable Object handlerData, @Nonnull EntityPlayer player,
											@Nonnull Side side );

	/**
	 * Called when the golem has been interacted with and it hasn't been handled by Thaumcraft.<br/>
	 * 
	 * @param golem
	 * @param handlerData
	 * Handler data attached to the golem.
	 * @param player
	 * @param side
	 * Server or Client
	 * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
	 */
	@Nullable
	public Object customInteraction( @Nonnull final EntityGolemBase golem, @Nullable Object handlerData, @Nonnull EntityPlayer player,
										@Nonnull Side side );

	/**
	 * Return true if your handler wants to be called when a golem is rendered.<br/>
	 * Note: This is called only once during registration.
	 * 
	 * @return
	 */
	public boolean needsRenderer();

	/**
	 * Called when the golem was left clicked by a player holding the Golemancers Bell.
	 * 
	 * @param golem
	 * @param handlerData
	 * Handler data attached to the golem.
	 * @param itemGolemPlacer
	 * Itemstack that will be used to hold the golem.
	 * @param player
	 * @param dismantled
	 * True if the golem will drop its core and upgrades.
	 * @param side
	 * Server or Client @Nonnull Side side
	 */
	public void onBellLeftClick( @Nonnull final EntityGolemBase golem, @Nullable Object handlerData, @Nonnull ItemStack itemGolemPlacer,
									@Nonnull EntityPlayer player, boolean dismantled, @Nonnull Side side );

	/**
	 * Called when sync data has changed.
	 * 
	 * @param syncData
	 * @param clientHandlerData
	 * Handler data attached to the golem on the client side.
	 * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
	 */
	@SideOnly(Side.CLIENT)
	public Object onSyncDataChanged( @Nonnull IGolemHookSyncRegistry syncData, @Nullable Object clientHandlerData );

	/**
	 * Called when reading the golem's NBT.<br />
	 * Note: Called during a world load, not when the golem is placed via its item.
	 * 
	 * @param golem
	 * @param syncData
	 * @param nbtTag
	 * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
	 */
	@Nullable
	public Object readEntityFromNBT( @Nonnull final EntityGolemBase golem, @Nonnull IGolemHookSyncRegistry syncData, @Nonnull NBTTagCompound nbtTag );

	/**
	 * Called when a golem is being rendered, if {@code needsRenderer} returned true during registration.
	 * 
	 * @param golem
	 * @param clientHandlerData
	 * @param x
	 * @param y
	 * @param z
	 * @param partialElaspsedTick
	 */
	@SideOnly(Side.CLIENT)
	public void renderGolem( @Nonnull EntityGolemBase golem, @Nullable Object clientHandlerData, double x, double y, double z,
								float partialElaspsedTick );

	/**
	 * Called just before the golem has finished setting up.<br/>
	 * If you are adding an AI script(s) to the golem, be aware that Thaumcraft clears all of the golems scripts at the beginning of this call. You
	 * will need to add the script(s) each time this is called, but you do not need to worry about removing them.<br/>
	 * Note: The golems inventory may not be ready.
	 * 
	 * @param golem
	 * @param handlerData
	 * Handler data attached to the golem.
	 * @param side
	 * Server or Client
	 * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
	 */
	@Nullable
	public Object setupGolem( @Nonnull final EntityGolemBase golem, @Nullable Object handlerData, @Nonnull IGolemHookSyncRegistry syncData,
								@Nonnull Side side );

	/**
	 * Called when a golem is being spawned via it's item.<br/>
	 * Note: This is not called if the item has no NBT data.
	 * 
	 * @param golem
	 * @param itemGolemPlacer
	 * @param side
	 * Server or Client
	 * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
	 */
	@Nullable
	public Object spawnGolemFromItemStack( @Nonnull final EntityGolemBase golem, @Nonnull ItemStack itemGolemPlacer, @Nonnull Side side );

	/**
	 * Called when saving the golem's NBT.<br />
	 * Note: Called during a world save, not when the golem is being turned into an item via Golemancer's Bell.
	 * 
	 * @param golem
	 * @param serverHandlerData
	 * @param nbtTag
	 */
	public void writeEntityNBT( @Nonnull final EntityGolemBase golem, @Nullable Object serverHandlerData, @Nonnull NBTTagCompound nbtTag );
}
