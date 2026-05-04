package com.convariance.sf;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarManager {

    private final SimpleFactionsPlugin plugin;

    public WarManager(SimpleFactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAtWar(String f1, String f2) {
        FileConfiguration cfg = plugin.getFactionsConfig();
        List<String> wars = cfg.getStringList("wars." + f1);
        return wars.contains(f2);
    }

    public void declareWar(String f1, String f2) {
        if (f1.equals(f2)) return;

        FileConfiguration cfg = plugin.getFactionsConfig();

        Set<String> f1Wars = new HashSet<>(cfg.getStringList("wars." + f1));
        Set<String> f2Wars = new HashSet<>(cfg.getStringList("wars." + f2));

        f1Wars.add(f2);
        f2Wars.add(f1);

        cfg.set("wars." + f1, List.copyOf(f1Wars));
        cfg.set("wars." + f2, List.copyOf(f2Wars));

        plugin.saveFactionsFile();
    }

    public void endWar(String f1, String f2) {
        FileConfiguration cfg = plugin.getFactionsConfig();

        List<String> f1Wars = cfg.getStringList("wars." + f1);
        List<String> f2Wars = cfg.getStringList("wars." + f2);

        f1Wars.remove(f2);
        f2Wars.remove(f1);

        cfg.set("wars." + f1, f1Wars);
        cfg.set("wars." + f2, f2Wars);

        plugin.saveFactionsFile();
    }
}
