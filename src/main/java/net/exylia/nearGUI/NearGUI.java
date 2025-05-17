/**
 * Clase principal del plugin NearGUI
 * Plugin que permite a los jugadores ver otros jugadores cercanos a través de una GUI
 * con sistema de permisos basado en radio de búsqueda
 */
package net.exylia.nearGUI;

import net.exylia.commons.config.ConfigManager;
import net.exylia.commons.menu.MenuBuilder;
import net.exylia.commons.menu.MenuManager;
import net.exylia.commons.utils.DebugUtils;
import net.exylia.nearGUI.commands.NearCommand;
import net.exylia.nearGUI.managers.NearManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class NearGUI extends JavaPlugin {

    private ConfigManager configManager;
    private static NearGUI instance;
    private NearManager nearManager;

    @Override
    public void onEnable() {
        // Guardar instancia
        instance = this;

        DebugUtils.setPrefix(getName());

        saveDefaultConfigs();

        loadManagers();
        registerCommands();

        DebugUtils.logInfo("Plugin cargado correctamente");
    }

    @Override
    public void onDisable() {
        DebugUtils.logInfo("Plugin desactivado");
    }

    private void registerCommands() {
        NearCommand nearCommand = new NearCommand(this);
        getCommand("near").setExecutor(nearCommand);
        getCommand("near").setTabCompleter(nearCommand);
    }

    private void loadManagers() {
        MenuManager.initialize(this);
        configManager = new ConfigManager(this, List.of("config", "messages", "menus"));
        nearManager = new NearManager(this);
    }

    private void saveDefaultConfigs() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("menus.yml", false);
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