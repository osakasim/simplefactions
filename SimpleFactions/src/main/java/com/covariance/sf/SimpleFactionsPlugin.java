package com.convariance.sf;

import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFactionsPlugin extends JavaPlugin {

    private WarManager warManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupFactionsFile();
        setupMessagesFile();

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
    private org.bukkit.configuration.file.FileConfiguration messagesConfig;

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

    public org.bukkit.configuration.file.FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void saveFactionsFile() {
        try {
            factionsConfig.save(factionsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMessagesFile() {
        String language = getConfig().getString("language", "en").toLowerCase(java.util.Locale.ROOT);
        String resourceName = switch (language) {
            case "hu" -> "messages_hu.yml";
            case "ru" -> "messages_ru.yml";
            case "es" -> "messages_es.yml";
            case "de" -> "messages_de.yml";
            case "fr" -> "messages_fr.yml";
            case "eo" -> "messages_eo.yml";
            default -> "messages_en.yml";
        };

        java.io.File messagesFile = new java.io.File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource(resourceName, false);
            java.io.File copied = new java.io.File(getDataFolder(), resourceName);
            if (copied.exists() && !copied.renameTo(messagesFile)) {
                getLogger().warning("Could not rename " + resourceName + " to messages.yml");
            }
        }
        messagesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(messagesFile);
    }
}

