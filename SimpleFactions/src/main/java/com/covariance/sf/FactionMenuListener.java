package com.convariance.sf;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FactionMenuListener implements Listener {

    private static final String MAIN_TITLE = "SimpleFactions Menu";
    private static final String INVITE_TITLE = "Invite Player";
    private static final String TRUST_TITLE = "Trust Player";
    private static final String WAR_TITLE = "Choose War Target";

    private final SimpleFactionsPlugin plugin;

    public FactionMenuListener(SimpleFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_TITLE);
        inv.setItem(10, item(Material.GRASS_BLOCK, "§aClaim Chunk", "§7Claim this chunk"));
        inv.setItem(11, item(Material.BARRIER, "§cUnclaim Chunk", "§7Unclaim this chunk"));
        inv.setItem(12, item(Material.MAP, "§eShow Map", "§7View nearby claims"));
        inv.setItem(13, item(Material.BOOK, "§bFaction List", "§7See all factions"));
        inv.setItem(14, item(Material.PLAYER_HEAD, "§dInvite Player", "§7Open player selector"));
        inv.setItem(15, item(Material.IRON_SWORD, "§4Declare War", "§7Open faction selector"));
        inv.setItem(16, item(Material.CHEST, "§3Trust Player", "§7Open player selector"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (title == null) return;

        if (title.equals(MAIN_TITLE)) {
            event.setCancelled(true);
            handleMainClick(player, event.getCurrentItem());
            return;
        }

        if (title.equals(INVITE_TITLE)) {
            event.setCancelled(true);
            handlePlayerSelectorClick(player, event.getCurrentItem(), "invite");
            return;
        }

        if (title.equals(TRUST_TITLE)) {
            event.setCancelled(true);
            handlePlayerSelectorClick(player, event.getCurrentItem(), "trust");
            return;
        }

        if (title.equals(WAR_TITLE)) {
            event.setCancelled(true);
            handleFactionSelectorClick(player, event.getCurrentItem());
        }
    }

    private void handleMainClick(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        switch (item.getType()) {
            case GRASS_BLOCK -> player.performCommand("f claim");
            case BARRIER -> player.performCommand("f unclaim");
            case MAP -> player.performCommand("f map");
            case BOOK -> player.performCommand("f list");
            case PLAYER_HEAD -> openPlayerSelector(player, INVITE_TITLE);
            case CHEST -> openPlayerSelector(player, TRUST_TITLE);
            case IRON_SWORD -> openFactionSelector(player);
            default -> { }
        }
    }

    private void openPlayerSelector(Player player, String title) {
        Inventory inv = Bukkit.createInventory(null, 54, title);
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) continue;
            if (slot >= inv.getSize()) break;
            inv.setItem(slot++, item(Material.PLAYER_HEAD, "§f" + online.getName(), "§7Click to select"));
        }
        player.openInventory(inv);
    }

    private void openFactionSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, WAR_TITLE);
        FileConfiguration cfg = plugin.getFactionsConfig();
        if (!cfg.contains("factions")) {
            player.sendMessage("§7No factions exist.");
            return;
        }

        String ownFaction = getPlayerFaction(player.getUniqueId(), cfg);
        int slot = 0;
        for (String faction : cfg.getConfigurationSection("factions").getKeys(false)) {
            if (faction.equalsIgnoreCase(ownFaction)) continue;
            if (slot >= inv.getSize()) break;
            inv.setItem(slot++, item(Material.IRON_SWORD, "§c" + faction, "§7Click to declare war"));
        }
        player.openInventory(inv);
    }

    private void handlePlayerSelectorClick(Player player, ItemStack item, String action) {
        if (item == null || item.getType().isAir()) return;
        String target = strip(item);
        if (target.isBlank()) return;
        player.closeInventory();
        player.performCommand("f " + action + " " + target);
    }

    private void handleFactionSelectorClick(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        String targetFaction = strip(item);
        if (targetFaction.isBlank()) return;
        player.closeInventory();
        player.performCommand("f war " + targetFaction);
    }

    private String getPlayerFaction(UUID uuid, FileConfiguration factions) {
        if (!factions.contains("factions")) return null;
        for (String faction : factions.getConfigurationSection("factions").getKeys(false)) {
            if (factions.getStringList("factions." + faction + ".members").contains(uuid.toString())) return faction;
        }
        return null;
    }

    private String strip(ItemStack item) {
        if (!item.hasItemMeta()) return "";
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return "";
        return meta.getDisplayName().replaceAll("§.", "").trim();
    }

    private static ItemStack item(Material mat, String name, String loreLine) {
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(loreLine);
            meta.setLore(lore);
            is.setItemMeta(meta);
        }
        return is;
    }
}
