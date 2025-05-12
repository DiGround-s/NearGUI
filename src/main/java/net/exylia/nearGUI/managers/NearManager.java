package net.exylia.nearGUI.managers;

import net.exylia.commons.menu.Menu;
import net.exylia.commons.menu.MenuItem;
import net.exylia.commons.menu.PaginationMenu;
import net.exylia.commons.utils.ColorUtils;
import net.exylia.nearGUI.NearGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class NearManager {
    private NearGUI plugin;
    // Caché opcional para mejorar rendimiento en servidores grandes
    private Map<UUID, List<Player>> nearbyPlayersCache;
    private long cacheExpiration = 5000; // 5 segundos
    private Map<UUID, Long> lastCacheUpdate;

    // Radio de búsqueda por defecto
    private double defaultRadius = 100.0;
    // Máximo radio permitido
    private double maxRadius = 500.0;
    // Mapa de permisos para radios personalizados
    private Map<String, Double> permissionRadiusMap;

    public NearManager(NearGUI plugin) {
        this.plugin = plugin;
        this.nearbyPlayersCache = new HashMap<>();
        this.lastCacheUpdate = new HashMap<>();
        this.permissionRadiusMap = new HashMap<>();

        loadConfig();
    }

    /**
     * Carga la configuración del plugin
     */
    public void loadConfig() {
        FileConfiguration config = plugin.getConfigManager().getConfig("config");

        // Cargar radio por defecto y máximo
        this.defaultRadius = config.getDouble("settings.default_radius", 100.0);
        this.maxRadius = config.getDouble("settings.max_radius", 500.0);

        // Cargar permisos de radio
        permissionRadiusMap.clear();
        if (config.contains("permissions.radius")) {
            for (String key : config.getConfigurationSection("permissions.radius").getKeys(false)) {
                double radius = config.getDouble("permissions.radius." + key);
                permissionRadiusMap.put("neargui.radius." + key, radius);
            }
        }
    }

    /**
     * Obtiene el radio máximo permitido para un jugador basado en sus permisos
     *
     * @param player Jugador a comprobar
     * @return Radio máximo permitido
     */
    public double getMaxRadiusForPlayer(Player player) {
        double radius = defaultRadius;

        // Comprobar si el jugador tiene acceso al radio máximo
        if (player.hasPermission("neargui.radius.max")) {
            return maxRadius;
        }

        // Comprobar permisos personalizados
        for (Map.Entry<String, Double> entry : permissionRadiusMap.entrySet()) {
            if (player.hasPermission(entry.getKey()) && entry.getValue() > radius) {
                radius = entry.getValue();
            }
        }

        return Math.min(radius, maxRadius);
    }

    /**
     * Obtiene todos los jugadores cercanos a un jugador específico dentro de un radio determinado.
     *
     * @param player El jugador de referencia
     * @param radius El radio en bloques para buscar jugadores cercanos
     * @return Lista de jugadores dentro del radio especificado
     */
    public List<Player> getNearbyPlayers(Player player, double radius) {
        List<Player> nearbyPlayers = new ArrayList<>();

        if (player == null || !player.isOnline()) {
            return nearbyPlayers;
        }

        Location playerLocation = player.getLocation();

        // Obtenemos todos los jugadores online en el servidor
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Evitamos incluir al propio jugador
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            // Verificamos si están en el mismo mundo
            if (!onlinePlayer.getWorld().equals(player.getWorld())) {
                continue;
            }

            // Calculamos la distancia
            double distance = playerLocation.distance(onlinePlayer.getLocation());

            // Verificamos si está dentro del radio
            if (distance <= radius) {
                nearbyPlayers.add(onlinePlayer);
            }
        }

        return nearbyPlayers;
    }

    /**
     * Obtiene todos los jugadores cercanos con su respectiva distancia
     *
     * @param player El jugador de referencia
     * @param radius El radio en bloques para buscar jugadores cercanos
     * @return Mapa de jugadores y sus distancias al jugador de referencia, ordenado por distancia
     */
    public Map<Player, Double> getNearbyPlayersWithDistance(Player player, double radius) {
        // Usamos LinkedHashMap para mantener el orden de inserción
        Map<Player, Double> nearbyPlayersWithDistance = new LinkedHashMap<>();

        if (player == null || !player.isOnline()) {
            return nearbyPlayersWithDistance;
        }

        Location playerLocation = player.getLocation();
        Map<Player, Double> tempMap = new HashMap<>();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (!onlinePlayer.getWorld().equals(player.getWorld())) {
                continue;
            }

            double distance = playerLocation.distance(onlinePlayer.getLocation());

            if (distance <= radius) {
                tempMap.put(onlinePlayer, distance);
            }
        }

        // Ordenar por distancia
        tempMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> nearbyPlayersWithDistance.put(entry.getKey(), entry.getValue()));

        return nearbyPlayersWithDistance;
    }

    /**
     * Crea un menú con los jugadores cercanos
     *
     * @param player El jugador para quien se crea el menú
     * @param radius El radio de búsqueda
     * @return El menú creado o null si no hay jugadores cercanos
     */
