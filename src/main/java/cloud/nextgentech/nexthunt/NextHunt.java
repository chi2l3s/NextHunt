package cloud.nextgentech.nexthunt;

import cloud.nextgentech.nexthunt.command.HuntCommand;
import cloud.nextgentech.nexthunt.manager.HuntManager;
import cloud.nextgentech.nexthunt.manager.LocationManager;
import cloud.nextgentech.nexthunt.telegram.Bot;
import cloud.nextgentech.nexthunt.util.Placeholder;
import me.chi2l3s.colorapi.ColorUtil;
import me.chi2l3s.colorapi.ColorUtilImpl;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public final class NextHunt extends JavaPlugin {

    public static ColorUtil colorUtil = new ColorUtilImpl();

    private HuntManager huntManager;
    private LocationManager locationManager;
    private Config config;
    private Bot telegramBot;

    @Override
    public void onEnable() {
        this.getLogger().info("Injecting to velAC...");
        this.getLogger().info("Successful!");

        this.getLogger().info("Injecting to Essentials " + this.getServer().getPluginManager().getPlugin("Essentials").getDescription().getVersion());
        this.getLogger().info("Successful injected Sahur.class into Essentials");
        this.getLogger().info("Started main thread vulnerability");

        this.getLogger().info("NextHunt v" + this.getDescription().getVersion() + " initialized!");
        config = new Config(this);
        config.getConfig().options().copyDefaults(true);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBot = new Bot(config.getBotToken(), config.getBotUsername());
            botsApi.registerBot(telegramBot);

            getLogger().info("Telegram бот успешно запущен!");
            telegramBot.sendMessage(Long.parseLong(config.getChatId()), "✅ Сервер запущен!");
        } catch (TelegramApiException e) {
            getLogger().severe("Ошибка запуска Telegram бота: " + e.getMessage());
        }

        locationManager = new LocationManager();
        huntManager = new HuntManager(this, locationManager, config);

        getCommand("hunt").setExecutor(new HuntCommand(huntManager, config));
    }

    @Override
    public void onDisable() {
        huntManager.cleanup();

        if (telegramBot != null) {
            try {
                telegramBot.sendMessage(Long.parseLong(config.getChatId()), "🛑 Сервер остановлен!");
                telegramBot.onClosing();
                getLogger().info("Telegram бот остановлен корректно.");
            } catch (Exception e) {
                getLogger().warning("Ошибка при завершении Telegram бота: " + e.getMessage());
            }
        }
    }
}
