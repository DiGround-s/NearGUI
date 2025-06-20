/**
 * Clase principal del plugin NearGUI
 * Plugin que permite a los jugadores ver otros jugadores cercanos a través de una GUI
 * con sistema de permisos basado en radio de búsqueda
 */
package net.exylia.nearGUI;

import net.exylia.commons.ExyliaPlugin;
import net.exylia.commons.config.ConfigManager;
import net.exylia.commons.menu.MenuBuilder;
import net.exylia.commons.menu.MenuManager;
import net.exylia.commons.utils.DebugUtils;
import net.exylia.nearGUI.commands.NearCommand;
import net.exylia.nearGUI.commands.ReloadCommand;
import net.exylia.nearGUI.managers.NearManager;

import java.util.List;

public final class NearGUI extends ExyliaPlugin {

    private ConfigManager configManager;
    private MenuBuilder menuBuilder;
    private static NearGUI instance;
    private NearManager nearManager;

    @Override
    public void onExyliaEnable() {
        instance = this;
        MenuManager.initialize(this);

        DebugUtils.setPrefix(getName());

        loadManagers();
        registerCommands();

        DebugUtils.logInfo("Plugin cargado correctamente");
    }

    @Override
    public void onExyliaDisable() {
        DebugUtils.logInfo("Plugin desactivado");
    }

    private void registerCommands() {
        NearCommand nearCommand = new NearCommand(this, configManager);
        getCommand("near").setExecutor(nearCommand);
        getCommand("near").setTabCompleter(nearCommand);
        getCommand("neargui-reload").setExecutor(new ReloadCommand(this, configManager));
    }

    private void loadManagers() {
        configManager = new ConfigManager(this, List.of("config", "messages", "menus/near"));
        nearManager = new NearManager(this);
    }

    public void reload() {
        reloadConfig();
        configManager.reloadAllConfigs();
        nearManager.loadConfig();
    }

    public static NearGUI getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public NearManager getNearManager() {
        return nearManager;
    }
}