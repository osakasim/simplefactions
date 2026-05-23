package com.convariance.sf;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class WarManager {

    private final SimpleFactionsPlugin plugin;

    public WarManager(SimpleFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAtWar(String f1, String f2) {
        if (f1 == null || f2 == null) return false;
        FileConfiguration cfg = plugin.getFactionsConfig();
        return cfg.getStringList("wars." + f1).contains(f2);
    }

    public void declareWar(String f1, String f2) {
        if (f1 == null || f2 == null || f1.equals(f2)) return;
        FileConfiguration cfg = plugin.getFactionsConfig();

        Set<String> f1Wars = new HashSet<>(cfg.getStringList("wars." + f1));
        Set<String> f2Wars = new HashSet<>(cfg.getStringList("wars." + f2));

        f1Wars.add(f2);
        f2Wars.add(f1);

        cfg.set("wars." + f1, List.copyOf(f1Wars));
        cfg.set("wars." + f2, List.copyOf(f2Wars));
        removeAllianceInternal(cfg, f1, f2);
        plugin.saveFactionsFile();
    }

    public void endWar(String f1, String f2) {
        if (f1 == null || f2 == null) return;
        FileConfiguration cfg = plugin.getFactionsConfig();

        Set<String> f1Wars = new HashSet<>(cfg.getStringList("wars." + f1));
        Set<String> f2Wars = new HashSet<>(cfg.getStringList("wars." + f2));
        f1Wars.remove(f2);
        f2Wars.remove(f1);

        cfg.set("wars." + f1, List.copyOf(f1Wars));
        cfg.set("wars." + f2, List.copyOf(f2Wars));
        plugin.saveFactionsFile();
    }

    public boolean isAlly(String f1, String f2) {
        if (f1 == null || f2 == null) return false;
        return plugin.getFactionsConfig().getStringList("relations.allies." + f1).contains(f2);
    }

    public void ally(String f1, String f2) {
        if (f1 == null || f2 == null || f1.equals(f2)) return;
        FileConfiguration cfg = plugin.getFactionsConfig();
        Set<String> a = new HashSet<>(cfg.getStringList("relations.allies." + f1));
        Set<String> b = new HashSet<>(cfg.getStringList("relations.allies." + f2));
        a.add(f2); b.add(f1);
        cfg.set("relations.allies." + f1, List.copyOf(a));
        cfg.set("relations.allies." + f2, List.copyOf(b));
        endWar(f1, f2);
        plugin.saveFactionsFile();
    }

    public void neutral(String f1, String f2) {
        if (f1 == null || f2 == null || f1.equals(f2)) return;
        FileConfiguration cfg = plugin.getFactionsConfig();
        removeAllianceInternal(cfg, f1, f2);
        cfg.set("relations.enemies." + f1, removeFrom(cfg.getStringList("relations.enemies." + f1), f2));
        cfg.set("relations.enemies." + f2, removeFrom(cfg.getStringList("relations.enemies." + f2), f1));
        endWar(f1, f2);
        plugin.saveFactionsFile();
    }

    private List<String> removeFrom(List<String> src, String item) {
        Set<String> s = new HashSet<>(src);
        s.remove(item);
        return List.copyOf(s);
    }

    private void removeAllianceInternal(FileConfiguration cfg, String f1, String f2) {
        cfg.set("relations.allies." + f1, removeFrom(cfg.getStringList("relations.allies." + f1), f2));
        cfg.set("relations.allies." + f2, removeFrom(cfg.getStringList("relations.allies." + f2), f1));
    }
}
