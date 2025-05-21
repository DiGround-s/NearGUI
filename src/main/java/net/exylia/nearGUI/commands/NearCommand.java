package net.exylia.nearGUI.commands;

import net.exylia.commons.config.ConfigManager;
import net.exylia.commons.utils.MessageUtils;
import net.exylia.nearGUI.NearGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NearCommand implements CommandExecutor, TabCompleter {
    private final NearGUI plugin;
    private final ConfigManager configManager;

    public NearCommand(NearGUI plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessageAsync(sender, configManager.getMessage("near.player-only"));
            return true;
        }

        if (!player.hasPermission("neargui.use")) {
            MessageUtils.sendMessageAsync(player, (configManager.getMessage("system.no-permission")));
            return true;
        }

        double radius = plugin.getNearManager().getMaxRadiusForPlayer(player);

        if (args.length > 0) {
            try {
                double requestedRadius = Double.parseDouble(args[0]);
                radius = Math.min(requestedRadius, plugin.getNearManager().getMaxRadiusForPlayer(player));
            } catch (NumberFormatException e) {
                MessageUtils.sendMessageAsync(player, (configManager.getMessage("near.invalid-radius")));
                return true;
            }
        }

        plugin.getNearManager().openNearInventory(player, radius);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Solo ofrecer completado para el primer argumento (radio)
        if (args.length == 1) {
            if (sender instanceof Player player && player.hasPermission("neargui.use")) {
                double maxRadius = plugin.getNearManager().getMaxRadiusForPlayer(player);

                completions.add(String.valueOf((int)maxRadius));
            }
        }

        return completions;
    }
}