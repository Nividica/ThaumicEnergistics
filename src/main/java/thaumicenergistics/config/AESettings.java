package thaumicenergistics.config;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
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
        ESSENTIA_EXPORT_BUS
    }

    static {
        addSetting(SUBJECT.ARCANE_TERMINAL, Settings.SORT_BY, SortOrder.NAME);
        addSetting(SUBJECT.ARCANE_TERMINAL, Settings.VIEW_MODE, ViewItems.ALL);
        addSetting(SUBJECT.ARCANE_TERMINAL, Settings.SORT_DIRECTION, SortDir.ASCENDING);

        addSetting(SUBJECT.ESSENTIA_TERMINAL, Settings.SORT_BY, SortOrder.NAME);
        addSetting(SUBJECT.ESSENTIA_TERMINAL, Settings.SORT_DIRECTION, SortDir.ASCENDING);

        addSetting(SUBJECT.ESSENTIA_IMPORT_BUS, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);

        addSetting(SUBJECT.ESSENTIA_EXPORT_BUS, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
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
