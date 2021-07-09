package thaumicenergistics.config;

import appeng.api.config.*;
import appeng.api.util.IConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Manages AE settings for Parts and TileEntities.
 *
 * @author Alex811
 */
public final class AESettings {
    private static final HashMap<SUBJECT, HashMap<Settings, Enum<?>>> SETTINGS = new HashMap<>();
    public enum SUBJECT {
        ARCANE_TERMINAL,
        ESSENTIA_TERMINAL,
        ESSENTIA_IMPORT_BUS,
        ESSENTIA_EXPORT_BUS,
        ESSENTIA_STORAGE_BUS
    }

    static {
        addSetting(SUBJECT.ARCANE_TERMINAL, Settings.SORT_BY, SortOrder.NAME);
        addSetting(SUBJECT.ARCANE_TERMINAL, Settings.VIEW_MODE, ViewItems.ALL);
        addSetting(SUBJECT.ARCANE_TERMINAL, Settings.SORT_DIRECTION, SortDir.ASCENDING);

        addSetting(SUBJECT.ESSENTIA_TERMINAL, Settings.SORT_BY, SortOrder.NAME);
        addSetting(SUBJECT.ESSENTIA_TERMINAL, Settings.SORT_DIRECTION, SortDir.ASCENDING);

        addSetting(SUBJECT.ESSENTIA_IMPORT_BUS, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);

        addSetting(SUBJECT.ESSENTIA_EXPORT_BUS, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);

        addSetting(SUBJECT.ESSENTIA_STORAGE_BUS, Settings.ACCESS, AccessRestriction.READ_WRITE);
        addSetting(SUBJECT.ESSENTIA_STORAGE_BUS, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
    }

    private static void addSetting(SUBJECT settingSubject, Settings setting, Enum<?> def){
        if(!SETTINGS.containsKey(settingSubject))
            SETTINGS.put(settingSubject, new HashMap<>());
        SETTINGS.get(settingSubject).put(setting, def);
    }

    public static void registerSettings(@Nullable SUBJECT settingSubject, @Nonnull IConfigManager configManager){
        if(settingSubject != null)
            SETTINGS.get(settingSubject).forEach(configManager::registerSetting);
    }
}
