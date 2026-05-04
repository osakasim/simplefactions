package com.convariance.sf;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionProtectionListener implements Listener {

    private final SimpleFactionsPlugin plugin;
    private final WarManager warManager;

    public ExplosionProtectionListener(SimpleFactionsPlugin plugin, WarManager warManager) {
        this.plugin = plugin;
        this.warManager = warManager;
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        FileConfiguration factions = plugin.getFactionsConfig();

        event.blockList().removeIf(block -> {
            Chunk chunk = block.getChunk();
            String world = chunk.getWorld().getName();
            String key = chunk.getX() + "," + chunk.getZ();

            if (!factions.contains("claims." + world + "." + key)) {
                return false; // wilderness → allow explosion
            }

            String owningFaction = factions.getString("claims." + world + "." + key);
            return owningFaction != null; // protect unless war overrides later
        });
    }
}
