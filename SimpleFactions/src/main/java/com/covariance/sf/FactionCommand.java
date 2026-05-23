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

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "menu" -> showMenuStub(player);
            case "map" -> showMap(player, factions);
            case "list" -> listFactions(player, factions);
            case "info" -> showInfo(player, factions, args);
            case "trust" -> trust(player, factions, uuid, args);
            case "untrust" -> untrust(player, factions, uuid, args);
            case "create" -> createFaction(player, factions, uuid, args);
            case "invite" -> invite(player, factions, uuid, args);
            case "accept" -> accept(player, factions, uuid);
            case "leave" -> leave(player, factions, uuid);
            case "kick" -> kick(player, factions, uuid, args);
            case "disband" -> disband(player, factions, uuid);
            case "claim" -> claim(player, factions, uuid);
            case "unclaim" -> unclaim(player, factions, uuid);
            case "war" -> war(player, factions, uuid, args);
            case "ally" -> ally(player, factions, uuid, args);
            case "neutral" -> neutral(player, factions, uuid, args);
            default -> player.sendMessage("§cUnknown subcommand. Use /f help");
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage("§6/f help, menu, map, list, info <faction>");
        p.sendMessage("§6/f create, invite, accept, leave, kick, disband");
        p.sendMessage("§6/f claim, unclaim, trust <player>, untrust <player>");
        p.sendMessage("§6/f war <faction>, ally <faction>, neutral <faction>");
    }

    private void showMenuStub(Player p) {
        FactionMenuListener.openMainMenu(p);
    }

    private boolean isOwner(UUID uuid, String faction, FileConfiguration factions) {
        return uuid.toString().equals(factions.getString("factions." + faction + ".owner"));
    }

    private boolean isTrusted(String faction, UUID uuid, FileConfiguration factions) {
        return factions.getStringList("factions." + faction + ".trusted").contains(uuid.toString());
    }

    private void trust(Player player, FileConfiguration factions, UUID uuid, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUsage: /f trust <player>"); return; }
        String faction = getPlayerFaction(uuid, factions);
        if (faction == null || !isOwner(uuid, faction, factions)) { player.sendMessage("§cOwner only."); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage("§cPlayer not found."); return; }
        List<String> trusted = factions.getStringList("factions." + faction + ".trusted");
        if (!trusted.contains(target.getUniqueId().toString())) trusted.add(target.getUniqueId().toString());
        factions.set("factions." + faction + ".trusted", trusted);
        plugin.saveFactionsFile();
        player.sendMessage("§aTrusted: " + target.getName());
    }

    private void untrust(Player player, FileConfiguration factions, UUID uuid, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUsage: /f untrust <player>"); return; }
        String faction = getPlayerFaction(uuid, factions);
        if (faction == null || !isOwner(uuid, faction, factions)) { player.sendMessage("§cOwner only."); return; }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { player.sendMessage("§cPlayer not found."); return; }
        List<String> trusted = factions.getStringList("factions." + faction + ".trusted");
        trusted.remove(target.getUniqueId().toString());
        factions.set("factions." + faction + ".trusted", trusted);
        plugin.saveFactionsFile();
        player.sendMessage("§eRemoved trust: " + target.getName());
    }

    private void createFaction(Player player, FileConfiguration factions, UUID uuid, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUsage: /f create <name>"); return; }
        if (getPlayerFaction(uuid, factions) != null) { player.sendMessage("§cYou are already in a faction."); return; }
        String name = args[1];
        if (factions.contains("factions." + name)) { player.sendMessage("§cThat faction already exists."); return; }
        factions.set("factions." + name + ".owner", uuid.toString());
        factions.set("factions." + name + ".members", new ArrayList<>(List.of(uuid.toString())));
        factions.set("factions." + name + ".trusted", new ArrayList<>());
        plugin.saveFactionsFile();
        player.sendMessage("§aFaction " + name + " created!");
    }

    private void invite(Player player, FileConfiguration factions, UUID uuid, String[] args) { if (args.length<2) return; String faction=getPlayerFaction(uuid,factions); if (faction==null||!isOwner(uuid,faction,factions)) return; Player t=Bukkit.getPlayer(args[1]); if (t==null||t.getUniqueId().equals(uuid)) return; invites.put(t.getUniqueId(),faction); t.sendMessage("§eInvited to §a"+faction+"§e. /f accept"); player.sendMessage("§aInvite sent."); }
    private void accept(Player player, FileConfiguration factions, UUID uuid) { if (!invites.containsKey(uuid)||getPlayerFaction(uuid,factions)!=null) return; String faction=invites.remove(uuid); List<String> m=factions.getStringList("factions."+faction+".members"); m.add(uuid.toString()); factions.set("factions."+faction+".members",m); plugin.saveFactionsFile(); player.sendMessage("§aJoined "+faction); }
    private void leave(Player player, FileConfiguration factions, UUID uuid) { String faction=getPlayerFaction(uuid,factions); if (faction==null||isOwner(uuid,faction,factions)) return; List<String> m=factions.getStringList("factions."+faction+".members"); m.remove(uuid.toString()); factions.set("factions."+faction+".members",m); plugin.saveFactionsFile(); player.sendMessage("§aLeft faction."); }
    private void kick(Player player, FileConfiguration factions, UUID uuid, String[] args) { if (args.length<2) return; String faction=getPlayerFaction(uuid,factions); if (faction==null||!isOwner(uuid,faction,factions)) return; Player t=Bukkit.getPlayer(args[1]); if (t==null||t.getUniqueId().equals(uuid)) return; List<String> m=factions.getStringList("factions."+faction+".members"); if (m.remove(t.getUniqueId().toString())) { factions.set("factions."+faction+".members",m); plugin.saveFactionsFile(); player.sendMessage("§aKicked."); } }
    private void disband(Player player, FileConfiguration factions, UUID uuid) { String faction=getPlayerFaction(uuid,factions); if (faction==null||!isOwner(uuid,faction,factions)) return; removeFactionClaims(faction,factions); factions.set("factions."+faction,null); factions.set("wars."+faction,null); factions.set("relations.allies."+faction,null); plugin.saveFactionsFile(); Bukkit.broadcastMessage("§cFaction disbanded: §4"+faction); }

    private void claim(Player player, FileConfiguration factions, UUID uuid) {
        String faction = getPlayerFaction(uuid, factions); if (faction == null) return;
        Chunk chunk = player.getLocation().getChunk(); String world = chunk.getWorld().getName(); String key = chunk.getX()+","+chunk.getZ();
        int minDist = plugin.getConfig().getInt("minClaimDistanceFromSpawnChunks", 30);
        Chunk spawnChunk = player.getWorld().getSpawnLocation().getChunk();
        int dx=Math.abs(chunk.getX()-spawnChunk.getX()), dz=Math.abs(chunk.getZ()-spawnChunk.getZ());
        if (dx < minDist && dz < minDist) { player.sendMessage("§cToo close to spawn."); return; }
        if (factions.contains("claims."+world+"."+key)) return;
        int members=factions.getStringList("factions."+faction+".members").size();
        int maxClaims=members*plugin.getConfig().getInt("chunksPerMember",10);
        int currentClaims=getFactionClaimCount(faction,factions);
        if (currentClaims>=maxClaims) return;
        factions.set("claims."+world+"."+key,faction); plugin.saveFactionsFile(); player.sendMessage("§aClaimed.");
    }

    private void unclaim(Player player, FileConfiguration factions, UUID uuid) { String faction=getPlayerFaction(uuid,factions); if (faction==null||!isOwner(uuid,faction,factions)) return; Chunk c=player.getLocation().getChunk(); String w=c.getWorld().getName(); String key=c.getX()+","+c.getZ(); if (faction.equals(factions.getString("claims."+w+"."+key))) { factions.set("claims."+w+"."+key,null); plugin.saveFactionsFile(); player.sendMessage("§aUnclaimed."); } }
    private void war(Player p, FileConfiguration factions, UUID uuid, String[] args){ if(args.length<2)return; String a=getPlayerFaction(uuid,factions), d=args[1]; if(a==null||!factions.contains("factions."+d)||a.equals(d)||!isOwner(uuid,a,factions)) return; warManager.declareWar(a,d); p.sendMessage("§cWar declared on "+d);}    
    private void ally(Player p, FileConfiguration factions, UUID uuid, String[] args){ if(args.length<2)return; String a=getPlayerFaction(uuid,factions), b=args[1]; if(a==null||!factions.contains("factions."+b)||a.equals(b)||!isOwner(uuid,a,factions)) return; warManager.ally(a,b); p.sendMessage("§aAlliance formed with "+b);}    
    private void neutral(Player p, FileConfiguration factions, UUID uuid, String[] args){ if(args.length<2)return; String a=getPlayerFaction(uuid,factions), b=args[1]; if(a==null||!factions.contains("factions."+b)||a.equals(b)||!isOwner(uuid,a,factions)) return; warManager.neutral(a,b); p.sendMessage("§eRelation set to neutral with "+b);}    

    private void listFactions(Player p, FileConfiguration f){ if(!f.contains("factions")){p.sendMessage("§7No factions.");return;} p.sendMessage("§6Factions: §f"+String.join(", ",f.getConfigurationSection("factions").getKeys(false))); }
    private void showInfo(Player p, FileConfiguration f, String[] args){ if(args.length<2){p.sendMessage("§cUsage: /f info <faction>");return;} String fac=args[1]; if(!f.contains("factions."+fac)){p.sendMessage("§cNot found.");return;} int members=f.getStringList("factions."+fac+".members").size(); int claims=getFactionClaimCount(fac,f); int trusted=f.getStringList("factions."+fac+".trusted").size(); p.sendMessage("§6"+fac+" §7Members: §f"+members+" §7Trusted: §f"+trusted+" §7Claims: §f"+claims); }

    private String getPlayerFaction(UUID uuid, FileConfiguration factions) { if (!factions.contains("factions")) return null; for (String faction : factions.getConfigurationSection("factions").getKeys(false)) { if (factions.getStringList("factions." + faction + ".members").contains(uuid.toString())) return faction; } return null; }
    private int getFactionClaimCount(String faction, FileConfiguration factions) { if (!factions.contains("claims")) return 0; int c=0; for (String w: factions.getConfigurationSection("claims").getKeys(false)) for (String k: factions.getConfigurationSection("claims."+w).getKeys(false)) if (faction.equals(factions.getString("claims."+w+"."+k))) c++; return c; }
    private void removeFactionClaims(String faction, FileConfiguration factions) { if (!factions.contains("claims")) return; for (String w: factions.getConfigurationSection("claims").getKeys(false)) for (String k: new ArrayList<>(factions.getConfigurationSection("claims."+w).getKeys(false))) if (faction.equals(factions.getString("claims."+w+"."+k))) factions.set("claims."+w+"."+k,null); }

    private void showMap(Player player, FileConfiguration factions) {
        Chunk center = player.getLocation().getChunk(); String world = player.getWorld().getName(); String pf = getPlayerFaction(player.getUniqueId(), factions);
        player.sendMessage("§6=== Faction Map ===");
        for (int z=4; z>=-4; z--) { StringBuilder line=new StringBuilder(); for (int x=-4; x<=4; x++) { int cx=center.getX()+x, cz=center.getZ()+z; String key=cx+","+cz; if (x==0&&z==0){line.append("§e✦ ");continue;} String owner=factions.getString("claims."+world+"."+key); if(owner==null) line.append("§7■ "); else if(owner.equals(pf)) line.append("§a■ "); else if(warManager.isAlly(pf, owner)) line.append("§b■ "); else if(warManager.isAtWar(pf, owner)) line.append("§4■ "); else line.append("§c■ "); } player.sendMessage(line.toString()); }
        player.sendMessage("§7■ Wild  §a■ Yours  §b■ Ally §c■ Other §4■ War §e✦ You");
    }
}
