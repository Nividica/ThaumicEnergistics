package thaumicenergistics.common.integration.tc;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.common.registries.ThEStrings;
import appeng.api.networking.IGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.DimensionalCoord;

public class DigiVisSourceData {

    private static final String NBT_KEY_HAS_DATA = "hasData";
    private static final String NBT_KEY_DATA = "data";
    private static final String NBT_KEY_UID = "uid";

    public static final String SOURCE_UNLOC_NAME = ThEStrings.Gui_DigiVisSource.getUnlocalized();

    /**
     * True if there is data.
     */
    private boolean hasData = false;

    /**
     * World the source is in.
     */
    private int worldID;

    /**
     * X position of the source.
     */
    private int x;

    /**
     * Y position of the source.
     */
    private int y;

    /**
     * Z position of the source.
     */
    private int z;

    /**
     * Side the part is attached to.
     */
    private ForgeDirection side;

    /**
     * ID of the source.
     */
    private long UID;

    /**
     * Cached reference to source.
     */
    private WeakReference<IDigiVisSource> digiVisSource;

    /**
     * Creates the info, setting that there is no data
     */
    public DigiVisSourceData() {
        this.clearData();
    }

    /**
     * Creates the data from a digivis source.
     *
     * @param digiVisSource
     */
    public DigiVisSourceData(final IDigiVisSource digiVisSource) {
        // Init the data fields.
        this.clearData();

        // Ensure there is a source.
        if (digiVisSource == null) {
            // No source
            return;
        }

        // Get the source location
        DimensionalCoord sourceLocation = digiVisSource.getLocation();

        // Get the world id
        this.worldID = sourceLocation.getWorld().provider.dimensionId;

        // Get the x,y,z
        this.x = sourceLocation.x;
        this.y = sourceLocation.y;
        this.z = sourceLocation.z;

        // Get the side
        this.side = digiVisSource.getSide();

        // Get the UID
        this.UID = digiVisSource.getUID();

        // Set that we have data
        this.hasData = true;
    }

    /**
     * Gets the vis source.
     *
     * @return
     */
    private IDigiVisSource getSource(final boolean forceUpdate) {
        // Do we have any data to load from?
        if (this.hasData) {
            // Do we have an source cached?
            IDigiVisSource vInt = this.digiVisSource.get();
            if (forceUpdate || (vInt == null)) {
                // Attempt to get the source
                this.refreshCache();

                // Return it if cached
                return this.digiVisSource.get();
            }

            // Return the cached
            return vInt;
        }

        // Nothing to load
        return null;
    }

    private void refreshCache() {
        // Clear the reference
        this.digiVisSource.clear();
        IDigiVisSource visSource = null;

        try {
            // Get the world
            World world = DimensionManager.getWorld(this.worldID);

            // Ensure the world is not null
            if (world == null) {
                return;
            }

            // Get the tile
            TileEntity tile = world.getTileEntity(this.x, this.y, this.z);

            // Ensure we got a tile
            if (tile == null) {
                return;
            }

            // Is the source a block?
            if (this.side.equals(ForgeDirection.UNKNOWN)) {
                // Ensure the block is a source
                if (!(tile instanceof IDigiVisSource)) {
                    return;
                }

                // Get the source
                visSource = (IDigiVisSource) tile;
            } else {
                // Ensure the tile is a part host
                if (!(tile instanceof IPartHost)) {
                    return;
                }

                // Get the part from the host
                IPart part = ((IPartHost) tile).getPart(this.side);

                // Ensure we got the part, and it is a vis source
                if (!(part instanceof IDigiVisSource)) {
                    return;
                }

                // Cast to the source
                visSource = (IDigiVisSource) part;
            }

            // Check the UID
            if (visSource.getUID() == this.UID) {
                // Set the source
                this.digiVisSource = new WeakReference<IDigiVisSource>(visSource);
            }
        } catch (Exception e) {}
    }

