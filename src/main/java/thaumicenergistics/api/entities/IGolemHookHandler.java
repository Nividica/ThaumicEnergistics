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
 * The {@code handlerData} object can be anything that you would like and will never be touched by anything other than
 * the {@code IGolemHookHandler} that created it. It simply serves as a means to attach instance data to a golem.<br/>
 * <hr/>
 * <strong>Keeping data synchronized.</strong><br/>
 * If you have data you need to send to the client, you can register that data when {@link #addDefaultSyncEntries} is
 * called. In order to reduce the amount of data that is sent to clients, you can only register individual characters of
 * data.<br/>
 * Keep in mind that the handlerData on the server-side will not automatically match the handlerData on the client side.
 * You must perform the synchronization yourself. The only data shared between the server and client are the registered
 * sync characters.<br/>
 * <strong>Example situation</strong>
 * <ol>
 * <li>[BothSides] Handler receives call to {@link #addDefaultSyncEntries}<br/>
 * Registers character with default value of 'n'<br/>
 * Receives ID of 13.</li>
 * <li>[Server] Handler receives call to {@link #customInteraction} an creates a complex object as its handler
 * data.</li>
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
 * In the above situation, the handlerData on both sides differs, but the complex object is only need on the server
 * side. The client only needs to know that the server-side handler has placed a 'y'(yes) value in the sync data, and
 * therefore the server expects all clients to perform the special render.<br/>
 * Following the above situation, if later during a call to setupGolem the server-side handler sets the sync character
 * to 'n'(no), when the client receives the update, the client-side handlerData can be set to, for example, null. This
 * will let the handler know to not perform the special render.<br/>
 * </br>
 * If you want to see an example of a class that implements this, lookup my {@code WirelessGolemHandler} class on the
 * ThE github.
 *
 * @author Nividica
 *
 * @see IGolemHookSyncRegistry
 *
 */
public interface IGolemHookHandler {

    /**
     * Defines interaction levels.
     *
     * @author Nividica
     *
     */
    public static enum InteractionLevel {
        /**
         * The interaction was not handled at all.<br>
         * <ul>
         * <li>GUI will be shown, if golem has one.</li>
         * <li>customInteraction() will not be called.</li>
         * <li>Sync data will not be sent.</li>
         * <li>setupGolem() will not be called.</li>
         * </ul>
         */
        NoInteraction,

        /**
         * The interaction was handled, but no sync data changed, and golem does not need to be re-setup.<br>
         * <ul>
         * <li>GUI will not be shown</li>
         * <li>customInteraction() called.
         * <li>
         * <li>Sync data will not be sent.</li>
         * <li>setupGolem() will not be called.</li>
         * </ul>
         */
        BasicInteraction,

        /**
         * The interaction was handled and sync data changed, but the golem does not need to be re-setup<br>
         * <ul>
         * <li>GUI will not be shown</li>
         * <li>customInteraction() called.</li>
         * <li>Sync data sent.</li>
         * <li>setupGolem() will not be called.</li>
         * </ul>
         */
        SyncInteraction,

        /**
         * The interaction was handled and the golem needs to be re-setup.
         * <ul>
         * <li>GUI will not be shown</li>
         * <li>customInteraction() called.</li>
         * <li>Sync data sent.</li>
         * <li>setupGolem() called.</li>
         * </ul>
         */
        FullInteraction;
    }

    /**
     * Any data you wish to sync between the server and clients can be added here.<br/>
     * The added bytes are considered the default state, and will be added to all golems in that state.
     *
     * @param syncRegistry
     */
    void addDefaultSyncEntries(@Nonnull IGolemHookSyncRegistry syncRegistry);

    /**
     * Called when the golem was left clicked by a player holding the Golemancers Bell.
     *
     * @param golem
     * @param handlerData     Handler data attached to the golem.
     * @param itemGolemPlacer Itemstack that will be used to hold the golem.
     * @param player
     * @param dismantled      True if the golem will drop its core and upgrades.
     * @param side            Server or Client @Nonnull Side side
     */
    void bellLeftClicked(@Nonnull final EntityGolemBase golem, @Nullable Object handlerData,
            @Nonnull ItemStack itemGolemPlacer, @Nonnull EntityPlayer player, boolean dismantled, @Nonnull Side side);

    /**
     * Return true if you can handle this interaction.<br/>
     *
     * @param golem
     * @param handlerData Handler data attached to the golem.
     * @param player
     * @param side        Server or Client
     * @return The level of interaction.
     */
    @Nonnull
    InteractionLevel canHandleInteraction(@Nonnull final EntityGolemBase golem, @Nullable Object handlerData,
            @Nonnull EntityPlayer player, @Nonnull Side side);

    /**
     * Called when the golem has been interacted with and it hasn't been handled by Thaumcraft.<br/>
     *
     * @param golem
     * @param handlerData Handler data attached to the golem.
     * @param syncData
     * @param player
     * @param side        Server or Client
     * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
     */
    @Nullable
    Object customInteraction(@Nonnull final EntityGolemBase golem, @Nullable Object handlerData,
            @Nonnull IGolemHookSyncRegistry syncData, @Nonnull EntityPlayer player, @Nonnull Side side);

    /**
     * Called when a golem receives a tick, if {@code needsDynamicUpdate} returned true during registration.<br>
     * If setting sync data, you do not need to track if the sync data has changed yourself. The {@code syncData} class
     * tracks this internally and will only ever send updates if the data has actually changed. Although for performance
     * reasons it would be a good idea to keep a tick counter and only set the data periodically if there is any
     * processing involved.<br>
     * Note: This is only called server side.
     *
     * @param golem
     * @param serverHandlerData
     * @param syncData
     */
    void golemTick(@Nonnull EntityGolemBase golem, @Nullable Object serverHandlerData,
            @Nonnull IGolemHookSyncRegistry syncData);

    /**
     * Return true if your handler wants to be called each time the golem gets a tick from the server.
     *
     * @return
     */
    boolean needsDynamicUpdates();

    /**
     * Return true if your handler wants to be called when a golem is rendered.<br/>
     * Note: This is called only once during registration.
     *
     * @return
     */
    boolean needsRenderer();

    /**
     * Called when reading the golem's NBT.<br />
     * Note: Called during a world load, not when the golem is placed via its item.
     *
     * @param golem
     * @param nbtTag
     * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
     */
    @Nullable
    Object readEntityFromNBT(@Nonnull final EntityGolemBase golem, @Nonnull NBTTagCompound nbtTag);

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
    void renderGolem(@Nonnull EntityGolemBase golem, @Nullable Object clientHandlerData, double x, double y, double z,
            float partialElaspsedTick);

    /**
     * Called just before the golem has finished setting up.<br/>
     * If you are adding an AI script(s) to the golem, be aware that Thaumcraft clears all of the golems scripts at the
     * beginning of this call. You will need to add the script(s) each time this is called, but you do not need to worry
     * about removing them.<br/>
     * Note: The golems inventory may not be ready.
     *
     * @param golem
     * @param handlerData Handler data attached to the golem.
     * @param side        Server or Client
     * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
     */
    @Nullable
    Object setupGolem(@Nonnull final EntityGolemBase golem, @Nullable Object handlerData,
            @Nonnull IGolemHookSyncRegistry syncData, @Nonnull Side side);

    /**
     * Called when a golem is being spawned via it's item.<br/>
     * Note: This is not called if the item has no NBT data.
     *
     * @param golem
     * @param itemGolemPlacer
     * @param side            Server or Client
     * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
     */
    @Nullable
    Object spawnGolemFromItemStack(@Nonnull final EntityGolemBase golem, @Nonnull ItemStack itemGolemPlacer,
            @Nonnull Side side);

    /**
     * Called when sync data has changed.
     *
     * @param syncData
     * @param clientHandlerData Handler data attached to the golem on the client side.
     * @return Return the handler data you wish to attach to the golem, or null to clear any attached data.
     */
    @SideOnly(Side.CLIENT)
    Object syncDataChanged(@Nonnull IGolemHookSyncRegistry syncData, @Nullable Object clientHandlerData);

    /**
     * Called when saving the golem's NBT.<br />
     * Note: Called during a world save, not when the golem is being turned into an item via Golemancer's Bell.
     *
     * @param golem
     * @param serverHandlerData
     * @param nbtTag
     */
    void writeEntityNBT(@Nonnull final EntityGolemBase golem, @Nullable Object serverHandlerData,
            @Nonnull NBTTagCompound nbtTag);
}
