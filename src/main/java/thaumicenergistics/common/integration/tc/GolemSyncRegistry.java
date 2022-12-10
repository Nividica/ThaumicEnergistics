package thaumicenergistics.common.integration.tc;

import com.google.common.base.Charsets;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import thaumicenergistics.api.entities.IGolemHookHandler;
import thaumicenergistics.api.entities.IGolemHookSyncRegistry;
import thaumicenergistics.common.utils.ThELog;

class GolemSyncRegistry implements IGolemHookSyncRegistry {
    /**
     * Maps ID => Handler
     */
    private static final HashMap<Integer, IGolemHookHandler> handlerMappings =
            new HashMap<Integer, IGolemHookHandler>();

    /**
     * Maps ID => Data
     */
    private final HashMap<Integer, Character> dataMappings = new HashMap<Integer, Character>();

    /**
     * Next ID
     */
    private Integer uuid = Integer.valueOf(13);

    /**
     * True if the data has changed.
     */
    private boolean hasDataChanged = false;

    /**
     * True if new values can be added.
     */
    public boolean canRegister = false;

    /**
     * The string that the mappings were read from.
     */
    public String lastUpdatedFrom = null;

    /**
     * Tracks the number of ticks since the last sync on the client side.
     */
    public float clientSyncTicks = 0.0f;

    public void copyDefaults(final GolemSyncRegistry defaults) {
        // Clear the maps
        this.dataMappings.clear();

        // Copy the defaults
        this.dataMappings.putAll(defaults.dataMappings);
        this.hasDataChanged = true;
    }

    @Override
    public char getSyncCharOrDefault(final int id, final char defaultChar) {
        return this.dataMappings.getOrDefault(Integer.valueOf(id), defaultChar);
    }

    /**
     * True if the data has changed since the last sync.
     *
     * @return
     */
    public boolean hasChanged() {
        return this.hasDataChanged;
    }

    /**
     * Converts the mappings into a string.
     *
     * @return
     */
    public String mappingsToString() {
        this.hasDataChanged = false;

        // Write the data
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream((this.dataMappings.size() * 6) + 4);
                DataOutputStream stream = new DataOutputStream(baos)) {

            // Write count
            stream.writeInt(this.dataMappings.size());

            for (Entry<Integer, Character> entry : this.dataMappings.entrySet()) {
                // Write the ID
                stream.writeInt(entry.getKey());

                // Write the char
                stream.writeChar(entry.getValue());
            }
            stream.close();

            // Return the string
            return baos.toString(Charsets.UTF_8.name());
        } catch (IOException e) {
            ThELog.error(e, "Unable to send golem sync data");
        }

        return "";
    }

    /**
     * Marks the registry as needing to be syncd.
     */
    public void markDirty() {
        this.hasDataChanged = true;
    }

    /**
     * Reads the mappings from a string
     *
     * @param data
     */
    public HashSet<IGolemHookHandler> readFromString(final String data) {
        if ((data == null) || (data.length() < 4)) {
            ThELog.warning("Incomplete golem sync data received");
            return null;
        }

        HashSet<IGolemHookHandler> handlersToUpdate = new HashSet<IGolemHookHandler>();

        // Mark the data as valid
        this.hasDataChanged = false;

        // Save the string
        this.lastUpdatedFrom = data;

        // Read the data
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes(Charsets.UTF_8.name()));
                DataInputStream stream = new DataInputStream(bais); ) {

            // Read the count
            int count = stream.readInt();

            for (int i = 0; i < count; ++i) {
                // Read the ID
                Integer id = Integer.valueOf(stream.readInt());

                // Read the char
                Character c = Character.valueOf(stream.readChar());

                // Add to the mapping
                this.dataMappings.put(id, c);

                // Mark for update
                handlersToUpdate.add(GolemSyncRegistry.handlerMappings.get(id));
            }

            // Close the stream
            stream.close();
        } catch (Exception e) {
            ThELog.error(e, "Malformed golem sync data received");
        }

        // Update handlers
        if (!handlersToUpdate.isEmpty()) {
            return handlersToUpdate;
        }

        return null;
    }

    @Override
    public int registerSyncChar(final IGolemHookHandler handler, final char c) {
        if (!this.canRegister) {
            throw new UnsupportedOperationException("Can not register new sync data at this location.");
        }

        // Add the mappings
        this.dataMappings.put(this.uuid, Character.valueOf(c));
        GolemSyncRegistry.handlerMappings.put(this.uuid, handler);
        this.hasDataChanged = true;

        // Get the id
        int id = this.uuid;

        // Increment the uuid
        this.uuid = Integer.valueOf(id + 1);

        return id;
    }

    @Override
    public void updateSyncChar(final IGolemHookHandler handler, final int id, final char c) {
        // Get the id
        Integer ID = Integer.valueOf(id);

        // Does the map have this id?
        if (!this.dataMappings.containsKey(ID)) {
            throw new UnsupportedOperationException(
                    "Unable to update sync byte '" + Integer.toString(id) + "', that ID is not mapped");
        }

        // Get the registered handler
        IGolemHookHandler registeredHandler = handlerMappings.get(ID);
        if (registeredHandler != handler) {
            throw new UnsupportedOperationException(
                    "Unable to update sync byte '" + Integer.toString(id) + "', that ID does not belong to you.");
        }

        // Update
        Character newValue = Character.valueOf(c);
        if (this.dataMappings.put(ID, newValue) != newValue) {
            this.hasDataChanged = true;
        }
    }
}
