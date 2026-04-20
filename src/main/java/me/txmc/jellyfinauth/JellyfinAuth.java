package me.txmc.jellyfinauth;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class JellyfinAuth extends JavaPlugin {
    private JellyfinAPI api;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String url = getConfig().getString("jellyfin.url");
        String apiKey = getConfig().getString("jellyfin.api-key");

        if (url == null || apiKey == null || url.isEmpty() || apiKey.isEmpty()) {
            getLogger().log(Level.WARNING, "Jellyfin configuration missing! Please add jellyfin.url and jellyfin.api-key to config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        api = new JellyfinAPI(url, apiKey);

        getCommand("jellyfin").setExecutor(new JellyfinCommand(this));
        getLogger().log(Level.INFO, "JellyfinAuth enabled successfully");
    }

    public JellyfinAPI getApi() {
        return api;
    }
}