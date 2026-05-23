package com.convariance.sf;

import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFactionsPlugin extends JavaPlugin {

    private WarManager warManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupFactionsFile();

        warManager = new WarManager(this);

        java.util.Objects.requireNonNull(getCommand("faction"), "Command 'faction' not defined in plugin.yml")
                .setExecutor(new FactionCommand(this, warManager));

        getServer().getPluginManager().registerEvents(
                new ClaimProtectionListener(this, warManager), this
        );
        getServer().getPluginManager().registerEvents(
                new ClaimBorderListener(this, warManager), this
        );
        getServer().getPluginManager().registerEvents(
                new ExplosionProtectionListener(this, warManager), this
        );
        getServer().getPluginManager().registerEvents(
                new FactionMenuListener(this), this
        );

        getLogger().info("SimpleFactions enabled.");
    }

    @Override
    public void onDisable() {
        saveFactionsFile();
    }

    // ===== factions.yml handling =====

    private java.io.File factionsFile;
    private org.bukkit.configuration.file.FileConfiguration factionsConfig;

    private void setupFactionsFile() {
        factionsFile = new java.io.File(getDataFolder(), "factions.yml");
        if (!factionsFile.exists()) {
            saveResource("factions.yml", false);
        }
        factionsConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(factionsFile);
    }

    public org.bukkit.configuration.file.FileConfiguration getFactionsConfig() {
        return factionsConfig;
    }

    public void saveFactionsFile() {
        try {
            factionsConfig.save(factionsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// im not even an american fascist why do i put myself through this? i wasnt even born here. im hungarian.