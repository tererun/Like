package amata1219.like.config;

import amata1219.like.define.HologramData;
import amata1219.like.handler.HologramDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HolographicConfig {

    private File dataFolder;
    private File configFile;
    private FileConfiguration config;
    private HologramDataHandler hologramDataHandler;


    public HolographicConfig() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("HolographicDisplays");
        this.dataFolder = plugin.getDataFolder();
        this.configFile = new File(dataFolder, "database.yml");
        this.hologramDataHandler = new HologramDataHandler(loadHologramDataList());
    }

    public HologramDataHandler getHologramDataHandler() {
        return hologramDataHandler;
    }

    public List<HologramData> loadHologramDataList() {
        List<HologramData> hologramDataList = new ArrayList<>();
        FileConfiguration fileConfiguration = getConfig();
        for (String name : fileConfiguration.getConfigurationSection("").getKeys(false)) {
            String path = name + ".";
            String positionPath = path + "position.";
            Location location = new Location(Bukkit.getWorld(fileConfiguration.getString(positionPath + "world")), fileConfiguration.getDouble(positionPath + "x"), fileConfiguration.getDouble(positionPath + "y"), fileConfiguration.getDouble(positionPath + "z"));
            hologramDataList.add(new HologramData(name, fileConfiguration.getStringList(name + ".lines"), location));
        }
        return hologramDataList;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        try {
            final InputStream configInputStream = new FileInputStream(configFile);
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(configInputStream, StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
