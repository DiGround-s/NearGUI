package net.exylia.nearGUI.commands;

import net.exylia.commons.config.ConfigManager;
import net.exylia.commons.utils.MessageUtils;
import net.exylia.nearGUI.NearGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final NearGUI plugin;
    private final ConfigManager configManager;

    public ReloadCommand(NearGUI plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("neargui.reload")) {
            MessageUtils.sendMessageAsync(sender, (configManager.getMessage("system.no-permission")));
            return true;
        }

        plugin.reload();
        MessageUtils.sendMessageAsync(sender, (configManager.getMessage("system.reload")));

        return true;
    }
}
