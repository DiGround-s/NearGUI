package net.exylia.nearGUI.commands;

import net.exylia.commons.config.ConfigManager;
import net.exylia.commons.menu.PaginationMenu;
import net.exylia.commons.utils.ColorUtils;
import net.exylia.nearGUI.NearGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand implements CommandExecutor {
    private final NearGUI plugin;
    private final ConfigManager configManager;

    public ReloadCommand(NearGUI plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Verificar si el remitente es un jugador
        if (!(sender instanceof Player player)) {
            ColorUtils.sendSenderMessage(sender, (configManager.getMessage("near.player-only")));
            return true;
        }

        // Verificar permiso
        if (!player.hasPermission("neargui.reload")) {
            ColorUtils.sendSenderMessage(sender, (configManager.getMessage("system.no-permission")));
            return true;
        }

        plugin.reload();
        ColorUtils.sendPlayerMessage(player, (configManager.getMessage("system.reload")));

        return true;
    }
}