    private IDigiVisSource tryGetSource(@Nonnull final IGrid destinationGrid, final boolean forceUpdate) {
        IDigiVisSource source = null;
        try {
            // Get this source
            source = this.getSource(forceUpdate);
            if (source != null) {
                // Get the source grid
                IGrid sourceGrid = source.getGrid();

                // Do the grids NOT match?
                if ((sourceGrid == null) || !destinationGrid.equals(sourceGrid)) {
                    // Grid mismatch
                    source = null;
                }
            }

        } catch (Exception e) {}

        return source;
    }

    /**
     * Erases all data
     */
    public void clearData() {
        this.hasData = false;
        this.worldID = -1;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.side = ForgeDirection.UNKNOWN;
        this.UID = 0;
        this.digiVisSource = new WeakReference<IDigiVisSource>(null);
    }

    /**
     * True if there is data.
     *
     * @return
     */
    public boolean hasSourceData() {
        return this.hasData;
    }

    /**
     * Reads the info directly from the tag.
     *
     * @param tag
     */
    public void readFromNBT(final NBTTagCompound tag) {
        // Clear all existing data
        this.clearData();

        // Ensure the tag is not null
        if (tag == null) {
            return;
        }

        // Is there data present in the tag?
        if (tag.getBoolean(DigiVisSourceData.NBT_KEY_HAS_DATA)) {
            // Ensure the required tags are present
            if (!tag.hasKey(DigiVisSourceData.NBT_KEY_UID) || !tag.hasKey(DigiVisSourceData.NBT_KEY_DATA)) {
                // Missing required data
                return;
            }

            // Load the info
            int[] info = tag.getIntArray(DigiVisSourceData.NBT_KEY_DATA);

            // Ensure the info contains the expected number of items
            if (info.length < 5) {
                // Invalid number of items
                return;
            }

            // Set each item
            this.worldID = info[0];
            this.x = info[1];
            this.y = info[2];
            this.z = info[3];
            this.side = ForgeDirection.getOrientation(info[4]);

            // Read the uid
            this.UID = tag.getLong(DigiVisSourceData.NBT_KEY_UID);

            // Mark there is data
            this.hasData = true;
        }
    }

    /**
     * Reads the info from the data tag.
     *
     * @param data
     * @param name
     */
    public void readFromNBT(final NBTTagCompound data, final String name) {
        this.readFromNBT(data.getCompoundTag(name));
    }

    /**
     * Verifies that the source exists, is active, and that the source and destination grid's are the same.
     *
     * @param destinationGrid
     *
     * @return Return's the source if valid, null otherwise.
     */
    public IDigiVisSource tryGetSource(final IGrid destinationGrid) {
        IDigiVisSource source = null;

        // Ensure the destination grid is not null
        if (destinationGrid != null) {
            // Get the cached source
            source = this.tryGetSource(destinationGrid, false);
            if (source == null) {
                // Force a refresh
                source = this.tryGetSource(destinationGrid, true);
            }
        }
        return source;
    }

    /**
     * Creates a new NBT tag and writes the data into it
     *
     * @return
     */
    public NBTTagCompound writeToNBT() {
        // Create the tag
        NBTTagCompound tag = new NBTTagCompound();

        // Write if we have data
        tag.setBoolean(DigiVisSourceData.NBT_KEY_HAS_DATA, this.hasData);

        if (this.hasData) {
            // Write the data
            tag.setIntArray(
                    DigiVisSourceData.NBT_KEY_DATA,
                    new int[] { this.worldID, this.x, this.y, this.z, this.side.ordinal() });

            // Write the uid
            tag.setLong(DigiVisSourceData.NBT_KEY_UID, this.UID);
        }

        return tag;
    }

    /**
     * Writes the info into the data tag.
     *
     * @param data
     * @param name
     */
    public void writeToNBT(final NBTTagCompound data, final String name) {
        // Write into the data tag
        data.setTag(name, this.writeToNBT());
    }
}
