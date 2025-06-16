package net.exylia.nearGUI.managers;

import net.exylia.commons.menu.MenuBuilder;
import net.exylia.commons.menu.MenuItem;
import net.exylia.commons.menu.PaginationMenu;
import net.exylia.commons.utils.MessageUtils;
import net.exylia.nearGUI.NearGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static net.exylia.commons.utils.MenuUtils.parseSlots;

public class NearManager {
    private final NearGUI plugin;
    private FileConfiguration menuConfig;
    private double defaultRadius = 100.0;
    private double maxRadius = 500.0;
    private final Map<String, Double> permissionRadiusMap;

    public NearManager(NearGUI plugin) {
        this.plugin = plugin;
        this.menuConfig = plugin.getConfigManager().getConfig("menus/near");
        this.permissionRadiusMap = new HashMap<>();
        loadConfig();
    }

    public void loadConfig() {
        this.menuConfig = plugin.getConfigManager().getConfig("menus/near");

        // El resto de tu código actual
        FileConfiguration config = plugin.getConfigManager().getConfig("config");
        this.defaultRadius = config.getDouble("settings.default_radius", 100.0);
        this.maxRadius = config.getDouble("settings.max_radius", 500.0);

        permissionRadiusMap.clear();
        if (config.contains("permissions.radius")) {
            for (String key : config.getConfigurationSection("permissions.radius").getKeys(false)) {
                double radius = config.getDouble("permissions.radius." + key);
                permissionRadiusMap.put("neargui.radius." + key, radius);
            }
        }
    }

    public double getMaxRadiusForPlayer(Player player) {
        double radius = defaultRadius;
        for (Map.Entry<String, Double> entry : permissionRadiusMap.entrySet()) {
            if (player.hasPermission(entry.getKey()) && entry.getValue() > radius) {
                radius = entry.getValue();
            }
        }
        return Math.min(radius, maxRadius);
    }

    public void openNearInventory(Player player, double radius) {
        PaginationMenu nearMenu = createNearPaginationMenu(player, radius);

        if (nearMenu != null) {
            nearMenu.open(player);
        } else {
            MessageUtils.sendMessageAsync(player, (plugin.getConfigManager().getMessage("near.no-players")));
        }
    }

    private PaginationMenu createNearPaginationMenu(Player player, double radius) {
        Map<Player, Double> nearbyPlayers = getNearbyPlayersWithDistance(player, radius);

        if (nearbyPlayers.isEmpty()) {
            return null;
        }

        String playerSlotsString = menuConfig.getString("players.slots", "0-26");
        int rows = menuConfig.getInt("rows", 3);
        int[] playerSlots = parseSlots(playerSlotsString, rows);

        MenuBuilder menuBuilder = new MenuBuilder(plugin);
        PaginationMenu nearMenu = menuBuilder.buildPaginationMenu(menuConfig, player, playerSlots);

        addPlayerItems(nearMenu, nearbyPlayers);

        return nearMenu;
    }

    public Map<Player, Double> getNearbyPlayersWithDistance(Player player, double radius) {
        Map<Player, Double> sortedPlayers = new LinkedHashMap<>();

        if (player == null || !player.isOnline()) {
            return sortedPlayers;
        }

        Location playerLocation = player.getLocation();

        Map<Player, Double> tempMap = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .filter(p -> p.getWorld().equals(player.getWorld()))
                .collect(Collectors.toMap(
                        p -> p,
                        p -> playerLocation.distance(p.getLocation()),
                        (v1, v2) -> v1,
                        HashMap::new
                ));

        tempMap.entrySet().stream()
                .filter(entry -> entry.getValue() <= radius)
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(entry -> sortedPlayers.put(entry.getKey(), entry.getValue()));

        return sortedPlayers;
    }

    private void addPlayerItems(PaginationMenu paginationMenu, Map<Player, Double> nearbyPlayers) {
        boolean playersUsePlaceholders = menuConfig.getBoolean("players.use_placeholders", true);
        boolean playersDynamicUpdate = menuConfig.getBoolean("players.dynamic_update", true);
        int playersUpdateInterval = menuConfig.getInt("players.update_interval", 100);
        String playerItemName = menuConfig.getString("players.name", "&b%player_name%");
        List<String> playerItemLore = menuConfig.getStringList("players.lore");
        String playerMaterial = menuConfig.getString("players.material", "PLAYER_HEAD");
        boolean hideAttributes = menuConfig.getBoolean("players.hide_attributes", false);

        for (Map.Entry<Player, Double> entry : nearbyPlayers.entrySet()) {
            Player nearbyPlayer = entry.getKey();
            double distance = entry.getValue();

            MenuItem playerItem = createPlayerItem(nearbyPlayer, distance, playerMaterial,
                    playerItemName, playerItemLore,
                    playersUsePlaceholders, playersDynamicUpdate,
                    playersUpdateInterval, hideAttributes);

            paginationMenu.addItem(playerItem);
        }
    }

    private MenuItem createPlayerItem(Player player, double distance, String material,
                                      String itemName, List<String> itemLore,
                                      boolean usePlaceholders, boolean dynamicUpdate,
                                      int updateInterval, boolean hideAttributes) {

        MenuItem playerItem = new MenuItem(material);

        playerItem.usePlaceholders(usePlaceholders);
        playerItem.setDynamicUpdate(dynamicUpdate);
        playerItem.setUpdateInterval(updateInterval);
        playerItem.setPlaceholderPlayer(player);
        playerItem.setName(itemName);
        if (hideAttributes) {
            playerItem.hideAllAttributes();
        }

        List<String> formattedLore = new ArrayList<>();
        for (String loreLine : itemLore) {
            String line = loreLine.replace("%distance%", String.format("%.2f", distance));
            formattedLore.add(line);
        }
        playerItem.setLoreFromList(formattedLore);

        playerItem.applySkullOwner(player);

        return playerItem;
    }
}