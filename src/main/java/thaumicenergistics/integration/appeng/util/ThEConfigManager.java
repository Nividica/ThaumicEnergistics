package thaumicenergistics.integration.appeng.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import thaumicenergistics.config.AESettings;

import javax.annotation.Nullable;

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

    public void registerSettings(@Nullable AESettings.SUBJECT settingSubject){
        AESettings.registerSettings(settingSubject, this);
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
            String value = tag.getString(key.name());
            Enum newValue;
            try {
                newValue = Enum.valueOf(old.getDeclaringClass(), value);
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