package net.exylia.nearGUI.commands;

import net.exylia.commons.menu.Menu;
import net.exylia.commons.menu.PaginationMenu;
import net.exylia.commons.utils.ColorUtils;
import net.exylia.nearGUI.NearGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NearCommand implements CommandExecutor, TabCompleter {
    private NearGUI plugin;

    public NearCommand(NearGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el remitente es un jugador
        if (!(sender instanceof Player player)) {
            String message = getMessage("near.player-only", "%prefix% &cEste comando solo puede ser usado por jugadores.");
            sender.sendMessage(ColorUtils.translateColors(message));
            return true;
        }

        // Verificar permiso
        if (!player.hasPermission("neargui.use")) {
            String message = getMessage("messages.no-permission", "%prefix% &cNo tienes permiso para usar este comando.");
            player.sendMessage(ColorUtils.translateColors(message));
            return true;
        }

        // Obtener radio personalizado si se especifica
        double radius = plugin.getNearManager().getMaxRadiusForPlayer(player);

        if (args.length > 0) {
            try {
                double requestedRadius = Double.parseDouble(args[0]);
                // Limitar al radio máximo permitido para el jugador
                radius = Math.min(requestedRadius, plugin.getNearManager().getMaxRadiusForPlayer(player));
            } catch (NumberFormatException e) {
                String message = getMessage("near.invalid-radius", "%prefix% &cRadio inválido. Usa un número positivo.");
                player.sendMessage(ColorUtils.translateColors(message));
                return true;
            }
        }

        // Intentar abrir un menú de paginación primero
        PaginationMenu paginationMenu = plugin.getNearManager().createNearPaginationMenu(player, radius);

        if (paginationMenu != null) {
            paginationMenu.open(player);
        } else {
            String message = getMessage("near.no-players", "%prefix% &cNo hay jugadores cerca.");
            player.sendMessage(ColorUtils.translateColors(message));
            // Si no hay menú de paginación o no hay jugadores, intentar con un menú normal
//            Menu menu = plugin.getNearManager().createNearMenu(player, radius);
//
//            if (menu != null) {
//                menu.open(player);
//            } else {
//                // No hay jugadores cercanos
//            }
        }

        return true;
    }

    /**
     * Obtiene un mensaje del archivo de mensajes
     *
     * @param path Ruta del mensaje
     * @param defaultMessage Mensaje por defecto
     * @return Mensaje formateado
     */
    private String getMessage(String path, String defaultMessage) {
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages");
        String prefix = messages.getString("prefix", "&8[&bNearGUI&8]");

        String message = messages.getString(path, defaultMessage);
        return message.replace("%prefix%", prefix);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Solo ofrecer completado para el primer argumento (radio)
        if (args.length == 1) {
            if (sender instanceof Player player && player.hasPermission("neargui.use")) {
                double maxRadius = plugin.getNearManager().getMaxRadiusForPlayer(player);

                // Sugerir algunos valores predeterminados
                completions.add("10");
                completions.add("50");
                completions.add("100");
                completions.add(String.valueOf((int)maxRadius));
            }
        }

        return completions;
    }
}