package com.convariance.sf;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionCommand implements CommandExecutor {

    private final SimpleFactionsPlugin plugin;
    private final WarManager warManager;
    private final Map<UUID, String> invites = new HashMap<>();

    public FactionCommand(SimpleFactionsPlugin plugin, WarManager warManager) {
        this.plugin = plugin;
        this.warManager = warManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        FileConfiguration factions = plugin.getFactionsConfig();
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage("§cUsage: /f create|invite|accept|leave|kick|disband|claim|war|map");
            return true;
        }

        /* ================= MAP ================= */
        if (args[0].equalsIgnoreCase("map")) {
            showMap(player, factions);
            return true;
        }

        /* ================= CREATE ================= */
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /f create <name>");
                return true;
            }

            if (getPlayerFaction(uuid, factions) != null) {
                player.sendMessage("§cYou are already in a faction.");
                return true;
            }

            String name = args[1];

            if (factions.contains("factions." + name)) {
                player.sendMessage("§cThat faction already exists.");
                return true;
            }

            factions.set("factions." + name + ".owner", uuid.toString());
            factions.set("factions." + name + ".members",
                    new ArrayList<>(List.of(uuid.toString())));
            plugin.saveFactionsFile();

            player.sendMessage("§aFaction " + name + " created!");
            return true;
        }

        /* ================= INVITE ================= */
        if (args[0].equalsIgnoreCase("invite")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /f invite <player>");
                return true;
            }

            String faction = getPlayerFaction(uuid, factions);
            if (faction == null) {
                player.sendMessage("§cYou are not in a faction.");
                return true;
            }

            if (!uuid.toString().equals(
                    factions.getString("factions." + faction + ".owner"))) {
                player.sendMessage("§cOnly the owner can invite.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            if (target.getUniqueId().equals(uuid)) {
                player.sendMessage("§cYou cannot invite yourself.");
                return true;
            }

            invites.put(target.getUniqueId(), faction);
            target.sendMessage("§eYou have been invited to join §a" + faction);
            target.sendMessage("§7Type §f/f accept §7to join.");
            player.sendMessage("§aInvite sent.");
            return true;
        }

        /* ================= ACCEPT ================= */
        if (args[0].equalsIgnoreCase("accept")) {
            if (!invites.containsKey(uuid)) {
                player.sendMessage("§cYou have no invites.");
                return true;
            }

            if (getPlayerFaction(uuid, factions) != null) {
                player.sendMessage("§cYou are already in a faction.");
                return true;
            }

            String faction = invites.remove(uuid);
            List<String> members =
                    factions.getStringList("factions." + faction + ".members");
            members.add(uuid.toString());
            factions.set("factions." + faction + ".members", members);
            plugin.saveFactionsFile();

            player.sendMessage("§aYou joined faction " + faction + "!");
            return true;
        }

        /* ================= LEAVE ================= */
        if (args[0].equalsIgnoreCase("leave")) {
            String faction = getPlayerFaction(uuid, factions);
            if (faction == null) {
                player.sendMessage("§cYou are not in a faction.");
                return true;
            }

            String owner = factions.getString("factions." + faction + ".owner");
            if (uuid.toString().equals(owner)) {
                player.sendMessage("§cOwner must disband the faction.");
                return true;
            }

            List<String> members = factions.getStringList("factions." + faction + ".members");
            members.remove(uuid.toString());
            factions.set("factions." + faction + ".members", members);
            plugin.saveFactionsFile();

            player.sendMessage("§aYou left faction " + faction + ".");
            return true;
        }

        /* ================= KICK ================= */
        if (args[0].equalsIgnoreCase("kick")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /f kick <player>");
                return true;
            }

            String faction = getPlayerFaction(uuid, factions);
            if (faction == null) {
                player.sendMessage("§cYou are not in a faction.");
                return true;
            }

            if (!uuid.toString().equals(
                    factions.getString("factions." + faction + ".owner"))) {
                player.sendMessage("§cOnly the owner can kick.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            if (target.getUniqueId().equals(uuid)) {
                player.sendMessage("§cYou cannot kick yourself.");
                return true;
            }

            List<String> members = factions.getStringList("factions." + faction + ".members");
            if (!members.remove(target.getUniqueId().toString())) {
                player.sendMessage("§cThat player is not in your faction.");
                return true;
            }

            factions.set("factions." + faction + ".members", members);
            plugin.saveFactionsFile();

            player.sendMessage("§aPlayer kicked.");
            target.sendMessage("§cYou were kicked from faction " + faction + ".");
            return true;
        }

        /* ================= DISBAND ================= */
        if (args[0].equalsIgnoreCase("disband")) {
            String faction = getPlayerFaction(uuid, factions);
            if (faction == null) {
                player.sendMessage("§cYou are not in a faction.");
                return true;
            }

            if (!uuid.toString().equals(
                    factions.getString("factions." + faction + ".owner"))) {
                player.sendMessage("§cOnly the owner can disband.");
                return true;
            }

            removeFactionClaims(faction, factions);
            factions.set("factions." + faction, null);
            factions.set("wars." + faction, null);
            plugin.saveFactionsFile();

            Bukkit.broadcastMessage("§cFaction §4" + faction + " §chas been disbanded!");
            return true;
        }

        /* ================= CLAIM ================= */
        if (args[0].equalsIgnoreCase("claim")) {
            String faction = getPlayerFaction(uuid, factions);
            if (faction == null) {
                player.sendMessage("§cYou are not in a faction.");
                return true;
            }

            Chunk chunk = player.getLocation().getChunk();
            String world = chunk.getWorld().getName();
            String key = chunk.getX() + "," + chunk.getZ();
			
			// ---- SPAWN DISTANCE CHECK (30 chunks) ----
Chunk spawnChunk = player.getWorld().getSpawnLocation().getChunk();

int dx = Math.abs(chunk.getX() - spawnChunk.getX());
int dz = Math.abs(chunk.getZ() - spawnChunk.getZ());

if (dx < 30 && dz < 30) {
    player.sendMessage("§cYou cannot claim within §e30 chunks §cof spawn.");
    return true;
}


            if (factions.contains("claims." + world + "." + key)) {
                player.sendMessage("§cThis chunk is already claimed.");
                return true;
            }

            int members = factions.getStringList("factions." + faction + ".members").size();
            int maxClaims = members * 10;
            int currentClaims = getFactionClaimCount(faction, factions);

            if (currentClaims >= maxClaims) {
                player.sendMessage("§cClaim limit reached (§e" +
                        currentClaims + "/" + maxClaims + "§c).");
                return true;
            }

            factions.set("claims." + world + "." + key, faction);
            plugin.saveFactionsFile();

            player.sendMessage("§aChunk claimed (§e" +
                    (currentClaims + 1) + "/" + maxClaims + "§a)");
            return true;
        }

