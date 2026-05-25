package com.convariance.sf;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClaimBorderListener implements Listener {

    private final SimpleFactionsPlugin plugin;
    private final WarManager warManager;

    public ClaimBorderListener(SimpleFactionsPlugin plugin, WarManager warManager) {
        this.plugin = plugin;
        this.warManager = warManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;

        Player player = event.getPlayer();
        Chunk chunk = event.getTo().getChunk();
        FileConfiguration factions = plugin.getFactionsConfig();

        String world = chunk.getWorld().getName();
        String key = chunk.getX() + "," + chunk.getZ();

        String owner = factions.getString("claims." + world + "." + key);
        String playerFaction = getPlayerFaction(player, factions);

        String msg;
        if (owner == null) {
            msg = color(plugin.getMessagesConfig().getString("messages.actionbar.territoryWilderness", "&7Wilderness"));
        } else if (owner.equals(playerFaction)) {
            msg = color(plugin.getMessagesConfig().getString("messages.actionbar.territoryOwn", "&aYour Territory"));
        } else if (playerFaction != null && warManager.isAlly(playerFaction, owner)) {
            msg = color(plugin.getMessagesConfig().getString("messages.actionbar.territoryAlly", "&bAlly Territory"));
        } else if (playerFaction != null && warManager.isAtWar(playerFaction, owner)) {
            msg = color(plugin.getMessagesConfig().getString("messages.actionbar.territoryEnemyWar", "&4Enemy Territory (WAR)"));
        } else {
            msg = color(plugin.getMessagesConfig().getString("messages.actionbar.territoryEnemy", "&cEnemy Territory"));
        }

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(msg)
        );
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
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

