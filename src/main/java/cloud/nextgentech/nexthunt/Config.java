package cloud.nextgentech.nexthunt;

import cloud.nextgentech.nexthunt.util.Time;
import com.tchristofferson.configupdater.ConfigUpdater;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Getter
public class Config {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        reload();
        load();
        if (config == null) {
            throw new IllegalStateException("Конфиг не найден!");
        } else {
            try {
                ConfigUpdater.update(plugin, "config.yml", file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            parse();
        }
    }

    private String botToken;
    private String botUsername;
    private String chatId;

    private String timerSound;
    private String startSound;
    private String stopSound;

    private World world;
    private double minX;
    private double maxX;
    private double minZ;
    private double maxZ;
    private int countdown;
    private int broadcastPeriod;
    private double coordinatedPeriod;
    private int topCount;

    private String prefix;
    private String configReload;
    private String bossBarCountDown;
    private String bossBarHunt;
    private String countdownMessage;
    private String end;
    private String start;
    private List<String> top;
    private List<String> finalStrings;
    private String topFormatted;

    private void parse() {
        ConfigurationSection telegram = config.getConfigurationSection("telegram");

        botToken = telegram.getString("token");
        botUsername = telegram.getString("username");
        chatId = telegram.getString("chatId");

        ConfigurationSection sounds = config.getConfigurationSection("sounds");

        timerSound = sounds.getString("timer");
        startSound = sounds.getString("start");
        stopSound = sounds.getString("stop");

        ConfigurationSection settings = config.getConfigurationSection("settings");

        ConfigurationSection worldSection = settings.getConfigurationSection("world");

        world = Bukkit.getWorld(worldSection.getString("name", "world"));
        minX = worldSection.getDouble("minX");
        maxX = worldSection.getDouble("maxX");
        minZ = worldSection.getDouble("minZ");
        maxZ = worldSection.getDouble("maxZ");

        countdown = Time.parseTime(settings.getString("countdown"));
        broadcastPeriod = Time.parseTime(settings.getString("broadcastPeriod"));
        coordinatedPeriod = settings.getDouble("coordinatesPeriod");
        topCount = settings.getInt("topCount");

        ConfigurationSection messages = config.getConfigurationSection("messages");

        prefix = messages.getString("prefix");
        configReload = messages.getString("configReload");

        ConfigurationSection bossBarSection = messages.getConfigurationSection("bossBar");

        bossBarCountDown = bossBarSection.getString("countdown");
        bossBarHunt = bossBarSection.getString("hunt");

        countdownMessage = messages.getString("countdown");
        end = messages.getString("end");
        start = messages.getString("start");
        top = messages.getStringList("top");
        finalStrings = messages.getStringList("final");
        topFormatted = messages.getString("topFormatted");
    }

    private void load() {
        if (this.file != null) {
            this.config = YamlConfiguration.loadConfiguration(file);
        }
    }


    private void reload() {
        if (this.file == null) {
            this.file = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!this.file.exists()) {
            this.plugin.saveResource("config.yml", false);
        }
    }
}