/* ================= UNCLAIM ================= */
if (args[0].equalsIgnoreCase("unclaim")) {

    String faction = getPlayerFaction(uuid, factions);
    if (faction == null) {
        player.sendMessage("§cYou are not in a faction.");
        return true;
    }

    // Owner-only (logical)
    if (!uuid.toString().equals(
            factions.getString("factions." + faction + ".owner"))) {
        player.sendMessage("§cOnly the faction owner can unclaim land.");
        return true;
    }

    Chunk chunk = player.getLocation().getChunk();
    String world = chunk.getWorld().getName();
    String key = chunk.getX() + "," + chunk.getZ();

    if (!factions.contains("claims." + world + "." + key)) {
        player.sendMessage("§cThis chunk is not claimed.");
        return true;
    }

    String ownerFaction = factions.getString("claims." + world + "." + key);
    if (!faction.equals(ownerFaction)) {
        player.sendMessage("§cYou do not own this chunk.");
        return true;
    }

    factions.set("claims." + world + "." + key, null);
    plugin.saveFactionsFile();

    player.sendMessage("§aChunk unclaimed.");
    return true;
}

        /* ================= WAR ================= */
        if (args[0].equalsIgnoreCase("war")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /f war <faction>");
                return true;
            }

            String attacker = getPlayerFaction(uuid, factions);
            String defender = args[1];

            if (attacker == null) {
                player.sendMessage("§cYou are not in a faction.");
                return true;
            }

            if (!factions.contains("factions." + defender)) {
                player.sendMessage("§cFaction does not exist.");
                return true;
            }

            if (attacker.equals(defender)) {
                player.sendMessage("§cYou cannot declare war on yourself.");
                return true;
            }

            if (!uuid.toString().equals(
                    factions.getString("factions." + attacker + ".owner"))) {
                player.sendMessage("§cOnly the owner can declare war.");
                return true;
            }

            if (warManager.isAtWar(attacker, defender)) {
                player.sendMessage("§cAlready at war.");
                return true;
            }

            warManager.declareWar(attacker, defender);
            player.sendMessage("§cWar declared on §4" + defender);
            return true;
        }

        player.sendMessage("§cUnknown command.");
        return true;
    }

    /* ================= HELPERS ================= */

    private String getPlayerFaction(UUID uuid, FileConfiguration factions) {
        if (!factions.contains("factions")) return null;
        for (String faction : factions.getConfigurationSection("factions").getKeys(false)) {
            if (factions.getStringList(
                    "factions." + faction + ".members").contains(uuid.toString())) {
                return faction;
            }
        }
        return null;
    }

    private int getFactionClaimCount(String faction, FileConfiguration factions) {
        if (!factions.contains("claims")) return 0;

        int count = 0;
        for (String world : factions.getConfigurationSection("claims").getKeys(false)) {
            for (String key : factions.getConfigurationSection("claims." + world).getKeys(false)) {
                if (faction.equals(factions.getString("claims." + world + "." + key))) {
                    count++;
                }
            }
        }
        return count;
    }

    private void removeFactionClaims(String faction, FileConfiguration factions) {
        if (!factions.contains("claims")) return;

        for (String world : factions.getConfigurationSection("claims").getKeys(false)) {
            for (String key :
                    new ArrayList<>(factions.getConfigurationSection("claims." + world).getKeys(false))) {
                if (faction.equals(factions.getString("claims." + world + "." + key))) {
                    factions.set("claims." + world + "." + key, null);
                }
            }
        }
    }

    private void showMap(Player player, FileConfiguration factions) {
        Chunk center = player.getLocation().getChunk();
        String world = player.getWorld().getName();
        String playerFaction = getPlayerFaction(player.getUniqueId(), factions);

        player.sendMessage("§6=== Faction Map ===");

        for (int z = 4; z >= -4; z--) {
            StringBuilder line = new StringBuilder();
            for (int x = -4; x <= 4; x++) {
                int cx = center.getX() + x;
                int cz = center.getZ() + z;
                String key = cx + "," + cz;

                if (x == 0 && z == 0) {
                    line.append("§e✦ ");
                    continue;
                }

                String owner = factions.getString("claims." + world + "." + key);
                if (owner == null) {
                    line.append("§7■ ");
                } else if (owner.equals(playerFaction)) {
                    line.append("§a■ ");
                } else {
                    line.append("§c■ ");
                }
            }
            player.sendMessage(line.toString());
        }

        player.sendMessage("§7■ Wilderness  §a■ Yours  §c■ Enemy  §e✦ You");
    }
}


//als adam grub und eva spann, vo war denn da der edelmann?