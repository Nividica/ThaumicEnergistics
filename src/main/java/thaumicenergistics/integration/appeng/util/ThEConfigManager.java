package thaumicenergistics.integration.appeng.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;

import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class ThEConfigManager implements IConfigManager {

    private Map<Settings, Enum> settings = new EnumMap<>(Settings.class);

    public ThEConfigManager() {
    }

    @Override
    public void registerSetting(Settings setting, Enum<?> defaultValue) {
        this.settings.put(setting, defaultValue);
    }

    @Override
    public Enum<?> putSetting(Settings setting, Enum<?> value) {
        Enum old = this.getSetting(setting);
        this.settings.put(setting, value);
        return old;
    }

    @Override
    public Enum<?> getSetting(Settings setting) {
        Enum v = this.settings.get(setting);
        if (v == null)
            throw new IllegalStateException("Setting '" + setting + "' has not been registered!");
        return v;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        this.settings.forEach((key, value) -> tag.setString(key.name(), value.name()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.settings.forEach((key, old) -> {
            if (!tag.hasKey(key.name()))
                return;
            ThELog.info("Reading existing setting");
            String value = tag.getString(key.name());
            Enum newValue;
            try {
                ThELog.info("value = {}", value);
                newValue = Enum.valueOf(old.getDeclaringClass(), value);
                ThELog.info("newValue = {}", newValue.name());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return;
            }
            this.putSetting(key, newValue);
        });
    }

    @Override
    public Set<Settings> getSettings() {
        return this.settings.keySet();
    }
}