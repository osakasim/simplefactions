package com.convariance.sf;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ClaimProtectionListener implements Listener {

    private final SimpleFactionsPlugin plugin;
    private final WarManager warManager;

    public ClaimProtectionListener(SimpleFactionsPlugin plugin, WarManager warManager) {
        this.plugin = plugin;
        this.warManager = warManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event.getBlock().getChunk(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event.getBlock().getChunk(), event);
    }

    private void handle(Player player, Chunk chunk, org.bukkit.event.Cancellable event) {

        FileConfiguration factions = plugin.getFactionsConfig();

        String world = chunk.getWorld().getName();
        String key = chunk.getX() + "," + chunk.getZ();

        // Unclaimed land
        if (!factions.contains("claims." + world + "." + key)) {
            return;
        }

        String owningFaction = factions.getString("claims." + world + "." + key);
        String playerFaction = getPlayerFaction(player, factions);

        // Admin bypass
        if (player.hasPermission("simplefactions.admin")) {
            return;
        }

        // Same faction → allow
        if (playerFaction != null && playerFaction.equals(owningFaction)) {
            return;
        }

        // War check → allow grief
        if (playerFaction != null && warManager.isAtWar(playerFaction, owningFaction)) {
            return;
        }

        // Otherwise: deny
        event.setCancelled(true);
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(
                        ChatColor.RED + "You are in " + owningFaction + " territory"
                )
        );
    }

    private String getPlayerFaction(Player player, FileConfiguration factions) {
        if (!factions.contains("factions")) return null;

        for (String faction : factions.getConfigurationSection("factions").getKeys(false)) {
            if (factions.getStringList("factions." + faction + ".members")
                    .contains(player.getUniqueId().toString())) {
                return faction;
            }
        }
        return null;
    }
}

// agghhhhhhhhhhh