//    public Menu createNearMenu(Player player, double radius) {
//        Map<Player, Double> nearbyPlayers = getNearbyPlayersWithDistance(player, radius);
//
//        if (nearbyPlayers.isEmpty()) {
//            // No hay jugadores cerca, devuelve null
//            return null;
//        }
//
//        // Intentamos usar el constructor de menús desde la configuración
//        Menu menu = plugin.getMenuBuilder().buildMenu("near");
//
//        // Si no existe la configuración del menú, creamos uno por defecto
//        if (menu == null) {
//            menu = new Menu("&bJugadores Cercanos", 4);
//
//            int slot = 0;
//            for (Map.Entry<Player, Double> entry : nearbyPlayers.entrySet()) {
//                Player nearbyPlayer = entry.getKey();
//                double distance = entry.getValue();
//
//                // Crear ítem del jugador
//                MenuItem playerItem = new MenuItem(Material.PLAYER_HEAD)
//                        .setName("&b" + nearbyPlayer.getName())
//                        .setLore(
//                                "&7Distancia: &e" + String.format("%.2f", distance) + " bloques",
//                                "&7Vida: &c" + String.format("%.1f", nearbyPlayer.getHealth()) + "&8/&c" + nearbyPlayer.getHealthScale(),
//                                "",
//                                "&eClick para teletransportarse"
//                        );
//
//                // Configurar cabeza del jugador
//                SkullMeta meta = (SkullMeta) playerItem.getItemStack().getItemMeta();
//                meta.setOwningPlayer(nearbyPlayer);
//                playerItem.getItemStack().setItemMeta(meta);
//
//                // Añadir acción de teletransporte si tiene permiso
//                if (player.hasPermission("neargui.teleport")) {
//                    playerItem.setClickHandler(info -> {
//                        info.getPlayer().teleport(nearbyPlayer.getLocation());
//                        info.getPlayer().closeInventory();
//                        Component message = ColorUtils.translateColors("&aTe has teletransportado a &e" + nearbyPlayer.getName());
//                        info.getPlayer().sendMessage(message);
//                    });
//                }
//
//                menu.setItem(slot, playerItem);
//                slot++;
//
//                // Limitar a los primeros 27 slots (3 filas)
//                if (slot >= 27) break;
//            }
//        } else {
//            // El menú existe en la configuración, pero necesitamos personalizar los ítems
//            // para los jugadores cercanos
//            List<Player> playersList = new ArrayList<>(nearbyPlayers.keySet());
//
//            // Si es un menú de paginación, lo creamos con PaginationMenu
//            PaginationMenu paginationMenu = plugin.getMenuBuilder().buildPaginationMenu("near");
//
//            if (paginationMenu != null) {
//                // Crear ítems para cada jugador
//                for (Map.Entry<Player, Double> entry : nearbyPlayers.entrySet()) {
//                    Player nearbyPlayer = entry.getKey();
//                    double distance = entry.getValue();
//
//                    // Obtener la plantilla del ítem
//                    MenuItem playerItem = new MenuItem(Material.PLAYER_HEAD)
//                            .setName("&b" + nearbyPlayer.getName())
//                            .setLore(
//                                    "&7Distancia: &e" + String.format("%.2f", distance) + " bloques",
//                                    "&7Vida: &c" + String.format("%.1f", nearbyPlayer.getHealth()) + "&8/&c" + nearbyPlayer.getHealthScale()
//                            )
//                            .usePlaceholders(true);
//
//                    // Configurar cabeza del jugador
//                    SkullMeta meta = (SkullMeta) playerItem.getItemStack().getItemMeta();
//                    meta.setOwningPlayer(nearbyPlayer);
//                    playerItem.getItemStack().setItemMeta(meta);
//
//                    // Añadir acción de teletransporte si tiene permiso
//                    if (player.hasPermission("neargui.teleport")) {
//                        playerItem.setClickHandler(info -> {
//                            info.getPlayer().teleport(nearbyPlayer.getLocation());
//                            info.getPlayer().closeInventory();
//                            Component message = ColorUtils.translateColors("&aTe has teletransportado a &e" + nearbyPlayer.getName());
//                            info.getPlayer().sendMessage(message);
//                        });
//                    }
//
//                    paginationMenu.addItem(playerItem);
//                }
//
//                // Devolver el menú de paginación
//                return paginationMenu;
//            } else {
//                // Personalizar manualmente los ítems en el menú
//                int slot = 0;
//                for (Map.Entry<Player, Double> entry : nearbyPlayers.entrySet()) {
//                    Player nearbyPlayer = entry.getKey();
//                    double distance = entry.getValue();
//
//                    // Crear ítem del jugador personalizado
//                    MenuItem playerItem = new MenuItem(Material.PLAYER_HEAD)
//                            .setName("&b" + nearbyPlayer.getName())
//                            .setLore(
//                                    "&7Distancia: &e" + String.format("%.2f", distance) + " bloques",
//                                    "&7Vida: &c" + String.format("%.1f", nearbyPlayer.getHealth()) + "&8/&c" + nearbyPlayer.getHealthScale()
//                            );
//
//                    // Configurar cabeza del jugador
//                    SkullMeta meta = (SkullMeta) playerItem.getItemStack().getItemMeta();
//                    meta.setOwningPlayer(nearbyPlayer);
//                    playerItem.getItemStack().setItemMeta(meta);
//
//                    // Añadir acción de teletransporte si tiene permiso
//                    if (player.hasPermission("neargui.teleport")) {
//                        playerItem.setClickHandler(info -> {
//                            info.getPlayer().teleport(nearbyPlayer.getLocation());
//                            info.getPlayer().closeInventory();
//                            Component message = ColorUtils.translateColors("&aTe has teletransportado a &e" + nearbyPlayer.getName());
//                            info.getPlayer().sendMessage(message);
//                        });
//                    }
//
//                    menu.setItem(slot, playerItem);
//                    slot++;
//
//                    // Limitar a los primeros 27 slots (3 filas)
//                    if (slot >= 27) break;
//                }
//            }
//        }
//
//        return menu;
//    }

    /**
     * Crea un menú paginado con los jugadores cercanos
     *
     * @param player El jugador para quien se crea el menú
     * @param radius El radio de búsqueda
     * @return El menú paginado creado o null si no hay jugadores cercanos
     */
    public PaginationMenu createNearPaginationMenu(Player player, double radius) {
        Map<Player, Double> nearbyPlayers = getNearbyPlayersWithDistance(player, radius);

        if (nearbyPlayers.isEmpty()) {
            // No hay jugadores cerca, devuelve null
            return null;
        }

        // Crear menú de paginación
        PaginationMenu paginationMenu = new PaginationMenu("&bJugadores Cercanos", 4,
                // Slots para los ítems (primeros 27 slots, 3 filas)
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26);

        // Botones de navegación
        MenuItem prevButton = new MenuItem(Material.ARROW)
                .setName("&8« &7Página anterior")
                .setGlowing(true);

        MenuItem nextButton = new MenuItem(Material.ARROW)
                .setName("&7Página siguiente &8»")
                .setGlowing(true);

        paginationMenu.setPreviousPageButton(prevButton, 27);
        paginationMenu.setNextPageButton(nextButton, 35);

        // Filler para los espacios vacíos
        MenuItem fillerItem = new MenuItem(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ");

        paginationMenu.setFillerItem(fillerItem);

        // Crear ítems para cada jugador
        for (Map.Entry<Player, Double> entry : nearbyPlayers.entrySet()) {
            Player nearbyPlayer = entry.getKey();
            double distance = entry.getValue();

            // Crear ítem del jugador
            MenuItem playerItem = new MenuItem(Material.PLAYER_HEAD)
                    .setName("&b" + nearbyPlayer.getName())
                    .setLore(
                            "&7Distancia: &e" + String.format("%.2f", distance) + " bloques",
                            "&7Vida: &c" + String.format("%.1f", nearbyPlayer.getHealth()) + "&8/&c" + nearbyPlayer.getHealthScale(),
                            "",
                            "&eClick para teletransportarse"
                    );

            // Configurar cabeza del jugador
            SkullMeta meta = (SkullMeta) playerItem.getItemStack().getItemMeta();
            meta.setOwningPlayer(nearbyPlayer);
            playerItem.getItemStack().setItemMeta(meta);

            // Añadir acción de teletransporte si tiene permiso
            if (player.hasPermission("neargui.teleport")) {
                final Player targetPlayer = nearbyPlayer; // Variable final para lambda
                playerItem.setClickHandler(info -> {
                    info.getPlayer().teleport(targetPlayer.getLocation());
                    info.getPlayer().closeInventory();
                    Component message = ColorUtils.translateColors("&aTe has teletransportado a &e" + targetPlayer.getName());
                    info.getPlayer().sendMessage(message);
                });
            }

            paginationMenu.addItem(playerItem);
        }

        return paginationMenu;
    }
}